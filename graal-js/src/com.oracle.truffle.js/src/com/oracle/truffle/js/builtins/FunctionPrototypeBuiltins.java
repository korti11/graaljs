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
package com.oracle.truffle.js.builtins;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.js.builtins.FunctionPrototypeBuiltinsFactory.HasInstanceNodeGen;
import com.oracle.truffle.js.builtins.FunctionPrototypeBuiltinsFactory.JSApplyNodeGen;
import com.oracle.truffle.js.builtins.FunctionPrototypeBuiltinsFactory.JSBindNodeGen;
import com.oracle.truffle.js.builtins.FunctionPrototypeBuiltinsFactory.JSCallNodeGen;
import com.oracle.truffle.js.builtins.FunctionPrototypeBuiltinsFactory.JSFunctionToStringNodeGen;
import com.oracle.truffle.js.nodes.access.GetPrototypeNode;
import com.oracle.truffle.js.nodes.access.HasPropertyCacheNode;
import com.oracle.truffle.js.nodes.access.PropertyGetNode;
import com.oracle.truffle.js.nodes.binary.InstanceofNode.OrdinaryHasInstanceNode;
import com.oracle.truffle.js.nodes.cast.JSToObjectArrayNode;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.nodes.function.JSBuiltinNode;
import com.oracle.truffle.js.nodes.function.JSFunctionCallNode;
import com.oracle.truffle.js.nodes.unary.IsCallableNode;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSArguments;
import com.oracle.truffle.js.runtime.JSConfig;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRuntime;
import com.oracle.truffle.js.runtime.SuppressFBWarnings;
import com.oracle.truffle.js.runtime.Symbol;
import com.oracle.truffle.js.runtime.builtins.BuiltinEnum;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.builtins.JSProxy;
import com.oracle.truffle.js.runtime.objects.JSObject;

/**
 * Contains builtins for {@linkplain JSFunction Function}.prototype.
 */
public final class FunctionPrototypeBuiltins extends JSBuiltinsContainer.SwitchEnum<FunctionPrototypeBuiltins.FunctionPrototype> {

    public static final JSBuiltinsContainer BUILTINS = new FunctionPrototypeBuiltins();
    public static final JSBuiltinsContainer BUILTINS_NASHORN_COMPAT = new FunctionPrototypeNashornCompatBuiltins();

    protected FunctionPrototypeBuiltins() {
        super(JSFunction.PROTOTYPE_NAME, FunctionPrototype.class);
    }

    public enum FunctionPrototype implements BuiltinEnum<FunctionPrototype> {
        bind(1),
        toString(0),
        apply(2),
        call(1),

        _hasInstance(1) {
            @Override
            public Object getKey() {
                return Symbol.SYMBOL_HAS_INSTANCE;
            }

            @Override
            public boolean isWritable() {
                return false;
            }

            @Override
            public boolean isConfigurable() {
                return false;
            }
        };

        private final int length;

        FunctionPrototype(int length) {
            this.length = length;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public int getECMAScriptVersion() {
            if (this == _hasInstance) {
                return 6;
            }
            return BuiltinEnum.super.getECMAScriptVersion();
        }
    }

    @Override
    protected Object createNode(JSContext context, JSBuiltin builtin, boolean construct, boolean newTarget, FunctionPrototype builtinEnum) {
        switch (builtinEnum) {
            case bind:
                return JSBindNodeGen.create(context, builtin, args().withThis().fixedArgs(1).varArgs().createArgumentNodes(context));
            case toString:
                return JSFunctionToStringNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            case apply:
                return JSApplyNodeGen.create(context, builtin, args().withThis().fixedArgs(2).createArgumentNodes(context));
            case call:
                return JSCallNodeGen.create(context, builtin, args().withThis().fixedArgs(1).varArgs().createArgumentNodes(context));
            case _hasInstance:
                return HasInstanceNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
        }
        return null;
    }

    public static final class FunctionPrototypeNashornCompatBuiltins extends JSBuiltinsContainer.SwitchEnum<FunctionPrototypeNashornCompatBuiltins.FunctionNashornCompat> {
        protected FunctionPrototypeNashornCompatBuiltins() {
            super(FunctionNashornCompat.class);
        }

        public enum FunctionNashornCompat implements BuiltinEnum<FunctionNashornCompat> {
            toSource(0);

            private final int length;

            FunctionNashornCompat(int length) {
                this.length = length;
            }

            @Override
            public int getLength() {
                return length;
            }
        }

        @Override
        protected Object createNode(JSContext context, JSBuiltin builtin, boolean construct, boolean newTarget, FunctionNashornCompat builtinEnum) {
            switch (builtinEnum) {
                case toSource:
                    return JSFunctionToStringNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            }
            return null;
        }
    }

    public abstract static class JSBindNode extends JSBuiltinNode {
        @Child private GetPrototypeNode getPrototypeNode;
        @Child private HasPropertyCacheNode hasFunctionLengthNode;
        @Child private PropertyGetNode getFunctionLengthNode;
        @Child private PropertyGetNode getFunctionNameNode;
        private final ConditionProfile mustSetLengthProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile setNameProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile hasFunctionLengthProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile isAsyncProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile setProtoProfile = ConditionProfile.createBinaryProfile();

