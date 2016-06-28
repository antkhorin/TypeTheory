public class TypeVariable extends Type {

    public String name;

    public TypeVariable(String name) {
        super(null, null, null);
        this.name = name;
    }

    public String toString() {
        return "'" + name;
    }

    public char getType() {
        return 'v';
    }
}
