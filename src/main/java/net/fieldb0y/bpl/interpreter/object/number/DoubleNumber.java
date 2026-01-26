package net.fieldb0y.bpl.interpreter.object.number;

public class DoubleNumber extends TypedNumber<Double> {
    public DoubleNumber(Double value) {
        super(value);
    }

    @Override
    protected Double add(Number n1, Number n2) {
        return n1.doubleValue() + n2.doubleValue();
    }

    @Override
    protected Double subtract(Number n1, Number n2) {
        return n1.doubleValue() - n2.doubleValue();
    }

    @Override
    protected Double multiply(Number n1, Number n2) {
        return n1.doubleValue() * n2.doubleValue();
    }

    @Override
    protected Double divide(Number n1, Number n2) {
        return n1.doubleValue() / n2.doubleValue();
    }

    @Override
    protected Double modulo(Number n1, Number n2) {
        return n1.doubleValue() % n2.doubleValue();
    }

    @Override
    protected Double opposite(Number n) {
        return -n.doubleValue();
    }

    @Override
    public TypedNumber<Double> instantiate(Number value) {
        return new DoubleNumber(value.doubleValue());
    }

    @Override
    public int getNumericRank() {
        return 3;
    }
}
