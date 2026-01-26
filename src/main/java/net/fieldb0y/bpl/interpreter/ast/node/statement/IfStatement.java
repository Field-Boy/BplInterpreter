package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;
import net.fieldb0y.bpl.interpreter.ast.node.expression.Expression;

public record IfStatement(Expression condition, Statement thenBranch, Statement elseBranch) implements Statement {
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitIfStatement(this);
    }
}
