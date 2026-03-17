package net.fieldb0y.bpl;

import net.fieldb0y.bpl.interpreter.Interpreter;
import net.fieldb0y.bpl.interpreter.Lexer;
import net.fieldb0y.bpl.interpreter.Parser;
import net.fieldb0y.bpl.interpreter.ast.Program;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.fieldb0y.bpl.interpreter.environment.function.builtin.BuiltinFunctions.*;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0){
            System.err.println("Error: program path is not defined.");
            System.exit(1);
        }

        String filePath = args[0];
        String sourceCode;
        try {
            sourceCode = Files.readString(Path.of(filePath));
        } catch (IOException e) {
            System.err.println("Error reading file '" + filePath + "': "  + e);
            return;
        }

        boolean logLexerOutput = false, logParserOutput = false;
        for (int i = 1; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "--log-lexer" -> logLexerOutput = true;
                case "--log-parser" -> logParserOutput = true;
            }
        }

        interpret(sourceCode, logLexerOutput, logParserOutput);
    }

    private static void interpret(String source, boolean logLexerOutput, boolean logParserOutput) {
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize());
        Program program = parser.parse();

        if (logLexerOutput) System.out.println(lexer.getTokens());
        if (logParserOutput) System.out.println(program);

        Interpreter interpreter = new Interpreter(
                PRINT, WRITE,
                INPUT, INPUT_EMPTY,
                LEN,
                NUM_SYS,
                PARSE_INT, PARSE_LONG, PARSE_FLOAT, PARSE_DOUBLE,
                LPAD, RPAD
        );
        interpreter.visitProgram(program);
    }
}