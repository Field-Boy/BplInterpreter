package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public record IdentifierExpression(String name) implements Expression {
    @Override
    public <T> T visit(AstVisitor<T> visitor) {
        return visitor.visitIdentifierExpression(this);
    }

    @Override
    public String toString() {
        return "Identifier(" + name + ")";
    }
}
