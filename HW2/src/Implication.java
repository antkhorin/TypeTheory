public class Implication extends Type {

    public Implication(Type type1, Type type2) {
        this.type1 = type1;
        this.type2 = type2;
    }

    public String toString() {
        if (type1.getType() == 'i') {
            return "(" + type1.toString() + ")->" + type2.toString();
        } else {
            return type1.toString() + "->" + type2.toString();
        }
    }

    public char getType() {
        return 'i';
    }
}
