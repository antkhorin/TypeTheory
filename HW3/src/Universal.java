public class Universal extends Type {

    public Universal(Type type1, Type type2) {
        super(type1, type2);
    }

    public String toString() {
        return "@" + type1.toString() + "." + type2.toString();
    }

    public char getType() {
        return 'u';
    }
}
