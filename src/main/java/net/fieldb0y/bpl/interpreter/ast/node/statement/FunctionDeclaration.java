package net.fieldb0y.bpl.interpreter.ast.node.statement;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.AstVisitor;
import net.fieldb0y.bpl.interpreter.environment.function.Parameter;

import java.util.List;

public record FunctionDeclaration(Token.Type returnType, String name, List<Parameter> params, BlockStatement body) implements Statement {
    @Override
    public <T> void visit(AstVisitor<T> visitor) {
        visitor.visitFunctionDeclaration(this);
    }
}
