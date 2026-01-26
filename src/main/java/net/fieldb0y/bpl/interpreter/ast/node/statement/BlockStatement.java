package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

import java.util.List;

public record BlockStatement(List<Statement> statements) implements Statement {
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitBlockStatement(this);
    }

    @Override
    public String toString() {
        return "Block(statements=" + statements.size() + ")";
    }
}
