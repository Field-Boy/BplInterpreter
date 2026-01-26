package net.fieldb0y.bpl.interpreter.environment.function;

import net.fieldb0y.bpl.interpreter.Interpreter;
import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.node.statement.FunctionDeclaration;
import net.fieldb0y.bpl.interpreter.environment.Environment;
import net.fieldb0y.bpl.interpreter.environment.variable.Variable;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.exception.helper.ReturnException;
import net.fieldb0y.bpl.interpreter.object.TypedObject;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

import java.util.ArrayList;
import java.util.List;

import static net.fieldb0y.bpl.interpreter.Token.Type.VOID_TYPE;

public class Function implements Callable {
    protected final FunctionDeclaration decl;
    private final Environment closure;

    public Function(FunctionDeclaration decl, Environment closure) {
        this.decl = decl;
        this.closure = closure;
    }

    @Override
    public TypedObject<?> call(Interpreter interpreter, List<TypedObject<?>> args){
        List<Parameter> params = decl.params();
        int paramsCount = params.size();
        int argsCount = args.size();

        if (argsCount != paramsCount)
            throw new RuntimeError("Expected " + paramsCount + " arguments, got " + argsCount);

        Environment env = new Environment(closure);
        for (int i = 0; i < paramsCount; i++){
            Parameter param = params.get(i);
            TypedObject<?> value = BplUtils.getTypedObject(param.type(), args.get(i));
            env.defineVar(param.name(), new Variable<>(BplUtils.captureNull(param.type(), value)));
        }

        try {
            interpreter.executeBlock(decl.body(), env);
        } catch (ReturnException e){
            TypedObject<?> retVal = e.getValue();
            if (BplUtils.canAssign(decl.returnType(), retVal))
                return e.getValue();
            throw new RuntimeError("Invalid type of return value in function '" + decl.name() + "'. Expected '" + decl.returnType() + "', but got '" + BplUtils.toTokenType(retVal) + "'");
        }

        if (decl.returnType() != VOID_TYPE)
            throw new RuntimeError("Expected 'return' in function declaration");
        return null;
    }

    @Override
    public FunctionSignature getSignature() {
        List<Token.Type> paramTypes = new ArrayList<>();
        decl.params().forEach(p -> paramTypes.add(p.type()));
        return new FunctionSignature(decl.name(), paramTypes);
    }

    public FunctionDeclaration getDeclaration() {
        return decl;
    }
}
