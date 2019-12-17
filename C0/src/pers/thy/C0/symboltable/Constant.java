package pers.thy.C0.symboltable;

public class Constant {
    int index;
    String type;
    String value;

    public Constant(int index, String type, String value) {
        this.index = index;
        this.type = type;
        this.value = value;
    }

    public Constant(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
