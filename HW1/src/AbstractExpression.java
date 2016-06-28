public class AbstractExpression {

    public Expression expression;

    public AbstractExpression(Expression expression) {
        this.expression = expression;
    }

    public char getType() {
        return expression.getType();
    }

    public String toString() {
        return expression.toString();
    };
}
