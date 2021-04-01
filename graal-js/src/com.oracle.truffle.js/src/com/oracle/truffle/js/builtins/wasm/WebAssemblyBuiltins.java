/*
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.js.builtins.wasm;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.js.runtime.builtins.wasm.JSWebAssembly;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.builtins.JSBuiltinsContainer;
import com.oracle.truffle.js.builtins.wasm.WebAssemblyBuiltinsFactory.WebAssemblyCompileNodeGen;
import com.oracle.truffle.js.builtins.wasm.WebAssemblyBuiltinsFactory.WebAssemblyInstantiateNodeGen;
import com.oracle.truffle.js.builtins.wasm.WebAssemblyBuiltinsFactory.WebAssemblyValidateNodeGen;
import com.oracle.truffle.js.nodes.access.IsObjectNode;
import com.oracle.truffle.js.nodes.control.TryCatchNode;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.nodes.function.JSBuiltinNode;
import com.oracle.truffle.js.nodes.function.JSFunctionCallNode;
import com.oracle.truffle.js.nodes.promise.NewPromiseCapabilityNode;
import com.oracle.truffle.js.nodes.promise.PerformPromiseThenNode;
import com.oracle.truffle.js.nodes.wasm.ExportByteSourceNode;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.GraalJSException;
import com.oracle.truffle.js.runtime.JSArguments;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.JavaScriptRootNode;
import com.oracle.truffle.js.runtime.builtins.BuiltinEnum;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.builtins.JSFunctionData;
import com.oracle.truffle.js.runtime.builtins.JSOrdinary;
import com.oracle.truffle.js.runtime.builtins.JSPromise;
import com.oracle.truffle.js.runtime.builtins.wasm.JSWebAssemblyInstance;
import com.oracle.truffle.js.runtime.builtins.wasm.JSWebAssemblyModule;
import com.oracle.truffle.js.runtime.builtins.wasm.JSWebAssemblyModuleObject;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.PromiseCapabilityRecord;
import com.oracle.truffle.js.runtime.objects.Undefined;

/**
 * WebAssembly built-ins.
 */
public class WebAssemblyBuiltins extends JSBuiltinsContainer.SwitchEnum<WebAssemblyBuiltins.WebAssembly> {

    public static final JSBuiltinsContainer BUILTINS = new WebAssemblyBuiltins();

    protected WebAssemblyBuiltins() {
        super(JSWebAssembly.CLASS_NAME, WebAssembly.class);
    }

    public enum WebAssembly implements BuiltinEnum<WebAssembly> {
        compile(1),
        instantiate(1),
        validate(1);

        private final int length;

        WebAssembly(int length) {
            this.length = length;
        }

        @Override
        public int getLength() {
            return length;
        }

        @Override
        public boolean isEnumerable() {
            return true;
        }

    }

    @Override
    protected Object createNode(JSContext context, JSBuiltin builtin, boolean construct, boolean newTarget, WebAssembly builtinEnum) {
        switch (builtinEnum) {
            case compile:
                return WebAssemblyCompileNodeGen.create(context, builtin, args().fixedArgs(1).createArgumentNodes(context));
            case instantiate:
                return WebAssemblyInstantiateNodeGen.create(context, builtin, args().fixedArgs(2).createArgumentNodes(context));
            case validate:
                return WebAssemblyValidateNodeGen.create(context, builtin, args().fixedArgs(1).createArgumentNodes(context));
        }
        return null;
    }

    // Helper node for built-ins that produce promise whose resolution can be obtained
    // synchronously.
    protected abstract static class PromisifiedBuiltinNode extends JSBuiltinNode {

        @Child NewPromiseCapabilityNode newPromiseCapability;
        @Child JSFunctionCallNode promiseResolutionCallNode;
        @Child TryCatchNode.GetErrorObjectNode getErrorObjectNode;

        public PromisifiedBuiltinNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.newPromiseCapability = NewPromiseCapabilityNode.create(context);
            this.promiseResolutionCallNode = JSFunctionCallNode.createCall();
        }

        protected abstract Object process(Object argument);

