package com.oracle.truffle.js.builtins;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.js.builtins.TemporalDurationPrototypeBuiltinsFactory.JSTemporalDurationAbsNodeGen;
import com.oracle.truffle.js.builtins.TemporalDurationPrototypeBuiltinsFactory.JSTemporalDurationNegatedNodeGen;
import com.oracle.truffle.js.builtins.TemporalDurationPrototypeBuiltinsFactory.JSTemporalDurationToStringNodeGen;
import com.oracle.truffle.js.builtins.TemporalDurationPrototypeBuiltinsFactory.JSTemporalDurationValueOfNodeGen;
import com.oracle.truffle.js.builtins.TemporalDurationPrototypeBuiltinsFactory.JSTemporalDurationWithNodeGen;
import com.oracle.truffle.js.nodes.access.IsObjectNode;
import com.oracle.truffle.js.nodes.cast.JSToIntegerAsLongNode;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.nodes.function.JSBuiltinNode;
import com.oracle.truffle.js.nodes.function.JSFunctionCallNode;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.BuiltinEnum;
import com.oracle.truffle.js.runtime.builtins.JSTemporalDuration;
import com.oracle.truffle.js.runtime.builtins.JSTemporalDurationObject;

public class TemporalDurationPrototypeBuiltins extends JSBuiltinsContainer.SwitchEnum<TemporalDurationPrototypeBuiltins.TemporalDurationPrototype> {

    public static final JSBuiltinsContainer BUILTINS = new TemporalDurationPrototypeBuiltins();

    protected TemporalDurationPrototypeBuiltins() {
        super(JSTemporalDuration.PROTOTYPE_NAME, TemporalDurationPrototype.class);
    }

    public enum TemporalDurationPrototype implements BuiltinEnum<TemporalDurationPrototype> {
        with(1),
        negated(0),
        abs(0),
        toString(0),
        toJSON(0),
        valueOf(0);

        private final int length;

        TemporalDurationPrototype(int length) {
            this.length = length;
        }

        @Override
        public int getLength() {
            return length;
        }
    }

    @Override
    protected Object createNode(JSContext context, JSBuiltin builtin, boolean construct, boolean newTarget, TemporalDurationPrototype builtinEnum) {
        switch (builtinEnum) {
            case with:
                return JSTemporalDurationWithNodeGen.create(context, builtin, args().withThis().fixedArgs(1).createArgumentNodes(context));
            case negated:
                return JSTemporalDurationNegatedNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            case abs:
                return JSTemporalDurationAbsNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            case toString:
            case toJSON:
                return JSTemporalDurationToStringNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
            case valueOf:
                return JSTemporalDurationValueOfNodeGen.create(context, builtin, args().withThis().createArgumentNodes(context));
        }
        return null;
    }

    // 7.3.15
    public abstract static class JSTemporalDurationWith extends JSBuiltinNode {

        protected JSTemporalDurationWith(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization(limit = "3")
        protected DynamicObject with(DynamicObject thisObj, DynamicObject temporalDurationLike,
                                     @Cached("create()") IsObjectNode isObjectNode,
                                     @CachedLibrary("temporalDurationLike") DynamicObjectLibrary dol,
                                     @Cached("create()") JSToIntegerAsLongNode toInt,
                                     @Cached("createNew()") JSFunctionCallNode functionCallNode) {
            JSTemporalDurationObject duration = (JSTemporalDurationObject) thisObj;
            DynamicObject durationLike = JSTemporalDuration.toPartialDuration(temporalDurationLike,
                    getContext().getRealm(), isObjectNode, dol, toInt);

            try {
                long years = dol.getLongOrDefault(durationLike, JSTemporalDuration.YEARS, duration.getYears());
                long months = dol.getLongOrDefault(durationLike, JSTemporalDuration.MONTHS, duration.getMonths());
                long weeks = dol.getLongOrDefault(durationLike, JSTemporalDuration.WEEKS, duration.getWeeks());
                long days = dol.getLongOrDefault(durationLike, JSTemporalDuration.DAYS, duration.getDays());
                long hours = dol.getLongOrDefault(durationLike, JSTemporalDuration.HOURS, duration.getHours());
                long minutes = dol.getLongOrDefault(durationLike, JSTemporalDuration.MINUTES, duration.getMinutes());
                long seconds = dol.getLongOrDefault(durationLike, JSTemporalDuration.SECONDS, duration.getSeconds());
                long milliseconds = dol.getLongOrDefault(durationLike, JSTemporalDuration.MILLISECONDS, duration.getMilliseconds());
                long microseconds = dol.getLongOrDefault(durationLike, JSTemporalDuration.MICROSECONDS, duration.getMicroseconds());
                long nanoseconds = dol.getLongOrDefault(durationLike, JSTemporalDuration.NANOSECONDS, duration.getNanoseconds());
                return JSTemporalDuration.createTemporalDurationFromInstance(duration, years, months, weeks, days,
                        hours, minutes, seconds, milliseconds, microseconds, nanoseconds, getContext().getRealm(),
                        functionCallNode);
            } catch (UnexpectedResultException e) {
                throw Errors.createTypeError(e.getMessage());
            }
        }
    }

    // 7.3.16
    public abstract static class JSTemporalDurationNegated extends JSBuiltinNode {

        protected JSTemporalDurationNegated(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected DynamicObject negated(DynamicObject thisObj,
                                        @Cached("createNew()") JSFunctionCallNode callNode) {
            JSTemporalDurationObject duration = (JSTemporalDurationObject) thisObj;
            return JSTemporalDuration.createTemporalDurationFromInstance(duration,
                    -duration.getYears(), -duration.getMonths(), -duration.getWeeks(), -duration.getDays(),
                    -duration.getHours(), -duration.getMinutes(), -duration.getSeconds(), -duration.getMilliseconds(),
                    -duration.getMicroseconds(), -duration.getNanoseconds(), getContext().getRealm(), callNode);
        }
    }

    // 7.3.17
    public abstract static class JSTemporalDurationAbs extends JSBuiltinNode {

        protected JSTemporalDurationAbs(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected DynamicObject abs(DynamicObject thisObj,
                                    @Cached("createNew()") JSFunctionCallNode callNode) {
            JSTemporalDurationObject duration = (JSTemporalDurationObject) thisObj;
            return JSTemporalDuration.createTemporalDurationFromInstance(duration,
                    Math.abs(duration.getYears()), Math.abs(duration.getMonths()), Math.abs(duration.getWeeks()),
                    Math.abs(duration.getDays()), Math.abs(duration.getHours()), Math.abs(duration.getMinutes()),
                    Math.abs(duration.getSeconds()), Math.abs(duration.getMilliseconds()),
                    Math.abs(duration.getMicroseconds()), Math.abs(duration.getNanoseconds()),
                    getContext().getRealm(), callNode);
        }
    }

    // 7.3.23
    public abstract static class JSTemporalDurationToString extends JSBuiltinNode {

        protected JSTemporalDurationToString(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected String toString(DynamicObject thisObj) {
            JSTemporalDurationObject duration = (JSTemporalDurationObject) thisObj;
            return JSTemporalDuration.temporalDurationToString(duration);
        }
    }

    public abstract static class JSTemporalDurationValueOf extends JSBuiltinNode {

        protected JSTemporalDurationValueOf(JSContext context, JSBuiltin builtin) {
            super(context, builtin);
        }

        @Specialization
        protected Object valueOf(DynamicObject thisObj) {
            throw Errors.createTypeError("Not supported.");
        }
    }
}
