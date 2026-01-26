package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;
import net.fieldb0y.bpl.interpreter.ast.node.expression.Expression;

public record VariableDeclaration(Boolean isConst, Token.Type type, String name, Expression initializer) implements Statement {
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitVariableDeclaration(this);
    }

    @Override
    public String toString() {
        return (isConst ? "const" : "") + " " + type + " " + name + (initializer != null ? " = " + initializer : "");
    }
}
