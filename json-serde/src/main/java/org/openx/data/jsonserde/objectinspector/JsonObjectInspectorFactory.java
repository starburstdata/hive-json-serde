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
package org.openx.data.jsonserde.objectinspector;


import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.MapTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TimestampLocalTZTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.UnionTypeInfo;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringBinaryObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringBooleanObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringByteObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringDateObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringDecimalObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringDoubleObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringFloatObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringIntObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringLongObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringShortObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringTimestampObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JsonStringJavaObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.TypeEntryShim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author rcongiu
 */
public final class JsonObjectInspectorFactory {

    static ConcurrentHashMap<Pair<TypeInfo, JsonStructOIOptions>, ObjectInspector> cachedJsonObjectInspector = new ConcurrentHashMap<>();

    private JsonObjectInspectorFactory() {
        throw new InstantiationError("This class must not be instantiated.");
    }

    /**
     *
     *
     * @param options
     * @param typeInfo
     * @return
     */
    public static ObjectInspector getJsonObjectInspectorFromTypeInfo(TypeInfo typeInfo, JsonStructOIOptions options) {
        final Pair<TypeInfo, JsonStructOIOptions> key = Pair.of(typeInfo, options);
        ObjectInspector result = cachedJsonObjectInspector.get(key);
        // let the factory cache the struct object inspectors since
        // their key also has some options
        if (result == null ||  typeInfo.getCategory() == ObjectInspector.Category.STRUCT) {
            switch (typeInfo.getCategory()) {
                case PRIMITIVE: {
                    PrimitiveTypeInfo pti = (PrimitiveTypeInfo) typeInfo;
                    result = getPrimitiveJavaObjectInspector(pti, options);
                    break;
                }
                case LIST: {
                    ObjectInspector elementObjectInspector = getJsonObjectInspectorFromTypeInfo(((ListTypeInfo) typeInfo).getListElementTypeInfo(), options);
                    result = JsonObjectInspectorFactory.getJsonListObjectInspector(elementObjectInspector, options);
                    break;
                }
                case MAP: {
                    MapTypeInfo mapTypeInfo = (MapTypeInfo) typeInfo;
                    ObjectInspector keyObjectInspector = getJsonObjectInspectorFromTypeInfo(mapTypeInfo.getMapKeyTypeInfo(), options);
                    ObjectInspector valueObjectInspector = getJsonObjectInspectorFromTypeInfo(mapTypeInfo.getMapValueTypeInfo(), options);
                    result = JsonObjectInspectorFactory.getJsonMapObjectInspector(keyObjectInspector, valueObjectInspector, options);
                    break;
                }
                case STRUCT: {
                    StructTypeInfo structTypeInfo = (StructTypeInfo) typeInfo;
                    List<String> fieldNames = structTypeInfo.getAllStructFieldNames();
                    List<TypeInfo> fieldTypeInfos = structTypeInfo.getAllStructFieldTypeInfos();
                    List<ObjectInspector> fieldObjectInspectors = new ArrayList<ObjectInspector>(fieldTypeInfos.size());
                    for (int i = 0; i < fieldTypeInfos.size(); i++) {
                        fieldObjectInspectors.add(getJsonObjectInspectorFromTypeInfo(fieldTypeInfos.get(i), options));
                    }
                    result = JsonObjectInspectorFactory.getJsonStructObjectInspector(fieldNames, fieldObjectInspectors, options);
                    break;
                }
                case UNION:{
                    List<ObjectInspector> ois = new LinkedList<ObjectInspector>();
                    for(TypeInfo ti : ((UnionTypeInfo) typeInfo).getAllUnionObjectTypeInfos()) {
                        ois.add(getJsonObjectInspectorFromTypeInfo(ti, options));
                    }
                    result = getJsonUnionObjectInspector(ois, options);
                    break;
                }

                default: {
                    result = null;
                }
            }
            cachedJsonObjectInspector.put(key, result);
        }
        return result;
    }


    static ConcurrentHashMap<Pair<ArrayList<Object>, JsonStructOIOptions>, JsonUnionObjectInspector> cachedJsonUnionObjectInspector
            = new ConcurrentHashMap<>();

