package org.openx.data.jsonserde.objectinspector.primitive;


import org.apache.hadoop.hive.serde2.lazy.LazyUtils;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.SettableBinaryObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.SettableStringObjectInspector;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;

import java.util.Arrays;
import java.util.Base64;

public class JavaStringBinaryObjectInspector
    extends AbstractPrimitiveJavaObjectInspector
        implements SettableBinaryObjectInspector {

    public JavaStringBinaryObjectInspector() {
        super(TypeEntryShim.binaryType);
    }

    @Override
    public BytesWritable getPrimitiveWritableObject(Object o) {
        if (o == null) {
            return null;
        }

        if (o instanceof String) {
            return new BytesWritable(Base64.getDecoder().decode((String) o));
        }
        return new BytesWritable((byte[])o);
    }

    @Override
    public byte[] getPrimitiveJavaObject(Object o) {
        if (o instanceof String) {
            return Base64.getDecoder().decode((String) o);
        }
        return (byte[])o;
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public byte[] set(Object o, byte[] bb) {
        return bb == null ? null : Base64.getEncoder().encode(bb);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public byte[] set(Object o, BytesWritable bw) {
        return bw == null ? null : Base64.getEncoder().encode(bw.getBytes());
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public byte[] create(byte[] bb) {
        return bb == null ? null : Base64.getEncoder().encode(bb);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public byte[] create(BytesWritable bw) {
        return bw == null ? null : Base64.getEncoder().encode(bw.getBytes());
    }

}
