package net.fieldb0y.bpl.interpreter.ast.node.expression;

import net.fieldb0y.bpl.interpreter.ast.node.AstNode;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;

public interface Expression extends AstNode {
    <T> T visit(AstVisitor<T> visitor);
}
