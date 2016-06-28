public class Universal extends Type {

    public Universal(Type type1, Type type2, Type type3) {
        super(type1, type2, type3);
    }

    public String toString() {
        return "@" + type1.toString() + "[" + type2.toString() + "]." + type3.toString();
    }

    public char getType() {
        return 'u';
    }
}
