package net.fieldb0y.bpl.interpreter.ast;

import net.fieldb0y.bpl.interpreter.ast.node.expression.*;
import net.fieldb0y.bpl.interpreter.ast.node.statement.*;

public interface AstVisitor<T> {
    void visitProgram(Program program);

    void visitVariableDeclaration(VariableDeclaration decl);
    void visitArrayVariableDeclaration(ArrayVariableDeclaration decl);
    void visitFunctionDeclaration(FunctionDeclaration decl);

    void visitIfStatement(IfStatement stmt);
    void visitForStatement(ForStatement stmt);
    void visitWhileStatement(WhileStatement stmt);
    void visitBlockStatement(BlockStatement stmt);
    void visitExpressionStatement(ExpressionStatement stmt);
    void visitReturnStatement(ReturnStatement stmt);
    void visitBreakStatement(BreakStatement stmt);
    void visitContinueStatement(ContinueStatement stmt);

    T visitFunctionCall(FunctionCall call);
    T visitBinaryExpression(BinaryExpression expr);
    T visitLiteralExpression(LiteralExpression expr);
    T visitArrayLiteralExpression(ArrayLiteralExpression expr);
    T visitArrayAccessExpression(ArrayAccessExpression expr);
    T visitIdentifierExpression(IdentifierExpression expr);
    T visitCastExpression(CastExpression expr);
}
