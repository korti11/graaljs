/*
 * Copyright (c) 2019, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.js.test.interop;

import static com.oracle.truffle.js.lang.JavaScriptLanguage.ID;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.junit.Test;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSRealm;

public class JSONStringifyInteropTest {
    @Test
    public void testNonReadableMembers() {
        try (Context context = Context.newBuilder(ID).allowHostAccess(HostAccess.ALL).build()) {
            context.getBindings(ID).putMember("myObj", new InvocableMemberObject(Collections.singletonMap("someKey", "someValue")));
            Value result = context.eval(ID, "JSON.stringify(myObj)");
            assertEquals("{}", result.asString());
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public static class InvocableMemberObject implements TruffleObject {
        private final Map<String, Object> invocables;

        public InvocableMemberObject(Map<String, Object> invocables) {
            this.invocables = invocables;
        }

        @ExportMessage
        public boolean hasMembers() {
            return true;
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        @TruffleBoundary
        final Object readMember(@SuppressWarnings("unused") String key) throws UnsupportedMessageException {
            throw UnsupportedMessageException.create();
        }

        @ExportMessage
        @TruffleBoundary
        final Object invokeMember(String key, @SuppressWarnings("unused") Object[] args) throws UnknownIdentifierException {
            if (invocables.containsKey(key)) {
                return invocables.get(key);
            } else {
                throw UnknownIdentifierException.create(key);
            }
        }

        @ExportMessage
        @TruffleBoundary
        final boolean isMemberInvocable(String key) {
            if (invocables.containsKey(key)) {
                return true;
            } else {
                return false;
            }
        }

        @TruffleBoundary
        final boolean isMemberExisting(String key) {
            return invocables.containsKey(key);
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        @TruffleBoundary
        final boolean isMemberReadable(@SuppressWarnings("unused") String key) {
            return false;
        }

        @ExportMessage
        @TruffleBoundary
        final Object getMembers(@SuppressWarnings("unused") boolean includeInternal,
                        @CachedContext(JavaScriptLanguage.class) JSRealm realm) {
            Set<String> keys = invocables.keySet();
            TruffleLanguage.Env env = realm.getEnv();
            return env.asGuestValue(keys.toArray(new Object[keys.size()]));
        }
    }
}