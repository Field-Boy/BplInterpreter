package net.fieldb0y.bpl.interpreter;


import net.fieldb0y.bpl.interpreter.exception.error.LexerError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.fieldb0y.bpl.interpreter.Token.Type.*;

public class Lexer {
    private String input;
    private final List<Token> tokens;

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, Token.Type> keywords = new HashMap<>();

    static {
        keywords.put("const", CONST);
        keywords.put("void", VOID_TYPE);
        keywords.put("int", INT_TYPE);
        keywords.put("long", LONG_TYPE);
        keywords.put("float", FLOAT_TYPE);
        keywords.put("double", DOUBLE_TYPE);
        keywords.put("char", CHAR_TYPE);
        keywords.put("string", STRING_TYPE);
        keywords.put("bool", BOOL_TYPE);

        keywords.put("in", IN);

        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("for", FOR);
        keywords.put("while", WHILE);
        keywords.put("break", BREAK);
        keywords.put("continue", CONTINUE);
        keywords.put("return", RETURN);

        keywords.put("true", TRUE);
        keywords.put("false", FALSE);

        keywords.put("null", NULL);
    }


    public Lexer(String input){
        this.input = input;
        tokens = new ArrayList<>();
    }

    public void prepare(){
        tokens.clear();
        start = 0;
        current = 0;
        line = 1;
    }

    public List<Token> tokenize(){
        prepare();
        while(!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, line));
        return tokens;
    }

    private void scanToken() {
        try {
            char c = advance();

            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                    break;
                case '\n':
                    line++;
                    break;

                case '(': addToken(LEFT_PAREN); break;
                case ')': addToken(RIGHT_PAREN); break;
                case '{': addToken(LEFT_BRACE); break;
                case '}': addToken(RIGHT_BRACE); break;
                case '[': addToken(LEFT_BRACKET); break;
                case ']': addToken(RIGHT_BRACKET); break;
                case ',': addToken(COMMA); break;
                case '.': addToken(DOT); break;
                case ';': addToken(SEMICOLON); break;

                case '+': addToken(match('=') ? PLUS_EQUAL : match('+') ? PLUS_PLUS : PLUS); break;
                case '-': addToken(match('=') ? MINUS_EQUAL : match('-') ? MINUS_MINUS : MINUS); break;
                case '*': addToken(match('=') ? STAR_EQUAL : STAR); break;
                case '/': {
                    if (match('=')) addToken(SLASH_EQUAL);
                    else if (match('/')) skipComment();
                    else addToken(SLASH); break;
                }
                case '%': addToken(match('=') ? PERCENT_EQUAL : PERCENT); break;

                case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
                case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
                case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
                case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;

                case '&': if (match('&')) addToken(AND); break;
                case '|': if (match('|')) addToken(OR); break;

                case '"': addString(); break;
                case '\'': addChar(); break;

                default: {
                    if (isDigit(c)) addNumber();
                    else if (Character.isAlphabetic(c) || c == '_') addIdentifier();
                    else {
                        throw new LexerError("Unexpected character");
                    }
                }
            }
        } catch (LexerError e){
            System.err.println("Lexer Error: " + e.getMessage());
        }
    }

    private void addIdentifier(){
        char ch = peek();
        while (Character.isLetterOrDigit(ch) || ch == '_') {
            advance();
            ch = peek();
        }

        String text = input.substring(start, current);
        Token.Type type = keywords.get(text);
        if (type != null){
            addToken(type);
        } else addToken(IDENTIFIER, text);
    }

    private void addString(){
        while (peek() != '"' && !isAtEnd()){
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()){
            throw new LexerError("Unterminated string");
        }
        advance();
        addToken(STRING, input.substring(start + 1, current - 1));
    }

    private void addChar(){
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            throw new LexerError("Unterminated character");
        }
        advance();

        String value = input.substring(start + 1, current - 1);
        if (value.length() != 1) {
            throw new LexerError("Invalid character literal");
        }
        addToken(CHAR, value.charAt(0));
    }

    private void addNumber(){
        while(isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())){
            do advance();
            while (isDigit(peek()));
        }

        char suffix = Character.toLowerCase(peek());
        String numStr = input.substring(start, current);

        if (suffix == 'f') {
            addToken(FLOAT_NUMBER, Float.parseFloat(numStr));
            advance();
        } else if (suffix == 'd') {
            addToken(DOUBLE_NUMBER, Double.parseDouble(numStr));
            advance();
        } else if (suffix == 'l') {
            addToken(LONG_NUMBER, Long.parseLong(numStr));
            advance();
        } else if (numStr.contains(".")) {
            addToken(DOUBLE_NUMBER, Double.parseDouble(numStr));
        } else {
            try {
                addToken(INT_NUMBER, Integer.parseInt(numStr));
            } catch (NumberFormatException e) {
                addToken(LONG_NUMBER, Long.parseLong(numStr));
            }
        }
    }

    private void skipComment(){
        while (peek() != '\n' && !isAtEnd()) advance();
    }

    private char advance() {
        return input.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (input.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peekNext() {
        if (current + 1 >= input.length()) return '\0';
        return input.charAt(current + 1);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return input.charAt(current);
    }

    private Token createToken(Token.Type type, Object literal){
        String text = input.substring(start, current);
        return new Token(type, text, literal, line);
    }

    private void addToken(Token token){
        tokens.add(token);
    }

    private void addToken(Token.Type type) {
        addToken(type, null);
    }

    private void addToken(Token.Type type, Object literal) {
        addToken(createToken(type, literal));
    }

    private boolean isDigit(Character c){
        return Character.isDigit(c);
    }

    private boolean isAtEnd(){
        return current >= input.length();
    }

    public String getInput() {
        return input;
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
