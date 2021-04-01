/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.trufflenode;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.object.DynamicObject;

/**
 * Realm-specific embedder data.
 */
public final class RealmData {
    private Object securityToken;
    private final Map<Integer, Object> embedderData = new HashMap<>();

    private DynamicObject nativeUtf8Write;
    private DynamicObject nativeUtf8Slice;
    private DynamicObject resolverFactory;
    private DynamicObject extrasBindingObject;

    public RealmData() {
    }

    public void setSecurityToken(Object securityToken) {
        this.securityToken = securityToken;
    }

    public Object getSecurityToken() {
        return securityToken;
    }

    public DynamicObject getNativeUtf8Write() {
        return nativeUtf8Write;
    }

    public void setNativeUtf8Write(DynamicObject nativeUtf8Write) {
        this.nativeUtf8Write = nativeUtf8Write;
    }

    public DynamicObject getNativeUtf8Slice() {
        return nativeUtf8Slice;
    }

    public void setNativeUtf8Slice(DynamicObject nativeUtf8Slice) {
        this.nativeUtf8Slice = nativeUtf8Slice;
    }

    public void setEmbedderData(int index, Object value) {
        embedderData.put(index, value);
    }

    public Object getEmbedderData(int index) {
        return embedderData.get(index);
    }

    public void setResolverFactory(DynamicObject resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    public DynamicObject getResolverFactory() {
        return resolverFactory;
    }

    public void setExtrasBindingObject(DynamicObject extrasBindingObject) {
        this.extrasBindingObject = extrasBindingObject;
    }

    public DynamicObject getExtrasBindingObject() {
        return extrasBindingObject;
    }

}
