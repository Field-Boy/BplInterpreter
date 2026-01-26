package net.fieldb0y.bpl.interpreter.object.number;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.object.BoolObject;
import net.fieldb0y.bpl.interpreter.object.CharObject;
import net.fieldb0y.bpl.interpreter.object.TypedObject;

import static net.fieldb0y.bpl.interpreter.Token.Type.*;

public abstract class TypedNumber<T extends Number> extends TypedObject<T> {
    public TypedNumber(T value) {
        super(value);
    }

    @Override
    public TypedObject<?> cast(Token.Type castType) {
        if (castType == CHAR_TYPE)
            return new CharObject((char)value.doubleValue());
        else if(castType == BOOL_TYPE){
            return new BoolObject(value.doubleValue() != 0.0);
        }
        return super.cast(castType);
    }

    @SuppressWarnings("unchecked")
    public TypedObject<T> assign(TypedObject<?> val) {
        if (val instanceof TypedNumber<?> tn){
            if (isWiderType(this, tn)){
                this.value = (T)tn.value;
                return this;
            }
        }
        throw new RuntimeError("Cannot assign " + get() + " to " + val.get());
    }

    @Override
    public boolean isAssignable(TypedObject<?> val) {
        if (val instanceof TypedNumber<?> tn)
            return isWiderType(this, tn);
        return val.get() == null;
    }

    public static TypedNumber<?> add(TypedNumber<?> n1, TypedNumber<?> n2){
        TypedNumber<?> wider = getWiderTypedNumber(n1, n2);
        return wider.instantiate(wider.add(n1.value, n2.value));
    }

    public static TypedNumber<?> subtract(TypedNumber<?> n1, TypedNumber<?> n2){
        TypedNumber<?> wider = getWiderTypedNumber(n1, n2);
        return wider.instantiate(wider.subtract(n1.value, n2.value));
    }

    public static TypedNumber<?> multiply(TypedNumber<?> n1, TypedNumber<?> n2){
        TypedNumber<?> wider = getWiderTypedNumber(n1, n2);
        return wider.instantiate(wider.multiply(n1.value, n2.value));
    }

    public static TypedNumber<?> divide(TypedNumber<?> n1, TypedNumber<?> n2){
        TypedNumber<?> wider = getWiderTypedNumber(n1, n2);
        return wider.instantiate(wider.divide(n1.value, n2.value));
    }

    public static TypedObject<?> modulo(TypedNumber<?> n1, TypedNumber<?> n2) {
        TypedNumber<?> wider = getWiderTypedNumber(n1, n2);
        return wider.instantiate(wider.modulo(n1.value, n2.value));
    }

    public static TypedNumber<?> increment(TypedNumber<?> n){
        return add(n, new IntegerNumber(1));
    }

    public static TypedNumber<?> decrement(TypedNumber<?> n){
        return subtract(n, new IntegerNumber(1));
    }

    public static BoolObject compare(TypedNumber<?> num1, String operator, TypedNumber<?> num2){
        double n1 = num1.value.doubleValue();
        double n2 = num2.value.doubleValue();

        boolean result = switch (operator){
            case ">" -> n1 > n2;
            case "<" -> n1 < n2;
            case ">=" -> n1 >= n2;
            case "<=" -> n1 <= n2;
            case "==" -> n1 == n2;
            case "!=" -> n1 != n2;
            default -> throw new RuntimeError("Invalid operator '" + operator + "' for comparing expression");
        };
        return new BoolObject(result);
    }

    public static <T extends Number> TypedNumber<T> opposite(TypedNumber<T> n){
        return n.instantiate(n.opposite(n.get()));
    }

    public static TypedNumber<?> getWiderTypedNumber(TypedNumber<?> n1, TypedNumber<?> n2){
        return isWiderType(n1, n2) ? n1 : n2;
    }

    public static boolean isWiderType(TypedNumber<?> n1, TypedNumber<?> n2){
        return n1.getNumericRank() >= n2.getNumericRank();
    }

    protected abstract T add(Number n1, Number n2);
    protected abstract T subtract(Number n1, Number n2);
    protected abstract T multiply(Number n1, Number n2);
    protected abstract T divide(Number n1, Number n2);
    protected abstract T modulo(Number n1, Number n2);
    protected abstract T opposite(Number n);

    public abstract TypedNumber<T> instantiate(Number value);
    public abstract int getNumericRank();
}
