public abstract class Expression {

    public Expression expression1;
    public Expression expression2;
    public Expression parent;

    public Expression(Expression expression1, Expression expression2) {
        this.expression1 = expression1;
        this.expression2 = expression2;
    }

    public abstract char getType();

    public abstract String toString();
}
