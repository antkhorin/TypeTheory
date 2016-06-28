import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("task1.in"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("task1.out"));
        String s = reader.readLine();
        AbstractExpression expression = parse(s);
        uniqueVariable(expression, new HashMap<>(), new HashMap<>());
        for (Map.Entry<AbstractExpression, String> entry : map.entrySet()) {
            ((Variable)entry.getKey().expression).name = entry.getValue();
        }
        int i = 0;
        while (reduction(expression)) {
            map.clear();
            System.out.println(i++);
            if (i == 5) {
                uniqueVariable(expression, new HashMap<>(), new HashMap<>());
                for (Map.Entry<AbstractExpression, String> entry : map.entrySet()) {
                    ((Variable) entry.getKey().expression).name = entry.getValue();
                }
            }

        }
        reduction(expression);
        writer.write(expression.toString());
        writer.close();
    }

    private static Map<AbstractExpression, String> map = new HashMap<>();

    private static AbstractExpression parse(String s) {
        s = s.replaceAll("\\s", " ").trim() + "$";
        int i = 0;
        AbstractExpression expression = null;
        AbstractExpression tmp;
        while (i < s.length()) {
            if (s.charAt(i) >= 'a' && s.charAt(i) <= 'z') {
                int j = i + 1;
                while (s.charAt(j) >= 'a' && s.charAt(j) <= 'z' || s.charAt(j) == '\'' || s.charAt(j) >= '0' && s.charAt(j) <= '9') {
                    j++;
                }
                tmp = new AbstractExpression(new Variable(s.substring(i, j)));
                if (expression == null) {
                    expression = tmp;
                } else {
                    expression = new AbstractExpression(new Application(expression, tmp));
                }
                i = j;
            } else if (s.charAt(i) == '\\') {
                int j = i + 1;
                while (s.charAt(j) != '.') {
                    j++;
                }
                tmp = new AbstractExpression(new Abstraction(new AbstractExpression(new Variable(s.substring(i + 1, j).replaceAll(" ", ""))), parse(s.substring(j + 1))));
                if (expression == null) {
                    return tmp;
                } else {
                    expression = new AbstractExpression(new Application(expression, tmp));
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
                    expression = new AbstractExpression(new Application(expression, parse(s.substring(i + 1, j - 1))));
                }
                i = j;
            }
            i++;
        }
        return expression;
    }

    private static boolean reduction(AbstractExpression expression) {
        if (expression.getType() == 'a') {
            if (expression.expression.expression1.getType() == 'l' && check(expression.expression.expression1.expression.expression2, expression.expression.expression1.expression.expression1,
                    new HashSet<>(), free(expression.expression.expression2, new HashSet<>(), new HashSet<>()))) {
                replace(expression.expression.expression1.expression.expression2, expression.expression.expression1.expression.expression1, expression.expression.expression2);
                expression.expression = expression.expression.expression1.expression.expression2.expression;
                return true;
            } else {
                return reduction(expression.expression.expression1) || reduction(expression.expression.expression2);
            }
        } else {
            return expression.getType() == 'l' && reduction(expression.expression.expression2);
        }
    }

    private static void replace(AbstractExpression expression, AbstractExpression variable, AbstractExpression replace) {
        if (expression.getType() == 'v' && expression.toString().equals(variable.toString())) {
            expression.expression = copy(replace).expression;
        } else if (expression.getType() == 'l' && !expression.expression.expression1.toString().equals(variable.toString())) {
            replace(expression.expression.expression2, variable, replace);
        } else if (expression.getType() == 'a') {
            replace(expression.expression.expression1, variable, replace);
            replace(expression.expression.expression2, variable, replace);
        }
    }

    private static boolean check(AbstractExpression expression, AbstractExpression variable, Set<String> bound, Set<String> free) {
        if (expression.getType() == 'v' && expression.toString().equals(variable.toString()) && !bound.contains(variable.toString())) {
            return !bound.stream().anyMatch(free::contains);
        } else if (expression.getType() == 'a') {
            return check(expression.expression.expression1, variable, bound, free) && check(expression.expression.expression2, variable, bound, free);
        } else if (expression.getType() == 'l') {
            boolean b = bound.add(expression.expression.expression1.toString());
            boolean k = check(expression.expression.expression2, variable, bound, free);
            if (b) {
                bound.remove(expression.expression.expression1.toString());
            }
            return k;
        }
        return true;
    }

    private static Set<String> free(AbstractExpression expression, Set<String > bound, Set<String> free) {
        if (expression.getType() == 'v') {
            if (!bound.contains(expression.toString())) {
                free.add(expression.toString());
            }
        } else if (expression.getType() == 'a') {
            free(expression.expression.expression1, bound, free);
            free(expression.expression.expression2, bound, free);
        } else {
            boolean b = bound.add(expression.expression.expression1.toString());
            free(expression.expression.expression2, bound, free);
            if (b) {
                bound.remove(expression.expression.expression1.toString());
            }
        }
        return free;
    }

    private static AbstractExpression copy(AbstractExpression expression) {
        if (expression.getType() == 'v') {
            return new AbstractExpression(new Variable(expression.toString()));
        } else if (expression.getType() == 'a') {
            return new AbstractExpression(new Application(copy(expression.expression.expression1), copy(expression.expression.expression2)));
        } else {
            return new AbstractExpression(new Abstraction(copy(expression.expression.expression1), copy(expression.expression.expression2)));
        }
    }

    private static void uniqueVariable(AbstractExpression expression, Map<String, String> mapping, Map<String, String> free) {
        if (expression.getType() == 'v') {
            if (mapping.containsKey(expression.toString())) {
                map.put(expression, mapping.get(expression.toString()));
            } else {
                if (free.containsKey(expression.toString())) {
                    map.put(expression, free.get(expression.toString()));
                } else {
                    String s = expression.toString();
                    while (map.values().contains(s)) {
                        s += (int)(Math.random() * 10);
                    }
                    map.put(expression, s);
                    free.put(expression.toString(), s);
                }
            }
        } else if (expression.getType() == 'l') {
            String s = expression.expression.expression1.toString();
            while (map.values().contains(s)) {
                s += (int)(Math.random() * 10);
            }
            map.put(expression.expression.expression1, s);
            s = mapping.put(expression.expression.expression1.toString(), s);
            uniqueVariable(expression.expression.expression2, mapping, free);
            if (s == null) {
                mapping.remove(expression.expression.expression1.toString());
            } else {
                mapping.put(expression.expression.expression1.toString(), s);
            }
        } else if (expression.getType() == 'a') {
            uniqueVariable(expression.expression.expression1, mapping, free);
            uniqueVariable(expression.expression.expression2, mapping, free);
        }
    }
}

