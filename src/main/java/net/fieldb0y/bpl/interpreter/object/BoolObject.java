package net.fieldb0y.bpl.interpreter.object;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.object.number.DoubleNumber;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

public class BoolObject extends TypedObject<Boolean> {
    public BoolObject(Boolean value) {
        super(value);
    }

    @Override
    public TypedObject<?> cast(Token.Type castType) {
        if (BplUtils.isNumberType(castType)){
            DoubleNumber v = new DoubleNumber(get() ? 1.0 : 0.0);
            return v.cast(castType);
        }
        return super.cast(castType);
    }

    public static BoolObject and(BoolObject b1, BoolObject b2){
        return new BoolObject(b1.get() && b2.get());
    }

    public static BoolObject or(BoolObject b1, BoolObject b2){
        return new BoolObject(b1.get() || b2.get());
    }

    public static BoolObject not(BoolObject obj){
        return new BoolObject(!obj.get());
    }
}
