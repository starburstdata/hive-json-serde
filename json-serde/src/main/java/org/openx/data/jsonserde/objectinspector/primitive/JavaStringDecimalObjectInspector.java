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

import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.ql.metadata.Hive;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.SettableHiveDecimalObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.HiveDecimalUtils;

import java.math.BigInteger;

public class JavaStringDecimalObjectInspector
        extends AbstractPrimitiveJavaObjectInspector
        implements SettableHiveDecimalObjectInspector {

    public JavaStringDecimalObjectInspector(DecimalTypeInfo typeInfo) {
        super(typeInfo);
    }

    @Override
    public HiveDecimalWritable getPrimitiveWritableObject(Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof String) {
            HiveDecimal dec = enforcePrecisionScale(HiveDecimal.create((String)o));
            return dec == null ? null : new HiveDecimalWritable(dec);
        }

        HiveDecimal dec = enforcePrecisionScale((HiveDecimal)o);
        return dec == null ? null : new HiveDecimalWritable(dec);
    }

    @Override
    public HiveDecimal getPrimitiveJavaObject(Object o) {
        HiveDecimal dec;
        if (o instanceof HiveDecimal) {
            dec = (HiveDecimal) o;
        } else if (o instanceof String) {
            dec = HiveDecimal.create((String) o);
        } else if (o instanceof Number) {
            dec = HiveDecimal.create(o.toString());
        } else {
            return null;
        }
        return enforcePrecisionScale(dec);
    }

    @Override
    public Object set(Object o, byte[] bytes, int scale) {
        return enforcePrecisionScale(HiveDecimal.create(new BigInteger(bytes), scale));
    }

    @Override
    public Object set(Object o, HiveDecimal t) {
        return enforcePrecisionScale(t);
    }

    @Override
    public Object set(Object o, HiveDecimalWritable t) {
        return t == null ? null : enforcePrecisionScale(t.getHiveDecimal());
    }

    @Override
    public Object create(byte[] bytes, int scale) {
        return HiveDecimal.create(new BigInteger(bytes), scale);
    }

    @Override
    public Object create(HiveDecimal t) {
        return t;
    }

    private HiveDecimal enforcePrecisionScale(HiveDecimal dec) {
        return HiveDecimalUtils.enforcePrecisionScale(dec,(DecimalTypeInfo)typeInfo);
    }

}
