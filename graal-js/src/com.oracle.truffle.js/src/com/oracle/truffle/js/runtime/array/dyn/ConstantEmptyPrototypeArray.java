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
package com.oracle.truffle.js.runtime.array.dyn;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.runtime.array.DynamicArray;
import com.oracle.truffle.js.runtime.array.ScriptArray;
import com.oracle.truffle.js.runtime.builtins.JSAbstractArray;
import com.oracle.truffle.js.runtime.objects.JSObject;

/**
 * This array type is reserved for Array.prototype and Object.prototype. It ensures an Assumption is
 * invalidated if the Array.prototype and Object.prototype is ever assigned an indexed property.
 */
public final class ConstantEmptyPrototypeArray extends AbstractConstantEmptyArray {
    public static ScriptArray createConstantEmptyPrototypeArray() {
        return new ConstantEmptyPrototypeArray(INTEGRITY_LEVEL_NONE, createCache());
    }

    private ConstantEmptyPrototypeArray(int integrityLevel, DynamicArrayCache cache) {
        super(integrityLevel, cache);
    }

    private static Assumption getArrayPrototypeNoElementsAssumption(DynamicObject object) {
        return JSObject.getJSContext(object).getArrayPrototypeNoElementsAssumption();
    }

    @Override
    public ScriptArray setLengthImpl(DynamicObject object, long length, ProfileHolder profile) {
        setCapacity(object, length);
        return this;
    }

    @Override
    public AbstractIntArray createWriteableInt(DynamicObject object, long index, int value, ProfileHolder profile) {
        getArrayPrototypeNoElementsAssumption(object).invalidate(JSAbstractArray.ARRAY_PROTOTYPE_NO_ELEMENTS_INVALIDATION);
        return super.createWriteableInt(object, index, value, profile);
    }

    @Override
    public AbstractDoubleArray createWriteableDouble(DynamicObject object, long index, double value, ProfileHolder profile) {
        getArrayPrototypeNoElementsAssumption(object).invalidate(JSAbstractArray.ARRAY_PROTOTYPE_NO_ELEMENTS_INVALIDATION);
        return super.createWriteableDouble(object, index, value, profile);
    }

    @Override
    public AbstractJSObjectArray createWriteableJSObject(DynamicObject object, long index, DynamicObject value, ProfileHolder profile) {
        getArrayPrototypeNoElementsAssumption(object).invalidate(JSAbstractArray.ARRAY_PROTOTYPE_NO_ELEMENTS_INVALIDATION);
        return super.createWriteableJSObject(object, index, value, profile);
    }

    @Override
    public AbstractObjectArray createWriteableObject(DynamicObject object, long index, Object value, ProfileHolder profile) {
        getArrayPrototypeNoElementsAssumption(object).invalidate(JSAbstractArray.ARRAY_PROTOTYPE_NO_ELEMENTS_INVALIDATION);
        return super.createWriteableObject(object, index, value, profile);
    }

    @Override
    public ScriptArray removeRangeImpl(DynamicObject object, long start, long end) {
        setCapacity(object, getCapacity(object) - (end - start));
        return this;
    }

    @Override
    public ScriptArray addRangeImpl(DynamicObject object, long offset, int size) {
        setCapacity(object, getCapacity(object) + size);
        return this;
    }

    @Override
    protected DynamicArray withIntegrityLevel(int newIntegrityLevel) {
        return new ConstantEmptyPrototypeArray(newIntegrityLevel, cache);
    }
}
