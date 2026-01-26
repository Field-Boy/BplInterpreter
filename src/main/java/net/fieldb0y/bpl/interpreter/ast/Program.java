package net.fieldb0y.bpl.interpreter.ast;

import net.fieldb0y.bpl.interpreter.ast.node.AstNode;
import net.fieldb0y.bpl.interpreter.ast.node.statement.Statement;

import java.util.List;

public record Program(List<Statement> statements) implements AstNode {
    public <T> void visit(AstVisitor<T> visitor){
        visitor.visitProgram(this);
    }
}
