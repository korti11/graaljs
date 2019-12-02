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
package com.oracle.truffle.js.builtins.commonjs;

import com.oracle.truffle.api.*;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.*;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.js.builtins.GlobalBuiltins;
import com.oracle.truffle.js.lang.JavaScriptLanguage;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.runtime.*;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.builtins.JSFunctionData;
import com.oracle.truffle.js.runtime.builtins.JSUserObject;
import com.oracle.truffle.js.runtime.objects.JSObject;

import java.util.Map;
import java.util.Objects;
import java.util.Stack;

import static com.oracle.truffle.js.builtins.commonjs.CommonJsResolution.isCoreModule;

public abstract class CommonJsRequireBuiltin extends GlobalBuiltins.JSFileLoadingOperation {

    private static final boolean LOG_REQUIRE_PATH_RESOLUTION = false;
    private static final Stack<String> requireDebugStack;

    static {
        requireDebugStack = LOG_REQUIRE_PATH_RESOLUTION ? new Stack<>() : null;
    }

    private static void log(Object... message) {
        if (LOG_REQUIRE_PATH_RESOLUTION) {
            StringBuilder s = new StringBuilder("['.'");
            for (String module : requireDebugStack) {
                s.append(" '").append(module).append("'");
            }
            s.append("] ");
            for (Object m : message) {
                s.append(m == null ? "null" : m.toString());
            }
            System.err.println(s.toString());
        }
    }

    private static void debugStackPush(String moduleIdentifier) {
        if (LOG_REQUIRE_PATH_RESOLUTION) {
            requireDebugStack.push(moduleIdentifier);
        }
    }

    private static void debugStackPop() {
        if (LOG_REQUIRE_PATH_RESOLUTION) {
            requireDebugStack.pop();
        }
    }

    public static final String FILENAME_VAR_NAME = "__filename";
    public static final String DIRNAME_VAR_NAME = "__dirname";
    public static final String MODULE_PROPERTY_NAME = "module";
    public static final String EXPORTS_PROPERTY_NAME = "exports";
    public static final String REQUIRE_PROPERTY_NAME = "require";
    public static final String RESOLVE_PROPERTY_NAME = "resolve";

    private static final String MODULE_END = "\n});";
    private static final String MODULE_PREAMBLE = "(function (exports, require, module, __filename, __dirname) {";

    private static final String LOADED_PROPERTY_NAME = "loaded";
    private static final String FILENAME_PROPERTY_NAME = "filename";
    private static final String ID_PROPERTY_NAME = "id";
    private static final String ENV_PROPERTY_NAME = "env";

    private static final String JS_EXT = ".js";
    private static final String JSON_EXT = ".json";
    private static final String NODE_EXT = ".node";

    private final TruffleFile modulesResolutionCwd;

    static TruffleFile getModuleResolveCurrentWorkingDirectory(JSContext context) {
        String cwdOption = context.getContextOptions().getRequireCwd();
        TruffleLanguage.Env env = context.getRealm().getEnv();
        String currentFileNameFromStack = CommonJsResolution.getCurrentFileNameFromStack();
        if (currentFileNameFromStack == null) {
            return cwdOption == null ? env.getCurrentWorkingDirectory() : env.getPublicTruffleFile(cwdOption);
        } else {
            TruffleFile truffleFile = env.getPublicTruffleFile(currentFileNameFromStack);
            assert truffleFile.isRegularFile() && truffleFile.getParent() != null;
            return truffleFile.getParent().normalize();
        }
    }

    CommonJsRequireBuiltin(JSContext context, JSBuiltin builtin) {
        super(context, builtin);
        this.modulesResolutionCwd = getModuleResolveCurrentWorkingDirectory(context);
    }

    @Specialization
    protected Object require(VirtualFrame frame, String moduleIdentifier) {
        DynamicObject currentRequire = (DynamicObject) JSArguments.getFunctionObject(frame.getArguments());
        TruffleLanguage.Env env = getContext().getRealm().getEnv();
        TruffleFile resolutionEntryPath = getModuleResolutionEntryPath(currentRequire, env);
        return requireImpl(moduleIdentifier, resolutionEntryPath);
    }

