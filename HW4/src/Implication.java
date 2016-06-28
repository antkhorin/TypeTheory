public class Implication extends Type {

    public Implication(Type type1, Type type2) {
        super(type1, type2, null);
    }

    public String toString() {
        if (type1.getType() == 'i') {
            return "(" + type1.toString() + ") -> " + type2.toString();
        } else {
            return type1.toString() + " -> " + type2.toString();
        }
    }

    public char getType() {
        return 'i';
    }
}
