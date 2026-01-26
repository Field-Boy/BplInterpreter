package net.fieldb0y.bpl.interpreter.object.number;

public class FloatNumber extends TypedNumber<Float> {
    public FloatNumber(Float value) {
        super(value);
    }

    @Override
    protected Float add(Number n1, Number n2) {
        return n1.floatValue() + n2.floatValue();
    }

    @Override
    protected Float subtract(Number n1, Number n2) {
        return n1.floatValue() - n2.floatValue();
    }

    @Override
    protected Float multiply(Number n1, Number n2) {
        return n1.floatValue() * n2.floatValue();
    }

    @Override
    protected Float divide(Number n1, Number n2) {
        return n1.floatValue() / n2.floatValue();
    }

    @Override
    protected Float modulo(Number n1, Number n2) {
        return n1.floatValue() % n2.floatValue();
    }

    @Override
    protected Float opposite(Number n) {
        return -n.floatValue();
    }

    @Override
    public TypedNumber<Float> instantiate(Number value) {
        return new FloatNumber(value.floatValue());
    }

    @Override
    public int getNumericRank() {
        return 2;
    }
}
