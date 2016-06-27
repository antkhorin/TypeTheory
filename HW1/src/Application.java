public class Application extends Expression {

    public Application(Expression expression1, Expression expression2) {
        super(expression1, expression2);
    }

    public char getType() {
        return 'a';
    }

    public String toString() {
        String s = "";
        if (expression1.getType() == 'l') {
            s += "(" + expression1.toString() + ")";
        } else {
            s += expression1.toString();
        }
        s += " ";
        if (expression2.getType() != 'v') {
            s += "(" + expression2.toString() + ")";
        } else {
            s += expression2.toString();
        }
        return s;
    }
}
