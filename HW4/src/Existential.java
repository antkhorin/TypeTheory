public class Existential extends Type {

    public Existential(Type type1, Type type2) {
        super(type1, type2, null);
    }

    public String toString() {
        return "(?" + type1.toString() + "." + type2.toString() + ")";
    }

    public char getType() {
        return 'e';
    }
}
