import com.sun.xml.internal.bind.v2.TODO;
import javafx.util.Pair;

import java.io.*;
import java.time.Year;
import java.util.*;

public class Main {

    private static int n = -1;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("task3.in"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("task3.out"));
        String s = "";
        String s1;
        while ((s1 = reader.readLine()) != null) {
            s += s1 + "\n";
        }
        Expression expression = parse(s);
        Map<String, String> free = new HashMap<>();
        uniqueVariable(expression, new HashMap<>(), free);
        Map<String, Type> context = new HashMap<>();
        free.values().forEach(e -> context.put(e, new TypeVariable("a" + ++n)));
        try {
            Pair<List<Pair<Type, Type>>, Type> p = w(context, expression);
            Type type = p.getValue();
            for (Pair<Type, Type> pair : p.getKey()) {
                type = replace(type, pair.getKey(), pair.getValue());
            }
            writer.write(type.toString());
            writer.newLine();
            for (Map.Entry<String, String> entry : free.entrySet()) {
                writer.write(entry.getKey() + ":");
                Type t = context.get(entry.getValue());
                for (Pair<Type, Type> pair : p.getKey()) {
                    t = replace(t, pair.getKey(), pair.getValue());
                }
                writer.write(t.toString());
                writer.newLine();
            }
        } catch (RuntimeException e) {
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
        } else if (expression.getType() == 'f') {
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
        } else {
            Let let = (Let) expression;
            String s = let.variable.toString();
            while (map.values().contains(s)) {
                s += "0";
            }
            map.put(let.variable, s);
            uniqueVariable(let.expression1, mapping, free);
            s = mapping.put(let.variable.toString(), s);
            uniqueVariable(let.expression2, mapping, free);
            if (s == null) {
                mapping.remove(let.variable.toString());
            } else {
                mapping.put(let.variable.toString(), s);
            }
        }
    }

    private static boolean solve(List<Pair<Type, Type>> system) {
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
                } else if (pair.getKey().getType() == 'i' && pair.getValue().getType() == 'v') {
                    system.set(i, new Pair<>(pair.getValue(), pair.getKey()));
                }
            }
            for (int i = 0; i < system.size(); i++) {
                Pair<Type, Type> pair = system.get(i);
                if (contains(pair.getValue(), pair.getKey())) {
                        return false;
                }
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
        } else if (type.getType() == 'i'){
            return new Implication(replace(type.type1, variable, replace), replace(type.type2, variable, replace));
        } else {
            return replace(type.type2, variable, replace);
        }
    }
    private static Type replace2(Type type, Type variable, Type replace, Set<String> bound) {
        if (type.getType() == 'v') {
            return type.toString().equals(variable.toString()) && !bound.contains(variable.toString()) ? replace : type;
        } else if (type.getType() == 'i') {
            return new Implication(replace2(type.type1, variable, replace, bound), replace2(type.type2, variable, replace, bound));
        } else {
            boolean b = bound.add(type.type1.toString());
            Type type1 = new Universal(type.type1, replace2(type.type2, variable, replace, bound));
            if (b) {
                bound.remove(type.type1.toString());
            }
            return type1;
        }
    }

    private static Pair<List<Pair<Type, Type>>, Type> w(Map<String, Type> context, Expression expression) {
        if (expression.getType() == 'v') {
            Type type = context.get(map.get(expression));
            while (type.getType() == 'u') {
                type = replace(type, type.type1, new TypeVariable("a" + ++n));
            }
            return new Pair<>(new ArrayList<>(0), type);
        } else if (expression.getType() == 'a') {
            Pair<List<Pair<Type, Type>>, Type> pair = w(context, expression.expression1);
            Map<String, Type> newContext = new HashMap<>();
            for (Map.Entry<String, Type> entry : context.entrySet()) {
                Type type = entry.getValue();
                for (Pair<Type, Type> p : pair.getKey()) {
                    type = replace2(type, p.getKey(), p.getValue(), new HashSet<>());
                }
                newContext.put(entry.getKey(), type);
            }
            Pair<List<Pair<Type, Type>>, Type> pair2 = w(newContext, expression.expression2);
            Type type = pair.getValue();
            for (Pair<Type, Type> p : pair2.getKey()) {
                type = replace(type, p.getKey(), p.getValue());
            }
            Type tmp = new TypeVariable("a" + ++n);
            List<Pair<Type, Type>> system = new ArrayList<>();
            system.add(new Pair<>(type, new Implication(pair2.getValue(), tmp)));
            if (solve(system)) {
                system.addAll(pair.getKey());
                system.addAll(pair2.getKey());
                if (!solve(system)) {
                    throw new RuntimeException();
                }
                for (Pair<Type, Type> p : system) {
                    tmp = replace(tmp, p.getKey(), p.getValue());
                }
                return new Pair<>(system, tmp);
            } else {
                throw new RuntimeException();
            }
        } else if (expression.getType() == 'f') {
            Type tmp = new TypeVariable("a" + ++n);
            context.put(map.get(expression.expression1), tmp);
            Pair<List<Pair<Type, Type>>, Type> pair = w(context, expression.expression2);
            for (Pair<Type, Type> p : pair.getKey()) {
                tmp = replace(tmp, p.getKey(), p.getValue());
            }
            context.remove(map.get(expression.expression1));
            return new Pair<>(pair.getKey(), new Implication(tmp, pair.getValue()));
        } else {
            Pair<List<Pair<Type, Type>>, Type> pair = w(context, expression.expression1);
            Type t = pair.getValue();
            Set<Type> free = new HashSet<>();
            free(t, new HashSet<>(), free);
            Map<String, Type> newContext = new HashMap<>();
            for (Map.Entry<String, Type> entry : context.entrySet()) {
                Type type = entry.getValue();
                for (Pair<Type, Type> p : pair.getKey()) {
                    type = replace2(type, p.getKey(), p.getValue(), new HashSet<>());
                }
                newContext.put(entry.getKey(), type);
            }
            Set<Type> contextFree = new HashSet<>();
            newContext.values().forEach(e -> free(e, new HashSet<>(), contextFree));
            free.removeAll(contextFree);
            for (Type type : free) {
                t = new Universal(type, t);
            }
            for (Pair<Type, Type> p : pair.getKey()) {
                t = replace2(t, p.getKey(), p.getValue(), new HashSet<>());
            }
            newContext.put(map.get(((Let)expression).variable), t);
            Pair<List<Pair<Type, Type>>, Type> pair2 = w(newContext, expression.expression2);
            List<Pair<Type, Type>> system = new ArrayList<>();
            system.addAll(pair.getKey());
            system.addAll(pair2.getKey());
            if (!solve(system)) {
                throw new RuntimeException();
            }
            return new Pair<>(system, pair2.getValue());
        }
    }

    private static void free(Type type, Set<String> bound, Set<Type> free) {
        if (type.getType() == 'v') {
            if (!bound.contains(type.toString())) {
                free.add(type);
            }
        } else if (type.getType() == 'i') {
            free(type.type1, bound, free);
            free(type.type2, bound, free);
        } else {
            boolean b = bound.add(type.type1.toString());
            free(type.type2, bound, free);
            if (b) {
                bound.remove(type.type1.toString());
            }
        }
    }
}

