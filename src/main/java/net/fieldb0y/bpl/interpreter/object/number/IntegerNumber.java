package net.fieldb0y.bpl.interpreter.object.number;

public class IntegerNumber extends TypedNumber<Integer> {
    public IntegerNumber(Integer value) {
        super(value);
    }

    @Override
    protected Integer add(Number n1, Number n2) {
        return n1.intValue() + n2.intValue();
    }

    @Override
    protected Integer subtract(Number n1, Number n2) {
        return n1.intValue() - n2.intValue();
    }

    @Override
    protected Integer multiply(Number n1, Number n2) {
        return n1.intValue() * n2.intValue();
    }

    @Override
    protected Integer divide(Number n1, Number n2) {
        return n1.intValue() / n2.intValue();
    }

    @Override
    protected Integer modulo(Number n1, Number n2) {
        return n1.intValue() % n2.intValue();
    }

    @Override
    protected Integer opposite(Number n) {
        return -n.intValue();
    }

    @Override
    public TypedNumber<Integer> instantiate(Number value) {
        return new IntegerNumber(value.intValue());
    }

    @Override
    public int getNumericRank() {
        return 0;
    }
}
