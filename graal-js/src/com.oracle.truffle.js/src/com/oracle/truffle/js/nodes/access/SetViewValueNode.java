/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.js.nodes.access;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Executed;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.cast.JSToBigIntNode;
import com.oracle.truffle.js.nodes.cast.JSToBooleanNode;
import com.oracle.truffle.js.nodes.cast.JSToIndexNode;
import com.oracle.truffle.js.nodes.cast.JSToNumberNode;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRuntime;
import com.oracle.truffle.js.runtime.array.TypedArray;
import com.oracle.truffle.js.runtime.array.TypedArrayFactory;
import com.oracle.truffle.js.runtime.builtins.JSArrayBuffer;
import com.oracle.truffle.js.runtime.builtins.JSDataView;
import com.oracle.truffle.js.runtime.objects.Undefined;

import java.util.Set;

public abstract class SetViewValueNode extends JavaScriptNode {
    private final TypedArrayFactory factory;
    private final JSContext context;
    @Child @Executed protected JavaScriptNode viewNode;
    @Child @Executed protected JavaScriptNode requestIndexNode;
    @Child @Executed protected JavaScriptNode isLittleEndianNode;
    @Child @Executed protected JavaScriptNode valueNode;
    @Child private JSToBooleanNode toBooleanNode;
    @Child private JSToNumberNode toNumberNode;
    @Child private JSToBigIntNode toBigIntNode;

    protected SetViewValueNode(JSContext context, String type, JavaScriptNode view, JavaScriptNode requestIndex, JavaScriptNode isLittleEndian, JavaScriptNode value) {
        this(context, GetViewValueNode.typedArrayFactoryFromType(type, context), view, requestIndex, isLittleEndian, value);
    }

    protected SetViewValueNode(JSContext context, TypedArrayFactory factory, JavaScriptNode view, JavaScriptNode requestIndex, JavaScriptNode isLittleEndian, JavaScriptNode value) {
        this.factory = factory;
        this.context = context;
        this.viewNode = view;
        this.requestIndexNode = requestIndex;
        this.isLittleEndianNode = isLittleEndian;
        this.valueNode = value;
        this.toBooleanNode = factory.getBytesPerElement() == 1 ? null : JSToBooleanNode.create();
        if (JSRuntime.isTypedArrayBigIntFactory(factory)) {
            this.toBigIntNode = JSToBigIntNode.create();
        } else {
            this.toNumberNode = JSToNumberNode.create();
        }
    }

    public abstract Object execute(DynamicObject dataView, Object byteOffset, Object littleEndian, Object value);

    @Specialization
    protected final Object doSet(Object view, Object requestIndex, Object littleEndian, Object value,
                    @Cached("create()") JSToIndexNode toIndexNode,
                    @Cached("create()") BranchProfile errorBranch,
                    @Cached("createClassProfile()") ValueProfile typeProfile) {
        if (!JSDataView.isJSDataView(view)) {
            errorBranch.enter();
            throw Errors.createTypeErrorNotADataView();
        }
        DynamicObject dataView = (DynamicObject) view;
        DynamicObject buffer = JSDataView.getArrayBuffer(dataView);

        long getIndex = toIndexNode.executeLong(requestIndex);
        Object numberValue = JSRuntime.isTypedArrayBigIntFactory(factory) ? toBigIntNode.executeBigInteger(value) : toNumberNode.executeNumber(value);
        boolean isLittleEndian = factory.getBytesPerElement() == 1 ? true : toBooleanNode.executeBoolean(littleEndian);

        if (!context.getTypedArrayNotDetachedAssumption().isValid()) {
            if (JSArrayBuffer.isDetachedBuffer(buffer)) {
                errorBranch.enter();
                throw Errors.createTypeErrorDetachedBuffer();
            }
        }
        int viewLength = JSDataView.typedArrayGetLength(dataView);
        int elementSize = factory.getBytesPerElement();
        if (getIndex + elementSize > viewLength) {
            errorBranch.enter();
            throw Errors.createRangeError("index + elementSize > viewLength");
        }
        int viewOffset = JSDataView.typedArrayGetOffset(dataView);

        assert getIndex + viewOffset <= Integer.MAX_VALUE;
        int bufferIndex = (int) (getIndex + viewOffset);
        TypedArray strategy = typeProfile.profile(factory.createArrayType(JSArrayBuffer.isJSDirectOrSharedArrayBuffer(buffer), true));
        strategy.setBufferElement(buffer, bufferIndex, isLittleEndian, numberValue);
        return Undefined.instance;
    }

    public static SetViewValueNode create(JSContext context, String type, JavaScriptNode view, JavaScriptNode requestIndex, JavaScriptNode isLittleEndian, JavaScriptNode value) {
        return SetViewValueNodeGen.create(context, type, view, requestIndex, isLittleEndian, value);
    }

    @Override
    protected JavaScriptNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
        return SetViewValueNodeGen.create(context, factory, cloneUninitialized(viewNode, materializedTags), cloneUninitialized(requestIndexNode, materializedTags),
                        cloneUninitialized(isLittleEndianNode, materializedTags), cloneUninitialized(valueNode, materializedTags));
    }
}
