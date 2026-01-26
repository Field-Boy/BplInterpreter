package net.fieldb0y.bpl.interpreter.environment.function.builtin;

import net.fieldb0y.bpl.interpreter.Interpreter;
import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.environment.function.Callable;
import net.fieldb0y.bpl.interpreter.environment.function.FunctionSignature;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.object.TypedObject;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

import java.util.List;
import java.util.function.BiFunction;

import static net.fieldb0y.bpl.interpreter.Token.Type.VOID_TYPE;

public class BuiltinFunction implements Callable {
    private final Token.Type returnType;
    private final FunctionSignature signature;
    private final BiFunction<Interpreter, List<TypedObject<?>>, TypedObject<?>> implementation;

    public BuiltinFunction(Token.Type returnType, FunctionSignature signature, BiFunction<Interpreter, List<TypedObject<?>>, TypedObject<?>> implementation) {
        this.returnType = returnType;
        this.signature = signature;
        this.implementation = implementation;
    }

    @Override
    public TypedObject<?> call(Interpreter interpreter, List<TypedObject<?>> args) {
        List<Token.Type> paramTypes = signature.paramTypes();
        if (args.size() != paramTypes.size())
            throw new RuntimeError("Expected " + paramTypes.size() + " arguments, got " + args.size());

        for (int i = 0; i < paramTypes.size(); i++){
            Token.Type paramType = paramTypes.get(i);
            TypedObject<?> arg = args.get(i);

            if (!BplUtils.canAssign(paramType, arg))
                throw getInvalidArgError(i, paramType, BplUtils.toTokenType(arg));
        }

        TypedObject<?> returnValue = implementation.apply(interpreter, args);
        if (returnType != VOID_TYPE && !BplUtils.canAssign(returnType, returnValue))
            throw new RuntimeError("Invalid type of return value in builtin function '" + signature.name() + "'. Expected '" + returnType + "', but got '" + BplUtils.toTokenType(returnValue) + "'");
        return returnValue;
    }

    private RuntimeError getInvalidArgError(int i, Token.Type paramType, Token.Type argType){
        throw new RuntimeError("Invalid" + i + " argument for function call '" + signature.name() + "'. " +
                "Expected '" + paramType + "' argument, but got " + argType);
    }

    @Override
    public FunctionSignature getSignature() {
        return signature;
    }

    public Token.Type getReturnType() {
        return returnType;
    }
}
