package pers.thy.C0.symboltable;

public class Variable {
    String type;
    String name;
    String functionName;
    boolean isConst;
    boolean isInitialized;

    public Variable(String type, String name, boolean isConst, boolean isInitialized) {
        this.type = type;
        this.name = name;
        this.isConst = isConst;
        this.isInitialized = isInitialized;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
