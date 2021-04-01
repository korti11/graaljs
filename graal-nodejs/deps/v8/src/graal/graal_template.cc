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

#include "graal_function_template.h"
#include "graal_isolate.h"
#include "graal_template.h"
#include "graal_value.h"

GraalTemplate::GraalTemplate(GraalIsolate* isolate, jobject java_template) : GraalData(isolate, java_template) {
}

void GraalTemplate::Set(v8::Local<v8::Value> key, v8::Local<v8::Data> value, v8::PropertyAttribute attributes) {
    jobject java_key = reinterpret_cast<GraalValue*> (*key)->GetJavaObject();
    jobject java_value = reinterpret_cast<GraalValue*> (*value)->GetJavaObject();
    jint java_attributes = static_cast<jint> (attributes);
    JNI_CALL_VOID(Isolate(), GraalAccessMethod::template_set, GetJavaObject(), java_key, java_value, java_attributes);
}

void GraalTemplate::SetAccessorProperty(
        v8::Local<v8::Name> name,
        v8::Local<v8::FunctionTemplate> getter,
        v8::Local<v8::FunctionTemplate> setter,
        v8::PropertyAttribute attributes) {
    jobject java_name = reinterpret_cast<GraalValue*> (*name)->GetJavaObject();
    jobject java_getter = getter.IsEmpty() ? nullptr : reinterpret_cast<GraalFunctionTemplate*> (*getter)->GetJavaObject();
    jobject java_setter = setter.IsEmpty() ? nullptr : reinterpret_cast<GraalFunctionTemplate*> (*setter)->GetJavaObject();
    jint java_attributes = static_cast<jint> (attributes);
    JNI_CALL_VOID(Isolate(), GraalAccessMethod::template_set_accessor_property, GetJavaObject(), java_name, java_getter, java_setter, java_attributes);
}
