import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Main {

    private static List<Pair<Type, Type>> system = new ArrayList<>();
    private static Map<String, Type> types = new HashMap<>();
    private static int n = -1;
    private static Type type;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("task2.in"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("task2.out"));
        String s = reader.readLine();
        Expression expression = parse(s);
        Map<String, String> free = new HashMap<>();
        uniqueVariable(expression, new HashMap<>(), free);
        type = makeSystem(expression);
        if (solve()) {
            for (Pair<Type, Type> pair : system) {
                type = replace(type, pair.getKey(), pair.getValue());
            }
            writer.write(type.toString());
            writer.newLine();
            for (Map.Entry<String, String> entry : free.entrySet()) {
                writer.write(entry.getKey() + ":");
                Type type = types.get(entry.getValue());
                for (Pair<Type, Type> pair : system) {
                    type = replace(type, pair.getKey(), pair.getValue());
                }
                writer.write(type.toString());
                writer.newLine();
            }
        } else {
            writer.write("Лямбда-выражение не имеет типа.");
        }
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

    private static Map<Expression, String> map = new HashMap<>();

    private static void uniqueVariable(Expression expression, Map<String, String> mapping, Map<String, String> free) {
        if (expression.getType() == 'v') {
            if (mapping.containsKey(expression.toString())) {
                map.put(expression, mapping.get(expression.toString()));
            } else {
                if (free.containsKey(expression.toString())) {
                    map.put(expression, free.get(expression.toString()));
                } else {
                    String s = expression.toString();
                    while (map.values().contains(s)) {
                        s += "0";
                    }
                    map.put(expression, s);
                    free.put(expression.toString(), s);
                }
            }
        } else if (expression.getType() == 'l') {
            String s = expression.expression1.toString();
            while (map.values().contains(s)) {
                s += "0";
            }
            map.put(expression.expression1, s);
            s = mapping.put(expression.expression1.toString(), s);
            uniqueVariable(expression.expression2, mapping, free);
            if (s == null) {
                mapping.remove(expression.expression1.toString());
            } else {
                mapping.put(expression.expression1.toString(), s);
            }
        } else if (expression.getType() == 'a') {
            uniqueVariable(expression.expression1, mapping, free);
            uniqueVariable(expression.expression2, mapping, free);
        }
    }

    private static Type makeSystem(Expression expression) {
        if (expression.getType() == 'v') {
            if (!types.containsKey(map.get(expression))) {
                Type type = new TypeVariable("a" + ++n);
                types.put(map.get(expression), type);
            }
            return types.get(map.get(expression));
        } else if (expression.getType() == 'l') {
            return new Implication(makeSystem(expression.expression1), makeSystem(expression.expression2));
        } else {
            Type type = new TypeVariable("a" + ++n);
            system.add(new Pair<>(makeSystem(expression.expression1), new Implication(makeSystem(expression.expression2), type)));
            return type;
        }
    }

    private static boolean solve() {
        l:
        while (true){
            for (int i = 0; i < system.size(); i++) {
                Pair<Type, Type> pair = system.get(i);
                if (pair.getKey().getType() == 'i' && pair.getValue().getType() == 'i') {
                    system.add(new Pair<>(pair.getKey().type1, pair.getValue().type1));
                    system.add(new Pair<>(pair.getKey().type2, pair.getValue().type2));
                    system.remove(i--);
                } else if (pair.getKey().getType() == 'v' && pair.getValue().getType() == 'v' && pair.getKey().toString().equals(pair.getValue().toString())) {
                    system.remove(i--);
                } else if (pair.getKey().getType() == 'i' && pair.getValue().getType() == 'v' ||
                        pair.getKey().getType() == 'v' && pair.getValue().getType() == 'v' && contains(type, pair.getValue())) {
                    system.set(i, new Pair<>(pair.getValue(), pair.getKey()));
                }
            }
            for (Pair<Type, Type> pair : system) {
                if (contains(pair.getValue(), pair.getKey())) {
                    return false;
                }
            }
            for (int i = 0; i < system.size(); i++) {
                Pair<Type, Type> pair = system.get(i);
                for (int j = 0; j < system.size(); j++) {
                    if (i != j) {
                        Pair<Type, Type> pair2 = system.get(j);
                        if (contains(pair2.getKey(), pair.getKey()) || contains(pair2.getValue(), pair.getKey())) {
                            system.add(new Pair<>(replace(pair2.getKey(), pair.getKey(), pair.getValue()), replace(pair2.getValue(), pair.getKey(), pair.getValue())));
                            system.remove(j);
                            continue l;
                        }
                    }
                }
            }
            return true;
        }
    }

    private static boolean contains(Type type, Type variable) {
        if (type.getType() == 'v') {
            return type.toString().equals(variable.toString());
        } else {
            return contains(type.type1, variable) || contains(type.type2, variable);
        }
    }

    private static Type replace(Type type, Type variable, Type replace) {
        if (type.getType() == 'v') {
            return type.toString().equals(variable.toString()) ? replace : type;
        } else {
            return new Implication(replace(type.type1, variable, replace), replace(type.type2, variable, replace));
        }
    }
}

