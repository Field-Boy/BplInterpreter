package net.fieldb0y.bpl.interpreter.environment.function;

import net.fieldb0y.bpl.interpreter.Token;

public record Parameter(String name, Token.Type type) {}
