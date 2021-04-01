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
package com.oracle.truffle.js.test.threading;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncTaskTests {

    /**
     * A JavaScript function can be executed asynchronously in another thread (using proper
     * synchronization).
     */
    @Test
    public void completableFuture() throws InterruptedException {
        final AtomicBoolean asyncTaskExecuted = new AtomicBoolean(false);
        final AtomicReference<Throwable> asyncException = new AtomicReference<>();
        final AtomicReference<Object> asyncJsResult = new AtomicReference<>();
        final ForkJoinPool testExecutor = new ForkJoinPool();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        try (Context cx = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).allowPolyglotAccess(PolyglotAccess.ALL).out(out).err(err).build()) {
            // Expose a Java method as a JavaScript function.
            cx.getBindings("js").putMember("javaNextTick", (CallableInt) (jsLambda) -> {
                // Submit a JavaScript function for async execution in another thread.
                CompletableFuture.supplyAsync(() -> {
                    asyncTaskExecuted.set(true);
                    synchronized (cx) {
                        // Re-enter context.
                        cx.enter();
                        try {
                            // Execute the JS callback function.
                            return jsLambda.execute();
                        } finally {
                            // Leave context.
                            cx.leave();
                        }
                    }
                }, testExecutor).whenComplete((r, ex) -> {
                    asyncException.set(ex);
                    asyncJsResult.set(r);
                });
            });
            // Create a JS function that will execute a Java callback asynchronously. Conceptually,
            // this is equivalent to `process.nextTick()` in Node.js
            Value jsFunction = cx.eval("js", "(function() {" +
                            "    return javaNextTick(()=>{console.log('something async'); return 42;});" +
                            "})");
            // The callback will execute a JS function in another concurrent thread. Synchronization
            // is needed to prevent data races.
            synchronized (cx) {
                // Execute the JS function. Context enter and leave are implicit.
                jsFunction.executeVoid();
            }
            testExecutor.shutdown();
            testExecutor.awaitTermination(1, TimeUnit.MINUTES);
            Assert.assertNull(asyncException.get());
            Assert.assertTrue(asyncTaskExecuted.get());
            Assert.assertTrue(new String(err.toByteArray()).isEmpty());
            Assert.assertEquals("something async\n", new String(out.toByteArray()));
            Assert.assertEquals(42, ((Value) asyncJsResult.get()).asInt());
        }
    }

    /**
     * A JavaScript function can be executed asynchronously in another thread (using proper
     * synchronization). Asynchronous execution can be mapped to a JavaScript Promise.
     */
    @Test
    public void completableFuturePromise() throws InterruptedException {
        final AtomicBoolean asyncTaskExecuted = new AtomicBoolean(false);
        final AtomicReference<Throwable> asyncException = new AtomicReference<>();
        final AtomicReference<Object> asyncJsResult = new AtomicReference<>();
        final ForkJoinPool testExecutor = new ForkJoinPool();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        try (Context cx = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).allowPolyglotAccess(PolyglotAccess.ALL).out(out).err(err).build()) {
            // Expose a Java thenable object as a JavaScript Promise.
            cx.getBindings("js").putMember("javaThenableInstance", (ThenableInt) (onResolve, onReject) -> {
                // Submit a completable future for async execution in another thread.
                CompletableFuture.supplyAsync(() -> {
                    asyncTaskExecuted.set(true);
                    synchronized (cx) {
                        // Re-enter context.
                        cx.enter();
                        try {
                            // Resolve the JS Promise with completion value 'post'.
                            // Execution flow will continue in the JS engine.
                            onResolve.execute("post");
                            // Returned value from Java completable future will be dispatched to
                            // Java's `whenComplete`.
                            return 42;
                        } catch (Throwable t) {
                            onReject.executeVoid(t);
                            return t;
                        } finally {
                            // Leave context.
                            cx.leave();
                        }
                    }
                }, testExecutor).whenComplete((r, ex) -> {
                    asyncException.set(ex);
                    asyncJsResult.set(r);
                });
            });
            // Create an async JS function that will wait for an async task executed in a Java async
            // executor.
            Value asyncJsFunction = cx.eval("js", "(async function() {" +
                            "    console.log('pre');" +
                            "    var post = await javaThenableInstance;" +
                            "    console.log(post);" +
                            "})");
            // The callback will execute a JS function in another concurrent thread. Synchronization
            // is needed to prevent data races.
            synchronized (cx) {
                // Execute the JS function. Context enter and leave are implicit.
                asyncJsFunction.executeVoid();
            }
            testExecutor.shutdown();
            testExecutor.awaitTermination(1, TimeUnit.MINUTES);
            Assert.assertNull(asyncException.get());
            Assert.assertTrue(asyncTaskExecuted.get());
            Assert.assertTrue(new String(err.toByteArray()).isEmpty());
            Assert.assertEquals("pre\npost\n", new String(out.toByteArray()));
            Assert.assertEquals(42, asyncJsResult.get());
        }
    }

    /**
     * Asynchronous execution in Java can be mapped to a JavaScript Promise. JavaScript code can
     * wait on multiple Java completable futures.
     */
    @Test
    public void completableFuturePromiseAll() throws InterruptedException {
        final AtomicInteger asyncTasksExecuted = new AtomicInteger(0);
        final AtomicReference<Throwable> asyncException = new AtomicReference<>();
        final ForkJoinPool testExecutor = new ForkJoinPool();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        try (Context cx = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).allowPolyglotAccess(PolyglotAccess.ALL).out(out).err(err).build()) {
            // Register three Java async tasks in the Polyglot context. JavaScript will treat them
            // as Promise objects.
            cx.getBindings("js").putMember("javaThenable1", createThenable(asyncTasksExecuted, asyncException, testExecutor, cx, 39));
            cx.getBindings("js").putMember("javaThenable2", createThenable(asyncTasksExecuted, asyncException, testExecutor, cx, 1));
            cx.getBindings("js").putMember("javaThenable3", createThenable(asyncTasksExecuted, asyncException, testExecutor, cx, 2));
            // Create an async JS function that will wait for an async task executed in a Java async
            // executor.
            Value asyncJsFunction = cx.eval("js", "(async function() {" +
                            "    console.log('pre');" +
                            "    var all = await Promise.all([javaThenable1, javaThenable2, javaThenable3]);" +
                            "    console.log('post');" +
                            "    console.log(all.reduce((x,y)=>x+y));" +
                            "})");
            // The callback will execute a JS function in another concurrent thread. Synchronization
            // is needed to prevent data races.
            synchronized (cx) {
                // Execute the JS function. Context enter and leave are implicit.
                asyncJsFunction.executeVoid();
            }
            testExecutor.shutdown();
            testExecutor.awaitTermination(1, TimeUnit.MINUTES);
            Assert.assertNull(asyncException.get());
            Assert.assertEquals(3, asyncTasksExecuted.get());
            Assert.assertTrue(new String(err.toByteArray()).isEmpty());
            Assert.assertEquals("pre\npost\n42\n", new String(out.toByteArray()));
        }
    }

    private static ThenableInt createThenable(AtomicInteger asyncTaskExecuted, AtomicReference<Throwable> asyncException, ForkJoinPool testExecutor, Context cx, int result) {
        return (onResolve, onReject) -> {
            // Submit a Java function for async execution in another thread.
            CompletableFuture.runAsync(() -> {
                asyncTaskExecuted.incrementAndGet();
                try {
                    // Simulate some parallel work.
                    // For example, blocking IO or a long-running Java method call.
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    asyncException.set(ex);
                }
                synchronized (cx) {
                    // Re-enter context.
                    cx.enter();
                    try {
                        // Resolve the JS Promise using an integer value.
                        // Execution flow will continue in the JS engine.
                        onResolve.execute(result);
                    } catch (Throwable t) {
                        onReject.executeVoid(t);
                    } finally {
                        // Leave context.
                        cx.leave();
                    }
                }
            }, testExecutor).whenComplete((r, ex) -> asyncException.set(ex));
        };
    }

    /**
     * A running JavaScript thread can be suspended (blocked) while JS execution can continue in
     * another thread.
     */
    @Test
    public void plainJavaThread() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final AtomicReference<Object> asyncJsResult = new AtomicReference<>();
        try (Context cx = Context.newBuilder("js").allowHostAccess(HostAccess.ALL).allowPolyglotAccess(PolyglotAccess.ALL).out(out).err(out).build()) {
            // Enter the current context from the main thread.
            cx.enter();
            // Expose a Java method as a JavaScript function.
            cx.getBindings("js").putMember("aJavaFunction", (CallableInt) (jsLambda) -> {
                // Create a new thread
                Thread thread = new Thread(() -> {
                    // Enter the JS context from another thread.
                    cx.enter();
                    // Execute a JavaScript function.
                    Value jsResult = jsLambda.execute();
                    asyncJsResult.set(jsResult);
                    // Leave the current context
                    cx.leave();
                });
                // Leave the current context.
                cx.leave();
                // Start thread and wait for completion.
                thread.start();
                // Halt until thread completes.
                thread.join();
                // Re-enter context from main thread.
                cx.enter();
            });
            // Create a JS function that will execute a Java callback.
            Value jsFunction = cx.eval("js", "(function() {" +
                            "    return aJavaFunction(()=>{ console.log('something'); return 42;});" +
                            "});");
            // Execute the JS function
            jsFunction.executeVoid();
            // The context can be used again from the current thread
            cx.eval("js", "console.log('something else');");
        }
        Assert.assertEquals(42, ((Value) asyncJsResult.get()).asInt());
        Assert.assertEquals("something\nsomething else\n", new String(out.toByteArray()));
    }

    @FunctionalInterface
    public interface CallableInt {
        void execute(Value jsLambda) throws InterruptedException;
    }

    @FunctionalInterface
    public interface ThenableInt {
        void then(Value onResolve, Value onReject);
    }
}
