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

import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.SettableLongObjectInspector;
import org.apache.hadoop.io.LongWritable;

import java.math.BigDecimal;

/**
 *
 * @author rcongiu
 */
public class JavaStringLongObjectInspector
        extends AbstractPrimitiveJavaObjectInspector
        implements SettableLongObjectInspector {

    public JavaStringLongObjectInspector() {
        super(TypeEntryShim.longType);
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        if(o == null) return null;
        return new LongWritable(get(o));
    }

    @Override
    public long get(Object o) {
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof Double) {
            return ((Double) o).longValue();
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).longValue();
        }
        return ParsePrimitiveUtils.parseLong(o.toString());
    }

    @Override
    public Object getPrimitiveJavaObject(Object o)
    {
        return get(o);
    }

    @Override
    public Object create(long value) {
        return value;
    }

    @Override
    public Object set(Object o, long value) {
        return value;
    }
}
