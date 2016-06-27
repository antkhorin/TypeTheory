public class Variable extends Expression {

    public String name;

    public Variable(String variable) {
        super(null, null);
        name = variable;
    }

    public char getType() {
        return 'v';
    }

    public String toString() {
        return name;
    }
}
