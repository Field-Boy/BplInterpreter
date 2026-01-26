package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;
import net.fieldb0y.bpl.interpreter.object.TypedObject;

import java.util.List;

public record FunctionCall(Expression callee, List<Expression> args) implements Expression {
    @Override
    public <T> T visit(AstVisitor<T> visitor) {
        return visitor.visitFunctionCall(this);
    }
}
