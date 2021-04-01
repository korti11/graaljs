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
package com.oracle.truffle.js.runtime.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRuntime;
import com.oracle.truffle.js.runtime.util.DebugCounter;
import com.oracle.truffle.js.runtime.util.UnmodifiableArrayList;

/**
 * Extra metadata associated with JavaScript object shapes.
 *
 * @see JSShape
 */
public final class JSShapeData {
    private static final Property[] EMPTY_PROPERTY_ARRAY = new Property[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private Property[] propertyArray;
    private String[] enumerablePropertyNames;

    private JSShapeData() {
    }

    private static Property[] createPropertiesArray(Shape shape) {
        CompilerAsserts.neverPartOfCompilation();
        propertyListAllocCount.inc();
        List<Property> ownProperties = shape.getPropertyList();
        sortProperties(ownProperties);
        return ownProperties.toArray(EMPTY_PROPERTY_ARRAY);
    }

    private static String[] createEnumerablePropertyNamesArray(Shape shape) {
        CompilerAsserts.neverPartOfCompilation();
        enumerablePropertyListAllocCount.inc();
        List<String> ownProperties = new ArrayList<>();
        shape.getPropertyList().forEach(property -> {
            if (JSProperty.isEnumerable(property) && property.getKey() instanceof String) {
                ownProperties.add((String) property.getKey());
            }
        });
        sortPropertyKeys(ownProperties);
        return ownProperties.toArray(EMPTY_STRING_ARRAY);
    }

    private static void sortProperties(List<Property> ownProperties) {
        CompilerAsserts.neverPartOfCompilation();
        Collections.sort(ownProperties, (o1, o2) -> JSRuntime.comparePropertyKeys(o1.getKey(), o2.getKey()));
    }

    private static void sortPropertyKeys(List<? extends Object> ownProperties) {
        CompilerAsserts.neverPartOfCompilation();
        Collections.sort(ownProperties, JSRuntime::comparePropertyKeys);
    }

    private static JSShapeData getShapeData(Shape shape) {
        CompilerAsserts.neverPartOfCompilation();
        JSContext context = JSShape.getJSContext(shape);
        synchronized (context) {
            Map<Shape, JSShapeData> map = context.getShapeDataMap();
            JSShapeData shapeData = map.get(shape);
            if (shapeData == null) {
                shapeData = new JSShapeData();
                map.put(shape, shapeData);
            }
            return shapeData;
        }
    }

    @TruffleBoundary
    private static Property[] getPropertiesArray(Shape shape) {
        assert shape.getPropertyCount() != 0;
        return getPropertiesArray(getShapeData(shape), shape);
    }

    private static Property[] getPropertiesArray(JSShapeData shapeData, Shape shape) {
        Property[] propertyArray = shapeData.propertyArray;
        if (propertyArray == null) {
            propertyArray = createPropertiesArray(shape);
            assert propertyArray.length == shape.getPropertyCount();
            shapeData.propertyArray = propertyArray;
        }
        return propertyArray;
    }

    static UnmodifiableArrayList<Property> getProperties(Shape shape) {
        return asUnmodifiableList(shape.getPropertyCount() == 0 ? EMPTY_PROPERTY_ARRAY : getPropertiesArray(shape));
    }

    @TruffleBoundary
    private static String[] getEnumerablePropertyNamesArray(Shape shape) {
        assert shape.getPropertyCount() != 0;
        return getEnumerablePropertyNamesArray(getShapeData(shape), shape);
    }

    private static String[] getEnumerablePropertyNamesArray(JSShapeData shapeData, Shape shape) {
        String[] enumeratePropertyNames = shapeData.enumerablePropertyNames;
        if (enumeratePropertyNames == null) {
            enumeratePropertyNames = createEnumerablePropertyNamesArray(shape);
            shapeData.enumerablePropertyNames = enumeratePropertyNames;
        }
        return enumeratePropertyNames;
    }

    static UnmodifiableArrayList<String> getEnumerablePropertyNames(Shape shape) {
        return asUnmodifiableList(shape.getPropertyCount() == 0 ? EMPTY_STRING_ARRAY : getEnumerablePropertyNamesArray(shape));
    }

    @TruffleBoundary
    private static Property[] getPropertiesArrayIfHasEnumerablePropertyNames(Shape shape) {
        assert shape.getPropertyCount() != 0;
        JSShapeData shapeData = getShapeData(shape);
        if (getEnumerablePropertyNamesArray(shapeData, shape).length == 0) {
            return EMPTY_PROPERTY_ARRAY;
        } else {
            return getPropertiesArray(shapeData, shape);
        }
    }

    static UnmodifiableArrayList<Property> getPropertiesIfHasEnumerablePropertyNames(Shape shape) {
        return asUnmodifiableList(shape.getPropertyCount() == 0 ? EMPTY_PROPERTY_ARRAY : getPropertiesArrayIfHasEnumerablePropertyNames(shape));
    }

    private static <T> UnmodifiableArrayList<T> asUnmodifiableList(T[] array) {
        return new UnmodifiableArrayList<>(array);
    }

    private static final DebugCounter enumerablePropertyListAllocCount = DebugCounter.create("Enumerable property lists allocated");
    private static final DebugCounter propertyListAllocCount = DebugCounter.create("Property lists allocated");
}
