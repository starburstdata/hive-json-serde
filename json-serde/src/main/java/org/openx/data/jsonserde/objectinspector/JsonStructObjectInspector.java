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

import io.starburst.openjson.JSONArray;
import io.starburst.openjson.JSONException;
import io.starburst.openjson.JSONObject;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StandardStructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.openx.data.jsonserde.objectinspector.primitive.JsonStringJavaObjectInspector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This Object Inspector is used to look into a JSonObject object.
 * We couldn't use StandardStructObjectInspector since that expects 
 * something that can be cast to an Array<Object>.
 * @author rcongiu
 */
public class JsonStructObjectInspector extends StandardStructObjectInspector {
    JsonStructOIOptions options = null;

  /*  
    public JsonStructObjectInspector(List<String> structFieldNames,
            List<ObjectInspector> structFieldObjectInspectors) {
        super(structFieldNames, structFieldObjectInspectors);
    } */
    
      public JsonStructObjectInspector(List<String> structFieldNames,
            List<ObjectInspector> structFieldObjectInspectors,JsonStructOIOptions opts) {
        super(structFieldNames, structFieldObjectInspectors);   
        
        options = opts;
    }

      /**
       * Extract the data from the requested field.
       * 
       * @param data
       * @param fieldRef
       * @return 
       */
    @Override
    public Object getStructFieldData(Object data, StructField fieldRef) {
        if (JsonObjectInspectorUtils.checkObject(data) == null) {
            return null;
        }
        
        if( data instanceof JSONObject) {
            return getStructFieldDataFromJsonObject((JSONObject) data, fieldRef );
        } if (data instanceof List) {
            // somehow we have the object parsed already
            return getStructFieldDataFromList((List) data, fieldRef );
        } else if (data instanceof JSONArray) {
            JSONArray ja = (JSONArray) data;
            // se #113: some people complain of receving bad JSON,
            // sometimes getting [] instead of {} for an empty field.
            // this line should help them
            if(ja.length() == 0 ) return null;
            return getStructFieldDataFromJsonArray(ja, fieldRef );
        } else {
            throw new Error("Data is not JSONObject  but " + data.getClass().getCanonicalName() +
                    " with value " + data.toString()) ;
        } 
    }
    
    /**
     * retrieves data assuming it's in a list, usually during serialization
     * @param data
     * @param fieldRef
     * @return 
     */
    public Object getStructFieldDataFromList(List data, StructField fieldRef ) {
       int idx = fields.indexOf(fieldRef);
       if(idx <0 || idx >= data.size()) {
           return null;
       } else {
           return data.get(idx);
       }
    }

    public Object getStructFieldDataFromJsonArray(JSONArray data, StructField fieldRef ) {
        int idx = fields.indexOf(fieldRef);
        try {
            return data.get(idx);
        } catch (JSONException ex) {
            return null;
        }
    }

    public Object getStructFieldDataFromJsonObject(JSONObject data, StructField fieldRef ) {
        if (JsonObjectInspectorUtils.checkObject(data) == null) {
            return null;
        }
        
        MyField f = (MyField) fieldRef;

        int fieldID = f.getFieldID();
        assert fieldID >= 0 && fieldID < fields.size();

        Object fieldData = null;
        
        try {
            if (data.has(getJsonField(fieldRef))) {
               fieldData = data.get(getJsonField(fieldRef));

            }  else if(options.dotsInKeyNames) {
                // no mappings...but there are dots in name
                for(Iterator i = data.keys(); i.hasNext(); ) {
                    String s  = (String) i.next(); // name in json object
                    if (s == null) break;

                    if(s.contains(".")) {
                        // substitute . with _
                        String name = s.replaceAll("\\.", "_");
                        // does it match the struct field name ?
                        if(fieldRef.getFieldName().equals(name)) {
                            fieldData = data.get(s);
                            break;
                        }
                    }
                }
            }
        } catch (JSONException ex) {
            // if key does not exist
        }
        if (fieldData == JSONObject.NULL) {
            fieldData = null;
        }
        if ((fieldData instanceof JSONObject || fieldData instanceof JSONArray) && fields.get(fieldID).getFieldObjectInspector() instanceof JsonStringJavaObjectInspector) {
            JsonStringJavaObjectInspector fieldInspector = (JsonStringJavaObjectInspector) fields.get(fieldID).getFieldObjectInspector();
            return fieldInspector.getPrimitiveJavaObject(fieldData);
        }
        return fieldData;
    }
    
    

    
    /**
     * called to map from hive to json
     * @param fr
     * @return 
     */
    protected String getJsonField(StructField fr) {
        if(options.getMappings() != null && options.getMappings().containsKey(fr.getFieldName())) {
            return options.getMappings().get(fr.getFieldName());
        } else {
            return fr.getFieldName();
        }
    }
    
    List<Object> values = new ArrayList<Object>();
    @Override
    public List<Object> getStructFieldsDataAsList(Object o) {
	if (JsonObjectInspectorUtils.checkObject(o) == null) {
            return null;
        }
        JSONObject jObj = (JSONObject) o;
        values.clear();

        for (int i = 0; i < fields.size(); i++) {
                if (jObj.has(getJsonField(fields.get(i)))){
                    values.add(getStructFieldData(o, fields.get(i)));
                } else {
                    values.add(null);
                }
        }
        return values;
    }
}
