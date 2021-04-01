/*
 * Copyright (c) 2019, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.js.test.builtins;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.SourceSection;
import org.graalvm.polyglot.Value;
import org.junit.Test;

import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSContextOptions;
import com.oracle.truffle.js.test.JSTest;

public class EvalTest {

    @Test
    public void testNestedIndirectEvalCaller() {
        try (Context context = JSTest.newContextBuilder().option(JSContextOptions.V8_COMPATIBILITY_MODE_NAME, "true").build()) {
            Value result = context.eval(JavaScriptLanguage.ID, "" +
                            "var obj = {toString() {return (0, eval)(`'return 42;'`);}};\n" +
                            "var fn = new Function(obj);\n" +
                            "fn();");
            assertEquals(42, result.asInt());

            try {
                context.eval(Source.newBuilder(JavaScriptLanguage.ID, "" +
                                "var obj = {toString() {return (0, eval)('throw new Error();');}};\n" +
                                "var fn = new Function(obj);\n" +
                                "fn();",
                                "test.js").buildLiteral());
            } catch (PolyglotException e) {
                assertThat(e.getPolyglotStackTrace().iterator().next().getSourceLocation().getSource().getName(), containsString("test.js:1"));
            }

            try {
                context.eval(Source.newBuilder(JavaScriptLanguage.ID, "" +
                                "var obj = {toString() {return (0, eval)(`'throw new Error();'`);}};\n" +
                                "var fn = new Function(obj);\n" +
                                "fn();",
                                "test.js").buildLiteral());
            } catch (PolyglotException e) {
                assertThat(e.getPolyglotStackTrace().iterator().next().getSourceLocation().getSource().getName(), containsString("test.js:2"));
            }
        }
    }

    @Test
    public void testDirectEvalCallSourceLocation() {
        testEvalCallSourceLocation(true);
    }

    @Test
    public void testIndirectEvalCallSourceLocation() {
        testEvalCallSourceLocation(false);
    }

    private static void testEvalCallSourceLocation(boolean directEval) {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        try (Context context = JSTest.newContextBuilder().err(err).build()) {
            try {
                String eval = directEval ? "eval" : "(0, eval)";
                context.eval(Source.newBuilder(JavaScriptLanguage.ID, "" +
                                "try {\n" +
                                "    " + eval + "('causeerror')\n" +
                                "} catch(e) {\n" +
                                "    console.error(e.stack)\n" +
                                "    throw e\n" +
                                "}", "test.js").buildLiteral());
            } catch (PolyglotException e) {
                Iterator<PolyglotException.StackFrame> iterator = e.getPolyglotStackTrace().iterator();
                assertThat(iterator.next().getSourceLocation().getSource().getName(), containsString("eval"));
                SourceSection sourceLocation = iterator.next().getSourceLocation();
                assertNotNull(sourceLocation);
                assertEquals("test.js", sourceLocation.getSource().getName());
                assertEquals(2, sourceLocation.getStartLine());
                assertEquals(2, sourceLocation.getEndLine());
            }
            String errorLog = err.toString();
            assertThat(errorLog, containsString("test.js:2"));
        }
    }

    private static void assertTrueResult(String source) {
        try (Context context = JSTest.newContextBuilder().option(JSContextOptions.V8_COMPATIBILITY_MODE_NAME, "true").build()) {
            Value result = context.eval(JavaScriptLanguage.ID, source);
            assertTrue(result.isBoolean());
            assertTrue(result.asBoolean());
        }
    }

    @Test
    public void testCallerInEval() {
        assertTrueResult("eval('var a = function() { return a.caller; }; var b = function() { return a(); }; b() == b;')");
    }

    @Test
    public void testCallerInIndirectEval() {
        assertTrueResult("(0, eval)('var a = function() { return a.caller; }; var b = function() { return a(); }; b() == b;')");
    }

    @Test
    public void testCallerAcrossEval() {
        assertTrueResult("var a = function() { return a.caller; }; var b = function() { return eval('a()'); }; b() == b;");
    }

    @Test
    public void testCallerAcrossIndirectEval() {
        assertTrueResult("var a = function() { return a.caller; }; var b = function() { return (0, eval)('a()'); }; b() == b;");
    }

}
