public class Let extends Expression {

    public Expression variable;

    public Let(Expression variable, Expression expression1, Expression expression2) {
        super(expression1, expression2);
        this.variable = variable;
    }

    public char getType() {
        return 'l';
    }

    public String toString() {
        return "let " + variable.toString() + " = " + expression1.toString() + " in " + expression2.toString();
    }
}
