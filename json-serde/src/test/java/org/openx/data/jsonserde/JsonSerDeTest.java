/*======================================================================*
 * Copyright (c) 2011, OpenX Technologies, Inc. All rights reserved.    *
 *                                                                      *
 * Licensed under the New BSD License (the "License"); you may not use  *
 * this file except in compliance with the License. Unless required     *
 * by applicable law or agreed to in writing, software distributed      *
 * under the License is distributed on an "AS IS" BASIS, WITHOUT        *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     *
 * See the License for the specific language governing permissions and  *
 * limitations under the License. See accompanying LICENSE file.        *
 *======================================================================*/
package org.openx.data.jsonserde;

import io.starburst.openjson.JSONArray;
import io.starburst.openjson.JSONException;
import io.starburst.openjson.JSONObject;
import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.objectinspector.ListObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Test;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringBooleanObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringByteObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringDecimalObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringDoubleObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringFloatObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringIntObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringLongObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringShortObjectInspector;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author rcongiu
 */
public class JsonSerDeTest {

    public JsonSerDeTest() {
    }

    public void initialize(JsonSerDe instance) throws Exception {
        System.out.println("initialize");

        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "one,two,three,four");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "boolean,float,array<string>,string");

        instance.initialize(null, tbl);
    }

    public void initialize2(JsonSerDe instance) throws Exception {
        System.out.println("initialize");

        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "one,two,three,four,five");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "boolean,float,array<string>,string,string");

        instance.initialize(null, tbl);
    }


    /**
     * Test of deserialize method, of class JsonSerDe.
     * expects  "one,two,three,four"
     * "boolean,float,array&lt;string&gt;,string");
     */
    @Test
    public void testDeserializeArray() throws Exception {
        JsonSerDe instance = new JsonSerDe();
        initialize(instance);

        System.out.println("deserialize");
        Writable w = new Text("[true,19.5, [\"red\",\"yellow\",\"orange\"],\"poop\"]");

        Object result = instance.deserialize(w);
        assertTrue(result instanceof JSONArray);

        StructObjectInspector soi = (StructObjectInspector) instance.getObjectInspector();

        assertEquals(Boolean.TRUE, soi.getStructFieldData(result, soi.getStructFieldRef("one")));

        JavaStringFloatObjectInspector jsfOi = (JavaStringFloatObjectInspector) soi.getStructFieldRef("two").getFieldObjectInspector();
        assertEquals(19.5, jsfOi.get(soi.getStructFieldData(result, soi.getStructFieldRef("two"))), 0.0);

        Object ar = soi.getStructFieldData(result, soi.getStructFieldRef("three"));
        assertTrue(ar instanceof JSONArray);

        JSONArray jar = (JSONArray) ar;
        assertTrue(jar.get(0) instanceof String);
        assertEquals("red", jar.get(0));

    }

    /**
     * Test of deserialize method, but passing an array.
     */
    @Test
    public void testDeserialize() throws Exception {
        JsonSerDe instance = new JsonSerDe();
        initialize(instance);

        System.out.println("deserialize");
        Writable w = new Text("{\"one\":true,\"three\":[\"red\",\"yellow\",\"orange\"],\"two\":19.5,\"four\":\"poop\"}");

        JSONObject result = (JSONObject) instance.deserialize(w);
        assertEquals("poop", result.get("four"));
        assertTrue(result.get("three") instanceof JSONArray);

        assertTrue(((JSONArray) result.get("three")).get(0) instanceof String);
        assertEquals("red", ((JSONArray) result.get("three")).get(0));

    }

    //   {"one":true,"three":["red","yellow",["blue","azure","cobalt","teal"],"orange"],"two":19.5,"four":"poop"}
    @Test
    public void testDeserialize2() throws Exception {
        JsonSerDe instance = new JsonSerDe();
        initialize(instance);

        Writable w = new Text("{\"one\":true,\"three\":[\"red\",\"yellow\",[\"blue\",\"azure\",\"cobalt\",\"teal\"],\"orange\"],\"two\":19.5,\"four\":\"poop\"}");

        JSONObject result = (JSONObject) instance.deserialize(w);
        assertEquals("poop", result.get("four"));

        assertTrue(result.get("three") instanceof JSONArray);

        assertTrue(((JSONArray) result.get("three")).get(0) instanceof String);
        assertEquals("red", ((JSONArray) result.get("three")).get(0));

    }


    /**
     * Test of deserialize method, of class JsonSerDe.
     */
    @Test
    public void testDeserializeNull() throws Exception {
        JsonSerDe instance = new JsonSerDe();
        initialize(instance);

        System.out.println("deserializeNull");
        Writable w = new Text("{\"one\":true,\"three\":[\"red\",\"yellow\",\"orange\", null],\"two\":null,\"four\":null}");

        StructObjectInspector soi = (StructObjectInspector) instance.getObjectInspector();
        JSONObject result = (JSONObject) instance.deserialize(w);
        assertSame(JSONObject.NULL, result.get("four"));

        assertNull(soi.getStructFieldData(result, soi.getStructFieldRef("four")));

        // same on number
        Object res = soi.getStructFieldData(result, soi.getStructFieldRef("two"));

        assertNull(res);

        // get the array
        res = soi.getStructFieldData(result, soi.getStructFieldRef("three"));
        ListObjectInspector loi = (ListObjectInspector) soi.getStructFieldRef("three").getFieldObjectInspector();

        // get the 4th element
        Object el = loi.getListElement(res, 3);
        StringObjectInspector elOi = (StringObjectInspector) loi.getListElementObjectInspector();
        String sres = elOi.getPrimitiveJavaObject(el);
        assertNull(sres);

        List<?> all = loi.getList(res);
        assertEquals(4, all.size());
        assertNull(all.get(3));
        assertEquals("red", all.get(0));

    }

    @Test
    public void testDeserialize2Initializations() throws Exception {
        JsonSerDe instance = new JsonSerDe();
        initialize(instance);

        Writable w = new Text("{\"one\":true,\"three\":[\"red\",\"yellow\",[\"blue\",\"azure\",\"cobalt\",\"teal\"],\"orange\"],\"two\":19.5,\"four\":\"poop\"}");

        JSONObject result = (JSONObject) instance.deserialize(w);
        assertEquals(result.get("four"), "poop");

        assertTrue(result.get("three") instanceof JSONArray);

        assertTrue(((JSONArray) result.get("three")).get(0) instanceof String);
        assertEquals(((JSONArray) result.get("three")).get(0), "red");

        // second initialization, new column
        initialize2(instance);

        result = (JSONObject) instance.deserialize(w);
        assertEquals(result.get("four"), "poop");

        assertTrue(result.get("three") instanceof JSONArray);

        assertTrue(((JSONArray) result.get("three")).get(0) instanceof String);
        assertEquals(((JSONArray) result.get("three")).get(0), "red");
    }


    @Test
    public void testDeserializePartialFieldSet() throws Exception {
        Writable w = new Text("{\"missing\":\"whocares\",\"one\":true,\"three\":[\"red\",\"yellow\",[\"blue\",\"azure\",\"cobalt\",\"teal\"],\"orange\"],\"two\":19.5,\"four\":\"poop\"}");
        JsonSerDe instance = new JsonSerDe();
        initialize(instance);
        JSONObject result = (JSONObject) instance.deserialize(w);
        assertEquals(result.get("four"), "poop");

        assertTrue(result.get("three") instanceof JSONArray);

        assertTrue(((JSONArray) result.get("three")).get(0) instanceof String);
        assertEquals(((JSONArray) result.get("three")).get(0), "red");
    }

    /**
     * Test of getSerializedClass method, of class JsonSerDe.
     */
    @Test
    public void testGetSerializedClass() throws Exception {
        JsonSerDe instance = new JsonSerDe();
        initialize(instance);

        System.out.println("getSerializedClass");
        Class<Text> expResult = Text.class;
        Class<? extends Writable> result = instance.getSerializedClass();
        assertEquals(expResult, result);

    }

    /**
     * Test of serialize method, of class JsonSerDe.
     */
    @Test
    public void testSerialize() throws Exception {
        System.out.println("testSerialize");

        JsonSerDe instance = new JsonSerDe();
        initialize(instance);

        ArrayList<Object> row = new ArrayList<>(5);

        List<ObjectInspector> lOi = new LinkedList<>();
        List<String> fieldNames = new LinkedList<>();

        row.add("HELLO");
        fieldNames.add("one");
        lOi.add(ObjectInspectorFactory.getReflectionObjectInspector(String.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA));

        row.add(10);
        fieldNames.add("two");
        lOi.add(ObjectInspectorFactory.getReflectionObjectInspector(Integer.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA));

        List<String> array = new LinkedList<>();
        array.add("String1");
        array.add("String2");

        row.add(array);
        fieldNames.add("three");
        lOi.add(ObjectInspectorFactory.getStandardListObjectInspector(
                ObjectInspectorFactory.getReflectionObjectInspector(String.class,
                        ObjectInspectorFactory.ObjectInspectorOptions.JAVA)));

        Map<String, String> m = new HashMap<>();
        m.put("k1", "v1");
        m.put("k2", "v2");

        row.add(m);
        fieldNames.add("four");
        lOi.add(ObjectInspectorFactory.getStandardMapObjectInspector(
                ObjectInspectorFactory.getReflectionObjectInspector(String.class,
                        ObjectInspectorFactory.ObjectInspectorOptions.JAVA),
                ObjectInspectorFactory.getReflectionObjectInspector(String.class,
                        ObjectInspectorFactory.ObjectInspectorOptions.JAVA)));


        StructObjectInspector soi = ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, lOi);

        Object result = instance.serialize(row, soi);

        JSONObject res = new JSONObject(true, result.toString());
        assertEquals(res.getString("one"), row.get(0));

        assertEquals(res.get("two"), row.get(1));

        // after serialization the internal contents of JSONObject are destroyed (overwritten by their string representation
        // (for map and arrays) 

        System.out.println("Serialized to " + result);
    }

    public JsonSerDe getMappedSerde() throws SerDeException {
        System.out.println("testMapping");
        JsonSerDe serde = new JsonSerDe();
        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "one,two,three,four,ts");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "boolean,float,array<string>,string,int");
        // this means, we call it ts but in data it's 'timestamp'
        tbl.setProperty("mapping.ts", "timestamp");

        serde.initialize(null, tbl);
        return serde;
    }

    public JsonSerDe getNumericSerde() throws SerDeException {
        System.out.println("getNumericSerde");
        JsonSerDe serde = new JsonSerDe();
        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "cboolean,ctinyint,csmallint,cint,cbigint,cfloat,cdouble,cdecimal");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "boolean,tinyint,smallint,int,bigint,float,double,decimal(38,18)");

        serde.initialize(null, tbl);
        return serde;
    }

    @Test
    public void testNumbers() throws SerDeException, JSONException {
        System.out.println("testNumbers");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "cboolean:true," +
                "ctinyint:1," +
                "csmallint:200," +
                "cint:12345," +
                "cbigint:123446767687867," +
                "cfloat:" + Math.PI + "," +
                "cdouble:" + Math.E + "," +
                "cdecimal:12345678901234567890.123456789012345678" +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cboolean");

        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringBooleanObjectInspector);
        JavaStringBooleanObjectInspector jboi = (JavaStringBooleanObjectInspector) sf.getFieldObjectInspector();
        assertTrue(jboi.get(result.get("cboolean")));

        sf = soi.getStructFieldRef("ctinyint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringByteObjectInspector);
        JavaStringByteObjectInspector boi = (JavaStringByteObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1, boi.get(result.get("ctinyint")));

        sf = soi.getStructFieldRef("csmallint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringShortObjectInspector);
        JavaStringShortObjectInspector shoi = (JavaStringShortObjectInspector) sf.getFieldObjectInspector();
        assertEquals(200, shoi.get(result.get("csmallint")));


        sf = soi.getStructFieldRef("cint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringIntObjectInspector);
        JavaStringIntObjectInspector oi = (JavaStringIntObjectInspector) sf.getFieldObjectInspector();
        assertEquals(12345, oi.get(result.get("cint")));

        sf = soi.getStructFieldRef("cbigint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringLongObjectInspector);
        JavaStringLongObjectInspector bioi = (JavaStringLongObjectInspector) sf.getFieldObjectInspector();
        assertEquals(123446767687867L, bioi.get(result.get("cbigint")));

        sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals((float) Math.PI, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Math.E, doi.get(result.get("cdouble")), 0);

        sf = soi.getStructFieldRef("cdecimal");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDecimalObjectInspector);
        JavaStringDecimalObjectInspector dbloi = (JavaStringDecimalObjectInspector) sf.getFieldObjectInspector();
        assertEquals(HiveDecimal.create("12345678901234567890.123456789012345678"), dbloi.getPrimitiveJavaObject(result.get("cdecimal")));
    }

    @Test
    public void testNegativeNumbers() throws SerDeException, JSONException {
        System.out.println("testNegativeNumbers");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "cboolean:true," +
                "ctinyint:-1," +
                "csmallint:-200," +
                "cint:-12345," +
                "cbigint:-123446767687867," +
                "cfloat:-" + Math.PI + "," +
                "cdouble:-" + Math.E + "," +
                "cdecimal:-12345678901234567890.123456789012345678" +
                "}");
        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cboolean");

        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringBooleanObjectInspector);
        JavaStringBooleanObjectInspector jboi = (JavaStringBooleanObjectInspector) sf.getFieldObjectInspector();
        assertTrue(jboi.get(result.get("cboolean")));

        sf = soi.getStructFieldRef("ctinyint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringByteObjectInspector);
        JavaStringByteObjectInspector boi = (JavaStringByteObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-1, boi.get(result.get("ctinyint")));

        sf = soi.getStructFieldRef("csmallint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringShortObjectInspector);
        JavaStringShortObjectInspector shoi = (JavaStringShortObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-200, shoi.get(result.get("csmallint")));

        sf = soi.getStructFieldRef("cint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringIntObjectInspector);
        JavaStringIntObjectInspector oi = (JavaStringIntObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-12345, oi.get(result.get("cint")));

        sf = soi.getStructFieldRef("cbigint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringLongObjectInspector);
        JavaStringLongObjectInspector bioi = (JavaStringLongObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-123446767687867L, bioi.get(result.get("cbigint")));

        sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-((float) Math.PI), foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-Math.E, doi.get(result.get("cdouble")), 0);

        sf = soi.getStructFieldRef("cdecimal");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDecimalObjectInspector);
        JavaStringDecimalObjectInspector dbloi = (JavaStringDecimalObjectInspector) sf.getFieldObjectInspector();
        assertEquals(HiveDecimal.create("-12345678901234567890.123456789012345678"), dbloi.getPrimitiveJavaObject(result.get("cdecimal")));
    }

    @Test
    public void testNarrowerTypeNumbers() throws SerDeException, JSONException {
        System.out.println("testNarrowerTypeNumbers");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "cboolean:true," +
                "ctinyint:1," +
                "csmallint:1," +
                "cint:1," +
                "cbigint:1," +
                "cfloat:0.5," +
                "cdouble:0.5," +
                "cdecimal:0.5" +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cboolean");

        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringBooleanObjectInspector);
        JavaStringBooleanObjectInspector jboi = (JavaStringBooleanObjectInspector) sf.getFieldObjectInspector();
        assertTrue(jboi.get(result.get("cboolean")));

        sf = soi.getStructFieldRef("ctinyint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringByteObjectInspector);
        JavaStringByteObjectInspector boi = (JavaStringByteObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1, boi.get(result.get("ctinyint")));

        sf = soi.getStructFieldRef("csmallint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringShortObjectInspector);
        JavaStringShortObjectInspector shoi = (JavaStringShortObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1, shoi.get(result.get("csmallint")));

        sf = soi.getStructFieldRef("cint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringIntObjectInspector);
        JavaStringIntObjectInspector oi = (JavaStringIntObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1, oi.get(result.get("cint")));

        sf = soi.getStructFieldRef("cbigint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringLongObjectInspector);
        JavaStringLongObjectInspector bioi = (JavaStringLongObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1L, bioi.get(result.get("cbigint")));

        sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(0.5f, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(0.5, doi.get(result.get("cdouble")), 0);

        sf = soi.getStructFieldRef("cdecimal");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDecimalObjectInspector);
        JavaStringDecimalObjectInspector dbloi = (JavaStringDecimalObjectInspector) sf.getFieldObjectInspector();
        assertEquals(HiveDecimal.create("0.5"), dbloi.getPrimitiveJavaObject(result.get("cdecimal")));
    }

    @Test
    public void testOverflowingNumbers() throws SerDeException, JSONException {
        System.out.println("testOverflowingNumbers");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "ctinyint:" + BigInteger.valueOf(Byte.MAX_VALUE).add(BigInteger.ONE) + "," +
                "csmallint:" + BigInteger.valueOf(Short.MAX_VALUE).add(BigInteger.ONE) + "," +
                "cint:" + BigInteger.valueOf(Integer.MAX_VALUE).add(BigInteger.ONE) + "," +
                "cbigint:" + BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE) + "," +
                "cfloat:" + BigDecimal.valueOf(Float.MAX_VALUE).multiply(BigDecimal.TEN) + "," +
                "cdouble:" + BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN) +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("ctinyint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringByteObjectInspector);
        JavaStringByteObjectInspector boi = (JavaStringByteObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Byte.MIN_VALUE, boi.get(result.get("ctinyint")));

        sf = soi.getStructFieldRef("csmallint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringShortObjectInspector);
        JavaStringShortObjectInspector shoi = (JavaStringShortObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Short.MIN_VALUE, shoi.get(result.get("csmallint")));

        sf = soi.getStructFieldRef("cint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringIntObjectInspector);
        JavaStringIntObjectInspector oi = (JavaStringIntObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Integer.MIN_VALUE, oi.get(result.get("cint")));

        sf = soi.getStructFieldRef("cbigint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringLongObjectInspector);
        JavaStringLongObjectInspector bioi = (JavaStringLongObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Long.MIN_VALUE, bioi.get(result.get("cbigint")));

        sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Float.POSITIVE_INFINITY, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Double.POSITIVE_INFINITY, doi.get(result.get("cdouble")), 0);
    }

    @Test
    public void testOverflowingNegativeNumbers() throws SerDeException, JSONException {
        System.out.println("testOverflowingNegativeNumbers");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "ctinyint:" + BigInteger.valueOf(Byte.MIN_VALUE).subtract(BigInteger.ONE) + "," +
                "csmallint:" + BigInteger.valueOf(Short.MIN_VALUE).subtract(BigInteger.ONE) + "," +
                "cint:" + BigInteger.valueOf(Integer.MIN_VALUE).subtract(BigInteger.ONE) + "," +
                "cbigint:" + BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE) + "," +
                "cfloat:" + BigDecimal.valueOf(Float.MAX_VALUE).negate().multiply(BigDecimal.TEN) + "," +
                "cdouble:" + BigDecimal.valueOf(Double.MAX_VALUE).negate().multiply(BigDecimal.TEN) +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("ctinyint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringByteObjectInspector);
        JavaStringByteObjectInspector boi = (JavaStringByteObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Byte.MAX_VALUE, boi.get(result.get("ctinyint")));

        sf = soi.getStructFieldRef("csmallint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringShortObjectInspector);
        JavaStringShortObjectInspector shoi = (JavaStringShortObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Short.MAX_VALUE, shoi.get(result.get("csmallint")));

        sf = soi.getStructFieldRef("cint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringIntObjectInspector);
        JavaStringIntObjectInspector oi = (JavaStringIntObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Integer.MAX_VALUE, oi.get(result.get("cint")));

        sf = soi.getStructFieldRef("cbigint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringLongObjectInspector);
        JavaStringLongObjectInspector bioi = (JavaStringLongObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Long.MAX_VALUE, bioi.get(result.get("cbigint")));

        sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Float.NEGATIVE_INFINITY, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(Double.NEGATIVE_INFINITY, doi.get(result.get("cdouble")), 0);
    }

    @Test
    public void testUnderflowingFloats() throws SerDeException, JSONException {
        System.out.println("testUnderflowingFloats");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "cfloat:" + BigDecimal.valueOf(Float.MIN_VALUE).divide(BigDecimal.TEN, MathContext.UNLIMITED) + "," +
                "cdouble:" + BigDecimal.valueOf(Double.MIN_VALUE).divide(BigDecimal.TEN, MathContext.UNLIMITED) +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(0f, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(0, doi.get(result.get("cdouble")), 0);
    }

    @Test
    public void testUnderflowingNegativeFloats() throws SerDeException, JSONException {
        System.out.println("testUnderflowingNegativeFloats");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "cfloat:" + BigDecimal.valueOf(Float.MIN_VALUE).negate().divide(BigDecimal.TEN, MathContext.UNLIMITED) + "," +
                "cdouble:" + BigDecimal.valueOf(Double.MIN_VALUE).negate().divide(BigDecimal.TEN, MathContext.UNLIMITED) +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(0f, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(0, doi.get(result.get("cdouble")), 0);
    }

    @Test
    public void testFloatingPointPrecisionLoss() throws SerDeException, JSONException {
        System.out.println("testFloatingPointPrecisionLoss");

        JsonSerDe serde = getNumericSerde();
        String veryLongNumber = "12345678901234567890.123456789012345678";
        Text line = new Text("{" +
                "cfloat:" + veryLongNumber + "," +
                "cdouble:" + veryLongNumber + "," +
                "cdecimal:" + veryLongNumber +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1.2345679e+19f, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1.2345678901234567e+19, doi.get(result.get("cdouble")), 0);

        sf = soi.getStructFieldRef("cdecimal");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDecimalObjectInspector);
        JavaStringDecimalObjectInspector dbloi = (JavaStringDecimalObjectInspector) sf.getFieldObjectInspector();
        assertEquals(HiveDecimal.create(veryLongNumber), dbloi.getPrimitiveJavaObject(result.get("cdecimal")));
    }

    @Test
    public void testNegativeFloatingPointPrecisionLoss() throws SerDeException, JSONException {
        System.out.println("testNegativeFloatingPointPrecisionLoss");

        JsonSerDe serde = getNumericSerde();
        String veryLongNumber = "-12345678901234567890.123456789012345678";
        Text line = new Text("{" +
                "cfloat:" + veryLongNumber + "," +
                "cdouble:" + veryLongNumber + "," +
                "cdecimal:" + veryLongNumber +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-1.2345679e+19f, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-1.2345678901234567e+19, doi.get(result.get("cdouble")), 0);

        sf = soi.getStructFieldRef("cdecimal");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDecimalObjectInspector);
        JavaStringDecimalObjectInspector dbloi = (JavaStringDecimalObjectInspector) sf.getFieldObjectInspector();
        assertEquals(HiveDecimal.create(veryLongNumber), dbloi.getPrimitiveJavaObject(result.get("cdecimal")));
    }

    /**
     * Test scientific notation
     */
    @Test
    public void testENotationNumbers() throws SerDeException, JSONException {
        System.out.println("testENotationNumbers");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{" +
                "cfloat:3.1415E02," +
                "cdouble:-1.65788E-12" +
                "}");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("cfloat");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringFloatObjectInspector);
        JavaStringFloatObjectInspector foi = (JavaStringFloatObjectInspector) sf.getFieldObjectInspector();
        assertEquals(3.1415E02f, foi.get(result.get("cfloat")), 0f);

        sf = soi.getStructFieldRef("cdouble");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringDoubleObjectInspector);
        JavaStringDoubleObjectInspector doi = (JavaStringDoubleObjectInspector) sf.getFieldObjectInspector();
        assertEquals(-1.65788E-12, doi.get(result.get("cdouble")), 0);
    }

    @Test
    public void testHexSupport() throws SerDeException, JSONException {
        System.out.println("testHexSupport");

        JsonSerDe serde = getNumericSerde();
        Text line = new Text("{ cboolean:true, ctinyint:0x01, csmallint:0x0a, cint:0Xabcd, cbigint:0xabcd121212 }");

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        JSONObject result = (JSONObject) serde.deserialize(line);

        StructField sf = soi.getStructFieldRef("ctinyint");

        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringByteObjectInspector);
        JavaStringByteObjectInspector boi = (JavaStringByteObjectInspector) sf.getFieldObjectInspector();
        assertEquals(1, boi.get(result.get("ctinyint")));

        sf = soi.getStructFieldRef("csmallint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringShortObjectInspector);
        JavaStringShortObjectInspector shoi = (JavaStringShortObjectInspector) sf.getFieldObjectInspector();
        assertEquals(10, shoi.get(result.get("csmallint")));

        sf = soi.getStructFieldRef("cint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringIntObjectInspector);
        JavaStringIntObjectInspector oi = (JavaStringIntObjectInspector) sf.getFieldObjectInspector();
        assertEquals(43981, oi.get(result.get("cint")));

        sf = soi.getStructFieldRef("cbigint");
        assertTrue(sf.getFieldObjectInspector() instanceof JavaStringLongObjectInspector);
        JavaStringLongObjectInspector bioi = (JavaStringLongObjectInspector) sf.getFieldObjectInspector();
        assertEquals(737879921170L, bioi.get(result.get("cbigint")));
    }


    @Test
    public void testSerializeWithMapping() throws SerDeException, JSONException {
        System.out.println("testSerializeWithMapping");

        JsonSerDe serde = getMappedSerde();

        System.out.println("serialize");
        ArrayList<Object> row = new ArrayList<>(5);

        List<ObjectInspector> lOi = new LinkedList<>();
        List<String> fieldNames = new LinkedList<>();

        row.add(Boolean.TRUE);
        fieldNames.add("one");
        lOi.add(ObjectInspectorFactory.getReflectionObjectInspector(Boolean.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA));

        row.add(43.2f);
        fieldNames.add("two");
        lOi.add(ObjectInspectorFactory.getReflectionObjectInspector(Float.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA));

        final List<String> lst = new LinkedList<>();
        row.add(lst);
        fieldNames.add("three");
        lOi.add(ObjectInspectorFactory.getStandardListObjectInspector(ObjectInspectorFactory
                .getReflectionObjectInspector(String.class, ObjectInspectorFactory.ObjectInspectorOptions.JAVA)));

        row.add("value1");
        fieldNames.add("four");
        lOi.add(ObjectInspectorFactory.getReflectionObjectInspector(String.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA));

        row.add(7898);
        fieldNames.add("ts");
        lOi.add(ObjectInspectorFactory.getReflectionObjectInspector(Integer.class,
                ObjectInspectorFactory.ObjectInspectorOptions.JAVA));

        StructObjectInspector soi = ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, lOi);

        Object obj = serde.serialize(row, soi);

        assertTrue(obj instanceof Text);
        String objs = obj.toString();

        // this is what we get.. but the order of the elements may vary...
        String res = "{\"timestamp\":7898,\"two\":43.2,\"one\":true,\"three\":[],\"four\":\"value1\"}";
        String[] r2 = res.substring(1, res.length() - 1).split(",");

        // they should be the same...let's hope spacing is the same
        assertEquals(objs.length(), res.length());

        for (String s : r2) {
            assertTrue(objs.contains(s));
        }
    }

    // {"one":true, "timestamp":1234567, "three":["red","yellow",["blue","azure","cobalt","teal"],"orange"],"two":19.5,"four":"poop"}
    @Test
    public void testMapping() throws SerDeException, IOException {
        System.out.println("testMapping");
        JsonSerDe serde = getMappedSerde();

        InputStream is = this.getClass().getResourceAsStream("/testkeyword.txt");

        LineNumberReader lnr = new LineNumberReader(new InputStreamReader(Objects.requireNonNull(is)));

        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();
        StructField sf = soi.getStructFieldRef("ts");

        String line;
        while ((line = lnr.readLine()) != null) {
            Text t = new Text(line);

            Object res = serde.deserialize(t);

            ObjectInspector foi = sf.getFieldObjectInspector();
            assertTrue(foi instanceof JavaStringIntObjectInspector);
            JavaStringIntObjectInspector jsioi = (JavaStringIntObjectInspector) foi;
            assertEquals(1234567, jsioi.get(soi.getStructFieldData(res, sf)));
        }

        try {
            is.close();
        } catch (IOException ignored) {
        }
    }

    @Test
    public void testCaseSensitiveMapping() throws SerDeException {
        System.out.println("testCaseSensitiveMapping");
        JsonSerDe serde = new JsonSerDe();
        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "time1,time2");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,string");
        // this means, we call it ts but in data it's 'timestamp'
        tbl.setProperty("mapping.time1", "Time");
        tbl.setProperty("mapping.time2", "time");
        tbl.setProperty(JsonSerDe.PROP_CASE_INSENSITIVE, "false");

        serde.initialize(null, tbl);
        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();
        Object res = serde.deserialize(new Text("{\"Time\":\"forme\",\"time\":\"foryou\"}"));

        assertEquals("forme", soi.getStructFieldData(res, soi.getStructFieldRef("time1")));
        assertEquals("foryou", soi.getStructFieldData(res, soi.getStructFieldRef("time2")));
    }

    @Test
    public void testNestedCaseSensitiveMapping() throws SerDeException {
        System.out.println("testCaseSensitiveMapping");
        JsonSerDe serde = new JsonSerDe();
        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "col1,col2");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,struct<time1:string>");
        // this means, we call it ts but in data it's 'timestamp'
        tbl.setProperty("mapping.time1", "Time");
        tbl.setProperty(JsonSerDe.PROP_CASE_INSENSITIVE, "false");

        serde.initialize(null, tbl);
        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();
        Object res = serde.deserialize(new Text("{\"col1\":\"forme\",\"col2\":{\"Time\":\"foryou\"}}"));

        assertEquals("forme", soi.getStructFieldData(res, soi.getStructFieldRef("col1")));
        StructObjectInspector soi2 = (StructObjectInspector) soi.getStructFieldRef("col2").getFieldObjectInspector();
        Object col2 = soi.getStructFieldData(res, soi.getStructFieldRef("col2"));
        assertEquals("foryou", soi2.getStructFieldData(col2, soi2.getStructFieldRef("time1")));
    }

    @Test
    public void testExplicitNullValueDefault() throws SerDeException {
        System.out.println("testExplicitNullValue");
        JsonSerDe serde = new JsonSerDe();
        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "stringCol,nullCol,missingCol");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,string,string");
        serde.initialize(null, tbl);
        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        // Load json string with one 'null' value and one 'missing' value
        Object res = serde.deserialize(new Text("{\"stringCol\":\"str\",\"nullCol\":null}"));

        // Get the serialized json string
        String jsonStr = serde.serialize(res, soi).toString();

        assertEquals("str", soi.getStructFieldData(res, soi.getStructFieldRef("stringCol")));
        assertNull(soi.getStructFieldData(res, soi.getStructFieldRef("nullCol")));
        assertNull(soi.getStructFieldData(res, soi.getStructFieldRef("missingCol")));
        assertEquals(jsonStr, "{\"stringCol\":\"str\"}");
    }

    @Test
    public void testExplicitNullValue() throws SerDeException {
        System.out.println("testExplicitNullValue");
        JsonSerDe serde = new JsonSerDe();
        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "stringCol,nullCol,missingCol");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,string,string");

        // Set 'explicit.null' to true
        tbl.setProperty(JsonSerDe.PROP_EXPLICIT_NULL, "true");

        serde.initialize(null, tbl);
        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();

        // Load json string with one 'null' value and one 'missing' value
        Object res = serde.deserialize(new Text("{\"stringCol\":\"str\",\"nullCol\":null}"));

        // Get the serialized json string
        String jsonStr = serde.serialize(res, soi).toString();

        assertEquals("str", soi.getStructFieldData(res, soi.getStructFieldRef("stringCol")));
        assertNull(soi.getStructFieldData(res, soi.getStructFieldRef("nullCol")));
        assertNull(soi.getStructFieldData(res, soi.getStructFieldRef("missingCol")));
        assertEquals(jsonStr, "{\"stringCol\":\"str\",\"nullCol\":null,\"missingCol\":null}");
    }

    @Test
    public void testNestedExplicitNullValue() throws SerDeException {
        System.out.println("testNestedExplicitNullValue");
        JsonSerDe serde = new JsonSerDe();
        Properties tbl = new Properties();
        tbl.setProperty(serdeConstants.LIST_COLUMNS, "structCol,structNullCol,missingStructCol");
        tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "struct<name:string>,struct<name:string>,struct<name:string>");

        // Set 'explicit.null' to true
        tbl.setProperty(JsonSerDe.PROP_EXPLICIT_NULL, "true");

        serde.initialize(null, tbl);
        StructObjectInspector soi = (StructObjectInspector) serde.getObjectInspector();
        Object res = serde.deserialize(new Text("{\"structCol\":{\"name\":\"myName\"},\"structNullCol\":{\"name\":null}}"));

        // Get the serialized json string
        String jsonStr = serde.serialize(res, soi).toString();

        StructObjectInspector structColSoi = (StructObjectInspector) soi.getStructFieldRef("structCol").getFieldObjectInspector();
        Object structCol = soi.getStructFieldData(res, soi.getStructFieldRef("structCol"));
        assertEquals("myName", structColSoi.getStructFieldData(structCol, structColSoi.getStructFieldRef("name")));

        StructObjectInspector structNullColSoi = (StructObjectInspector) soi.getStructFieldRef("structNullCol").getFieldObjectInspector();
        Object structNullCol = soi.getStructFieldData(res, soi.getStructFieldRef("structNullCol"));
        assertNull(structNullColSoi.getStructFieldData(structNullCol, structNullColSoi.getStructFieldRef("name")));

        assertNull(soi.getStructFieldData(res, soi.getStructFieldRef("missingStructCol")));

        assertEquals("{\"structCol\":{\"name\":\"myName\"},\"structNullCol\":{\"name\":null},\"missingStructCol\":null}", jsonStr);
    }
}
