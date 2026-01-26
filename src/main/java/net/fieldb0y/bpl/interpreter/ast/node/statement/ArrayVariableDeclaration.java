package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;
import net.fieldb0y.bpl.interpreter.ast.node.expression.Expression;

import java.util.List;

public record ArrayVariableDeclaration(boolean isConst, Token.Type type, String name, List<Expression> dimensions, Expression initializer) implements Statement {
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitArrayVariableDeclaration(this);
    }
}
