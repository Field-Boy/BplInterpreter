package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;
import net.fieldb0y.bpl.interpreter.ast.node.expression.Expression;

public record WhileStatement(Expression condition, Statement body) implements Statement {
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitWhileStatement(this);
    }
}
