package net.fieldb0y.bpl.interpreter.object;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.exception.error.RuntimeError;
import net.fieldb0y.bpl.interpreter.utils.ArrayUtils;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

import java.util.ArrayList;
import java.util.List;

public class ArrayObject extends TypedObject<List<TypedObject<?>>> {
    private final List<Integer> dimensions;
    private final Token.Type type;

    public ArrayObject(Token.Type type, List<Integer> dimensions) {
        super(new ArrayList<>());
        this.dimensions = new ArrayList<>(dimensions);
        this.type = type;
        initializeArray(dimensions, 0);
    }

    public <R, T extends TypedObject<R>> ArrayObject(Token.Type type, List<Integer> dimensions, List<T> elements){
        super(new ArrayList<>(elements));
        this.dimensions = new ArrayList<>(dimensions);
        this.type = type;
    }

    private void initializeArray(List<Integer> dims, int depth){
        if (depth == dims.size() - 1){
            for (int i = 0; i < dims.get(depth); i++){
                value.add(BplUtils.getDefaultValue(type));
            }
        } else {
            List<Integer> subDimensions = dims.subList(depth + 1, dims.size());
            for (int i = 0; i < dims.get(depth); i++){
                value.add(new ArrayObject(type, subDimensions));
            }
        }
    }

    public TypedObject<?> getElement(int index) {
        if (index < 0 || index >= dimensions.getFirst())
            throw new RuntimeError("Array index out of bounds: " + index + " (size: " + dimensions.getFirst() + ")");
        return value.get(index);
    }

    public TypedObject<?> getElement(List<Integer> indices){
        if (indices.size() > dimensions.size())
            throw new RuntimeError("Too many array indices. Max = " + dimensions.size() + ", but got " + indices.size());

        if (indices.size() == 1)
            return getElement(indices.getFirst());

        TypedObject<?> current = getElement(indices.getFirst());
        for (int i = 1; i < indices.size(); i++) {
            if (!(current instanceof ArrayObject arr))
                throw new RuntimeError("Index applied to non-array");
            current = arr.getElement(indices.get(i));
        }
        return current;
    }

    public void setElement(int index, TypedObject<?> val){
        if (index < 0 || index >= dimensions.getFirst())
            throw new RuntimeError("Array index out of bounds: " + index);

        TypedObject<?> current = value.get(index);
        if (dimensions.size() > 1){
            if (!(val instanceof ArrayObject arr))
                throw new RuntimeError("Cannot assign " + val + " to index " + index + ". Array is multidimensional, so you have to assign array-type element");

            if (arr.type != type)
                throw new RuntimeError("Cannot assign array with " + arr.type + " elements to index " + index + " of array with " + type + " elements");

            List<Integer> expectedSubDimensions = dimensions.subList(1, dimensions.size());
            List<Integer> actualSubDimensions = arr.getDimensions();

            if (!expectedSubDimensions.equals(actualSubDimensions))
                throw new RuntimeError("Cannot assign array with dimensions " + ArrayUtils.formatDimensions(actualSubDimensions) +
                        " to index " + index + " of array. Expected dimensions: " + ArrayUtils.formatDimensions(expectedSubDimensions));

            value.set(index, arr);
        } else {
            if (!current.isAssignable(val))
                throw new RuntimeError("Cannot assign '" + BplUtils.toTokenType(val) + "' to index " + index + " of array with " + type + " elements");
            value.set(index, BplUtils.getTypedObject(type, val));
        }
    }

    public void setElement(List<Integer> indices, TypedObject<?> val){
        if (indices.size() > dimensions.size())
            throw new RuntimeError("Too many array indices. Max = " + dimensions.size() + ", but got " + indices.size());

        if (indices.size() == 1) {
            setElement(indices.getFirst(), val);
            return;
        }

        ArrayObject current = this;
        for (int i = 0; i < indices.size() - 1; i++) {
            TypedObject<?> obj = current.getElement(indices.get(i));
            if (!(obj instanceof ArrayObject arr)) {
                throw new RuntimeError("Index applied to non-array");
            }
            current = arr;
        }
        current.setElement(indices.getLast(), val);
    }

    public Token.Type getElementType() {
        return type;
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    public int getDimensionsCount() {
        return dimensions.size();
    }

    public int size() {
        return size(0);
    }

    public int size(int dim) {
        if (dimensions.size() <= dim)
            throw new RuntimeError("Cannot get size of dimension " + dim + ". Max = " + dimensions.size() + ", but got " + dim);
        return dimensions.get(dim);
    }

    @Override
    public boolean isAssignable(TypedObject<?> val) {
        if (!(val instanceof ArrayObject arr)) return false;
        return arr.type == this.type && arr.dimensions.equals(this.dimensions);
    }
}
