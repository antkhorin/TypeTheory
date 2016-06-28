public abstract class Type {

    public Type type1;
    public Type type2;
    public Type type3;

    public Type(Type type1, Type type2, Type type3) {
        this.type1 = type1;
        this.type2 = type2;
        this.type3 = type3;

    }

    public abstract String toString();

    public abstract char getType();
}
