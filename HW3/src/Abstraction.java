public class Abstraction extends Expression {

    public Abstraction(Expression expression1, Expression expression2) {
        super(expression1, expression2);
    }

    public char getType() {
        return 'f';
    }

    public String toString() {
        String s = "\\" + expression1.toString() + ".";
        String tmp = expression2.toString();
        if (tmp.charAt(0) == '(') {
            tmp = tmp.substring(1, tmp.length() - 1);
        }
        s += tmp;
        return s;
    }
}
