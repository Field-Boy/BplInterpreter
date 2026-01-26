package net.fieldb0y.bpl.interpreter.utils;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.object.*;
import net.fieldb0y.bpl.interpreter.object.number.DoubleNumber;
import net.fieldb0y.bpl.interpreter.object.number.FloatNumber;
import net.fieldb0y.bpl.interpreter.object.number.IntegerNumber;
import net.fieldb0y.bpl.interpreter.object.number.LongNumber;

import java.util.List;

import static net.fieldb0y.bpl.interpreter.Token.Type.*;

public class BplUtils {
    public static boolean canAssign(Token.Type type, TypedObject<?> val){
        if (type == ANY_TYPE) return true;

        TypedObject<?> should;
        try {
            should = BplUtils.getTypedObject(type, val);
        } catch (RuntimeError e){
            return false;
        }
        return should.isAssignable(val);
    }

    public static Token.Type toTokenType(TypedObject<?> obj){
        return switch (obj){
            case null -> NULL;
            case IntegerNumber ignored -> INT_TYPE;
            case LongNumber ignored -> LONG_TYPE;
            case FloatNumber ignored -> FLOAT_TYPE;
            case DoubleNumber ignored -> DOUBLE_TYPE;
            case StringObject ignored -> STRING_TYPE;
            case CharObject ignored -> CHAR_TYPE;
            case BoolObject ignored -> BOOL_TYPE;
            case ArrayObject ignored -> ARRAY_TYPE;
            default -> {
                if (obj.get() instanceof List)
                    yield ARRAY_TYPE;
                throw new RuntimeError("Cannot recognize type of object '" + obj + "'");
            }
        };
    }

    public static <T> TypedObject<?> getTypedObject(Token.Type type, TypedObject<T> value) {
        if (type == ANY_TYPE) return value;

        try {
            T v = value.get();
            return switch (type) {
                case INT_TYPE -> new IntegerNumber(v != null ? ((Number)v).intValue() : null);
                case LONG_TYPE -> new LongNumber(v != null ? ((Number)v).longValue() : null);
                case FLOAT_TYPE -> new FloatNumber(v != null ? ((Number)v).floatValue() : null);
                case DOUBLE_TYPE -> new DoubleNumber(v != null ? ((Number)v).doubleValue() : null);
                case STRING_TYPE -> StringObject.stringify(value);
                case CHAR_TYPE -> new CharObject((Character)v);
                case BOOL_TYPE -> new BoolObject((Boolean)v);
                default -> throw new RuntimeError("Cannot recognize type of value '" + value + "'");
            };
        } catch (ClassCastException e){
            throw new RuntimeError("Type of value '" + value + "' is not " + type);
        }
    }

    public static TypedObject<?> getTypedObjectFromLiteral(Token.Type type, Object value){
        try {
            return switch (type) {
                case INT_NUMBER -> new IntegerNumber(((Number)value).intValue());
                case LONG_NUMBER -> new LongNumber(((Number)value).longValue());
                case FLOAT_NUMBER -> new FloatNumber(((Number)value).floatValue());
                case DOUBLE_NUMBER -> new DoubleNumber(((Number)value).doubleValue());
                case STRING -> new StringObject(value.toString());
                case CHAR -> new CharObject((Character)value);
                case BOOL -> new BoolObject((Boolean)value);
                case NULL -> new TypedObject<>(null);
                default -> throw new RuntimeError("Cannot recognize type of literal value '" + value + "'");
            };
        } catch (ClassCastException e){
            throw new RuntimeError("Type of literal value '" + value + "' is not " + type);
        }
    }

    public static TypedObject<?> getDefaultValue(Token.Type type){
        return switch (type){
            case INT_TYPE -> new IntegerNumber(0);
            case LONG_TYPE -> new LongNumber(0L);
            case FLOAT_TYPE -> new FloatNumber(0.0f);
            case DOUBLE_TYPE -> new DoubleNumber(0.0);
            case BOOL_TYPE -> new BoolObject(false);
            case STRING_TYPE -> new StringObject("");
            case CHAR_TYPE -> new CharObject(Character.MIN_VALUE);
            default -> new TypedObject<>(null);
        };
    }

    public static int getConversionCost(Token.Type from, Token.Type to){
        if (from == to) return 0;
        if (to == ANY_TYPE) return 1;

        if (from == INT_TYPE && to == LONG_TYPE) return 1;
        if (from == INT_TYPE && to == FLOAT_TYPE) return 2;
        if (from == INT_TYPE && to == DOUBLE_TYPE) return 1;

        if (from == LONG_TYPE && to == FLOAT_TYPE) return 3;
        if (from == LONG_TYPE && to == DOUBLE_TYPE) return 2;

        if (from == FLOAT_TYPE && to == DOUBLE_TYPE) return 1;

        return -1;
    }

    public static TypedObject<?> getNullObject(Token.Type type){
        return getTypedObject(type, null);
    }

    public static TypedObject<?> captureNull(Token.Type type, TypedObject<?> value) {
        return value == null ? getNullObject(type) : value;
    }

    public static boolean isNumberType(Token.Type type){
        return type == INT_TYPE || type == LONG_TYPE
                || type == FLOAT_TYPE || type == DOUBLE_TYPE;
    }
}