    @CompilerDirectives.TruffleBoundary
    private Object requireImpl(String moduleIdentifier, TruffleFile entryPath) {
        log("required module '", moduleIdentifier, "'                                core:", isCoreModule(moduleIdentifier), " from path ", entryPath.normalize());
        if (isCoreModule(moduleIdentifier) || "".equals(moduleIdentifier)) {
            String moduleReplacementName = getContext().getContextOptions().getCommonJsRequireBuiltins().get(moduleIdentifier);
            if (moduleReplacementName != null && !"".equals(moduleReplacementName)) {
                return requireImpl(moduleReplacementName, modulesResolutionCwd);
            }
            throw fail(moduleIdentifier);
        }
        TruffleFile maybeModule = CommonJsResolution.resolve(getContext(), moduleIdentifier, entryPath);
        log("module ", moduleIdentifier, " resolved to ", maybeModule);
        if (maybeModule == null) {
            throw fail(moduleIdentifier);
        }
        if (isJsFile(maybeModule)) {
            return evalJavaScriptFile(maybeModule, moduleIdentifier);
        } else if (isJsonFile(maybeModule)) {
            return evalJsonFile(maybeModule);
        } else if (isNodeBinFile(maybeModule)) {
            return fail("Unsupported .node file: ", moduleIdentifier);
        } else {
            throw fail(moduleIdentifier);
        }
    }

    private Object evalJavaScriptFile(TruffleFile modulePath, String moduleIdentifier) {
        JSRealm realm = getContext().getRealm();
        // If cached, return from cache. This is by design to avoid infinite require loops.
        Map<TruffleFile, DynamicObject> commonJsCache = realm.getCommonJsRequireCache();
        if (commonJsCache.containsKey(modulePath.normalize())) {
            DynamicObject moduleBuiltin = commonJsCache.get(modulePath.normalize());
            Object cached = JSObject.get(moduleBuiltin, EXPORTS_PROPERTY_NAME);
            log("returning cached '", modulePath.normalize(), "'  APIs: {", JSObject.enumerableOwnNames((DynamicObject) cached), "}");
            return cached;
        }
        // Read the file.
        Source source = sourceFromPath(modulePath.toString(), realm);
        String filenameBuiltin = modulePath.normalize().toString();
        if (modulePath.getParent() == null) {
            throw fail(moduleIdentifier);
        }
        // Create `require` and other builtins for this module.
        String dirnameBuiltin = modulePath.getParent().getAbsoluteFile().normalize().toString();
        DynamicObject exportsBuiltin = createExportsBuiltin(realm);
        DynamicObject moduleBuiltin = createModuleBuiltin(realm, exportsBuiltin, filenameBuiltin);
        DynamicObject requireBuiltin = createRequireBuiltin(realm, moduleBuiltin, filenameBuiltin);
        DynamicObject env = JSUserObject.create(getContext());
        JSObject.set(env, ENV_PROPERTY_NAME, JSUserObject.create(getContext()));
        // Parse the module
        CharSequence characters = MODULE_PREAMBLE + source.getCharacters() + MODULE_END;
        Source moduleSources = Source.newBuilder(JavaScriptLanguage.ID, characters, filenameBuiltin).mimeType(JavaScriptLanguage.TEXT_MIME_TYPE).build();
        CallTarget moduleCallTarget = realm.getEnv().parsePublic(moduleSources);
        Object moduleExecutableFunction = moduleCallTarget.call();
        // Execute the module.
        if (JSFunction.isJSFunction(moduleExecutableFunction)) {
            commonJsCache.put(modulePath.normalize(), moduleBuiltin);
            try {
                debugStackPush(moduleIdentifier);
                log("executing '", filenameBuiltin, "' for ", moduleIdentifier);
                JSFunction.call(JSArguments.create(moduleExecutableFunction, moduleExecutableFunction, exportsBuiltin, requireBuiltin, moduleBuiltin, filenameBuiltin, dirnameBuiltin, env));
                JSObject.set(moduleBuiltin, LOADED_PROPERTY_NAME, true);
                return JSObject.get(moduleBuiltin, EXPORTS_PROPERTY_NAME);
            } catch (Exception e) {
                log("EXCEPTION: '", e.getMessage(), "'");
                throw e;
            } finally {
                debugStackPop();
                Object module = JSObject.get(moduleBuiltin, EXPORTS_PROPERTY_NAME);
                log("done '", moduleIdentifier, "' module.exports: ", module, "   APIs: {", JSObject.enumerableOwnNames((DynamicObject) module), "}");
            }
        }
        return null;
    }

