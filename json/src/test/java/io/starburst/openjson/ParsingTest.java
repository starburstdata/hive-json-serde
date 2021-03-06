/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.starburst.openjson;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParsingTest {

    @Test
    public void testParsingNoObjects() {
        try {
            new JSONTokener(true, "").nextValue();
            fail();
        } catch (JSONException ignored) {
        }
    }

    @Test
    public void testParsingLiterals() throws JSONException {
        assertParsed(Boolean.TRUE, "true");
        assertParsed(Boolean.FALSE, "false");
        assertParsed(JSONObject.NULL, "null");
        assertParsed(JSONObject.NULL, "NULL");
        assertParsed(Boolean.FALSE, "False");
        assertParsed(Boolean.TRUE, "truE");
    }

    @Test
    public void testParsingQuotedStrings() throws JSONException {
        assertParsed("abc", "\"abc\"");
        assertParsed("123", "\"123\"");
        assertParsed("foo\nbar", "\"foo\\nbar\"");
        assertParsed("foo bar", "\"foo\\u0020bar\"");
        assertParsed("\"{}[]/\\:,=;#", "\"\\\"{}[]/\\\\:,=;#\"");
    }

    @Test
    public void testParsingSingleQuotedStrings() throws JSONException {
        assertParsed("abc", "'abc'");
        assertParsed("123", "'123'");
        assertParsed("foo\nbar", "'foo\\nbar'");
        assertParsed("foo bar", "'foo\\u0020bar'");
        assertParsed("\"{}[]/\\:,=;#", "'\\\"{}[]/\\\\:,=;#'");
    }

    @Test
    public void testParsingUnquotedStrings() throws JSONException {
        assertParsed("abc", "abc");
        assertParsed("123abc", "123abc");
        assertParsed("123e0x", "123e0x");
        assertParsed("123e", "123e");
        assertParsed("123ee21", "123ee21");
        assertParsed("0xFFFFFFFFFFFFFFFFF", "0xFFFFFFFFFFFFFFFFF");
    }

    /**
     * Unfortunately the original implementation attempts to figure out what
     * Java number type best suits an input value.
     */
    @Test
    public void testParsingNumbersThatAreBestRepresentedAsLongs() throws JSONException {
        assertParsed(Long.MAX_VALUE, Long.toString(Long.MAX_VALUE));
        assertParsed((long) Integer.MAX_VALUE + 1L, Long.toString((long) Integer.MAX_VALUE + 1L));
        assertParsed(Long.MIN_VALUE, Long.toString(Long.MIN_VALUE));
        assertParsed((long) Integer.MIN_VALUE - 1L, Long.toString((long) Integer.MIN_VALUE - 1L));
        assertParsed(Long.MAX_VALUE - 1L, Long.toString(Long.MAX_VALUE - 1L));
        assertParsed(Long.MIN_VALUE + 1L, Long.toString(Long.MIN_VALUE + 1L));
    }

    @Test
    public void testParsingNumbersThatAreBestRepresentedAsIntegers() throws JSONException {
        assertParsed(0, "0");
        assertParsed(5, "5");
        assertParsed(Integer.MIN_VALUE, Integer.toString(Integer.MIN_VALUE));
        assertParsed(Integer.MAX_VALUE, Integer.toString(Integer.MAX_VALUE));
    }

    @Test
    public void testParsingNegativeZero() throws JSONException {
        assertParsed(0, "-0");
    }

    @Test
    public void testParsingIntegersWithAdditionalPrecisionYieldDoubles() throws JSONException {
        assertParsed(1d, "1.00");
        assertParsed(1d, "1.0");
        assertParsed(0d, "0.0");
        assertParsed(-0d, "-0.0");
    }

    @Test
    public void testParsingNumbersThatAreBestRepresentedAsDoubles() throws JSONException {
        assertParsed(Double.MAX_VALUE, Double.toString(Double.MAX_VALUE));
        assertParsed(Double.MIN_NORMAL, Double.toString(Double.MIN_NORMAL));
        assertParsed(Double.MIN_VALUE, Double.toString(Double.MIN_VALUE));
        assertParsed(1.7976931348623157E308, "1.7976931348623157e308");
        assertParsed(2.2250738585072014E-308, "2.2250738585072014E-308");
        assertParsed(4.9E-324, "4.9E-324");
        assertParsed(4.9E-324, "4.9e-324");
    }

    @Test
    public void testParsingNumbersThatAreBestRepresentedAsDecimals() throws JSONException {
        assertParsed(new BigDecimal("9223372036854775808"), "9223372036854775808");
        assertParsed(new BigDecimal("-9223372036854775809"), "-9223372036854775809");
        assertParsed(new BigDecimal("12345678901234567890.123456789012345678"), "12345678901234567890.123456789012345678");
        assertParsed(new BigDecimal("-12345678901234567890.123456789012345678"), "-12345678901234567890.123456789012345678");
    }

    @Test
    public void testParsingOctalNumbers() throws JSONException {
        assertParsed(5, "05");
        assertParsed(8, "010");
        assertParsed(1046, "02026");
    }

    @Test
    public void testParsingHexNumbers() throws JSONException {
        assertParsed(5, "0x5");
        assertParsed(16, "0x10");
        assertParsed(8230, "0x2026");
        assertParsed(180150010, "0xABCDEFA");
        assertParsed(2077093803, "0x7BCDEFAB");
    }

    @Test
    public void testParsingLargeHexValues() throws JSONException {
        assertParsed(Integer.MAX_VALUE, "0x7FFFFFFF");
        String message = "Hex values are parsed as Strings if their signed " +
                "value is greater than Integer.MAX_VALUE.";
        assertParsed(message, 0x80000000L, "0x80000000");
    }

    @Test
    public void test64BitHexValues() throws JSONException {
        // note that this is different from the same test in the original Android
        // this is due to the fact that Long.parseLong doesn't correctly handle
        // the value -1 expressed as unsigned hex if you use the normal JDK. Presumably
        // the Android equivalent program does this better.
        assertParsed("Large hex longs shouldn't yield ints or strings",
                0xFFFFFFFFFFFFFFFL, "0xFFFFFFFFFFFFFFF");
    }

    @Test
    public void testParsingWithCommentsAndWhitespace() throws JSONException {
        assertParsed("baz", "  // foo bar \n baz");
        assertParsed("baz", "  // foo bar \r baz");
        assertParsed("baz", "  // foo bar \r\n baz");
        assertParsed("baz", "  # foo bar \n baz");
        assertParsed("baz", "  # foo bar \r baz");
        assertParsed("baz", "  # foo bar \r\n baz");
        assertParsed(5, "  /* foo bar \n baz */ 5");
        assertParsed(5, "  /* foo bar \n baz */ 5 // quux");
        assertParsed(5, "  5   ");
        assertParsed(5, "  5  \r\n\t ");
        assertParsed(5, "\r\n\t   5 ");
    }

    @Test
    public void testParsingArrays() throws JSONException {
        assertParsed(array(), "[]");
        assertParsed(array(5, 6, true), "[5,6,true]");
        assertParsed(array(5, 6, array()), "[5,6,[]]");
        assertParsed(array(5, 6, 7), "[5;6;7]");
        assertParsed(array(5, 6, 7), "[5  , 6 \t; \r\n 7\n]");
        assertParsed(array(5, 6, 7, null), "[5,6,7,]");
        assertParsed(array(null, null), "[,]");
        assertParsed(array(5, null, null, null, 5), "[5,,,,5]");
        assertParsed(array(null, 5), "[,5]");
        assertParsed(array(null, null, null), "[,,]");
        assertParsed(array(null, null, null, 5), "[,,,5]");
    }

    @Test
    public void testParsingObjects() throws JSONException {
        assertParsed(object("foo", 5), "{\"foo\": 5}");
        assertParsed(object("foo", 5), "{foo: 5}");
        assertParsed(object("foo", 5, "bar", "baz"), "{\"foo\": 5, \"bar\": \"baz\"}");
        assertParsed(object("foo", 5, "bar", "baz"), "{\"foo\": 5; \"bar\": \"baz\"}");
        assertParsed(object("foo", 5, "bar", "baz"), "{\"foo\"= 5; \"bar\"= \"baz\"}");
        assertParsed(object("foo", 5, "bar", "baz"), "{\"foo\"=> 5; \"bar\"=> \"baz\"}");
        assertParsed(object("foo", object(), "bar", array()), "{\"foo\"=> {}; \"bar\"=> []}");
        assertParsed(object("foo", object("foo", array(5, 6))), "{\"foo\": {\"foo\": [5, 6]}}");
        assertParsed(object("foo", object("foo", array(5, 6))), "{\"foo\":\n\t{\t \"foo\":[5,\r6]}}");
    }

    @Test
    public void testSyntaxProblemUnterminatedObject() {
        assertParseFail("{");
        assertParseFail("{\"foo\"");
        assertParseFail("{\"foo\":");
        assertParseFail("{\"foo\":bar");
        assertParseFail("{\"foo\":bar,");
        assertParseFail("{\"foo\":bar,\"baz\"");
        assertParseFail("{\"foo\":bar,\"baz\":");
        assertParseFail("{\"foo\":bar,\"baz\":true");
        assertParseFail("{\"foo\":bar,\"baz\":true,");
    }

    @Test
    public void testSyntaxProblemEmptyString() {
        assertParseFail("");
    }

    @Test
    public void testSyntaxProblemUnterminatedArray() {
        assertParseFail("[");
        assertParseFail("[,");
        assertParseFail("[,,");
        assertParseFail("[true");
        assertParseFail("[true,");
        assertParseFail("[true,,");
    }

    @Test
    public void testSyntaxProblemMalformedObject() {
        assertParseFail("{:}");
        assertParseFail("{\"key\":}");
        assertParseFail("{:true}");
        assertParseFail("{\"key\":true:}");
        assertParseFail("{null:true}");
        assertParseFail("{true:true}");
        assertParseFail("{0xFF:true}");
    }

    private void assertParseFail(String malformedJson) {
        try {
            new JSONTokener(true, malformedJson).nextValue();
            fail("Successfully parsed: \"" + malformedJson + "\"");
        } catch (JSONException ignored) {
        } catch (StackOverflowError e) {
            fail("Stack overflowed on input: \"" + malformedJson + "\"");
        }
    }

    private JSONArray array(Object... elements) {
        return new JSONArray(true, Arrays.asList(elements));
    }

    private JSONObject object(Object... keyValuePairs) throws JSONException {
        JSONObject result = new JSONObject(true);
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            result.put((String) keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return result;
    }

    private void assertParsed(String message, Object expected, String json) throws JSONException {
        Object actual = new JSONTokener(true, json).nextValue();
        actual = canonicalize(actual);
        expected = canonicalize(expected);
        assertEquals("For input \"" + json + "\" " + message, expected, actual);
    }

    private void assertParsed(Object expected, String json) throws JSONException {
        assertParsed("", expected, json);
    }

    /**
     * Since they don't implement equals or hashCode properly, this recursively
     * replaces JSONObjects with an equivalent HashMap, and JSONArrays with the
     * equivalent ArrayList.
     */
    private Object canonicalize(Object input) throws JSONException {
        if (input instanceof JSONArray) {
            JSONArray array = (JSONArray) input;
            List<Object> result = new ArrayList<Object>();
            for (int i = 0; i < array.length(); i++) {
                result.add(canonicalize(array.opt(i)));
            }
            return result;
        } else if (input instanceof JSONObject) {
            JSONObject object = (JSONObject) input;
            Map<String, Object> result = new HashMap<String, Object>();
            for (Iterator<?> i = object.keys(); i.hasNext(); ) {
                String key = (String) i.next();
                result.put(key, canonicalize(object.get(key)));
            }
            return result;
        } else if (input == null || input.equals(JSONObject.NULL)) {
            return JSONObject.NULL;
        } else {
            return input;
        }
    }
}
