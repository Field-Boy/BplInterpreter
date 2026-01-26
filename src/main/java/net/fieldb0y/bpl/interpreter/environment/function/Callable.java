package net.fieldb0y.bpl.interpreter.environment.function;

import net.fieldb0y.bpl.interpreter.Interpreter;
import net.fieldb0y.bpl.interpreter.object.TypedObject;

import java.util.List;

public interface Callable {
    TypedObject<?> call(Interpreter interpreter, List<TypedObject<?>> args);
    FunctionSignature getSignature();
}
