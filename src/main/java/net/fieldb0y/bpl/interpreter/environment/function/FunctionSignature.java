package net.fieldb0y.bpl.interpreter.environment.function;

import net.fieldb0y.bpl.interpreter.Token;

import java.util.List;

public record FunctionSignature(String name, List<Token.Type> paramTypes) {}