    public static JsonUnionObjectInspector getJsonUnionObjectInspector(
            List<ObjectInspector> ois,
            JsonStructOIOptions options) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(ois);
        signature.add(options);
        final Pair<ArrayList<Object>, JsonStructOIOptions> key = Pair.of(signature, options);
        JsonUnionObjectInspector result = cachedJsonUnionObjectInspector.get(key);
        if (result == null) {
            result = new JsonUnionObjectInspector(ois, options);
            cachedJsonUnionObjectInspector.put(key, result);
        }
        return result;
    }

    /*
     * Caches Struct Object Inspectors
     */
    static ConcurrentHashMap<Pair<ArrayList<Object>, JsonStructOIOptions>, JsonStructObjectInspector> cachedStandardStructObjectInspector
            = new ConcurrentHashMap<>();


    public static JsonStructObjectInspector getJsonStructObjectInspector(
            List<String> structFieldNames,
            List<ObjectInspector> structFieldObjectInspectors,
            JsonStructOIOptions options) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(structFieldNames);
        signature.add(structFieldObjectInspectors);
        signature.add(options);
        final Pair<ArrayList<Object>, JsonStructOIOptions> key = Pair.of(signature, options);

        JsonStructObjectInspector result = cachedStandardStructObjectInspector.get(key);
        if (result == null) {
            result = new JsonStructObjectInspector(structFieldNames, structFieldObjectInspectors, options);
            cachedStandardStructObjectInspector.put(key, result);
        }
        return result;
    }

    /*
     * Caches the List object inspectors
     */
    static ConcurrentHashMap<Pair<ArrayList<Object>, JsonStructOIOptions>, JsonListObjectInspector> cachedJsonListObjectInspector
            = new ConcurrentHashMap<>();

    public static JsonListObjectInspector getJsonListObjectInspector(
            ObjectInspector listElementObjectInspector,
            JsonStructOIOptions options) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(listElementObjectInspector);
        final Pair<ArrayList<Object>, JsonStructOIOptions> key = Pair.of(signature, options);
        JsonListObjectInspector result = cachedJsonListObjectInspector.get(key);
        if (result == null) {
            result = new JsonListObjectInspector(listElementObjectInspector);
            cachedJsonListObjectInspector.put(key, result);
        }
        return result;
    }

    /*
     * Caches Map ObjectInspectors
     */
    static ConcurrentHashMap<Pair<ArrayList<Object>, JsonStructOIOptions>, JsonMapObjectInspector> cachedJsonMapObjectInspector
            = new ConcurrentHashMap<>();

    public static JsonMapObjectInspector getJsonMapObjectInspector(
            ObjectInspector mapKeyObjectInspector,
            ObjectInspector mapValueObjectInspector,
            JsonStructOIOptions options) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(mapKeyObjectInspector);
        signature.add(mapValueObjectInspector);
        final Pair<ArrayList<Object>, JsonStructOIOptions> key = Pair.of(signature, options);
        JsonMapObjectInspector result = cachedJsonMapObjectInspector.get(key);
        if (result == null) {
            result = new JsonMapObjectInspector(mapKeyObjectInspector, mapValueObjectInspector);
            cachedJsonMapObjectInspector.put(key, result);
        }
        return result;
    }

    static final ConcurrentHashMap<Pair<PrimitiveTypeInfo, JsonStructOIOptions>, AbstractPrimitiveJavaObjectInspector> primitiveOICache
            = new ConcurrentHashMap<>();

    /**
     * gets the appropriate adapter wrapper around the object inspector if
     * necessary, that is, if we're dealing with numbers. The JSON parser won't
     * parse the number because it's deferred (lazy).
     *
     * @param primitiveTypeInfo
     * @return
     */
    public static AbstractPrimitiveJavaObjectInspector getPrimitiveJavaObjectInspector(PrimitiveTypeInfo primitiveTypeInfo, JsonStructOIOptions options) {
        final Pair<PrimitiveTypeInfo, JsonStructOIOptions> key = Pair.of(primitiveTypeInfo, options);
        if (!primitiveOICache.containsKey(key)) {
            if (primitiveTypeInfo == TypeEntryShim.booleanType) {
                primitiveOICache.put(key, new JavaStringBooleanObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.byteType) {
                primitiveOICache.put(key, new JavaStringByteObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.shortType) {
                primitiveOICache.put(key, new JavaStringShortObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.intType) {
                primitiveOICache.put(key, new JavaStringIntObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.longType) {
                primitiveOICache.put(key, new JavaStringLongObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.floatType) {
                primitiveOICache.put(key, new JavaStringFloatObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.doubleType) {
                primitiveOICache.put(key, new JavaStringDoubleObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.binaryType) {
                primitiveOICache.put(key, new JavaStringBinaryObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.dateType) {
                primitiveOICache.put(key, new JavaStringDateObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.stringType) {
                primitiveOICache.put(key, new JsonStringJavaObjectInspector());
            } else if (primitiveTypeInfo == TypeEntryShim.timestampType) {
                primitiveOICache.put(key, new JavaStringTimestampObjectInspector(options.getTimestampFormats()));
            } else if (primitiveTypeInfo instanceof DecimalTypeInfo) {
                primitiveOICache.put(key, new JavaStringDecimalObjectInspector((DecimalTypeInfo) primitiveTypeInfo));
            } else {
                primitiveOICache.put(key, PrimitiveObjectInspectorFactory.getPrimitiveJavaObjectInspector(primitiveTypeInfo));
            }
        }
        return primitiveOICache.get(key);
    }


}
