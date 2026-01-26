package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

import java.util.List;

public record ArrayLiteralExpression(List<Expression> elements) implements Expression {
    @Override
    public <T> T visit(AstVisitor<T> visitor) {
        return visitor.visitArrayLiteralExpression(this);
    }
}
