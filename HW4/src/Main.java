import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("task4.in"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("task4.out"));
        String s = "";
        String s1;
        while ((s1 = reader.readLine()) != null) {
            s += s1 + "\n";
        }
        Expression expression = parse(s);
        writer.write(algorithm(expression, new TypeVariable("t")).toString());
        writer.close();
    }

    private static Expression parse(String s) {
        s = s.replaceAll("\\s", " ").trim() + "$";
        int i = 0;
        Expression expression = null;
        Expression tmp;
        while (i < s.length()) {
            if (s.length() - i > 4 && s.substring(i, i + 4).equals("let ")) {
                i += 4;
                int j = i;
                while (s.charAt(j) != '=') {
                    j++;
                }
                tmp = new Variable(s.substring(i, j - 1));
                i = j + 2;
                int b = 0;
                j = i;
                while (b > 0 || !s.substring(j, j + 4).equals(" in ")) {
                    if (s.charAt(j) == '(') {
                        b++;
                    } else if (s.charAt(j) == ')') {
                        b--;
                    }
                    j++;
                }
                Expression tmp1 = parse(s.substring(i, j));
                return new Let(tmp, tmp1, parse(s.substring(j + 4, s.length())));
            } else if (s.charAt(i) >= 'a' && s.charAt(i) <= 'z') {
                int j = i + 1;
                while (s.charAt(j) >= 'a' && s.charAt(j) <= 'z' || s.charAt(j) == '\'' || s.charAt(j) >= '0' && s.charAt(j) <= '9') {
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

    private static int n = -1;

    private static Type algorithm(Expression expression, Type type) {
        if (expression.getType() == 'v') {
            return new EqVarType(new TypeVariable(expression.toString()), type);
        } else if (expression.getType() == 'a') {
            Type var = new TypeVariable("a" + ++n);
            return new Existential(var, new Conjunction(algorithm(expression.expression1, new Implication(var, type)), algorithm(expression.expression2, var)));
        } else if (expression.getType() == 'f') {
            Type var1 = new TypeVariable("a" + ++n);
            Type var2 = new TypeVariable("a" + ++n);
            return new Existential(var1, new Existential(var2, new Def(expression.expression1, var1,
                    new Conjunction(algorithm(expression.expression2, var2), new EqType(new Implication(var1, var2), type)))));
        } else {
            Type var1 = new TypeVariable("a" + ++n);
            Type var2 = new TypeVariable("a" + ++n);
            Expression var = ((Let)expression).variable;
            return new Def(var, new Universal(var1, algorithm(expression.expression1, var1), var1),
                    new Existential(var2, new Conjunction(new EqVarType(new TypeVariable(var.toString()), var2), algorithm(expression.expression2, type))));
        }
    }
}