        protected DynamicObject promisify(Object argument) {
            JSRealm realm = getContext().getRealm();
            PromiseCapabilityRecord promiseCapability = newPromiseCapability.execute(realm.getPromiseConstructor());
            try {
                Object resolution = process(argument);
                promiseResolutionCallNode.executeCall(JSArguments.createOneArg(Undefined.instance, promiseCapability.getResolve(), resolution));
            } catch (Throwable ex) {
                InteropLibrary interop = InteropLibrary.getUncached(ex);
                if (interop.isException(ex)) {
                    try {
                        ExceptionType type = interop.getExceptionType(ex);
                        if (type != ExceptionType.EXIT && type != ExceptionType.INTERRUPT) {
                            Throwable exception = ex;
                            if (type == ExceptionType.PARSE_ERROR) {
                                exception = Errors.createCompileError(ex, this);
                            }
                            if (getErrorObjectNode == null) {
                                CompilerDirectives.transferToInterpreterAndInvalidate();
                                getErrorObjectNode = insert(TryCatchNode.GetErrorObjectNode.create(getContext()));
                            }
                            Object error = getErrorObjectNode.execute(exception);
                            promiseResolutionCallNode.executeCall(JSArguments.createOneArg(Undefined.instance, promiseCapability.getReject(), error));
                        } else {
                            throw ex;
                        }
                    } catch (UnsupportedMessageException umex) {
                        throw Errors.shouldNotReachHere(umex);
                    }
                } else {
                    throw ex;
                }
            }
            return promiseCapability.getPromise();
        }

    }

    public abstract static class WebAssemblyCompileNode extends PromisifiedBuiltinNode {

        @Child ExportByteSourceNode exportByteSourceNode;

        public WebAssemblyCompileNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.exportByteSourceNode = ExportByteSourceNode.create(context, "WebAssembly.compile(): Argument 0 must be a buffer source", "WebAssembly.compile(): BufferSource argument is empty");
        }

        @Specialization
        protected Object compile(Object byteSource) {
            return promisify(byteSource);
        }

        @Override
        protected Object process(Object argument) {
            try {
                Object byteSource = exportByteSourceNode.execute(argument);

                Object compile = getContext().getRealm().getWASMCompileFunction();
                Object wasmModule = InteropLibrary.getUncached(compile).execute(compile, byteSource);

                return JSWebAssemblyModule.create(getContext(), wasmModule);
            } catch (InteropException ex) {
                throw Errors.shouldNotReachHere(ex);
            }
        }

    }

    public abstract static class WebAssemblyInstantiateNode extends PromisifiedBuiltinNode {

        @Child ExportByteSourceNode exportByteSourceNode;
        @Child IsObjectNode isObjectNode;
        @Child PerformPromiseThenNode performPromiseThenNode;

        public WebAssemblyInstantiateNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.exportByteSourceNode = ExportByteSourceNode.create(context, "WebAssembly.instantiate(): Argument 0 must be a buffer source or a WebAssembly.Module object",
                            "WebAssembly.instantiate(): BufferSource argument is empty");
            this.isObjectNode = IsObjectNode.create();
            this.performPromiseThenNode = PerformPromiseThenNode.create(context);
        }

        @Specialization
        protected Object instantiate(Object byteSourceOrModule, Object importObject) {
            DynamicObject promise = promisify(new Object[]{byteSourceOrModule, importObject});

            if (byteSourceOrModule instanceof JSWebAssemblyModuleObject) {
                return promise;
            }

            assert !JSPromise.isPending(promise);
            if (JSPromise.isRejected(promise)) {
                return promise;
            }

            JSContext context = getContext();
            JSRealm realm = context.getRealm();
            PromiseCapabilityRecord promiseCapability = newPromiseCapability.execute(realm.getPromiseConstructor());
            JSFunctionData functionData = context.getOrCreateBuiltinFunctionData(JSContext.BuiltinFunctionKey.WebAssemblySourceInstantiation, (c) -> {
                CallTarget callTarget = Truffle.getRuntime().createCallTarget(new JavaScriptRootNode(c.getLanguage(), null, null) {
                    @Override
                    public Object execute(VirtualFrame frame) {
                        InstantiatedSourceInfo info = (InstantiatedSourceInfo) JSArguments.getUserArgument(frame.getArguments(), 0);
                        Object jsInstance = instantiateModule(context, info.getWasmModule(), info.getImportObject());
                        return toJSInstantiatedSource(context, info.getWasmModule(), jsInstance);
                    }
                });
                return JSFunctionData.createCallOnly(c, callTarget, 1, "");
            });

            performPromiseThenNode.execute(promise, JSFunction.create(realm, functionData), Undefined.instance, promiseCapability);
            return promiseCapability.getPromise();
        }

        @Override
        protected Object process(Object argument) {
            Object[] args = (Object[]) argument;
            Object byteSourceOrModule = args[0];
            Object importObject = args[1];

            if (importObject != Undefined.instance && !isObjectNode.executeBoolean(importObject)) {
                throw Errors.createTypeError("WebAssembly.instantiate(): Argument 1 must be an object", this);
            }

            if (byteSourceOrModule instanceof JSWebAssemblyModuleObject) {
                Object wasmModule = ((JSWebAssemblyModuleObject) byteSourceOrModule).getWASMModule();
                return instantiateModule(getContext(), wasmModule, importObject);
            }

            Object wasmByteSource = exportByteSourceNode.execute(byteSourceOrModule);

            try {
                Object compile = getContext().getRealm().getWASMCompileFunction();
                try {
                    Object wasmModule = InteropLibrary.getUncached(compile).execute(compile, wasmByteSource);
                    return new InstantiatedSourceInfo(wasmModule, importObject);
                } catch (AbstractTruffleException ex) {
                    ExceptionType type = InteropLibrary.getUncached(ex).getExceptionType(ex);
                    if (type == ExceptionType.PARSE_ERROR) {
                        throw Errors.createCompileError(ex, this);
                    } else {
                        throw ex;
                    }
                }
            } catch (InteropException ex) {
                throw Errors.shouldNotReachHere(ex);
            }
        }

        static Object instantiateModule(JSContext context, Object wasmModule, Object importObject) {
            Object wasmImportObject = JSWebAssemblyInstance.transformImportObject(context, wasmModule, importObject);
            Object instantiate = context.getRealm().getWASMInstantiateFunction();
            Object wasmInstance;
            try {
                wasmInstance = InteropLibrary.getUncached(instantiate).execute(instantiate, wasmModule, wasmImportObject);
            } catch (GraalJSException jsex) {
                throw jsex;
            } catch (AbstractTruffleException ex) {
                throw Errors.createLinkError(ex, null);
            } catch (InteropException ex) {
                throw Errors.shouldNotReachHere(ex);
            }

            return JSWebAssemblyInstance.create(context, wasmInstance, wasmModule);
        }

        static Object toJSInstantiatedSource(JSContext context, Object wasmModule, Object jsInstance) {
            DynamicObject instantiatedSource = JSOrdinary.create(context);
            JSObject.set(instantiatedSource, "module", JSWebAssemblyModule.create(context, wasmModule));
            JSObject.set(instantiatedSource, "instance", jsInstance);
            return instantiatedSource;
        }

    }

    public abstract static class WebAssemblyValidateNode extends JSBuiltinNode {

        @Child ExportByteSourceNode exportByteSourceNode;

        public WebAssemblyValidateNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            this.exportByteSourceNode = ExportByteSourceNode.create(context, "WebAssembly.validate(): Argument 0 must be a buffer source", null);
        }

        private Object validateImpl(Object byteSource) {
            try {
                Object validate = getContext().getRealm().getWASMValidateFunction();
                return InteropLibrary.getUncached(validate).execute(validate, byteSource);
            } catch (InteropException ex) {
                throw Errors.shouldNotReachHere(ex);
            }
        }

        @Specialization
        protected Object validate(Object byteSource) {
            return validateImpl(exportByteSourceNode.execute(byteSource));
        }

    }

    // Helper TruffleObject used to pass information through promise chain
    // during instantiation of a source.
    static final class InstantiatedSourceInfo implements TruffleObject {
        private final Object wasmModule;
        private final Object importObject;

        InstantiatedSourceInfo(Object wasmModule, Object importObject) {
            this.wasmModule = wasmModule;
            this.importObject = importObject;
        }

        Object getWasmModule() {
            return wasmModule;
        }

        Object getImportObject() {
            return importObject;
        }
    }

}
