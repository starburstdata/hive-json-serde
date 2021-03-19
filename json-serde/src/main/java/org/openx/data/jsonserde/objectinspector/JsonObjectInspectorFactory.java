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


import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.MapTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.UnionTypeInfo;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringBooleanObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringByteObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringDateObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringDoubleObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringFloatObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringIntObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringLongObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringShortObjectInspector;
import org.openx.data.jsonserde.objectinspector.primitive.JavaStringTimestampObjectInspector;
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

    static ConcurrentHashMap<TypeInfo, ObjectInspector> cachedJsonObjectInspector = new ConcurrentHashMap<>();

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
    public static ObjectInspector getJsonObjectInspectorFromTypeInfo(
            TypeInfo typeInfo, JsonStructOIOptions options) {
        ObjectInspector result = cachedJsonObjectInspector.get(typeInfo);
        // let the factory cache the struct object inspectors since
        // their key also has some options
        if (result == null ||  typeInfo.getCategory() == ObjectInspector.Category.STRUCT) {
            switch (typeInfo.getCategory()) {
                case PRIMITIVE: {
                    PrimitiveTypeInfo pti = (PrimitiveTypeInfo) typeInfo;
                    result = getPrimitiveJavaObjectInspector(pti);
                    break;
                }
                case LIST: {
                    ObjectInspector elementObjectInspector
                            = getJsonObjectInspectorFromTypeInfo(
                            ((ListTypeInfo) typeInfo).getListElementTypeInfo(),
                            options);
                    result = JsonObjectInspectorFactory.getJsonListObjectInspector(elementObjectInspector);
                    break;
                }
                case MAP: {
                    MapTypeInfo mapTypeInfo = (MapTypeInfo) typeInfo;
                    ObjectInspector keyObjectInspector = getJsonObjectInspectorFromTypeInfo(mapTypeInfo.getMapKeyTypeInfo(), options);
                    ObjectInspector valueObjectInspector = getJsonObjectInspectorFromTypeInfo(mapTypeInfo.getMapValueTypeInfo(), options);
                    result = JsonObjectInspectorFactory.getJsonMapObjectInspector(keyObjectInspector,
                            valueObjectInspector);
                    break;
                }
                case STRUCT: {
                    StructTypeInfo structTypeInfo = (StructTypeInfo) typeInfo;
                    List<String> fieldNames = structTypeInfo.getAllStructFieldNames();
                    List<TypeInfo> fieldTypeInfos = structTypeInfo.getAllStructFieldTypeInfos();
                    List<ObjectInspector> fieldObjectInspectors = new ArrayList<ObjectInspector>(
                            fieldTypeInfos.size());
                    for (int i = 0; i < fieldTypeInfos.size(); i++) {
                        fieldObjectInspectors.add(getJsonObjectInspectorFromTypeInfo(
                                fieldTypeInfos.get(i), options));
                    }
                    result = JsonObjectInspectorFactory.getJsonStructObjectInspector(fieldNames,
                            fieldObjectInspectors, options);
                    break;
                }
                case UNION:{
                    List<ObjectInspector> ois = new LinkedList<ObjectInspector>();
                    for(  TypeInfo ti : ((UnionTypeInfo) typeInfo).getAllUnionObjectTypeInfos()) {
                        ois.add(getJsonObjectInspectorFromTypeInfo(ti, options));
                    }
                    result = getJsonUnionObjectInspector(ois, options);
                    break;
                }

                default: {
                    result = null;
                }
            }
            cachedJsonObjectInspector.put(typeInfo, result);
        }
        return result;
    }


    static ConcurrentHashMap<ArrayList<Object>, JsonUnionObjectInspector> cachedJsonUnionObjectInspector
            = new ConcurrentHashMap<>();

    public static JsonUnionObjectInspector getJsonUnionObjectInspector(
            List<ObjectInspector> ois,
            JsonStructOIOptions options) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(ois);
        signature.add(options);
        JsonUnionObjectInspector result = cachedJsonUnionObjectInspector
                .get(signature);
        if (result == null) {
            result = new JsonUnionObjectInspector(ois, options);
            cachedJsonUnionObjectInspector.put(signature,result);

        }
        return result;
    }

    /*
     * Caches Struct Object Inspectors
     */
    static ConcurrentHashMap<ArrayList<Object>, JsonStructObjectInspector> cachedStandardStructObjectInspector
            = new ConcurrentHashMap<>();


    public static JsonStructObjectInspector getJsonStructObjectInspector(
            List<String> structFieldNames,
            List<ObjectInspector> structFieldObjectInspectors,
            JsonStructOIOptions options) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(structFieldNames);
        signature.add(structFieldObjectInspectors);
        signature.add(options);

        JsonStructObjectInspector result = cachedStandardStructObjectInspector.get(signature);
        if (result == null) {
            result = new JsonStructObjectInspector(structFieldNames,
                    structFieldObjectInspectors, options);
            cachedStandardStructObjectInspector.put(signature, result);
        }
        return result;
    }

    /*
     * Caches the List object inspectors
     */
    static ConcurrentHashMap<ArrayList<Object>, JsonListObjectInspector> cachedJsonListObjectInspector
            = new ConcurrentHashMap<>();

    public static JsonListObjectInspector getJsonListObjectInspector(
            ObjectInspector listElementObjectInspector) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(listElementObjectInspector);
        JsonListObjectInspector result = cachedJsonListObjectInspector
                .get(signature);
        if (result == null) {
            result = new JsonListObjectInspector(listElementObjectInspector);
            cachedJsonListObjectInspector.put(signature, result);
        }
        return result;
    }

    /*
     * Caches Map ObjectInspectors
     */
    static ConcurrentHashMap<ArrayList<Object>, JsonMapObjectInspector> cachedJsonMapObjectInspector
            = new ConcurrentHashMap<>();

    public static JsonMapObjectInspector getJsonMapObjectInspector(
            ObjectInspector mapKeyObjectInspector,
            ObjectInspector mapValueObjectInspector) {
        ArrayList<Object> signature = new ArrayList<Object>();
        signature.add(mapKeyObjectInspector);
        signature.add(mapValueObjectInspector);
        JsonMapObjectInspector result = cachedJsonMapObjectInspector
                .get(signature);
        if (result == null) {
            result = new JsonMapObjectInspector(mapKeyObjectInspector,
                    mapValueObjectInspector);
            cachedJsonMapObjectInspector.put(signature, result);
        }
        return result;
    }

    static final ConcurrentHashMap<PrimitiveTypeInfo, AbstractPrimitiveJavaObjectInspector> primitiveOICache
            = new ConcurrentHashMap<>();

    static {
        primitiveOICache.put(TypeEntryShim.booleanType, new JavaStringBooleanObjectInspector());
        primitiveOICache.put(TypeEntryShim.byteType, new JavaStringByteObjectInspector());
        primitiveOICache.put(TypeEntryShim.shortType, new JavaStringShortObjectInspector());
        primitiveOICache.put(TypeEntryShim.intType, new JavaStringIntObjectInspector());
        primitiveOICache.put(TypeEntryShim.longType, new JavaStringLongObjectInspector());
        primitiveOICache.put(TypeEntryShim.floatType, new JavaStringFloatObjectInspector());
        primitiveOICache.put(TypeEntryShim.doubleType, new JavaStringDoubleObjectInspector());
        primitiveOICache.put(TypeEntryShim.dateType, new JavaStringDateObjectInspector());
        primitiveOICache.put(TypeEntryShim.timestampType, new JavaStringTimestampObjectInspector());
        // add the OIs that were introduced in different versions of hive
        TypeEntryShim.addObjectInspectors(primitiveOICache);
    }

    /**
     * gets the appropriate adapter wrapper around the object inspector if
     * necessary, that is, if we're dealing with numbers. The JSON parser won't
     * parse the number because it's deferred (lazy).
     *
     * @param primitiveTypeInfo
     * @return
     */
    public static AbstractPrimitiveJavaObjectInspector getPrimitiveJavaObjectInspector(PrimitiveTypeInfo primitiveTypeInfo) {
            if (!primitiveOICache.containsKey(primitiveTypeInfo)) {
                primitiveOICache.put(primitiveTypeInfo, PrimitiveObjectInspectorFactory.getPrimitiveJavaObjectInspector(primitiveTypeInfo));
            }
            return primitiveOICache.get(primitiveTypeInfo);
    }


}
