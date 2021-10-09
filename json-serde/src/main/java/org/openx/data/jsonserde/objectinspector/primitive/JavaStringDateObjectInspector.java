package org.openx.data.jsonserde.objectinspector.primitive;

import org.apache.hadoop.hive.serde2.io.DateWritableV2;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.AbstractPrimitiveJavaObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.SettableDateObjectInspector;

import java.sql.Date;

/**
 * Created by rcongiu on 11/12/15.
 */
public class JavaStringDateObjectInspector  extends AbstractPrimitiveJavaObjectInspector
        implements SettableDateObjectInspector {

    public JavaStringDateObjectInspector() {
        super(TypeEntryShim.dateType);
    }

    @Override
    @Deprecated
    public Object set(Object o, Date date) {
        return date.toString();
    }

    @Override
    public Object set(Object o, org.apache.hadoop.hive.common.type.Date date) {
        return date.toString();
    }

    @Override
    public Object set(Object o, DateWritableV2 date) {
        return date.toString();
    }

    @Override
    @Deprecated
    public Object create(Date date) {
        return date.toString();
    }

    @Override
    public Object create(org.apache.hadoop.hive.common.type.Date date) {
        return date.toString();
    }

    @Override
    public DateWritableV2 getPrimitiveWritableObject(Object o) {
        if (o == null) return null;

        if (o instanceof String) {
            return new DateWritableV2(org.apache.hadoop.hive.common.type.Date.valueOf((String)o));
        }
        if (o instanceof Integer) {
            return new DateWritableV2((Integer) o);
        }
        if (o instanceof Date) {
            return new DateWritableV2(org.apache.hadoop.hive.common.type.Date.ofEpochDay((int) ((Date) o).toLocalDate().toEpochDay()));
        }
        return new DateWritableV2((org.apache.hadoop.hive.common.type.Date) o);
    }

    @Override
    public org.apache.hadoop.hive.common.type.Date getPrimitiveJavaObject(Object o) {
        if (o instanceof String) {
           return org.apache.hadoop.hive.common.type.Date.valueOf((String)o);
        }
        if (o instanceof Integer) {
            return org.apache.hadoop.hive.common.type.Date.ofEpochDay((Integer) o);
        }
        if (o instanceof Date) {
            return org.apache.hadoop.hive.common.type.Date.ofEpochDay((int) ((Date) o).toLocalDate().toEpochDay());
        }
        return (org.apache.hadoop.hive.common.type.Date) o;
    }
}