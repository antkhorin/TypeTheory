public class Def extends Type {

    public Expression variable;

    public Def(Expression variable, Type type1, Type type2) {
        super(type1, type2, null);
        this.variable = variable;
    }

    public char getType() {
        return 'd';
    }

    public String toString() {
        return "(def " + variable.toString() + " : " + type1.toString() + " in " + type2.toString() + ")";
    }
}
