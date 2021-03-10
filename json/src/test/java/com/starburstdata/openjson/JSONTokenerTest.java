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

package com.starburstdata.openjson;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * This black box test was written without inspecting the non-free org.json sourcecode.
 */
public class JSONTokenerTest extends TestCase {

    public void testNulls() throws JSONException {
        // JSONTokener accepts null, only to fail later on almost all APIs!
        new JSONTokener(true, (String) null).back();

        try {
            new JSONTokener(true, (String) null).more();
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).next();
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).next(3);
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).next('A');
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).nextClean();
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).nextString('"');
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).nextTo('A');
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).nextTo("ABC");
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).nextValue();
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).skipPast("ABC");
            fail();
        } catch (NullPointerException ignored) {
        }

        try {
            new JSONTokener(true, (String) null).skipTo('A');
            fail();
        } catch (NullPointerException ignored) {
        }

        //noinspection ThrowableResultOfMethodCallIgnored
        assertEquals("foo! at character 0 of null",
                new JSONTokener(true, (String) null).syntaxError("foo!").getMessage());

        assertEquals(" at character 0 of null", new JSONTokener(true, (String) null).toString());
    }

    public void testEmptyString() throws JSONException {
        JSONTokener backTokener = new JSONTokener(true, "");
        backTokener.back();
        assertEquals(" at character 0 of ", backTokener.toString());
        assertFalse(new JSONTokener(true, "").more());
        assertEquals('\0', new JSONTokener(true, "").next());
        try {
            new JSONTokener(true, "").next(3);
            fail();
        } catch (JSONException ignored) {
        }
        try {
            new JSONTokener(true, "").next('A');
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals('\0', new JSONTokener(true, "").nextClean());
        try {
            new JSONTokener(true, "").nextString('"');
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals("", new JSONTokener(true, "").nextTo('A'));
        assertEquals("", new JSONTokener(true, "").nextTo("ABC"));
        try {
            new JSONTokener(true, "").nextValue();
            fail();
        } catch (JSONException ignored) {
        }
        new JSONTokener(true, "").skipPast("ABC");
        assertEquals('\0', new JSONTokener(true, "").skipTo('A'));
        //noinspection ThrowableResultOfMethodCallIgnored
        assertEquals("foo! at character 0 of ",
                new JSONTokener(true, "").syntaxError("foo!").getMessage());
        assertEquals(" at character 0 of ", new JSONTokener(true, "").toString());
    }

    public void testCharacterNavigation() throws JSONException {
        JSONTokener abcdeTokener = new JSONTokener(true, "ABCDE");
        assertEquals('A', abcdeTokener.next());
        assertEquals('B', abcdeTokener.next('B'));
        assertEquals("CD", abcdeTokener.next(2));
        try {
            abcdeTokener.next(2);
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals('E', abcdeTokener.nextClean());
        assertEquals('\0', abcdeTokener.next());
        assertFalse(abcdeTokener.more());
        abcdeTokener.back();
        assertTrue(abcdeTokener.more());
        assertEquals('E', abcdeTokener.next());
    }

    public void testBackNextAndMore() throws JSONException {
        JSONTokener abcTokener = new JSONTokener(true, "ABC");
        assertTrue(abcTokener.more());
        abcTokener.next();
        abcTokener.next();
        assertTrue(abcTokener.more());
        abcTokener.next();
        assertFalse(abcTokener.more());
        abcTokener.back();
        assertTrue(abcTokener.more());
        abcTokener.next();
        assertFalse(abcTokener.more());
        abcTokener.back();
        abcTokener.back();
        abcTokener.back();
        abcTokener.back(); // you can back up before the beginning of a String!
        assertEquals('A', abcTokener.next());
    }

    public void testNextMatching() throws JSONException {
        JSONTokener abcdTokener = new JSONTokener(true, "ABCD");
        assertEquals('A', abcdTokener.next('A'));
        try {
            abcdTokener.next('C'); // although it failed, this op consumes a character of input
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals('C', abcdTokener.next('C'));
        assertEquals('D', abcdTokener.next('D'));
        try {
            abcdTokener.next('E');
            fail();
        } catch (JSONException ignored) {
        }
    }

    public void testNextN() throws JSONException {
        JSONTokener abcdeTokener = new JSONTokener(true, "ABCDEF");
        assertEquals("", abcdeTokener.next(0));
        try {
            abcdeTokener.next(7);
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals("ABC", abcdeTokener.next(3));
        try {
            abcdeTokener.next(4);
            fail();
        } catch (JSONException ignored) {
        }
    }

    public void testNextNWithAllRemaining() throws JSONException {
        JSONTokener tokener = new JSONTokener(true, "ABCDEF");
        tokener.next(3);
        try {
            tokener.next(3);
        } catch (JSONException e) {
            AssertionFailedError error = new AssertionFailedError("off-by-one error?");
            error.initCause(e);
            throw error;
        }
    }

    public void testNext0() throws JSONException {
        JSONTokener tokener = new JSONTokener(true, "ABCDEF");
        tokener.next(5);
        tokener.next();
        try {
            tokener.next(0);
        } catch (JSONException e) {
            Error error = new AssertionFailedError("Returning an empty string should be valid");
            error.initCause(e);
            throw error;
        }
    }

    public void testNextCleanComments() throws JSONException {
        JSONTokener tokener = new JSONTokener(
                true, "  A  /*XX*/B/*XX//XX\n//XX\nXX*/C//X//X//X\nD/*X*///X\n");
        assertEquals('A', tokener.nextClean());
        assertEquals('B', tokener.nextClean());
        assertEquals('C', tokener.nextClean());
        assertEquals('D', tokener.nextClean());
        assertEquals('\0', tokener.nextClean());
    }

    public void testNextCleanNestedCStyleComments() throws JSONException {
        JSONTokener tokener = new JSONTokener(true, "A /* B /* C */ D */ E");
        assertEquals('A', tokener.nextClean());
        assertEquals('D', tokener.nextClean());
        assertEquals('*', tokener.nextClean());
        assertEquals('/', tokener.nextClean());
        assertEquals('E', tokener.nextClean());
    }

    /**
     * Some applications rely on parsing '#' to lead an end-of-line comment.
     * http://b/2571423
     */
    public void testNextCleanHashComments() throws JSONException {
        JSONTokener tokener = new JSONTokener(true, "A # B */ /* C */ \nD #");
        assertEquals('A', tokener.nextClean());
        assertEquals('D', tokener.nextClean());
        assertEquals('\0', tokener.nextClean());
    }

    public void testNextCleanCommentsTrailingSingleSlash() throws JSONException {
        JSONTokener tokener = new JSONTokener(true, " / S /");
        assertEquals('/', tokener.nextClean());
        assertEquals('S', tokener.nextClean());
        assertEquals('/', tokener.nextClean());
        assertEquals("nextClean doesn't consume a trailing slash",
                '\0', tokener.nextClean());
    }

    public void testNextCleanTrailingOpenComment() throws JSONException {
        try {
            new JSONTokener(true, "  /* ").nextClean();
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals('\0', new JSONTokener(true, "  // ").nextClean());
    }

    public void testNextCleanNewlineDelimiters() throws JSONException {
        assertEquals('B', new JSONTokener(true, "  // \r\n  B ").nextClean());
        assertEquals('B', new JSONTokener(true, "  // \n  B ").nextClean());
        assertEquals('B', new JSONTokener(true, "  // \r  B ").nextClean());
    }

    public void testNextCleanSkippedWhitespace() throws JSONException {
        assertEquals("character tabulation", 'A', new JSONTokener(true, "\tA").nextClean());
        assertEquals("line feed",            'A', new JSONTokener(true, "\nA").nextClean());
        assertEquals("carriage return",      'A', new JSONTokener(true, "\rA").nextClean());
        assertEquals("space",                'A', new JSONTokener(true, " A").nextClean());
    }

    /**
     * Tests which characters tokener treats as ignorable whitespace. See Kevin Bourrillion's
     * <a href="https://spreadsheets.google.com/pub?key=pd8dAQyHbdewRsnE5x5GzKQ">list
     * of whitespace characters</a>.
     */
    public void testNextCleanRetainedWhitespace() throws JSONException {
        assertNotClean("null",                      '\u0000');
        assertNotClean("next line",                 '\u0085');
        assertNotClean("non-breaking space",        '\u00a0');
        assertNotClean("ogham space mark",          '\u1680');
        assertNotClean("mongolian vowel separator", '\u180e');
        assertNotClean("en quad",                   '\u2000');
        assertNotClean("em quad",                   '\u2001');
        assertNotClean("en space",                  '\u2002');
        assertNotClean("em space",                  '\u2003');
        assertNotClean("three-per-em space",        '\u2004');
        assertNotClean("four-per-em space",         '\u2005');
        assertNotClean("six-per-em space",          '\u2006');
        assertNotClean("figure space",              '\u2007');
        assertNotClean("punctuation space",         '\u2008');
        assertNotClean("thin space",                '\u2009');
        assertNotClean("hair space",                '\u200a');
        assertNotClean("zero-width space",          '\u200b');
        assertNotClean("left-to-right mark",        '\u200e');
        assertNotClean("right-to-left mark",        '\u200f');
        assertNotClean("line separator",            '\u2028');
        assertNotClean("paragraph separator",       '\u2029');
        assertNotClean("narrow non-breaking space", '\u202f');
        assertNotClean("medium mathematical space", '\u205f');
        assertNotClean("ideographic space",         '\u3000');
        assertNotClean("line tabulation",           '\u000b');
        assertNotClean("form feed",                 '\u000c');
        assertNotClean("information separator 4",   '\u001c');
        assertNotClean("information separator 3",   '\u001d');
        assertNotClean("information separator 2",   '\u001e');
        assertNotClean("information separator 1",   '\u001f');
    }

    private void assertNotClean(String name, char c) throws JSONException {
        assertEquals("The character " + name + " is not whitespace according to the JSON spec.",
                c, new JSONTokener(true, new String(new char[] { c, 'A' })).nextClean());
    }

    public void testNextString() throws JSONException {
        assertEquals("", new JSONTokener(true, "'").nextString('\''));
        assertEquals("", new JSONTokener(true, "\"").nextString('\"'));
        assertEquals("ABC", new JSONTokener(true, "ABC'DEF").nextString('\''));
        assertEquals("ABC", new JSONTokener(true, "ABC'''DEF").nextString('\''));

        // nextString permits slash-escaping of arbitrary characters!
        assertEquals("ABC", new JSONTokener(true, "A\\B\\C'DEF").nextString('\''));

        JSONTokener tokener = new JSONTokener(true, " 'abc' 'def' \"ghi\"");
        tokener.next();
        assertEquals('\'', tokener.next());
        assertEquals("abc", tokener.nextString('\''));
        tokener.next();
        assertEquals('\'', tokener.next());
        assertEquals("def", tokener.nextString('\''));
        tokener.next();
        assertEquals('"', tokener.next());
        assertEquals("ghi", tokener.nextString('\"'));
        assertFalse(tokener.more());
    }

    public void testNextStringNoDelimiter() throws JSONException {
        try {
            new JSONTokener(true, "").nextString('\'');
            fail();
        } catch (JSONException ignored) {
        }

        JSONTokener tokener = new JSONTokener(true, " 'abc");
        tokener.next();
        tokener.next();
        try {
            tokener.next('\'');
            fail();
        } catch (JSONException ignored) {
        }
    }

    public void testNextStringEscapedQuote() throws JSONException {
        try {
            new JSONTokener(true, "abc\\").nextString('"');
            fail();
        } catch (JSONException ignored) {
        }

        // we're mixing Java escaping like \" and JavaScript escaping like \\\"
        // which makes these tests extra tricky to read!
        assertEquals("abc\"def", new JSONTokener(true, "abc\\\"def\"ghi").nextString('"'));
        assertEquals("abc\\def", new JSONTokener(true, "abc\\\\def\"ghi").nextString('"'));
        assertEquals("abc/def", new JSONTokener(true, "abc\\/def\"ghi").nextString('"'));
        assertEquals("abc\bdef", new JSONTokener(true, "abc\\bdef\"ghi").nextString('"'));
        assertEquals("abc\fdef", new JSONTokener(true, "abc\\fdef\"ghi").nextString('"'));
        assertEquals("abc\ndef", new JSONTokener(true, "abc\\ndef\"ghi").nextString('"'));
        assertEquals("abc\rdef", new JSONTokener(true, "abc\\rdef\"ghi").nextString('"'));
        assertEquals("abc\tdef", new JSONTokener(true, "abc\\tdef\"ghi").nextString('"'));
    }

    public void testNextStringUnicodeEscaped() throws JSONException {
        // we're mixing Java escaping like \\ and JavaScript escaping like \\u
        assertEquals("abc def", new JSONTokener(true, "abc\\u0020def\"ghi").nextString('"'));
        assertEquals("abcU0020def", new JSONTokener(true, "abc\\U0020def\"ghi").nextString('"'));

        // JSON requires 4 hex characters after a unicode escape
        try {
            new JSONTokener(true, "abc\\u002\"").nextString('"');
            fail();
        } catch (JSONException ignored) {
        }
        try {
            new JSONTokener(true, "abc\\u").nextString('"');
            fail();
        } catch (JSONException ignored) {
        }
        try {
            new JSONTokener(true, "abc\\u    \"").nextString('"');
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals("abc\"def", new JSONTokener(true, "abc\\u0022def\"ghi").nextString('"'));
        try {
            new JSONTokener(true, "abc\\u000G\"").nextString('"');
            fail();
        } catch (JSONException ignored) {
        }
    }

    public void testNextStringNonQuote() throws JSONException {
        assertEquals("AB", new JSONTokener(true, "ABC").nextString('C'));
        assertEquals("ABCD", new JSONTokener(true, "AB\\CDC").nextString('C'));
        assertEquals("AB\nC", new JSONTokener(true, "AB\\nCn").nextString('n'));
    }

    public void testNextTo() throws JSONException {
        assertEquals("ABC", new JSONTokener(true, "ABCDEFG").nextTo("DHI"));
        assertEquals("ABCDEF", new JSONTokener(true, "ABCDEF").nextTo(""));

        JSONTokener tokener = new JSONTokener(true, "ABC\rDEF\nGHI\r\nJKL");
        assertEquals("ABC", tokener.nextTo("M"));
        assertEquals('\r', tokener.next());
        assertEquals("DEF", tokener.nextTo("M"));
        assertEquals('\n', tokener.next());
        assertEquals("GHI", tokener.nextTo("M"));
        assertEquals('\r', tokener.next());
        assertEquals('\n', tokener.next());
        assertEquals("JKL", tokener.nextTo("M"));

        tokener = new JSONTokener(true, "ABCDEFGHI");
        assertEquals("ABC", tokener.nextTo("DEF"));
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('D', tokener.next());
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('E', tokener.next());
        assertEquals("", tokener.nextTo("DEF"));
        assertEquals('F', tokener.next());
        assertEquals("GHI", tokener.nextTo("DEF"));
        assertEquals("", tokener.nextTo("DEF"));

        tokener = new JSONTokener(true, " \t \fABC \t DEF");
        assertEquals("ABC", tokener.nextTo("DEF"));
        assertEquals('D', tokener.next());

        tokener = new JSONTokener(true, " \t \fABC \n DEF");
        assertEquals("ABC", tokener.nextTo("\n"));
        assertEquals("", tokener.nextTo("\n"));

        tokener = new JSONTokener(true, "");
        try {
            tokener.nextTo(null);
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    public void testNextToTrimming() {
        assertEquals("ABC", new JSONTokener(true, "\t ABC \tDEF").nextTo("DE"));
        assertEquals("ABC", new JSONTokener(true, "\t ABC \tDEF").nextTo('D'));
    }

    public void testNextToTrailing() {
        assertEquals("ABC DEF", new JSONTokener(true, "\t ABC DEF \t").nextTo("G"));
        assertEquals("ABC DEF", new JSONTokener(true, "\t ABC DEF \t").nextTo('G'));
    }

    public void testNextToDoesntStopOnNull() {
        String message = "nextTo() shouldn't stop after \\0 characters";
        JSONTokener tokener = new JSONTokener(true, " \0\t \fABC \n DEF");
        assertEquals(message, "ABC", tokener.nextTo("D"));
        assertEquals(message, '\n', tokener.next());
        assertEquals(message, "", tokener.nextTo("D"));
    }

    public void testNextToConsumesNull() {
        String message = "nextTo shouldn't consume \\0.";
        JSONTokener tokener = new JSONTokener(true, "ABC\0DEF");
        assertEquals(message, "ABC", tokener.nextTo("\0"));
        assertEquals(message, '\0', tokener.next());
        assertEquals(message, "DEF", tokener.nextTo("\0"));
    }

    public void testSkipPast() {
        JSONTokener tokener = new JSONTokener(true, "ABCDEF");
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("EF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener(true, "ABCDEF");
        tokener.skipPast("ABCDEF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener(true, "ABCDEF");
        tokener.skipPast("G");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener(true, "ABC\0ABC");
        tokener.skipPast("ABC");
        assertEquals('\0', tokener.next());
        assertEquals('A', tokener.next());

        tokener = new JSONTokener(true, "\0ABC");
        tokener.skipPast("ABC");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener(true, "ABC\nDEF");
        tokener.skipPast("DEF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener(true, "ABC");
        tokener.skipPast("ABCDEF");
        assertEquals('\0', tokener.next());

        tokener = new JSONTokener(true, "ABCDABCDABCD");
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());
        tokener.skipPast("ABC");
        assertEquals('D', tokener.next());

        tokener = new JSONTokener(true, "");
        try {
            tokener.skipPast(null);
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    public void testSkipTo() {
        JSONTokener tokener = new JSONTokener(true, "ABCDEF");
        tokener.skipTo('A');
        assertEquals('A', tokener.next());
        tokener.skipTo('D');
        assertEquals('D', tokener.next());
        tokener.skipTo('G');
        assertEquals('E', tokener.next());
        tokener.skipTo('A');
        assertEquals('F', tokener.next());

        tokener = new JSONTokener(true, "ABC\nDEF");
        tokener.skipTo('F');
        assertEquals('F', tokener.next());

        tokener = new JSONTokener(true, "ABCfDEF");
        tokener.skipTo('F');
        assertEquals('F', tokener.next());

        tokener = new JSONTokener(true, "ABC/* DEF */");
        tokener.skipTo('D');
        assertEquals('D', tokener.next());
    }

    public void testSkipToStopsOnNull() {
        JSONTokener tokener = new JSONTokener(true, "ABC\0DEF");
        tokener.skipTo('F');
        assertEquals("skipTo shouldn't stop when it sees '\\0'", 'F', tokener.next());
    }

    public void testBomIgnoredAsFirstCharacterOfDocument() throws JSONException {
        JSONTokener tokener = new JSONTokener(true, "\ufeff[]");
        JSONArray array = (JSONArray) tokener.nextValue();
        assertEquals(0, array.length());
    }

    public void testBomTreatedAsCharacterInRestOfDocument() throws JSONException {
        JSONTokener tokener = new JSONTokener(true, "[\ufeff]");
        JSONArray array = (JSONArray) tokener.nextValue();
        assertEquals(1, array.length());
    }

    public void testDehexchar() {
        assertEquals( 0, JSONTokener.dehexchar('0'));
        assertEquals( 1, JSONTokener.dehexchar('1'));
        assertEquals( 2, JSONTokener.dehexchar('2'));
        assertEquals( 3, JSONTokener.dehexchar('3'));
        assertEquals( 4, JSONTokener.dehexchar('4'));
        assertEquals( 5, JSONTokener.dehexchar('5'));
        assertEquals( 6, JSONTokener.dehexchar('6'));
        assertEquals( 7, JSONTokener.dehexchar('7'));
        assertEquals( 8, JSONTokener.dehexchar('8'));
        assertEquals( 9, JSONTokener.dehexchar('9'));
        assertEquals(10, JSONTokener.dehexchar('A'));
        assertEquals(11, JSONTokener.dehexchar('B'));
        assertEquals(12, JSONTokener.dehexchar('C'));
        assertEquals(13, JSONTokener.dehexchar('D'));
        assertEquals(14, JSONTokener.dehexchar('E'));
        assertEquals(15, JSONTokener.dehexchar('F'));
        assertEquals(10, JSONTokener.dehexchar('a'));
        assertEquals(11, JSONTokener.dehexchar('b'));
        assertEquals(12, JSONTokener.dehexchar('c'));
        assertEquals(13, JSONTokener.dehexchar('d'));
        assertEquals(14, JSONTokener.dehexchar('e'));
        assertEquals(15, JSONTokener.dehexchar('f'));

        for (int c = 0; c <= 0xFFFF; c++) {
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')) {
                continue;
            }
            assertEquals("dehexchar " + c, -1, JSONTokener.dehexchar((char) c));
        }
    }
}
