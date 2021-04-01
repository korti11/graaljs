/*
 * Copyright (c) 2018, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.trufflenode.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.access.JSConstantNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode.ObjectLiteralMemberNode;
import com.oracle.truffle.js.nodes.access.PropertySetNode;
import com.oracle.truffle.js.nodes.function.JSFunctionExpressionNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.Symbol;
import com.oracle.truffle.js.runtime.builtins.JSFunctionData;
import com.oracle.truffle.js.runtime.objects.JSAttributes;
import com.oracle.truffle.js.runtime.objects.JSOrdinaryObject;
import com.oracle.truffle.js.runtime.objects.Undefined;
import com.oracle.truffle.js.runtime.util.Pair;
import com.oracle.truffle.trufflenode.GraalJSAccess;
import com.oracle.truffle.trufflenode.info.Accessor;
import com.oracle.truffle.trufflenode.info.FunctionTemplate;
import com.oracle.truffle.trufflenode.info.ObjectTemplate;
import com.oracle.truffle.trufflenode.info.Value;

public class ObjectTemplateNode extends JavaScriptBaseNode {
    @Children ObjectLiteralNode.ObjectLiteralMemberNode[] members;
    private final JSContext context;

    ObjectTemplateNode(ObjectLiteralNode.ObjectLiteralMemberNode[] members, JSContext context) {
        this.members = members;
        this.context = context;
    }

    @ExplodeLoop
    public DynamicObject executeWithObject(VirtualFrame frame, DynamicObject object) {
        for (int i = 0; i < members.length; i++) {
            members[i].executeVoid(frame, object, context);
        }
        return object;
    }

    /**
     * @see GraalJSAccess#objectTemplateInstantiate
     */
    public static ObjectTemplateNode fromObjectTemplate(ObjectTemplate template, JSContext context, GraalJSAccess graalJSAccess) {
        List<ObjectLiteralNode.ObjectLiteralMemberNode> members = new ArrayList<>();

        for (Accessor accessor : template.getAccessors()) {
            Pair<JSFunctionData, JSFunctionData> pair = accessor.getFunctions(context);
            JavaScriptNode getterNode = null;
            JavaScriptNode setterNode = null;
            if (pair.getFirst() != null) {
                getterNode = ObjectLiteralNode.MakeMethodNode.createWithKey(context, JSFunctionExpressionNode.create(pair.getFirst(), null), GraalJSAccess.HOLDER_KEY);
            }
            if (pair.getSecond() != null) {
                setterNode = ObjectLiteralNode.MakeMethodNode.createWithKey(context, JSFunctionExpressionNode.create(pair.getSecond(), null), GraalJSAccess.HOLDER_KEY);
            }
            members.add(ObjectLiteralNode.newAccessorMember(accessor.getName(), false, accessor.getAttributes(), getterNode, setterNode));
        }

        for (Value value : template.getValues()) {
            JavaScriptNode valueNode;
            Object propertyValue = value.getValue();
            if (propertyValue instanceof FunctionTemplate) {
                // process all found FunctionTemplates, recursively
                FunctionTemplate functionTempl = (FunctionTemplate) propertyValue;
                valueNode = JSConstantNode.create(graalJSAccess.functionTemplateGetFunction(context.getRealm(), functionTempl));
            } else {
                valueNode = JSConstantNode.create(propertyValue);
            }
            Object name = value.getName();
            int attributes = value.getAttributes();
            if (propertyValue instanceof Pair) {
                Pair<?, ?> pair = (Pair<?, ?>) propertyValue;
                JSRealm realm = context.getRealm();
                Object getterTemplate = pair.getFirst();
                Object setterTemplate = pair.getSecond();
                Object getter = (getterTemplate == null) ? Undefined.instance : graalJSAccess.functionTemplateGetFunction(realm, getterTemplate);
                Object setter = (setterTemplate == null) ? Undefined.instance : graalJSAccess.functionTemplateGetFunction(realm, setterTemplate);
                JavaScriptNode getterNode = JSConstantNode.create(getter);
                JavaScriptNode setterNode = JSConstantNode.create(setter);
                members.add(ObjectLiteralNode.newAccessorMember(name, false, attributes, getterNode, setterNode));
            } else if (name instanceof String || name instanceof Symbol) {
                members.add(ObjectLiteralNode.newDataMember(name, false, attributes, valueNode));
            } else if (name instanceof HiddenKey) {
                if (!template.hasPropertyHandler()) {
                    members.add(new InternalFieldNode(false, attributes, (HiddenKey) name, propertyValue, context));
                } // else set on the proxy/handler
            } else {
                members.add(ObjectLiteralNode.newComputedDataMember(JSConstantNode.create(name), false, attributes, valueNode));
            }
        }

        if (template.getInternalFieldCount() > 0) {
            members.add(new SetInternalFieldCountNode(template.getInternalFieldCount()));
        }

        return new ObjectTemplateNode(members.toArray(ObjectLiteralNode.ObjectLiteralMemberNode.EMPTY), context);
    }

    private static final class InternalFieldNode extends ObjectLiteralNode.ObjectLiteralMemberNode {
        @Child PropertySetNode setNode;
        private final Object value;

        private InternalFieldNode(boolean isStatic, int attributes, HiddenKey key, Object value, JSContext context) {
            super(isStatic, attributes);
            this.setNode = PropertySetNode.createSetHidden(key, context);
            this.value = value;
        }

        @Override
        public void executeVoid(VirtualFrame frame, DynamicObject receiver, DynamicObject homeObject, JSContext context) {
            setNode.setValue(receiver, value);
        }

        @Override
        protected ObjectLiteralMemberNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
            return new InternalFieldNode(isStatic, attributes, (HiddenKey) setNode.getKey(), value, setNode.getContext());
        }
    }

    private static final class SetInternalFieldCountNode extends ObjectLiteralNode.ObjectLiteralMemberNode {
        private final int value;
        @Child PropertySetNode setNode;

        private SetInternalFieldCountNode(int value) {
            super(false, JSAttributes.NOT_ENUMERABLE);
            this.value = value;
        }

        @Override
        public void executeVoid(VirtualFrame frame, DynamicObject receiver, DynamicObject homeObject, JSContext context) {
            if (receiver instanceof JSOrdinaryObject.InternalFieldLayout) {
                ((JSOrdinaryObject.InternalFieldLayout) receiver).setInternalFieldCount(value);
            } else {
                if (setNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    setNode = insert(PropertySetNode.createSetHidden(GraalJSAccess.INTERNAL_FIELD_COUNT_KEY, context));
                }
                setNode.setValueInt(receiver, value);
            }
        }

        @Override
        protected ObjectLiteralMemberNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
            return new SetInternalFieldCountNode(value);
        }
    }
}