    private DynamicObject evalJsonFile(TruffleFile jsonFile) {
        try {
            if (fileExists(jsonFile)) {
                Source source = null;
                JSRealm realm = getContext().getRealm();
                TruffleFile file = GlobalBuiltins.resolveRelativeFilePath(jsonFile.toString(), realm.getEnv());
                if (file.isRegularFile()) {
                    source = sourceFromTruffleFile(file);
                } else {
                    throw fail(jsonFile.toString());
                }
                DynamicObject parse = (DynamicObject) realm.getJsonParseFunctionObject();
                assert source != null;
                String jsonString = source.getCharacters().toString().replace('\n', ' ');
                Object jsonObj = JSFunction.call(JSArguments.create(parse, parse, jsonString));
                if (JSObject.isJSObject(jsonObj)) {
                    return (DynamicObject) jsonObj;
                }
            }
            throw fail(jsonFile.toString());
        } catch (SecurityException e) {
            throw Errors.createErrorFromException(e);
        }
    }

    private static JSException fail(String moduleIdentifier) {
        return JSException.create(JSErrorType.TypeError, "Cannot load CommonJs module: '" + moduleIdentifier + "'");
    }

    private static JSException fail(String... message) {
        StringBuilder sb = new StringBuilder();
        for (String s : message) {
            sb.append(s);
        }
        return JSException.create(JSErrorType.TypeError, sb.toString());
    }

    private static DynamicObject createModuleBuiltin(JSRealm realm, DynamicObject exportsBuiltin, String fileNameBuiltin) {
        DynamicObject module = JSUserObject.create(realm.getContext(), realm);
        JSObject.set(module, EXPORTS_PROPERTY_NAME, exportsBuiltin);
        JSObject.set(module, ID_PROPERTY_NAME, fileNameBuiltin);
        JSObject.set(module, FILENAME_PROPERTY_NAME, fileNameBuiltin);
        JSObject.set(module, LOADED_PROPERTY_NAME, false);
        return module;
    }

    private static DynamicObject createRequireBuiltin(JSRealm realm, DynamicObject moduleBuiltin, String fileNameBuiltin) {
        DynamicObject mainRequire = (DynamicObject) realm.getCommonJsRequireFunctionObject();
        DynamicObject mainResolve = (DynamicObject) JSObject.get(mainRequire, RESOLVE_PROPERTY_NAME);
        JSFunctionData functionData = JSFunction.getFunctionData(mainRequire);
        DynamicObject newRequire = JSFunction.create(realm, functionData);
        JSObject.set(newRequire, MODULE_PROPERTY_NAME, moduleBuiltin);
        JSObject.set(newRequire, RESOLVE_PROPERTY_NAME, mainResolve);
        // XXX(db) Here, we store the current filename in the (new) require builtin.
        // In this way, we avoid managing a shadow stack to track the current require's parent.
        // In Node.js, this is done using a (closed) level variable.
        JSObject.set(newRequire, FILENAME_VAR_NAME, fileNameBuiltin);
        return newRequire;
    }

    private static DynamicObject createExportsBuiltin(JSRealm realm) {
        return JSUserObject.create(realm.getContext(), realm);
    }

    private static boolean isNodeBinFile(TruffleFile maybeModule) {
        return hasExtension(Objects.requireNonNull(maybeModule.getName()), NODE_EXT);
    }

    private static boolean isJsFile(TruffleFile maybeModule) {
        return hasExtension(Objects.requireNonNull(maybeModule.getName()), JS_EXT);
    }

    private static boolean isJsonFile(TruffleFile maybeModule) {
        return hasExtension(Objects.requireNonNull(maybeModule.getName()), JSON_EXT);
    }

    private static boolean fileExists(TruffleFile modulePath) {
        return modulePath.exists() && modulePath.isRegularFile();
    }

    private TruffleFile getModuleResolutionEntryPath(DynamicObject currentRequire, TruffleLanguage.Env env) {
        if (JSObject.isJSObject(currentRequire)) {
            Object maybeFilename = JSObject.get(currentRequire, FILENAME_VAR_NAME);
            if (JSRuntime.isString(maybeFilename)) {
                String fileName = (String) maybeFilename;
                if (isFile(env, fileName)) {
                    return getParent(env, fileName);
                }
            }
            // dirname not a string. Use default cwd.
        }
        // This is not a nested `require()` call, so we use the default cwd.
        return getModuleResolveCurrentWorkingDirectory(getContext());
    }

    private static TruffleFile getParent(TruffleLanguage.Env env, String fileName) {
        return env.getPublicTruffleFile(fileName).getParent();
    }

    private static boolean isFile(TruffleLanguage.Env env, String fileName) {
        return env.getPublicTruffleFile(fileName).exists();
    }

    private static boolean hasExtension(String fileName, String ext) {
        return fileName.lastIndexOf(ext) > 0 && fileName.lastIndexOf(ext) == fileName.length() - ext.length();
    }

}
