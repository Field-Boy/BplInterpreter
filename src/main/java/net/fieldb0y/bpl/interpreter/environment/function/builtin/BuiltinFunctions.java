package net.fieldb0y.bpl.interpreter.environment.function.builtin;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.environment.function.FunctionSignature;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.object.ArrayObject;
import net.fieldb0y.bpl.interpreter.object.StringObject;
import net.fieldb0y.bpl.interpreter.object.TypedObject;
import net.fieldb0y.bpl.interpreter.object.number.DoubleNumber;
import net.fieldb0y.bpl.interpreter.object.number.FloatNumber;
import net.fieldb0y.bpl.interpreter.object.number.IntegerNumber;
import net.fieldb0y.bpl.interpreter.object.number.LongNumber;
import net.fieldb0y.bpl.interpreter.utils.ArrayUtils;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

import java.util.List;
import java.util.Scanner;

import static net.fieldb0y.bpl.interpreter.Token.Type.*;

public class BuiltinFunctions {
    private static final Scanner scanner = new Scanner(System.in);

    public static final BuiltinFunction PRINT = new BuiltinFunction(VOID_TYPE, new FunctionSignature("print", List.of(ANY_TYPE)), (interpreter, args) -> {
        TypedObject<?> arg = args.getFirst();
        System.out.println(arg.get());
        return null;
    });

    public static final BuiltinFunction WRITE = new BuiltinFunction(VOID_TYPE, new FunctionSignature("write", List.of(ANY_TYPE)), (interpreter, args) -> {
        TypedObject<?> arg = args.getFirst();
        System.out.print(arg);
        return null;
    });

    public static final BuiltinFunction LEN = new BuiltinFunction(INT_TYPE, new FunctionSignature("len", List.of(ANY_TYPE)), (interpreter, args) -> {
       TypedObject<?> arg = args.getFirst();
        return new IntegerNumber(switch (arg){
             case ArrayObject array -> array.size();
             case ArrayUtils.ArrayLiteralResult arrayLiteral -> arrayLiteral.get().size();
             case StringObject string -> string.get().length();
             default -> throw new RuntimeError("Unable to get length of '" + BplUtils.toTokenType(arg) + "' object. Expected 'STRING_TYPE' or 'ARRAY_TYPE'");
        });
    });

    public static final BuiltinFunction INPUT = new BuiltinFunction(STRING_TYPE, new FunctionSignature("input", List.of(ANY_TYPE)), (interpreter, args) -> {
        Object prompt = args.getFirst().get();
        if (prompt != null) {
            System.out.print(prompt);
        }

        if (scanner.hasNextLine()) {
            String inputData = scanner.nextLine();
            return new StringObject(inputData);
        }
        return new StringObject("");
    });

    public static final BuiltinFunction INPUT_EMPTY = new BuiltinFunction(STRING_TYPE, new FunctionSignature("input", List.of()), (interpreter, args) -> {
        if (scanner.hasNextLine()) {
            return new StringObject(scanner.nextLine());
        }
        return new StringObject("");
    });

    public static final BuiltinFunction NUM_SYS = new BuiltinFunction(STRING_TYPE, new FunctionSignature("numSys", List.of(ANY_TYPE, INT_TYPE, INT_TYPE)), (interpreter, args) -> {
                String numberStr = args.getFirst().get().toString();
                int fromBase = (int) args.get(1).get();
                int toBase = (int) args.get(2).get();

                try {
                    long decimalValue = Long.parseLong(numberStr, fromBase);
                    String result = Long.toString(decimalValue, toBase);

                    return new StringObject(result);
                } catch (NumberFormatException e) {
                    throw new RuntimeError("Invalid number '" + numberStr + "' for radix " + fromBase);
                } catch (IllegalArgumentException e) {
                    throw new RuntimeError("Invalid radix. Must be between " + Character.MIN_RADIX + " and " + Character.MAX_RADIX);
                }
            }
    );

    public static final BuiltinFunction LPAD = new BuiltinFunction(STRING_TYPE, new FunctionSignature("lpad", List.of(ANY_TYPE, INT_TYPE, STRING_TYPE)), (interpreter, args) -> {
                String original = args.getFirst().get().toString();
                int targetLength = (int)args.get(1).get();
                String padChar = args.get(2).get().toString();

                if (padChar.isEmpty()) {
                    throw new RuntimeError("Padding string cannot be empty");
                }

                StringBuilder sb = new StringBuilder(original);
                while (sb.length() < targetLength) {
                    sb.insert(0, padChar);
                }

                if (sb.length() > targetLength) {
                    return new StringObject(sb.substring(sb.length() - targetLength));
                }
                return new StringObject(sb.toString());
            }
    );

    public static final BuiltinFunction RPAD = new BuiltinFunction(STRING_TYPE, new FunctionSignature("rpad", List.of(ANY_TYPE, INT_TYPE, STRING_TYPE)), (interpreter, args) -> {
                String original = args.get(0).get().toString();
                int targetLength = (int) args.get(1).get();
                String padChar = args.get(2).get().toString();

                StringBuilder sb = new StringBuilder(original);
                while (sb.length() < targetLength) {
                    sb.append(padChar);
                }

                if (sb.length() > targetLength) {
                    return new StringObject(sb.substring(0, targetLength));
                }
                return new StringObject(sb.toString());
            }
    );

    public static final BuiltinFunction PARSE_INT = new BuiltinFunction(INT_TYPE, new FunctionSignature("parseInt", List.of(STRING_TYPE)), (interpreter, args) ->
            new IntegerNumber(Integer.parseInt(((StringObject)args.getFirst()).get()))
    );

    public static final BuiltinFunction PARSE_LONG = new BuiltinFunction(LONG_TYPE, new FunctionSignature("parseLong", List.of(STRING_TYPE)), (interpreter, args) ->
            new LongNumber(Long.parseLong(((StringObject)args.getFirst()).get()))
    );

    public static final BuiltinFunction PARSE_FLOAT = new BuiltinFunction(FLOAT_TYPE, new FunctionSignature("parseFloat", List.of(STRING_TYPE)), (interpreter, args) ->
            new FloatNumber(Float.parseFloat(((StringObject)args.getFirst()).get()))
    );

    public static final BuiltinFunction PARSE_DOUBLE = new BuiltinFunction(DOUBLE_TYPE, new FunctionSignature("parseDouble", List.of(STRING_TYPE)), (interpreter, args) ->
            new DoubleNumber(Double.parseDouble(((StringObject)args.getFirst()).get()))
    );
}
