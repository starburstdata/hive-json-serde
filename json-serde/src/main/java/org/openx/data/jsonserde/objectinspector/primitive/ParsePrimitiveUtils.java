/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openx.data.jsonserde.objectinspector.primitive;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author rcongiu
 */
public final class ParsePrimitiveUtils {

    private ParsePrimitiveUtils() {
        throw new InstantiationError("This class must not be instantiated.");
    }

    private static final DateTimeFormatter NO_COLON_OFFSET_FORMAT;
    private static final DateTimeFormatter LOCAL_PRINT_FORMATTER;
    private static final DateTimeFormatter UTC_PRINT_FORMATTER;

    static {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
        // Date part
        builder.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        builder.appendOffset("+HHMM", "Z");
        NO_COLON_OFFSET_FORMAT = builder.toFormatter().withResolverStyle(ResolverStyle.LENIENT);
        builder = new DateTimeFormatterBuilder();
        // Date and time parts
        builder.append(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // Fractional part
        builder.optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd();
        LOCAL_PRINT_FORMATTER = builder.toFormatter();
        builder = new DateTimeFormatterBuilder();
        // Date and time parts
        builder.append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        // Fractional part
        builder.optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd();
        builder.appendLiteral('Z');
        UTC_PRINT_FORMATTER = builder.toFormatter();
    }

    static Pattern hasTZOffset = Pattern.compile(".+(\\+|-)\\d{2}:?\\d{2}$");

    public static boolean isHex(String s) {
        return s.startsWith("0x") || s.startsWith("0X");
    }

    public static byte parseByte(String s) {
        if (isHex(s)) {
            return Byte.parseByte(s.substring(2), 16);
        } else {
            return Byte.parseByte(s);
        }
    }

    public static int parseInt(String s) {
        if (isHex(s)) {
            return Integer.parseInt(s.substring(2), 16);
        } else {
            return Integer.parseInt(s);
        }
    }

    public static short parseShort(String s) {
        if (isHex(s)) {
            return Short.parseShort(s.substring(2), 16);
        } else {
            return Short.parseShort(s);
        }
    }

    public static long parseLong(String s) {
        if (isHex(s)) {
            return Long.parseLong(s.substring(2), 16);
        } else {
            return Long.parseLong(s);
        }
    }

    public static String serializeAsUTC(Timestamp ts) {
        return UTC_PRINT_FORMATTER.format(ts.toInstant());
    }

    public static String serializeAsUTC(org.apache.hadoop.hive.common.type.Timestamp ts) {
        return ts.format(UTC_PRINT_FORMATTER);
    }

    public static org.apache.hadoop.hive.common.type.Timestamp parseTimestamp(String s, List<DateTimeFormatter> timestampFormaters) {
        if (timestampFormaters == null) {
            final String sampleUnixTimestampInMs = "1454612111000";
            org.apache.hadoop.hive.common.type.Timestamp value;
            if (s.indexOf(':') > 0) {
                value = org.apache.hadoop.hive.common.type.Timestamp.valueOf(nonUTCFormat(s));
            } else if (s.indexOf('.') >= 0) {
                // it's a float
                double secondsFromEpoch = Double.parseDouble(s);
                value = org.apache.hadoop.hive.common.type.Timestamp.ofEpochMilli((long) (secondsFromEpoch * 1_000));
            } else {
                // integer
                long timestampValue = Long.parseLong(s);
                boolean isTimestampInMs = s.length() >= sampleUnixTimestampInMs.length();
                if (isTimestampInMs) {
                    value = org.apache.hadoop.hive.common.type.Timestamp.ofEpochMilli(timestampValue);
                } else {
                    value = org.apache.hadoop.hive.common.type.Timestamp.ofEpochSecond(timestampValue);
                }
            }
            return value;
        } else {
            DateTimeParseException lastException = null;
            for (DateTimeFormatter formatter : timestampFormaters) {
                try {
                    ZonedDateTime zonedDateTime = formatter.parse(s, ZonedDateTime::from);
                    return org.apache.hadoop.hive.common.type.Timestamp.ofEpochSecond(zonedDateTime.toEpochSecond(), zonedDateTime.getNano());
                } catch (DateTimeParseException ex) {
                    lastException = ex;
                }
            }
            throw lastException;
        }
    }

    /**
     * Timestamp.parse gets an absolute time, without the timezone.
     * This function translates to the right string format that Timestamp
     * can parse.
     *
     * @param s
     * @return
     */
    public static String nonUTCFormat(String s) {
        ZonedDateTime parsed = null;
        try {
            if (s.endsWith("Z") || hasTZOffset.matcher(s).matches()) {
                if (s.charAt(s.length() - 3) == ':') {
                    parsed = ZonedDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME); // -06:00
                } else {
                    parsed = ZonedDateTime.parse(s, NO_COLON_OFFSET_FORMAT); // -0600
                }
            } else {
                return s;
            }
        } catch (DateTimeParseException e) {
                e.printStackTrace();
        }

        if (parsed != null) {
            return LOCAL_PRINT_FORMATTER.format(parsed.withZoneSameInstant(ZoneId.of("UTC")));
        } else {
            return s;
        }
    }


}
