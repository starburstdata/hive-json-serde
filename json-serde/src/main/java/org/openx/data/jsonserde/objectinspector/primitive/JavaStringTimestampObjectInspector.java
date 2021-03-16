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

package org.openx.data.jsonserde.objectinspector.primitive;

import org.apache.hadoop.hive.serde2.io.TimestampWritableV2;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.SettableTimestampObjectInspector;

import java.sql.Timestamp;

/**
 * A timestamp that is stored in a String
 * @author rcongiu
 */
public class JavaStringTimestampObjectInspector extends AbstractPrimitiveJavaObjectInspector
    implements SettableTimestampObjectInspector {
    
    public JavaStringTimestampObjectInspector() {
        super(TypeEntryShim.timestampType);
    }

    @Override
    public Object set(Object o, byte[] bytes, int offset) {
        return create(bytes,offset);
    }

    @Override
    @Deprecated
    public Object set(Object o, Timestamp tmstmp) {
        return ParsePrimitiveUtils.serializeAsUTC(tmstmp);
    }

    @Override
    public Object set(Object o, org.apache.hadoop.hive.common.type.Timestamp timestamp) {
        return ParsePrimitiveUtils.serializeAsUTC(timestamp);
    }

    @Override
    public Object set(Object o, TimestampWritableV2 tw) {
        return create(tw.getTimestamp());
    }

    @Override
    public Object create(byte[] bytes, int offset) {
       return new TimestampWritableV2(bytes, offset).toString();
    }

    @Override
    @Deprecated
    public Object create(Timestamp tmstmp) {
        return ParsePrimitiveUtils.serializeAsUTC(tmstmp);
    }

    @Override
    public Object create(org.apache.hadoop.hive.common.type.Timestamp timestamp) {
        return ParsePrimitiveUtils.serializeAsUTC(timestamp);
    }

    @Override
    public TimestampWritableV2 getPrimitiveWritableObject(Object o) {
        if(o == null) return null;
        
        if(o instanceof String) {
           return new TimestampWritableV2(org.apache.hadoop.hive.common.type.Timestamp.valueOf((String)o));
        } else if (o instanceof org.apache.hadoop.hive.common.type.Timestamp) {
          return new TimestampWritableV2((org.apache.hadoop.hive.common.type.Timestamp) o);
        } else {
            return (TimestampWritableV2) o;
        }
    }

    @Override
    public org.apache.hadoop.hive.common.type.Timestamp getPrimitiveJavaObject(Object o) {
        if(o instanceof String) {
            return ParsePrimitiveUtils.parseTimestamp((String) o);
        } else if(o instanceof Number) {
            return ParsePrimitiveUtils.parseTimestamp(o.toString());
        } else {
           return (org.apache.hadoop.hive.common.type.Timestamp) o;
        }
    }
}