        public JSBindNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.getPrototypeNode = GetPrototypeNode.create();
            this.hasFunctionLengthNode = HasPropertyCacheNode.create(JSFunction.LENGTH, context, true);
            this.getFunctionLengthNode = PropertyGetNode.create(JSFunction.LENGTH, false, context);
            this.getFunctionNameNode = PropertyGetNode.create(JSFunction.NAME, false, context);
        }

        @SuppressFBWarnings(value = "ES_COMPARING_STRINGS_WITH_EQ", justification = "fast path")
        @Specialization(guards = "isJSFunction(thisFnObj)")
        protected DynamicObject bindFunction(DynamicObject thisFnObj, Object thisArg, Object[] args) {
            DynamicObject proto = getPrototypeNode.executeJSObject(thisFnObj);

            DynamicObject boundFunction = JSFunction.boundFunctionCreate(getContext(), thisFnObj, thisArg, args, proto, isAsyncProfile, setProtoProfile);

            long length = 0;
            boolean mustSetLength = true;
            if (hasFunctionLengthProfile.profile(hasFunctionLengthNode.hasProperty(thisFnObj))) {
                long targetLen = getFunctionLength(thisFnObj);
                length = Math.max(0, targetLen - args.length);
                if (targetLen == JSFunction.getLength(thisFnObj)) {
                    mustSetLength = false;
                }
            }
            if (mustSetLengthProfile.profile(mustSetLength)) {
                JSFunction.setFunctionLength(boundFunction, JSRuntime.longToIntOrDouble(length));
            }

            Object targetName = getFunctionNameNode.getValue(thisFnObj);
            if (!(targetName instanceof String)) {
                if (JSRuntime.isLazyString(targetName)) {
                    targetName = JSRuntime.toStringIsString(targetName);
                } else {
                    targetName = "";
                }
            }
            if (setNameProfile.profile(targetName != JSFunction.getName(thisFnObj))) {
                JSFunction.setBoundFunctionName(boundFunction, (String) targetName);
            }

            return boundFunction;
        }

        private long getFunctionLength(DynamicObject thisFnObj) {
            Object len = getFunctionLengthNode.getValue(thisFnObj);
            if (len instanceof Integer) {
                return ((Integer) len).longValue();
            } else if (JSRuntime.isNumber(len)) {
                return JSRuntime.toInteger((Number) len);
            }
            return 0L;
        }

