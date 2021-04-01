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
package com.oracle.truffle.js.runtime.java.adapter;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.security.AllPermission;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Supplier;

import org.graalvm.polyglot.Value;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * This class encapsulates the bytecode of the adapter class and can be used to load it into the JVM
 * as an actual Class. It can be invoked repeatedly to create multiple adapter classes from the same
 * bytecode; adapter classes that have class-level overrides must be re-created for every set of
 * such overrides. Note that while this class is named "class loader", it does not, in fact, extend
 * {@code ClassLoader}, but rather uses them internally. Instances of this class are normally
 * created by {@link JavaAdapterBytecodeGenerator}.
 */
final class JavaAdapterClassLoader {
    static final ProtectionDomain GENERATED_PROTECTION_DOMAIN = createGeneratedProtectionDomain();
    static final Collection<String> VISIBLE_INTERNAL_CLASS_NAMES = Collections.unmodifiableCollection(new HashSet<>(Arrays.asList(JavaAdapterServices.class.getName(), Value.class.getName())));

    private final String className;
    private final byte[] classBytes;

    JavaAdapterClassLoader(String className, byte[] classBytes) {
        this.className = className.replace('/', '.');
        this.classBytes = classBytes;
    }

    /**
     * Loads the generated adapter class into the JVM.
     *
     * @param parentLoader the parent class loader for the generated class loader
     * @return the generated adapter class
     */
    Class<?> generateClass(ClassLoader parentLoader, Value classOverrides) {
        try {
            return Class.forName(className, true, createClassLoader(parentLoader, classOverrides));
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private ClassLoader createClassLoader(final ClassLoader parentLoader, final Value classOverrides) {
        final class CLImpl extends SecureClassLoader implements Supplier<Value> {
            private static final boolean PRINT_CODE = false;
            private final ClassLoader myLoader = getClass().getClassLoader();

            private CLImpl() {
                super(parentLoader);
            }

            @Override
            public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
                try {
                    return super.loadClass(name, resolve);
                } catch (final SecurityException se) {
                    /*
                     * we may be implementing an interface or extending a class that was loaded by a
                     * loader that prevents package.access. If so, it'd throw SecurityException for
                     * internal classes used by generated adapter classes.
                     */
                    if (VISIBLE_INTERNAL_CLASS_NAMES.contains(name)) {
                        return loadInternalClass(name);
                    }
                    throw se;
                }
            }

            private Class<?> loadInternalClass(final String name) throws ClassNotFoundException {
                return myLoader != null ? myLoader.loadClass(name) : Class.forName(name, false, myLoader);
            }

            @Override
            protected Class<?> findClass(final String name) throws ClassNotFoundException {
                if (name.equals(className)) {
                    if (PRINT_CODE) {
                        printCode(classBytes);
                    }
                    return defineClass(name, classBytes, 0, classBytes.length, GENERATED_PROTECTION_DOMAIN);
                }
                if (VISIBLE_INTERNAL_CLASS_NAMES.contains(name)) {
                    return loadInternalClass(name);
                }
                throw new ClassNotFoundException(name);
            }

            private void printCode(byte[] bytecode) {
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (PrintWriter pw = new PrintWriter(baos)) {
                    new ClassReader(bytecode).accept(new TraceClassVisitor(pw), 0);
                }
                System.out.println(new String(baos.toByteArray()));
            }

            @Override
            public Value get() {
                return classOverrides;
            }
        }
        return new CLImpl();
    }

    private static ProtectionDomain createGeneratedProtectionDomain() {
        /*
         * Generated classes need to have AllPermission. Since we require the "createClassLoader"
         * RuntimePermission, we can create a class loader that'll load new classes with any
         * permissions. Our generated classes are just delegating adapters, so having AllPermission
         * can't cause anything wrong; the effective set of permissions for the executing script
         * functions will still be limited by the permissions of the caller and the permissions of
         * the script.
         */
        final Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        return new ProtectionDomain(new CodeSource(null, (CodeSigner[]) null), permissions);
    }

    @SuppressWarnings("unchecked")
    static Value getClassOverrides(ClassLoader classLoader) {
        return ((Supplier<Value>) classLoader).get();
    }
}
