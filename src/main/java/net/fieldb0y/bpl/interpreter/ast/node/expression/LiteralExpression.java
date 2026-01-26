package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public record LiteralExpression(Object value, Token.Type type) implements Expression {
    @Override
    public <R> R visit(AstVisitor<R> visitor) {
        return visitor.visitLiteralExpression(this);
    }

    @Override
    public String toString() {
        return "Literal(" + type + " : " + value + ")";
    }
}
