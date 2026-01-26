package net.fieldb0y.bpl.interpreter.object;

import java.util.Objects;

public class StringObject extends TypedObject<String> {
    public StringObject(String value) {
        super(value);
    }

    public static StringObject makeString(TypedObject<?> obj1, TypedObject<?> obj2){
        return combine(stringify(obj1), stringify(obj2));
    }

    public static StringObject combine(StringObject str1, StringObject str2){
        return new StringObject(str1.get() + str2.get());
    }

    public static <T> StringObject stringify(TypedObject<T> obj){
         T o = obj.get();
        return new StringObject(o != null ? o.toString() : "null");
    }
}
