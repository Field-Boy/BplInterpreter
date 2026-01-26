package net.fieldb0y.bpl.interpreter.environment.variable;

import net.fieldb0y.bpl.interpreter.object.TypedObject;

public class Variable<T> {
    private TypedObject<T> value;
    private final boolean isConst;

    public Variable(){
        this(null, false);
    }

    public Variable(boolean isConst){
        this(null, isConst);
    }

    public Variable(TypedObject<T> value){
        this(value, false);
    }

    public Variable(TypedObject<T> value, boolean isConst){
        this.value = value;
        this.isConst = isConst;
    }

    public TypedObject<T> assign(TypedObject<?> newValue){
        return value.assign(newValue);
    }

    public boolean isConst(){
        return isConst;
    }

    public TypedObject<T> getValue() {
        return value;
    }
}
