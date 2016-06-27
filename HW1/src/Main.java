import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("task1.in"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("task1.out"));
        String s = reader.readLine();
        Expression expression = parse(s);
        Expression expr;
        while ((expr = reduction(expression)) != null) {
            if (expr.parent == null) {
                expression = expr.expression1.expression2;
                expression.parent = null;
            }
        }
        writer.write(expression.toString());
        writer.close();
    }

    private static Expression parse(String s) {
        s = s.replaceAll("\\s", " ").trim() + "$";
        int i = 0;
        Expression expression = null;
        Expression tmp;
        while (i < s.length()) {
            if (s.charAt(i) >= 'a' && s.charAt(i) <= 'z') {
                int j = i + 1;
                while (s.charAt(j) >= 'a' && s.charAt(j) <= 'z' || s.charAt(j) == '\'') {
                    j++;
                }
                tmp = new Variable(s.substring(i, j));
                if (expression == null) {
                    expression = tmp;
                } else {
                    expression = new Application(expression, tmp);
                    expression.expression1.parent = expression;
                    expression.expression2.parent = expression;
                }
                i = j;
            } else if (s.charAt(i) == '\\') {
                int j = i + 1;
                while (s.charAt(j) != '.') {
                    j++;
                }
                tmp = new Abstraction(new Variable(s.substring(i + 1, j).replaceAll(" ", "")), parse(s.substring(j + 1)));
                tmp.expression1.parent = tmp;
                tmp.expression2.parent = tmp;
                if (expression == null) {
                    return tmp;
                } else {
                    expression = new Application(expression, tmp);
                    expression.expression1.parent = expression;
                    expression.expression2.parent = expression;
                }
                return expression;
            } else if (s.charAt(i) == '(') {
                int j = i + 1;
                int b = 1;
                while (b > 0) {
                    if (s.charAt(j) == '(') {
                        b++;
                    } else if (s.charAt(j) == ')') {
                        b--;
                    }
                    j++;
                }
                if (expression == null) {
                    expression = parse(s.substring(i + 1, j - 1));
                } else {
                    expression = new Application(expression, parse(s.substring(i + 1, j - 1)));
                    expression.expression1.parent = expression;
                    expression.expression2.parent = expression;
                }
                i = j;
            }
            i++;
        }
        return expression;
    }

    private static Expression reduction(Expression expression) {
        if (expression.getType() == 'a') {
            if (expression.expression1.getType() == 'l') {
                replace(expression.expression1.expression2, expression.expression1.expression1, expression.expression2);
                if (expression.parent != null) {
                    if (expression.parent.expression1 == expression) {
                        expression.parent.expression1 = expression.expression1.expression2;
                        expression.parent.expression1.parent = expression.parent;
                    } else {
                        expression.parent.expression2 = expression.expression1.expression2;
                        expression.parent.expression2.parent = expression.parent;
                    }
                }
                return expression;
            } else {
                Expression expr = reduction(expression.expression1);
                if (expr == null) {
                    return reduction(expression.expression2);
                } else {
                    return expr;
                }
            }
        } else if (expression.getType() == 'l') {
            return reduction(expression.expression2);
        } else {
            return null;
        }
    }

    private static void replace(Expression expression, Expression variable, Expression replace) {
        if (expression.getType() == 'v' && expression.toString().equals(variable.toString())) {
            Expression tmp = copy(replace);
            if (expression.parent.expression1 == expression) {
                expression.parent.expression1 = tmp;
            } else {
                expression.parent.expression2 = tmp;
            }
            tmp.parent = expression.parent;
        } else if (expression.getType() == 'l' && !expression.expression1.toString().equals(variable.toString())) {
            replace(expression.expression2, variable, replace);
        } else if (expression.getType() == 'a') {
            replace(expression.expression1, variable, replace);
            replace(expression.expression2, variable, replace);
        }
    }

    private static Expression copy(Expression expression) {
        if (expression.getType() == 'l') {
            Expression expr = new Abstraction(copy(expression.expression1), copy(expression.expression2));
            expr.expression1.parent = expr;
            expr.expression2.parent = expr;
            return expr;
        } else if (expression.getType() == 'a') {
            Expression expr = new Application(copy(expression.expression1), copy(expression.expression2));
            expr.expression1.parent = expr;
            expr.expression2.parent = expr;
            return expr;
        } else {
            return new Variable(expression.toString());
        }
    }
}

