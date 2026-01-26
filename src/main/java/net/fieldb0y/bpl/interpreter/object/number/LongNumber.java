package net.fieldb0y.bpl.interpreter.object.number;

public class LongNumber extends TypedNumber<Long> {
    public LongNumber(Long value) {
        super(value);
    }

    @Override
    protected Long add(Number n1, Number n2) {
        return n1.longValue() + n2.longValue();
    }

    @Override
    protected Long subtract(Number n1, Number n2) {
        return n1.longValue() - n2.longValue();
    }

    @Override
    protected Long multiply(Number n1, Number n2) {
        return n1.longValue() * n2.longValue();
    }

    @Override
    protected Long divide(Number n1, Number n2) {
        return n1.longValue() / n2.longValue();
    }

    @Override
    protected Long modulo(Number n1, Number n2) {
        return n1.longValue() % n2.longValue();
    }

    @Override
    protected Long opposite(Number n) {
        return -n.longValue();
    }

    @Override
    public TypedNumber<Long> instantiate(Number value) {
        return new LongNumber(value.longValue());
    }

    @Override
    public int getNumericRank() {
        return 1;
    }
}
