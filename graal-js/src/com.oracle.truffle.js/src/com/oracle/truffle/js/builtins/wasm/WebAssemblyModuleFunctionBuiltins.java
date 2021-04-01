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

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.builtins.wasm.JSWebAssemblyModule;
import com.oracle.truffle.js.builtins.JSBuiltinsContainer;
import com.oracle.truffle.js.builtins.wasm.WebAssemblyModuleFunctionBuiltinsFactory.WebAssemblyModuleCustomSectionsNodeGen;
import com.oracle.truffle.js.builtins.wasm.WebAssemblyModuleFunctionBuiltinsFactory.WebAssemblyModuleExportsNodeGen;
import com.oracle.truffle.js.builtins.wasm.WebAssemblyModuleFunctionBuiltinsFactory.WebAssemblyModuleImportsNodeGen;
import com.oracle.truffle.js.nodes.cast.JSToStringNode;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.nodes.function.JSBuiltinNode;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.BuiltinEnum;
import com.oracle.truffle.js.runtime.builtins.JSArray;
import com.oracle.truffle.js.runtime.builtins.JSArrayBuffer;
import com.oracle.truffle.js.runtime.builtins.JSOrdinary;
import com.oracle.truffle.js.runtime.builtins.wasm.JSWebAssemblyModuleObject;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.Undefined;

/**
 * WebAssembly.Module built-ins.
 */
public class WebAssemblyModuleFunctionBuiltins extends JSBuiltinsContainer.SwitchEnum<WebAssemblyModuleFunctionBuiltins.ModuleFunction> {

    public static final JSBuiltinsContainer BUILTINS = new WebAssemblyModuleFunctionBuiltins();

    protected WebAssemblyModuleFunctionBuiltins() {
        super(JSWebAssemblyModule.CLASS_NAME, ModuleFunction.class);
    }

    public enum ModuleFunction implements BuiltinEnum<ModuleFunction> {
        exports(1),
        imports(1),
        customSections(2);

        private final int length;

