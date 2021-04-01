package com.oracle.truffle.js.runtime.util;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.js.nodes.access.IsObjectNode;
import com.oracle.truffle.js.nodes.cast.JSToBooleanNode;
import com.oracle.truffle.js.nodes.cast.JSToDoubleNode;
import com.oracle.truffle.js.nodes.cast.JSToIntegerAsLongNode;
import com.oracle.truffle.js.nodes.cast.JSToNumberNode;
import com.oracle.truffle.js.nodes.cast.JSToStringNode;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.JSRuntime;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.JSObjectUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TemporalUtil {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String MONTH_CODE = "monthCode";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";
    private static final String MILLISECOND = "millisecond";
    private static final String MICROSECOND = "microsecond";
    private static final String NANOSECOND = "nanosecond";

    private static final String YEARS = "years";
    private static final String MONTHS = "months";
    private static final String WEEKS = "weeks";
    private static final String DAYS = "days";
    private static final String HOURS = "hours";
    private static final String MINUTES = "minutes";
    private static final String SECONDS = "seconds";
    private static final String MILLISECONDS = "milliseconds";
    private static final String MICROSECONDS = "microseconds";
    private static final String NANOSECONDS = "nanoseconds";

    private static final String OFFSET = "offset";
    private static final String ERA = "era";
    private static final String ERA_YEAR = "eraYear";

    private static final Function<Object, Object> toIntegerOrInfinity = (argument-> (long) JSToDoubleNode.create().executeDouble(argument));
    private static final Function<Object, Object> toPositiveIntegerOrInfinity = TemporalUtil::toPositiveIntegerOrInfinity;
    private static final Function<Object, Object> toInteger = (argument -> (long) JSToIntegerAsLongNode.create().executeLong(argument));
    private static final Function<Object, Object> toString = (argument -> JSToStringNode.create().executeString(argument));

    private static final Set<String> singularUnits = toSet(YEAR, MONTH, DAY, HOUR, MINUTE, SECOND,
            MILLISECOND, MICROSECOND, NANOSECOND);
    private static final Set<String> pluralUnits = toSet(YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS,
            MILLISECONDS, MICROSECONDS, NANOSECONDS);
    private static final Map<String, String> pluralToSingular = toMap(
            new String[] {YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS},
            new String[] {YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND, MICROSECOND, NANOSECOND}
    );
    private static final Map<String, String> singularToPlural = toMap(
            new String[] {YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND, MICROSECOND, NANOSECOND},
            new String[] {YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS}
    );
    private static final Map<String, Function<Object, Object>> temporalFieldConversion = toMap(
            new String[]{YEAR, MONTH, MONTH_CODE, DAY, HOUR, MINUTE, SECOND, MILLISECOND, MICROSECOND, NANOSECOND, YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS, OFFSET, ERA, ERA_YEAR},
            new Function[]{toIntegerOrInfinity, toPositiveIntegerOrInfinity, toString, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity,
                    toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toIntegerOrInfinity, toString, toString, toInteger}
    );
    private static final Map<String, Object> temporalFieldDefaults = toMap(
            new String[] { YEAR, MONTH, MONTH_CODE, DAY, HOUR, MINUTE, SECOND, MILLISECOND, MICROSECOND, NANOSECOND, YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS, OFFSET, ERA, ERA_YEAR },
            new Object[] { null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null, null, null }
    );

    // 13.1
    public static DynamicObject normalizeOptionsObject(DynamicObject options,
                                                       JSRealm realm,
                                                       IsObjectNode isObject) {
        if(JSRuntime.isNullOrUndefined(options)) {
            DynamicObject newOptions = JSObjectUtil.createOrdinaryPrototypeObject(realm);
            return  newOptions;
        }
        if(isObject.executeBoolean(options)) {
            return options;
        }
        throw Errors.createTypeError("Options is not undefined and not an object.");
    }

    // 13.2
    public static Object getOptions(DynamicObject options, String property, String type,
                                    Set<Object> values, Object fallback,
                                    DynamicObjectLibrary dol, IsObjectNode isObjectNode,
                                    JSToBooleanNode toBoolean, JSToStringNode toString) {
        assert isObjectNode.executeBoolean(options);
        Object value = dol.getOrDefault(options, property, null);
        if(value == null) {
            return fallback;
        }
        assert type.equals("boolean") || type.equals("string");
        if(type.equals("boolean")) {
            value = toBoolean.executeBoolean(value);
        } else if(type.equals("string")) {
            value = toString.executeString(value);
        }
        if(values != null && !values.contains(value)) {
            throw Errors.createRangeError(
                    String.format("Given options value: %s is not contained in values: %s", value, values));
        }
        return value;
    }

    // 13.3
    public static double defaultNumberOptions(Object value, double minimum, double maximum, double fallback,
                                             JSToNumberNode toNumber) {
        if (value == null) {
            return fallback;
        }
        double numberValue = toNumber.executeNumber(value).doubleValue();
        if (numberValue == Double.NaN || numberValue < minimum || numberValue > maximum) {
            throw Errors.createRangeError("Numeric value out of range.");
        }
        return Math.floor(numberValue);
    }

    // 13.4
    public static double getNumberOption(DynamicObject options, String property, double minimum, double maximum,
                                   double fallback, DynamicObjectLibrary dol, IsObjectNode isObject,
                                   JSToNumberNode numberNode) {
        assert isObject.executeBoolean(options);
        Object value = dol.getOrDefault(options, property, null);
        return defaultNumberOptions(value, minimum, maximum, fallback, numberNode);
    }

    // 13.5
    public static Object getStringOrNumberOption(DynamicObject options, String property, Set<String> stringValues,
                                                 double minimum, double maximum, Object fallback, DynamicObjectLibrary dol,
                                                 IsObjectNode isObject, JSToNumberNode toNumber, JSToStringNode toString) {
        assert isObject.executeBoolean(options);
        Object value = dol.getOrDefault(options, property, null);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number) {
            double numberValue = toNumber.executeNumber(value).doubleValue();
            if (numberValue == Double.NaN || numberValue < minimum || numberValue > maximum) {
                throw Errors.createRangeError("Numeric value out of range.");
            }
            return Math.floor(numberValue);
        }
        value = toString.executeString(value);
        if (stringValues != null && !stringValues.contains(value)) {
            throw Errors.createRangeError("Given string value is not in string values");
        }
        return value;
    }

    // 13.8
    public static String toTemporalOverflow(DynamicObject normalizedOptions,
                                            DynamicObjectLibrary dol,
                                            IsObjectNode isObjectNode,
                                            JSToBooleanNode toBoolean, JSToStringNode toString) {
        Set<Object> values = new HashSet<>();
        values.add("constrain");
        values.add("reject");
        return (String) getOptions(normalizedOptions, "overflow", "string", values, "constrain", dol,
                isObjectNode, toBoolean, toString);
    }

    // 13.11
    public static String toTemporalRoundingMode(DynamicObject normalizedOptions, String fallback,
                                                DynamicObjectLibrary dol, IsObjectNode isObjectNode,
                                                JSToBooleanNode toBoolean, JSToStringNode toString) {
        return (String) getOptions(normalizedOptions, "roundingMode", "string", toSet("ceil", "floor", "trunc", "nearest"),
                fallback, dol, isObjectNode, toBoolean, toString);
    }

    // 13.17
    public static double toTemporalRoundingIncrement(DynamicObject normalizedOptions, Double dividend, boolean inclusive,
                                                     DynamicObjectLibrary dol, IsObjectNode isObject,
                                                     JSToNumberNode toNumber) {
        double maximum;
        if(dividend == null) {
            maximum = Double.POSITIVE_INFINITY;
        } else if (inclusive) {
            maximum = dividend;
        } else if (dividend > 1) {
            maximum = dividend - 1;
        } else {
            maximum = 1;
        }
        double increment = getNumberOption(normalizedOptions, "roundingIncrement", 1, maximum, 1,
                dol, isObject, toNumber);
        if (dividend != null && dividend % increment != 0) {
            throw Errors.createRangeError("Increment out of range.");
        }
        return increment;
    }

    // 13.19
    public static DynamicObject toSecondsStringPrecision(DynamicObject normalizedOptions, DynamicObjectLibrary dol,
                                                         IsObjectNode isObject, JSToBooleanNode toBoolean, JSToStringNode toString,
                                                         JSToNumberNode toNumberNode, JSRealm realm) {
        String smallestUnit = (String) getOptions(normalizedOptions, "smallestUnit", "string", toSet(MINUTE, SECOND,
                MILLISECOND, MICROSECOND, NANOSECOND, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS, NANOSECONDS),
                null, dol, isObject, toBoolean, toString);
        if (pluralUnits.contains(smallestUnit)) {
            smallestUnit = pluralToSingular.get(smallestUnit);
        }
        DynamicObject record = JSObjectUtil.createOrdinaryPrototypeObject(realm);
        if(smallestUnit != null) {
            if (smallestUnit.equals(MINUTE)) {
                JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", MINUTE);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", MINUTE);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", 1);
                return record;
            }
            if (smallestUnit.equals(SECOND)) {
                JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", 0);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", SECOND);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", 1);
                return record;
            }
            if (smallestUnit.equals(MILLISECOND)) {
                JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", 3);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", MILLISECOND);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", 1);
                return record;
            }
            if (smallestUnit.equals(MICROSECOND)) {
                JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", 6);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", MICROSECOND);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", 1);
                return record;
            }
            if (smallestUnit.equals(NANOSECOND)) {
                JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", 9);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", NANOSECOND);
                JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", 1);
                return record;
            }
        }
        assert smallestUnit == null;
        Object digits = getStringOrNumberOption(normalizedOptions, "fractionalSecondDigits",
                Collections.singleton("auto"), 0, 9, "auto", dol, isObject, toNumberNode, toString);
        if (digits.equals("auto")) {
            JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", "auto");
            JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", NANOSECOND);
            JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", 1);
            return record;
        }
        if (digits.equals(0)) {
            JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", 0);
            JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", SECOND);
            JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", 1);
            return record;
        }
        if (digits.equals(1) || digits.equals(2) || digits.equals(3)) {
            JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", digits);
            JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", MILLISECOND);
            JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", Math.pow(10, 3 - (long) digits));
            return record;
        }
        if (digits.equals(4) || digits.equals(5) || digits.equals(6)) {
            JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", digits);
            JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", MICROSECOND);
            JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", Math.pow(10, 6 - (long) digits));
            return record;
        }
        assert digits.equals(7) || digits.equals(8) || digits.equals(9);
        JSObjectUtil.putDataProperty(realm.getContext(), record, "precision", digits);
        JSObjectUtil.putDataProperty(realm.getContext(), record, "unit", NANOSECOND);
        JSObjectUtil.putDataProperty(realm.getContext(), record, "increment", Math.pow(10, 9 - (long) digits));
        return record;
    }

    // 13.21
    public static String toLargestTemporalUnit(DynamicObject normalizedOptions, Set<String> disallowedUnits, String defaultUnit,
                                               DynamicObjectLibrary dol, IsObjectNode isObjectNode,
                                               JSToBooleanNode toBoolean, JSToStringNode toString) {
        assert !disallowedUnits.contains(defaultUnit) && !disallowedUnits.contains("auto");
        String largestUnit = (String) getOptions(normalizedOptions, "largestUnit", "string", toSet(
                "auto", "year", "years", "month", "months", "week", "weeks", "day", "days", "hour",
                "hours", "minute", "minutes", "second", "seconds", "millisecond", "milliseconds", "microsecond",
                "microseconds", "nanosecond", "nanoseconds"), "auto", dol, isObjectNode, toBoolean, toString);
        if (largestUnit.equals("auto")) {
            return defaultUnit;
        }
        if (singularUnits.contains(largestUnit)) {
            largestUnit = singularToPlural.get(largestUnit);
        }
        if (disallowedUnits.contains(largestUnit)) {
            throw Errors.createRangeError("Largest unit is not allowed.");
        }
        return largestUnit;
    }

    // 13.22
    public static String toSmallestTemporalUnit(DynamicObject normalizedOptions, Set<String> disallowedUnits,
                                                DynamicObjectLibrary dol, IsObjectNode isObjectNode,
                                                JSToBooleanNode toBoolean, JSToStringNode toString) {
        String smallestUnit = (String) getOptions(normalizedOptions, "smallestUnit", "string", toSet("day", "days", "hour",
                "hours", "minute", "minutes", "second", "seconds", "millisecond", "milliseconds", "microsecond",
                "microseconds", "nanosecond", "nanoseconds"), null, dol, isObjectNode, toBoolean, toString);
        if (smallestUnit == null) {
            throw Errors.createRangeError("No smallest unit found.");
        }
        if(pluralUnits.contains(smallestUnit)) {
            smallestUnit = pluralToSingular.get(smallestUnit);
        }
        if(disallowedUnits.contains(smallestUnit)) {
            throw Errors.createRangeError("Smallest unit not allowed.");
        }
        return smallestUnit;
    }

    // 13.23
    public static String toSmallestTemporalDurationUnit(DynamicObject normalizedOptions, String fallback, Set<String> disallowedUnits,
                                                        DynamicObjectLibrary dol, IsObjectNode isObjectNode,
                                                        JSToBooleanNode toBoolean, JSToStringNode toString) {
        String smallestUnit = (String) getOptions(normalizedOptions, "smallestUnit", "string", toSet(
                "year", "years", "month", "months", "week", "weeks", "day", "days", "hour",
                "hours", "minute", "minutes", "second", "seconds", "millisecond", "milliseconds", "microsecond",
                "microseconds", "nanosecond", "nanoseconds"), fallback, dol , isObjectNode, toBoolean, toString);
        if (singularUnits.contains(smallestUnit)) {
            smallestUnit = singularToPlural.get(smallestUnit);
        }
        if (disallowedUnits.contains(smallestUnit)) {
            throw Errors.createRangeError("Smallest unit not allowed.");
        }
        return smallestUnit;
    }

    // 13.24
    public static String toTemporalDurationTotalUnit(DynamicObject normalizedOptions,
                                                     DynamicObjectLibrary dol, IsObjectNode isObjectNode,
                                                     JSToBooleanNode toBoolean, JSToStringNode toString) {
        String unit = (String) getOptions(normalizedOptions, "unit", "string", toSet(YEARS, YEAR, MONTHS, MONTH, WEEKS,
                DAYS, DAY, HOURS, HOUR, MINUTES, MINUTE, SECONDS, SECOND, MILLISECONDS, MILLISECOND, MICROSECONDS,
                MICROSECOND, NANOSECONDS, NANOSECONDS),
                null, dol, isObjectNode, toBoolean, toString);
        if (singularUnits.contains(unit)) {
            unit = singularToPlural.get(unit);
        }
        return unit;
    }

    // 13.26
    public static DynamicObject toRelativeTemporalObject(DynamicObject options, IsObjectNode isObject,
                                                         DynamicObjectLibrary dol) {
        assert isObject.executeBoolean(options);
        DynamicObject value = (DynamicObject) dol.getOrDefault(options, "relativeTo", null);
        if (value == null) {
            return value;
        }
        // TODO: https://tc39.es/proposal-temporal/#sec-temporal-torelativetemporalobject
        return null;
    }

    // 13.27
    public static void validateTemporalUnitRange(String largestUnit, String smallestUnit) {
        if(smallestUnit.equals(YEARS) && !largestUnit.equals(YEARS)) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if (smallestUnit.equals(MONTHS) && !largestUnit.equals(YEARS) && !largestUnit.equals(MONTHS)) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if (smallestUnit.equals(WEEKS) && !largestUnit.equals(YEARS) && !largestUnit.equals(MONTHS) && !largestUnit.equals(WEEKS)) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if (smallestUnit.equals(DAYS) && !largestUnit.equals(YEARS) && !largestUnit.equals(MONTHS) && !largestUnit.equals(WEEKS) && !largestUnit.equals(DAYS)) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if (smallestUnit.equals(HOURS) && !largestUnit.equals(YEARS) && !largestUnit.equals(MONTHS) && !largestUnit.equals(WEEKS) && !largestUnit.equals(DAYS) && !largestUnit.equals(HOURS)) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if (smallestUnit.equals(MINUTES) && (largestUnit.equals(SECONDS) || largestUnit.equals(MILLISECONDS) || largestUnit.equals(MICROSECONDS) || largestUnit.equals(NANOSECONDS))) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if(smallestUnit.equals(SECONDS) && (largestUnit.equals(MILLISECONDS) || largestUnit.equals(MICROSECONDS) || largestUnit.equals(NANOSECONDS))) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if(smallestUnit.equals(MILLISECONDS) && (largestUnit.equals(MICROSECONDS) || largestUnit.equals(NANOSECONDS))) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
        if(smallestUnit.equals(MICROSECONDS) && largestUnit.equals(NANOSECONDS)) {
            throw Errors.createRangeError("Smallest unit is out of range.");
        }
    }

    // 13.28
    public static String largerOfTwoTemporalDurationUnits(String u1, String u2) {
        if(u1.equals(YEARS) || u2.equals(YEARS)) {
            return YEARS;
        }
        if(u1.equals(MONTHS) || u2.equals(MONTHS)) {
            return MONTHS;
        }
        if(u1.equals(WEEKS) || u2.equals(WEEKS)) {
            return WEEKS;
        }
        if(u1.equals(DAYS) || u2.equals(DAYS)) {
            return DAYS;
        }
        if(u1.equals(HOURS) || u2.equals(HOURS)) {
            return HOURS;
        }
        if(u1.equals(MINUTES) || u2.equals(MINUTES)) {
            return MINUTES;
        }
        if(u1.equals(SECONDS) || u2.equals(SECONDS)) {
            return SECONDS;
        }
        if(u1.equals(MILLISECONDS) || u2.equals(MILLISECONDS)) {
            return MILLISECONDS;
        }
        if(u1.equals(MICROSECONDS) || u2.equals(MICROSECONDS)) {
            return MICROSECONDS;
        }
        return NANOSECONDS;
    }

    // 13.31
    public static Long maximumTemporalDurationRoundingIncrement(String unit) {
        if (unit.equals(YEARS) || unit.equals(MONTHS) || unit.equals(WEEKS) || unit.equals(DAYS)) {
            return null;
        }
        if (unit.equals(HOURS)) {
            return 24L;
        }
        if (unit.equals(MINUTES) || unit.equals(SECONDS)) {
            return 60L;
        }
        assert unit.equals(MILLISECONDS) || unit.equals(MICROSECONDS) || unit.equals(NANOSECONDS);
        return 1000L;
    }

    // 13.32
    public static String formatSecondsStringPart(long second, long millisecond, long microsecond, long nanosecond,
                                                 Object precision) {
        if (precision.equals(MINUTE)) {
            return "";
        }
        String secondString = String.format("%1$2d", second).replace(" ", "0");
        long fraction = (millisecond * 1_000_000) + (microsecond * 1_000) + nanosecond;
        String fractionString = "";
        if (precision.equals("auto")) {
            if (fraction == 0) {
                return secondString;
            }
            fractionString = fractionString.concat(String.format("%1$3d", millisecond).replace(" ", "0"));
            fractionString = fractionString.concat(String.format("%1$3d", microsecond).replace(" ", "0"));
            fractionString = fractionString.concat(String.format("%1$3d", nanosecond).replace(" ", "0"));
            fractionString = longestSubstring(fractionString);
        } else {
            if (precision.equals(0)) {
                return secondString;
            }
            fractionString = fractionString.concat(String.format("%1$3d", millisecond).replace(" ", "0"));
            fractionString = fractionString.concat(String.format("%1$3d", microsecond).replace(" ", "0"));
            fractionString = fractionString.concat(String.format("%1$3d", nanosecond).replace(" ", "0"));
            fractionString = fractionString.substring(0, (int) precision);
        }
        return secondString.concat(".").concat(fractionString);
    }

    private static String longestSubstring(String s) {
        while (s.endsWith("0")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    // 13.33
    public static double nonNegativeModulo(double x, double y) {
        double result = x % y;
        if (result == -0) {
            return 0;
        }
        if (result < 0) {
            result = result + y;
        }
        return result;
    }

    // 13.34
    public static long sign(long n) {
        if (n > 0) {
            return 1;
        }
        if (n < 0) {
            return -1;
        }
        return n;
    }

    // 13.35
    public static long constraintToRange(long x, long minimum, long maximum) {
        return Math.min(Math.max(x, minimum), maximum);
    }

    // 13.36
    public static double roundHalfAwayFromZero(double x) {
        return Math.round(x);
    }

    // 13.37
    public static double roundNumberToIncrement(double x, double increment, String roundingMode) {
        assert roundingMode.equals("ceil") || roundingMode.equals("floor") || roundingMode.equals("trunc")
                || roundingMode.equals("nearest");
        double quotient = x / increment;
        double rounded;
        if (roundingMode.equals("ceil")) {
            rounded = -Math.floor(-quotient);
        } else if (roundingMode.equals("floor")) {
            rounded = Math.floor(quotient);
        } else if (roundingMode.equals("trunc")) {
            rounded = (long) quotient;
        } else {
            rounded = roundHalfAwayFromZero(x);
        }
        return rounded * increment;
    }

    // 13.51
    public static long toPositiveIntegerOrInfinity(Object argument) {
        double integer = JSToDoubleNode.create().executeDouble(argument);
        if (integer == Double.NEGATIVE_INFINITY) {
            throw Errors.createRangeError("Integer should not be negative infinity.");
        }
        if (integer <= 0) {
            throw Errors.createRangeError("Integer should be positive.");
        }
        return (long) integer;
    }

    // 13.52
    public static DynamicObject prepareTemporalFields(DynamicObject fields, Set<String> fieldNames, Set<String> requiredFields, JSRealm realm, IsObjectNode isObjectNode, DynamicObjectLibrary dol) {
        assert isObjectNode.executeBoolean(fields);
        DynamicObject result = JSObjectUtil.createOrdinaryPrototypeObject(realm);
        for (String property : fieldNames) {
            Object value = dol.getOrDefault(fields, property, null);
            if (value == null) {
                if (requiredFields.contains(property)) {
                    throw Errors.createTypeError(String.format("Property %s is required.", property));
                } else {
                    if (temporalFieldDefaults.containsKey(property)) {
                        value = temporalFieldDefaults.get(property);
                    }
                }
            } else {
                if (temporalFieldConversion.containsKey(property)) {
                    Function<Object, Object> conversion = temporalFieldConversion.get(property);
                    value = conversion.apply(value);
                }
            }
            JSObjectUtil.putDataProperty(realm.getContext(), result, property, value);
        }
        return result;
    }

    public static <T> Set<T> toSet(T... values) {
        return Arrays.stream(values).collect(Collectors.toSet());
    }

    private static <T,I> Map<T,I> toMap(T[] keys, I[] values) {
        Map<T, I> map = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }
}
