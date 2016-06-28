public class EqVarType extends Type {

    public EqVarType(Type type1, Type type2) {
        super(type1, type2, null);
    }

    public String toString() {
        return type1.toString().substring(1) + " < " + type2.toString();
    }

    public char getType() {
        return 'u';
    }
}