        ModuleFunction(int length) {
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
    protected Object createNode(JSContext context, JSBuiltin builtin, boolean construct, boolean newTarget, ModuleFunction builtinEnum) {
        switch (builtinEnum) {
            case exports:
                return WebAssemblyModuleExportsNodeGen.create(context, builtin, args().fixedArgs(1).createArgumentNodes(context));
            case imports:
                return WebAssemblyModuleImportsNodeGen.create(context, builtin, args().fixedArgs(1).createArgumentNodes(context));
            case customSections:
                return WebAssemblyModuleCustomSectionsNodeGen.create(context, builtin, args().fixedArgs(2).createArgumentNodes(context));
        }
        return null;
    }

    public abstract static class WebAssemblyModuleExportsNode extends JSBuiltinNode {

        public WebAssemblyModuleExportsNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object exportsOfModule(JSWebAssemblyModuleObject moduleObject) {
            try {
                Object exportsFunction = getContext().getRealm().getWASMModuleExportsFunction();
                Object wasmExports = InteropLibrary.getUncached(exportsFunction).execute(exportsFunction, moduleObject.getWASMModule());
                return toJSExports(wasmExports);
            } catch (InteropException ex) {
                throw Errors.shouldNotReachHere(ex);
            }
        }

        @Fallback
        protected Object exportsOfOther(@SuppressWarnings("unused") Object other) {
            throw Errors.createTypeError("WebAssembly.Module.exports(): Argument 0 must be a WebAssembly.Module", this);
        }

        private Object toJSExports(Object wasmExports) throws InteropException {
            InteropLibrary interop = InteropLibrary.getUncached(wasmExports);
            int size = (int) interop.getArraySize(wasmExports);
            Object[] exports = new Object[size];
            for (int i = 0; i < size; i++) {
                Object wasmExport = interop.readArrayElement(wasmExports, i);
                exports[i] = toJSExport(wasmExport);
            }
            return JSArray.createConstantObjectArray(getContext(), exports);
        }

        private Object toJSExport(Object wasmExport) throws InteropException {
            DynamicObject export = JSOrdinary.create(getContext());
            InteropLibrary interop = InteropLibrary.getUncached(wasmExport);
            for (String key : new String[]{"name", "kind"}) {
                Object value = interop.readMember(wasmExport, key);
                JSObject.set(export, key, value);
            }
            return export;
        }

    }

    public abstract static class WebAssemblyModuleImportsNode extends JSBuiltinNode {

        public WebAssemblyModuleImportsNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object importsOfModule(JSWebAssemblyModuleObject moduleObject) {
            try {
                Object importsFunction = getContext().getRealm().getWASMModuleImportsFunction();
                Object wasmImports = InteropLibrary.getUncached(importsFunction).execute(importsFunction, moduleObject.getWASMModule());
                return toJSImports(wasmImports);
            } catch (InteropException ex) {
                throw Errors.shouldNotReachHere(ex);
            }
        }

        @Fallback
        protected Object importsOfOther(@SuppressWarnings("unused") Object other) {
            throw Errors.createTypeError("WebAssembly.Module.imports(): Argument 0 must be a WebAssembly.Module", this);
        }

        private Object toJSImports(Object wasmImports) throws InteropException {
            InteropLibrary interop = InteropLibrary.getUncached(wasmImports);
            int size = (int) interop.getArraySize(wasmImports);
            Object[] imports = new Object[size];
            for (int i = 0; i < size; i++) {
                Object wasmImport = interop.readArrayElement(wasmImports, i);
                imports[i] = toJSImport(wasmImport);
            }
            return JSArray.createConstantObjectArray(getContext(), imports);
        }

        private Object toJSImport(Object wasmImport) throws InteropException {
            DynamicObject export = JSOrdinary.create(getContext());
            InteropLibrary interop = InteropLibrary.getUncached(wasmImport);
            for (String key : new String[]{"module", "name", "kind"}) {
                Object value = interop.readMember(wasmImport, key);
                JSObject.set(export, key, value);
            }
            return export;
        }

    }

    public abstract static class WebAssemblyModuleCustomSectionsNode extends JSBuiltinNode {
        @Child JSToStringNode toStringNode;

        public WebAssemblyModuleCustomSectionsNode(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
            toStringNode = JSToStringNode.create();
        }

        @Specialization
        protected Object customSectionsOfModule(JSWebAssemblyModuleObject moduleObject, Object sectionName) {
            if (sectionName == Undefined.instance) {
                throw Errors.createTypeError("WebAssembly.Module.customSections(): Argument 1 is required");
            }
            String name = toStringNode.executeString(sectionName);
            try {
                Object customSectionsFunction = getContext().getRealm().getWASMModuleCustomSectionsFunction();
                Object wasmCustomSections = InteropLibrary.getUncached(customSectionsFunction).execute(customSectionsFunction, moduleObject.getWASMModule(), name);
                return toJSArrayOfArrayBuffers(wasmCustomSections);
            } catch (InteropException ex) {
                throw Errors.shouldNotReachHere(ex);
            }
        }

        private Object toJSArrayOfArrayBuffers(Object wasmSections) throws InteropException {
            InteropLibrary interop = InteropLibrary.getUncached(wasmSections);
            int size = (int) interop.getArraySize(wasmSections);
            Object[] sections = new Object[size];
            for (int i = 0; i < size; i++) {
                Object wasmSection = interop.readArrayElement(wasmSections, i);
                sections[i] = toArrayBuffer(wasmSection);
            }
            return JSArray.createConstantObjectArray(getContext(), sections);
        }

        private Object toArrayBuffer(Object wasmSection) throws InteropException {
            InteropLibrary interop = InteropLibrary.getUncached(wasmSection);
            int size = (int) interop.getArraySize(wasmSection);
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = (byte) interop.readArrayElement(wasmSection, i);
            }
            return JSArrayBuffer.createArrayBuffer(getContext(), data);
        }

        @Fallback
        protected Object customSectionsOfOther(@SuppressWarnings("unused") Object other,
                        @SuppressWarnings("unused") Object sectionName) {
            throw Errors.createTypeError("WebAssembly.Module.customSections(): Argument 0 must be a WebAssembly.Module", this);
        }

    }

}
