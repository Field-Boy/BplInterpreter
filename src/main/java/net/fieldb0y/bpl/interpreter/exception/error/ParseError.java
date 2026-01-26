package net.fieldb0y.bpl.interpreter.exception.error;

public class ParseError extends RuntimeException {
    public ParseError(String message){
        super(message);
    }
}
