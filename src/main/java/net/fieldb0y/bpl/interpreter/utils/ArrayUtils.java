package net.fieldb0y.bpl.interpreter.utils;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.ast.node.expression.Expression;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.object.ArrayObject;
import net.fieldb0y.bpl.interpreter.object.TypedObject;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {
    public static ArrayObject literalToArray(Token.Type type, ArrayLiteralResult literal){
        List<TypedObject<?>> literalElements = literal.get();
        List<Integer> dimensions = inferDimensions(literal);
        ArrayObject array = new ArrayObject(type, dimensions);

        for (int i = 0; i < dimensions.getFirst(); i++) {
            TypedObject<?> element = literalElements.get(i);
            TypedObject<?> finalElement;

            if(element instanceof ArrayLiteralResult cLiteral) {
                finalElement = literalToArray(type, cLiteral);
            } else {
                if (!BplUtils.canAssign(type, element))
                    throw new RuntimeError("Unable to assign value '" + element.get() + "' to index " + i + " of " + type + " array");
                finalElement = BplUtils.getTypedObject(type, element);
            }
            array.setElement(i, finalElement);
        }
        return array;
    }


    public static List<Integer> inferDimensions(ArrayLiteralResult literal){
        List<Integer> dims = new ArrayList<>();
        ArrayLiteralResult current = literal;

        while (current != null){
            List<TypedObject<?>> cElements = current.elements;

            if (cElements.isEmpty()) {
                throw new RuntimeError("Array literal cannot have empty dimensions");
            }

            dims.add(cElements.size());

            TypedObject<?> first = cElements.getFirst();
            if (first instanceof ArrayLiteralResult firstArr){
                int expectedSize = firstArr.elements.size();

                for (TypedObject<?> element : current.elements){
                    if (!(element instanceof ArrayLiteralResult arr)) {
                        throw new RuntimeError("Inconsistent array literal structure: " +
                                "expected nested array but got " + element.getClass().getSimpleName());
                    }
                    if (arr.elements.size() != expectedSize) {
                        throw new RuntimeError("Jagged arrays are not supported: " +
                                "expected size " + expectedSize + " but got " + arr.elements.size());
                    }
                }
                current = firstArr;
            } else {
                for (int i = 1; i < cElements.size(); i++) {
                    if (cElements.get(i) instanceof ArrayLiteralResult) {
                        throw new RuntimeError("Inconsistent array literal structure: " +
                                "mixing scalar values and arrays at the same level");
                    }
                }
                break;
            }
        }
        return dims;
    }

    public static String formatDimensions(List<Integer> dimensions) {
        StringBuilder sb = new StringBuilder();
        for (int dim : dimensions) {
            sb.append("[").append(dim).append("]");
        }
        return sb.toString();
    }

    public static String formatDimensionsWithoutSize(List<Integer> dimensions){
        StringBuilder sb = new StringBuilder();
        dimensions.forEach(d -> sb.append("[]"));
        return sb.toString();
    }

    public static class ArrayLiteralResult extends TypedObject<List<TypedObject<?>>> {
        protected final List<TypedObject<?>> elements;

        public ArrayLiteralResult(List<TypedObject<?>> elements) {
            super(elements);
            this.elements = elements;
        }
    }
}
