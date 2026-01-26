package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.ast.node.AstNode;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public interface Statement extends AstNode {
    <T> void visit(AstVisitor<T> visitor);
}
