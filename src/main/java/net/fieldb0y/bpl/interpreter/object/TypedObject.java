package net.fieldb0y.bpl.interpreter.object;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

import java.util.Objects;

public class TypedObject<T> {
    protected T value;

    public TypedObject(T value){
        this.value = value;
    }

    public T get(){
        return value;
    }

    @SuppressWarnings("unchecked")
    public TypedObject<T> assign(TypedObject<?> val){
        if (isAssignable(val)){
            this.value = (T)value.getClass().cast(val.get());
            return this;
        }
        throw new RuntimeError("Cannot assign " + get() + " to " + val.get());
    }

    public boolean isAssignable(TypedObject<?> val){
        Class<?> valClass = val.getClass();
        return val.get() == null || valClass.isInstance(this);
    }

    public TypedObject<?> cast(Token.Type castType){
        try {
            return BplUtils.getTypedObject(castType, this);
        } catch (RuntimeError e){
            throw new RuntimeError("Unable to cast " + get() + " to '" + castType + "'");
        }
    }

    public static BoolObject equals(TypedObject<?> obj1, TypedObject<?> obj2){
        return new BoolObject(Objects.equals(obj1.get(), obj2.get()));
    }

    public static BoolObject notEquals(TypedObject<?> obj1, TypedObject<?> obj2){
        return new BoolObject(!Objects.equals(obj1.get(), obj2.get()));
    }

    @Override
    public String toString() {
        return get().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypedObject<?> that = (TypedObject<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
