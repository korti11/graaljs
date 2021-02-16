/*
 * Copyright (c) 2019, 2021, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.js.runtime.builtins;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidBufferOffsetException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSAgentWaiterList;
import com.oracle.truffle.js.runtime.array.ByteArrayAccess;
import com.oracle.truffle.js.runtime.array.ByteBufferAccess;
import com.oracle.truffle.js.runtime.objects.JSNonProxyObject;
import com.oracle.truffle.js.runtime.util.DirectByteBufferHelper;

public abstract class JSArrayBufferObject extends JSNonProxyObject {

    public static final String CLASS_NAME = "ArrayBuffer";
    public static final String PROTOTYPE_NAME = CLASS_NAME + ".prototype";

    protected JSArrayBufferObject(Shape shape) {
        super(shape);
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
    }

    public abstract int getByteLength();

    public abstract void detachArrayBuffer();

    public abstract boolean isDetached();

    public static byte[] getByteArray(DynamicObject thisObj) {
        assert JSAbstractBuffer.isJSAbstractHeapBuffer(thisObj);
        return ((Heap) thisObj).getByteArray();
    }

    public static int getByteLength(DynamicObject thisObj) {
        assert JSAbstractBuffer.isJSAbstractHeapBuffer(thisObj);
        return getByteArray(thisObj).length;
    }

    public static int getDirectByteLength(DynamicObject thisObj) {
        return getDirectByteBuffer(thisObj).capacity();
    }

    public static void setDirectByteBuffer(DynamicObject thisObj, ByteBuffer buffer) {
        ((DirectBase) thisObj).setByteBuffer(buffer);
    }

    public static ByteBuffer getDirectByteBuffer(DynamicObject thisObj) {
        assert JSArrayBuffer.isJSDirectArrayBuffer(thisObj) || JSSharedArrayBuffer.isJSSharedArrayBuffer(thisObj);
        return DirectByteBufferHelper.cast(((DirectBase) thisObj).getByteBuffer());
    }

    public static JSAgentWaiterList getWaiterList(DynamicObject thisObj) {
        return ((Shared) thisObj).getWaiterList();
    }

    public static void setWaiterList(DynamicObject thisObj, JSAgentWaiterList waiterList) {
        ((Shared) thisObj).setWaiterList(waiterList);
    }

    @ExportLibrary(InteropLibrary.class)
    public static final class Heap extends JSArrayBufferObject {
        byte[] byteArray;

        protected Heap(Shape shape, byte[] byteArray) {
            super(shape);
            this.byteArray = byteArray;
        }

        public byte[] getByteArray() {
            return byteArray;
        }

        @Override
        public void detachArrayBuffer() {
            this.byteArray = null;
        }

        @Override
        public boolean isDetached() {
            return byteArray == null;
        }

        @Override
        public int getByteLength() {
            return byteArray.length;
        }

        @ExportMessage
        boolean hasBufferElements() {
            return !isDetached();
        }

        @ExportMessage
        long getBufferSize() {
            return isDetached() ? 0 : getByteLength();
        }

        private void ensureNotDetached() throws UnsupportedMessageException {
            if (isDetached()) {
                throw UnsupportedMessageException.create();
            }
        }

        @ExportMessage
        byte readBufferByte(long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return byteArray[Math.toIntExact(byteOffset)];
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Byte.BYTES);
            }
        }

        @ExportMessage
        short readBufferShort(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return (short) ByteArrayAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getInt16(byteArray, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Short.BYTES);
            }
        }

        @ExportMessage
        int readBufferInt(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteArrayAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getInt32(byteArray, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Integer.BYTES);
            }
        }

        @ExportMessage
        long readBufferLong(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteArrayAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getInt64(byteArray, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Long.BYTES);
            }
        }

        @ExportMessage
        float readBufferFloat(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteArrayAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getFloat(byteArray, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Float.BYTES);
            }
        }

        @ExportMessage
        double readBufferDouble(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteArrayAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getDouble(byteArray, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Double.BYTES);
            }
        }
    }

    @ExportLibrary(InteropLibrary.class)
    public abstract static class DirectBase extends JSArrayBufferObject {
        ByteBuffer byteBuffer;

        protected DirectBase(Shape shape, ByteBuffer byteBuffer) {
            super(shape);
            this.byteBuffer = byteBuffer;
        }

        public final ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        public final void setByteBuffer(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        @Override
        public final int getByteLength() {
            return byteBuffer.limit();
        }

        @Override
        public abstract void detachArrayBuffer();

        @ExportMessage
        final boolean hasBufferElements() {
            return !isDetached();
        }

        @ExportMessage
        final long getBufferSize() {
            return isDetached() ? 0 : getByteLength();
        }

        private void ensureNotDetached() throws UnsupportedMessageException {
            if (isDetached()) {
                throw UnsupportedMessageException.create();
            }
        }

        @ExportMessage
        final byte readBufferByte(long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return byteBuffer.get(Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Byte.BYTES);
            }
        }

        @ExportMessage
        final short readBufferShort(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return (short) ByteBufferAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getInt16(byteBuffer, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Short.BYTES);
            }
        }

        @ExportMessage
        final int readBufferInt(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteBufferAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getInt32(byteBuffer, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Integer.BYTES);
            }
        }

        @ExportMessage
        final long readBufferLong(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteBufferAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getInt64(byteBuffer, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Long.BYTES);
            }
        }

        @ExportMessage
        final float readBufferFloat(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteBufferAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getFloat(byteBuffer, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Float.BYTES);
            }
        }

        @ExportMessage
        final double readBufferDouble(ByteOrder order, long byteOffset) throws UnsupportedMessageException, InvalidBufferOffsetException {
            ensureNotDetached();
            try {
                return ByteBufferAccess.forOrder(order == ByteOrder.LITTLE_ENDIAN).getDouble(byteBuffer, Math.toIntExact(byteOffset));
            } catch (IndexOutOfBoundsException | ArithmeticException e) {
                throw InvalidBufferOffsetException.create(byteOffset, Double.BYTES);
            }
        }
    }

    public static final class Direct extends DirectBase {
        protected Direct(Shape shape, ByteBuffer byteBuffer) {
            super(shape, byteBuffer);
        }

        @Override
        public void detachArrayBuffer() {
            this.byteBuffer = null;
        }

        @Override
        public boolean isDetached() {
            return byteBuffer == null;
        }
    }

    public static final class Shared extends DirectBase {
        JSAgentWaiterList waiterList;

        protected Shared(Shape shape, ByteBuffer byteBuffer, JSAgentWaiterList waiterList) {
            super(shape, byteBuffer);
            this.waiterList = waiterList;
        }

        public JSAgentWaiterList getWaiterList() {
            return waiterList;
        }

        public void setWaiterList(JSAgentWaiterList waiterList) {
            this.waiterList = waiterList;
        }

        @Override
        public void detachArrayBuffer() {
            throw Errors.unsupported("SharedArrayBuffer cannot be detached");
        }

        @Override
        public boolean isDetached() {
            return false;
        }
    }

    public static DynamicObject createHeapArrayBuffer(Shape shape, byte[] byteArray) {
        return new Heap(shape, byteArray);
    }

    public static DynamicObject createDirectArrayBuffer(Shape shape, ByteBuffer byteBuffer) {
        return new Direct(shape, byteBuffer);
    }

    public static DynamicObject createSharedArrayBuffer(Shape shape, ByteBuffer byteBuffer, JSAgentWaiterList waiterList) {
        return new Shared(shape, byteBuffer, waiterList);
    }
}
