package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public record BinaryExpression(Expression left, Token operator, Expression right) implements Expression {
    @Override
    public <T> T visit(AstVisitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }

    @Override
    public String toString() {
        return "Binary(" + left + " " + operator.lexeme() + " " + right + ")";
    }
}
