package net.fieldb0y.bpl.interpreter.exception.helper;

import net.fieldb0y.bpl.interpreter.object.TypedObject;

public class ReturnException extends HelperException {
    private final TypedObject<?> value;

    public ReturnException(TypedObject<?> value){
        this.value = value;
    }

    public TypedObject<?> getValue() {
        return value;
    }
}
