package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public class ContinueStatement implements Statement {
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitContinueStatement(this);
    }
}
