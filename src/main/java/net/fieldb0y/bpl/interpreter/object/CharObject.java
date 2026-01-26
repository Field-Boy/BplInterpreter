package net.fieldb0y.bpl.interpreter.object;

import net.fieldb0y.bpl.interpreter.Token;
import net.fieldb0y.bpl.interpreter.object.number.DoubleNumber;
import net.fieldb0y.bpl.interpreter.utils.BplUtils;

public class CharObject extends TypedObject<Character> {
    public CharObject(Character value) {
        super(value);
    }

    @Override
    public TypedObject<?> cast(Token.Type castType) {
        if (BplUtils.isNumberType(castType)){
            DoubleNumber v = new DoubleNumber((double)get());
            return v.cast(castType);
        }
        return super.cast(castType);
    }
}