        @TruffleBoundary
        @Specialization(guards = {"isJSProxy(thisObj)"})
        protected DynamicObject bindProxy(DynamicObject thisObj, Object thisArg, Object[] args) {
            final DynamicObject proto = JSObject.getPrototype(thisObj);

            final Object target = JSProxy.getTarget(thisObj);
            Object innerFunction = target;
            for (;;) {
                if (JSFunction.isJSFunction(innerFunction)) {
                    break;
                } else if (JSProxy.isJSProxy(innerFunction)) {
                    innerFunction = JSProxy.getTarget((DynamicObject) innerFunction);
                } else {
                    throw Errors.createTypeErrorNotAFunction(thisObj);
                }
            }
            assert JSFunction.isJSFunction(innerFunction);

            DynamicObject boundFunction = JSFunction.boundFunctionCreate(getContext(), (DynamicObject) innerFunction, thisArg, args, proto, isAsyncProfile, setProtoProfile);

            long length = 0;
            boolean targetHasLength = JSObject.hasOwnProperty(thisObj, JSFunction.LENGTH);
            if (targetHasLength) {
                Object targetLen = JSObject.get(thisObj, JSFunction.LENGTH);
                if (JSRuntime.isNumber(targetLen)) {
                    long targetLenInt = JSRuntime.toInteger(targetLen);
                    length = Math.max(0, targetLenInt - args.length);
                }
            }
            JSFunction.setFunctionLength(boundFunction, JSRuntime.longToIntOrDouble(length));

            Object targetName = JSObject.get(thisObj, JSFunction.NAME);
            if (!JSRuntime.isString(targetName)) {
                targetName = "";
            }
            JSFunction.setBoundFunctionName(boundFunction, (String) targetName);

            return boundFunction;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"!isJSFunction(thisObj)", "!isJSProxy(thisObj)"})
        protected DynamicObject bindError(Object thisObj, Object thisArg, Object[] arg) {
            throw Errors.createTypeErrorNotAFunction(thisObj);
        }

    }

    public abstract static class JSFunctionToStringNode extends JSBuiltinNode {

        private static final String NATIVE_CODE_STR = "function () { [native code] }";

        public JSFunctionToStringNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        protected boolean isRootTarget(DynamicObject fnObj) {
            return JSFunction.getCallTarget(fnObj) instanceof RootCallTarget;
        }

        protected boolean isBoundTarget(DynamicObject fnObj) {
            return JSFunction.isBoundFunction(fnObj);
        }

        @TruffleBoundary
        @Specialization(guards = {"isJSFunction(fnObj)", "isRootTarget(fnObj)", "!isBoundTarget(fnObj)"})
        protected String toStringDefault(DynamicObject fnObj) {
            RootCallTarget dct = (RootCallTarget) JSFunction.getCallTarget(fnObj);
            return toStringDefaultTarget(dct, fnObj);
        }

        @Specialization(guards = {"isJSFunction(fnObj)", "isRootTarget(fnObj)", "isBoundTarget(fnObj)"})
        protected String toStringBound(DynamicObject fnObj) {
            if (getContext().isOptionV8CompatibilityMode()) {
                return NATIVE_CODE_STR;
            } else {
                String name = JSFunction.getName(fnObj);
                return getNameIntl(name);
            }
        }

        @TruffleBoundary
        private static String getNameIntl(String name) {
            return "function " + name.substring(name.lastIndexOf(' ') + 1) + "() { [native code] }";
        }

        @TruffleBoundary
        @Specialization(guards = {"isJSFunction(fnObj)", "!isRootTarget(fnObj)"})
        protected String toString(DynamicObject fnObj) {
            CallTarget ct = JSFunction.getCallTarget(fnObj);
            return ct.toString();
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"isES2019OrLater()", "!isJSFunction(fnObj)", "isCallable.executeBoolean(fnObj)"}, limit = "1")
        protected String toStringCallable(Object fnObj,
                        @Cached @Shared("isCallable") IsCallableNode isCallable) {
            return NATIVE_CODE_STR;
        }

        @SuppressWarnings("unused")
        @Specialization(guards = {"isES2019OrLater()", "!isCallable.executeBoolean(fnObj)"}, limit = "1")
        protected String toStringNotCallable(Object fnObj,
                        @Cached @Shared("isCallable") IsCallableNode isCallable) {
            throw Errors.createTypeErrorNotAFunction(fnObj);
        }

        @Specialization(guards = {"!isES2019OrLater()", "!isJSFunction(fnObj)"})
        protected String toStringNotFunction(Object fnObj) {
            throw Errors.createTypeErrorNotAFunction(fnObj);
        }

        final boolean isES2019OrLater() {
            return getContext().getEcmaScriptVersion() >= JSConfig.ECMAScript2019;
        }

        @TruffleBoundary
        private static String toStringDefaultTarget(RootCallTarget dct, DynamicObject fnObj) {
            RootNode rn = dct.getRootNode();
            SourceSection ssect = rn.getSourceSection();
            String result;
            if (ssect == null || !ssect.isAvailable() || ssect.getSource().isInternal()) {
                result = "function " + JSFunction.getName(fnObj) + "() { [native code] }";
            } else {
                result = ssect.getCharacters().toString();
            }
            return result;
        }
    }

    public abstract static class JSApplyNode extends JSBuiltinNode {

        @Child private JSFunctionCallNode call;
        @Child private JSToObjectArrayNode toObjectArray;

        public JSApplyNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.call = JSFunctionCallNode.createCall();
            this.toObjectArray = JSToObjectArrayNode.create(context, true);
        }

        @Specialization(guards = "isJSFunction(function)")
        protected Object applyFunction(DynamicObject function, Object target, Object args) {
            return apply(function, target, args);
        }

        @Specialization(guards = "isCallable.executeBoolean(function)", replaces = "applyFunction", limit = "1")
        protected Object applyCallable(Object function, Object target, Object args,
                        @Cached @Shared("isCallable") @SuppressWarnings("unused") IsCallableNode isCallable) {
            return apply(function, target, args);
        }

        private Object apply(Object function, Object target, Object args) {
            Object[] applyUserArgs = toObjectArray.executeObjectArray(args);
            assert applyUserArgs.length <= getContext().getContextOptions().getMaxApplyArgumentLength();
            Object[] passedOnArguments = JSArguments.create(target, function, applyUserArgs);
            return call.executeCall(passedOnArguments);
        }

        @SuppressWarnings("unused")
        @Specialization(guards = "!isCallable.executeBoolean(function)", limit = "1")
        protected Object error(Object function, Object target, Object args,
                        @Cached @Shared("isCallable") @SuppressWarnings("unused") IsCallableNode isCallable) {
            throw Errors.createTypeErrorNotAFunction(function);
        }
    }

    public abstract static class JSCallNode extends JSBuiltinNode {

        @Child private JSFunctionCallNode callNode;

        public JSCallNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.callNode = JSFunctionCallNode.createCall();
        }

        @Specialization
        protected Object call(Object function, Object target, Object[] args) {
            return callNode.executeCall(JSArguments.create(target, function, args));
        }

    }

    public abstract static class HasInstanceNode extends JSBuiltinNode {
        @Child OrdinaryHasInstanceNode ordinaryHasInstanceNode;

        public HasInstanceNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.ordinaryHasInstanceNode = OrdinaryHasInstanceNode.create(context);
        }

        @Specialization
        protected boolean hasInstance(Object thisObj, Object value) {
            return ordinaryHasInstanceNode.executeBoolean(value, thisObj);
        }
    }
}
