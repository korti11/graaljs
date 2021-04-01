/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
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

#ifndef GRAAL_VALUE_H_
#define GRAAL_VALUE_H_

#include "graal_data.h"
#include "include/v8.h"

class GraalValue : public GraalData {
public:
    GraalValue(GraalIsolate* isolate, jobject java_object);
    static GraalValue* FromJavaObject(GraalIsolate* isolate, jobject java_object);
    static GraalValue* FromJavaObject(GraalIsolate* isolate, jobject java_object, bool create_new_local_ref);
    static GraalValue* FromJavaObject(GraalIsolate* isolate, jobject java_object, int type, bool use_shared_buffer);
    static GraalValue* FromJavaObject(GraalIsolate* isolate, jobject java_object, int type, bool use_shared_buffer, void* placement);
    virtual bool IsObject() const;
    virtual bool IsFunction() const;
    virtual bool IsExternal() const;
    virtual bool IsArray() const;
    virtual bool IsInt32() const;
    virtual bool IsUint32() const;
    virtual bool IsNumber() const;
    virtual bool IsBoolean() const;
    virtual bool IsArrayBuffer() const;
    virtual bool IsArrayBufferView() const;
    virtual bool IsDate() const;
    virtual bool IsRegExp() const;
    virtual bool IsUint8Array() const;
    virtual bool IsUint8ClampedArray() const;
    virtual bool IsInt8Array() const;
    virtual bool IsUint16Array() const;
    virtual bool IsInt16Array() const;
    virtual bool IsUint32Array() const;
    virtual bool IsInt32Array() const;
    virtual bool IsFloat32Array() const;
    virtual bool IsFloat64Array() const;
    virtual bool IsBigInt64Array() const;
    virtual bool IsBigUint64Array() const;
    virtual bool IsMap() const;
    virtual bool IsSet() const;
    virtual bool IsPromise() const;
    virtual bool IsProxy() const;
    virtual bool IsSymbol() const;
    virtual bool IsName() const;
    virtual bool IsNull() const;
    virtual bool IsUndefined() const;
    virtual bool IsTrue() const;
    virtual bool IsFalse() const;
    virtual bool IsDataView() const;
    virtual bool IsBigInt() const;
    bool IsNativeError() const;
    bool IsMapIterator() const;
    bool IsSetIterator() const;
    bool IsSharedArrayBuffer() const;
    bool IsArgumentsObject() const;
    bool IsBooleanObject() const;
    bool IsNumberObject() const;
    bool IsStringObject() const;
    bool IsSymbolObject() const;
    bool IsBigIntObject() const;
    bool IsWeakMap() const;
    bool IsWeakSet() const;
    bool IsAsyncFunction() const;
    bool IsGeneratorFunction() const;
    bool IsGeneratorObject() const;
    bool IsModuleNamespaceObject() const;
    int32_t Int32Value() const;
    uint32_t Uint32Value() const;
    int64_t IntegerValue() const;
    v8::Maybe<int64_t> IntegerValue(v8::Local<v8::Context> context) const;
    bool BooleanValue() const;
    double NumberValue() const;
    v8::Local<v8::Object> ToObject(v8::Isolate* isolate) const;
    v8::Local<v8::String> ToString(v8::Isolate* isolate) const;
    v8::Local<v8::Boolean> ToBoolean(v8::Isolate* isolate) const;
    v8::Local<v8::Integer> ToInteger(v8::Isolate* isolate) const;
    v8::Local<v8::Int32> ToInt32(v8::Isolate* isolate) const;
    v8::Local<v8::Uint32> ToUint32(v8::Isolate* isolate) const;
    v8::Local<v8::Number> ToNumber(v8::Isolate* isolate) const;
    v8::Local<v8::Uint32> ToArrayIndex() const;
    v8::Maybe<bool> Equals(v8::Local<v8::Value> that) const;
    bool StrictEquals(v8::Local<v8::Value> that) const;
    bool InstanceOf(v8::Local<v8::Object> object);
    v8::Local<v8::String> TypeOf(v8::Isolate* isolate);
};

#endif /* GRAAL_VALUE_H_ */
