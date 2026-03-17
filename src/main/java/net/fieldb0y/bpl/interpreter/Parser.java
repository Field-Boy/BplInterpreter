package net.fieldb0y.bpl.interpreter;

import net.fieldb0y.bpl.interpreter.ast.Program;
import net.fieldb0y.bpl.interpreter.ast.node.expression.*;
import net.fieldb0y.bpl.interpreter.ast.node.statement.*;
import net.fieldb0y.bpl.interpreter.environment.function.Parameter;
import net.fieldb0y.bpl.interpreter.exception.error.ParseError;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;

import java.util.ArrayList;
import java.util.List;

import static net.fieldb0y.bpl.interpreter.Token.Type.*;

public class Parser {
    private final List<Token> tokens;
    int current = 0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    public Program parse(){
        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd()){
            Statement stmt = statement();
            if (stmt != null){
                statements.add(stmt);
            }
        }
        return new Program(statements);
    }

    private Statement statement(){
        try {
            if (isTypeToken(peek())
                    && peekAhead(1).type() == IDENTIFIER
                    && peekAhead(2).type() == LEFT_PAREN)
                return functionDeclaration();

            if (match(IF)) return ifStatement();
            if (match(FOR)) return forStatement();
            if (match(WHILE)) return whileStatement();
            if (match(RETURN)) return returnStatement();
            if (match(BREAK)) return breakStatement();
            if (match(CONTINUE)) return continueStatement();
            if (match(LEFT_BRACE)) return blockStatement();

            if (peek().type() == CONST || isTypeToken(peek()))
                return variableDeclaration();

            return expressionStatement();
        } catch (ParseError e){
            synchronize();
            System.err.println("Parse Error: " + e.getMessage());
        }
        return null;
    }

    private Statement variableDeclaration(){
        boolean isConst = match(CONST);
        List<Expression> dimensions = new ArrayList<>();

        Token.Type type = advance().type();
        fillDimensions(dimensions);

        if (!isTypeToken(type))
            throw new ParseError("Expected type, got " + type);
        if (type == VOID_TYPE)
            throw new ParseError("Variables cannot have type 'void'");

        Token nameToken = consume(IDENTIFIER, "Expected variable name");
        String name = nameToken.lexeme();
        fillDimensions(dimensions);

        Expression initializer = null;
        if (match(EQUAL))
            initializer = expression();

        consumeSemicolon("variable declaration");
        if (!dimensions.isEmpty())
            return new ArrayVariableDeclaration(isConst, type, name, dimensions, initializer);
        return new VariableDeclaration(isConst, type, name, initializer);
    }

    private List<Expression> fillDimensions(List<Expression> dimensions){
        while (match(LEFT_BRACKET)){
            if (!check(RIGHT_BRACKET)) {
                dimensions.add(expression());
                consumeRightBracket("array dimension size");
            } else {
                advance();
                dimensions.add(null);
            }
        }
        return dimensions;
    }

    private Statement functionDeclaration(){
        Token.Type returnType = advance().type();

        Token nameToken = consume(IDENTIFIER, "Expected function name");
        String name = nameToken.lexeme();

        consumeLeftParen("function name");
        List<Parameter> parameters = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                Token.Type paramType = advance().type();
                if (!isTypeToken(paramType))
                    throw new ParseError("Expected parameter type, got " + paramType);

                Token paramNameToken = consume(IDENTIFIER, "Expected parameter name");
                parameters.add(new Parameter(paramNameToken.lexeme(), paramType));
            } while (match(COMMA));
        }
        consumeRightParen("function parameters");

        consume(LEFT_BRACE, "Expected '{' before function body");
        Statement body = blockStatement();
        return new FunctionDeclaration(returnType, name, parameters, (BlockStatement)body);
    }

    private Token.Type getTypeTokenOrThrow(Token.Type type, String expected){
        if (check(LEFT_BRACKET))
            return ARRAY_TYPE;

        if (!isTypeToken(type))
            throw new RuntimeError("Expected " + expected + ", got " + type);
        return type;
    }

    private Statement ifStatement(){
        consumeLeftParen("'if'");
        Expression condition = expression();
        consumeRightParen("'if' condition");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(ELSE))
            elseBranch = statement();

        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private Statement forStatement(){
        consumeLeftParen("'for'");

        Statement initializer = variableDeclaration();
        if (initializer instanceof VariableDeclaration declaration && declaration.initializer() == null)
            throw new RuntimeException("Var initializer in 'for' loop can not be null!");

        Expression condition = expression();
        consumeSemicolon("'for' loop condition");
        Expression increment = expression();
        consumeRightParen("'for' loop increment");

        Statement body = statement();
        return new ForStatement(initializer, condition, increment, body);
    }

    private Statement whileStatement(){
        consumeLeftParen("'while'");
        Expression condition = expression();
        consumeRightParen("'while' loop expression");

        Statement body = statement();
        return new WhileStatement(condition, body);
    }

    private Statement returnStatement(){
        Expression value = null;
        if (!check(Token.Type.SEMICOLON))
            value = expression();

        consumeSemicolon("return value");
        return new ReturnStatement(value);
    }

    private Statement breakStatement(){
        consumeSemicolon("break");
        return new BreakStatement();
    }

    private Statement continueStatement(){
        consumeSemicolon("continue");
        return new ContinueStatement();
    }

    private Statement blockStatement(){
        List<Statement> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()){
            Statement stmt = statement();
            if (stmt != null)
                statements.add(stmt);
        }

        consume(RIGHT_BRACE, "Expected '}' after block");
        return new BlockStatement(statements);
    }

    private Statement expressionStatement(){
        Expression expr = expression();
        consumeSemicolon("expression");
        return new ExpressionStatement(expr);
    }

    private Expression expression(){
        return assignment();
    }

    private Expression assignment() {
        Expression expr = logicalOr();

        if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, PERCENT_EQUAL)) {
            Token operator = previous();
            Expression value = assignment();
            return new BinaryExpression(expr, operator, value);
        }

        return expr;
    }

    private Expression logicalOr() {
        Expression expr = logicalAnd();

        while (match(Token.Type.OR)) {
            Token operator = previous();
            Expression right = logicalAnd();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression logicalAnd() {
        Expression expr = equality();

        while (match(Token.Type.AND)) {
            Token operator = previous();
            Expression right = equality();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression equality() {
        Expression expr = comparison();

        while (match(Token.Type.EQUAL_EQUAL, Token.Type.BANG_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        Expression expr = term();

        while (match(Token.Type.GREATER, Token.Type.GREATER_EQUAL,
                Token.Type.LESS, Token.Type.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }


    private  Expression term(){
        Expression expr = factor();
        while (match(PLUS, MINUS, PERCENT)) {
            expr = new BinaryExpression(expr, previous(), factor());
        }
        return expr;
    }

    private Expression factor(){
        Expression expr = unary();
        while (match(STAR, SLASH)){
            expr = new BinaryExpression(expr, previous(), unary());
        }
        return expr;
    }

    private Expression unary(){
        if (check(LEFT_PAREN) && isTypeToken(peekAhead(1))){
            advance();
            Token.Type castType = advance().type();
            consumeRightParen("type in cast");
            Expression expr = unary();
            return new CastExpression(castType, expr);
        }

        if (match(BANG, MINUS))
            return new BinaryExpression(null, previous(), unary());
        return postfix();
    }

    private Expression postfix() {
        Expression expr = call();

        if (match(LEFT_BRACKET)) {
            List<Expression> indices = new ArrayList<>();
            do {
                indices.add(expression());
                consumeRightBracket("array index");
            } while (match(LEFT_BRACKET));
            expr = new ArrayAccessExpression(expr, indices);
        }

        if (match(PLUS_PLUS, MINUS_MINUS)){
            return new BinaryExpression(expr, previous(), null);
        }
        return expr;
    }

    private Expression call(){
        Expression expr = primary();
        while (match(LEFT_PAREN)){
            expr = finishCall(expr);
        }
        return expr;
    }

    private Expression finishCall(Expression callee){
        List<Expression> args =  new ArrayList<>();

        if (!check(RIGHT_PAREN)){
            do {
                args.add(expression());
            } while (match(COMMA));
        }

        consumeRightParen("arguments");
        return new FunctionCall(callee, args);
    }

    private Expression primary(){
        if (match(TRUE))
            return new LiteralExpression(true, BOOL);
        if (match(FALSE))
            return new LiteralExpression(false, BOOL);
        if (match(NULL))
            return new LiteralExpression(null, NULL);

        if (matchNumber())
            return new LiteralExpression(previous().literal(), previous().type());
        if (match(STRING))
            return new LiteralExpression(previous().literal(), STRING);
        if (match(CHAR))
            return new LiteralExpression(previous().literal(), CHAR);

        if (match(IDENTIFIER)){
            String name = previous().lexeme();
            return new IdentifierExpression(name);
        }

        if (match(LEFT_PAREN)) {
            Expression expr = expression();
            consumeRightParen("expression");
            return expr;
        }

        if (match(LEFT_BRACE)) {
            List<Expression> elements = new ArrayList<>();

            if (!check(RIGHT_BRACE)){
                do {
                    elements.add(expression());
                } while (match(COMMA));
            }
            consume(RIGHT_BRACE, "Expected '}' after array literal elements");
            return new ArrayLiteralExpression(elements);
        }

        throw new ParseError("Expected expression at line " + peek().line());
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type() == SEMICOLON) return;

            switch (peek().type()) {
                case IF: case FOR: case WHILE: case RETURN:
                case INT_TYPE: case STRING_TYPE: case LEFT_BRACE:
                    return;
            }
            advance();
        }
    }

    private boolean isAtEnd(){
        return peek().type() == EOF;
    }

    private Token advance(){
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean match(Token.Type... types){
        for (Token.Type type : types) {
            if (check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean matchNumber(){
        return match(INT_NUMBER, LONG_NUMBER, FLOAT_NUMBER, DOUBLE_NUMBER);
    }

    private void consumeLeftBracket(String after){
        consume(LEFT_BRACKET, "Expected '[' after " + after);
    }

    private void consumeRightBracket(String after){
        consume(RIGHT_BRACKET, "Expected ']' after " + after);
    }

    private void consumeLeftParen(String after){
        consume(LEFT_PAREN, "Expected '(' after " + after);
    }

    private void consumeRightParen(String after){
        consume(RIGHT_PAREN, "Expected ')' after " + after);
    }

    private void consumeSemicolon(String after){
        consume(SEMICOLON, "Expected ';' after " + after);
    }

    private Token consume(Token.Type type, String message){
        if (check(type))
            return advance();
        throw new ParseError(message + " at line " + peek().line());
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token peekAhead(int offset) {
        int index = current + offset;
        if (index >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(index);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private boolean check(Token.Type type){
        return !isAtEnd() && peek().type() == type;
    }

    private boolean isTypeToken(Token token){
        return isTypeToken(token.type());
    }

    private boolean isTypeToken(Token.Type type){
        return type == VOID_TYPE || type == INT_TYPE || type == LONG_TYPE || type == FLOAT_TYPE
                || type == DOUBLE_TYPE || type == CHAR_TYPE || type == STRING_TYPE || type == BOOL_TYPE || type == ARRAY_TYPE;
    }
}
