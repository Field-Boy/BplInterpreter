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
    public static void main(String[] args) throws IOException {
        if (args.length == 0){
            System.err.println("Error: program path is not defined.");
            System.exit(1);
        }

        String sourceCode = Files.readString(Path.of(args[0]));
        interpret(sourceCode);
    }

    private static void interpret(String source){
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.tokenize());
        Program program = parser.parse();

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