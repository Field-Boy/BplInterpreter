package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public record CastExpression(Token.Type castType, Expression expression) implements Expression {
    @Override
    public <T> T visit(AstVisitor<T> visitor) {
        return visitor.visitCastExpression(this);
    }
}
