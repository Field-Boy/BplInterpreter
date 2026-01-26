package net.fieldb0y.bpl.interpreter.environment;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.environment.function.Callable;
import net.fieldb0y.bpl.interpreter.environment.function.FunctionSignature;
import net.fieldb0y.bpl.interpreter.environment.variable.Variable;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.object.TypedObject;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

import java.util.*;

public class Environment {
    private final Map<String, Variable<?>> variables = new HashMap<>();
    private final Map<FunctionSignature, Callable> functions = new HashMap<>();
    private final Environment parent;

    public Environment(){
        this(null);
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void defineVar(String name, Variable<?> variable){
        if (!variables.containsKey(name)){
            variables.put(name, variable);
        } else throw new RuntimeError("Variable '" + name + "' already defined");
    }

    public void defineFunction(Callable function){
        FunctionSignature signature = function.getSignature();

        if (!functions.containsKey(signature))
            functions.put(signature, function);
        else throw new RuntimeError("Function '" + signature.name() + "' already defined");
    }

    public <T extends TypedObject<?>> void setVar(String name, T value){
        if (variables.containsKey(name)){
            Variable<?> var = variables.get(name);
            if (var.isConst())
                throw new RuntimeError("Cannot assign const variable '" + name + "'");

            TypedObject<?> varVal = var.getValue();
            if (!varVal.getClass().equals(value.getClass()))
                throw new RuntimeError("Cannot assign " + value.getClass().getSimpleName() + " to variable '" + name + "' of type " + varVal.getClass().getSimpleName());
            var.assign(value);
        } else if (parent != null) {
            parent.setVar(name, value);
        } else throw new RuntimeError("Cannot assign " + value + " to undefined variable '" + name + "'");
    }

    public Variable<?> getVar(String name){
        if (variables.containsKey(name))
            return variables.get(name);
        else if(parent != null)
            return parent.getVar(name);
        throw new RuntimeError("Undefined variable '" + name + "'");
    }

    public Callable getFunction(String name, List<TypedObject<?>> args){
        List<Token.Type> argTypes = new ArrayList<>();
        args.forEach(a -> argTypes.add(BplUtils.toTokenType(a)));

        FunctionSignature signature = new FunctionSignature(name, argTypes);
        Callable func = functions.get(signature);
        if (func != null) {
            return func;
        } else {
            func = findCompatibleFunc(name, argTypes);
            if (func != null)
                return func;
            else if(parent != null)
                return parent.getFunction(name, args);
        }
        throw new RuntimeError("Undefined function '" + name + "' with args: " + args);
    }

    private Callable findCompatibleFunc(String name, List<Token.Type> argTypes){
        Callable bestMatch = null;
        int bestMatchScore = Integer.MAX_VALUE;

        for (Map.Entry<FunctionSignature, Callable> entry : functions.entrySet()){
            FunctionSignature signature = entry.getKey();

            if (!name.equals(signature.name()) || signature.paramTypes().size() != argTypes.size())
                continue;

            int conversionScore = 0;
            boolean isCompatible = true;

            for(int i = 0; i < argTypes.size(); i++){
                Token.Type argType = argTypes.get(i);
                Token.Type paramType = signature.paramTypes().get(i);

                int argConversionScore = BplUtils.getConversionCost(argType, paramType);
                if (argConversionScore != -1)
                    conversionScore += argConversionScore;
                else {
                    isCompatible = false;
                    break;
                }
            }

            if (isCompatible && conversionScore < bestMatchScore){
                bestMatch = entry.getValue();
                bestMatchScore = conversionScore;
            }
        }
        return bestMatch;
    }

    public TypedObject<?> getVarValue(String name){
        return getVar(name).getValue();
    }

    public Environment getParent() {
        return parent;
    }
}
