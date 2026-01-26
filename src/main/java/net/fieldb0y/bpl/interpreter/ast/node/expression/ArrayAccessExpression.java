package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

import java.util.List;

public record ArrayAccessExpression(Expression expression, List<Expression> indices) implements Expression {
    @Override
    public <T> T visit(AstVisitor<T> visitor) {
        return visitor.visitArrayAccessExpression(this);
    }
}
