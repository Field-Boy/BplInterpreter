package net.fieldb0y.bpl.interpreter;

public record Token(Type type, String lexeme, Object literal, int line) {
    public Token(Type type, int line){
        this(type, "", null, line);
    }

    public enum Type {
        EOF,

        IDENTIFIER,
        STRING,
        CHAR,
        BOOL,

        INT_NUMBER,
        LONG_NUMBER,
        FLOAT_NUMBER,
        DOUBLE_NUMBER,

        CONST,
        ANY_TYPE,
        VOID_TYPE,
        INT_TYPE,
        LONG_TYPE,
        FLOAT_TYPE,
        DOUBLE_TYPE,
        CHAR_TYPE,
        STRING_TYPE,
        BOOL_TYPE,
        ARRAY_TYPE,

        IN,

        IF,
        ELSE,
        FOR,
        WHILE,
        BREAK,
        CONTINUE,
        RETURN,

        LEFT_PAREN, RIGHT_PAREN,
        LEFT_BRACE, RIGHT_BRACE,
        LEFT_BRACKET, RIGHT_BRACKET,
        COMMA, DOT, SEMICOLON,

        MINUS, MINUS_EQUAL,
        PLUS, PLUS_EQUAL,
        PLUS_PLUS, MINUS_MINUS,
        STAR, STAR_EQUAL,
        SLASH, SLASH_EQUAL,
        PERCENT, PERCENT_EQUAL,

        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        LESS, LESS_EQUAL,
        GREATER, GREATER_EQUAL,

        OR, AND,
        TRUE, FALSE,
        NULL
    }
}
