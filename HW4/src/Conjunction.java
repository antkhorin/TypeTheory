public class Conjunction extends Type {

    public Conjunction(Type type1, Type type2) {
        super(type1, type2, null);
    }

    public String toString() {
        return "(" + type1.toString() + " & " + type2.toString() + ")";
    }

    public char getType() {
        return 'c';
    }
}
