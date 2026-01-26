package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public record BreakStatement() implements Statement{
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitBreakStatement(this);
    }
}
