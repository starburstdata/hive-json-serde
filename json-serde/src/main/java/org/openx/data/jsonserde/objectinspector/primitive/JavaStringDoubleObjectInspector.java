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

import org.apache.hadoop.hive.serde2.io.DoubleWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.SettableDoubleObjectInspector;

import java.math.BigDecimal;

/**
 *
 * @author rcongiu
 */
public class JavaStringDoubleObjectInspector extends AbstractPrimitiveJavaObjectInspector
        implements SettableDoubleObjectInspector {

    public JavaStringDoubleObjectInspector() {
        super(TypeEntryShim.doubleType);
    }

    @Override
    public Object getPrimitiveWritableObject(Object o) {
        if(o == null) return null;
        return new DoubleWritable(get(o));
    }

    @Override
    public double get(Object o) {
        if (o instanceof Integer) {
            return ((Integer) o).doubleValue();
        }
        if (o instanceof Long) {
            return ((Long) o).doubleValue();
        }
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).doubleValue();
        }
        return Double.parseDouble(o.toString());
    }

    @Override
    public Object getPrimitiveJavaObject(Object o) {
        return get(o);
    }

    @Override
    public Object create(double value) {
        return value;
    }

    @Override
    public Object set(Object o, double value) {
        return value;
    }
    
}
