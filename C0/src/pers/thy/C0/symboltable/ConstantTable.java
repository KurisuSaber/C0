package pers.thy.C0.symboltable;

import java.util.ArrayList;

public class ConstantTable {
    public ArrayList<Constant> constants = new ArrayList<>();

    private ConstantTable() {}

    private static ConstantTable constantTable = new ConstantTable();

    public static ConstantTable getConstantTable() { return constantTable; }

    public boolean isDeclared(String identifier) {
        for (int i = 0; i < constants.size(); i++) {
            if (constants.get(i).value.equals(identifier))
                return true;
        }
        return false;
    }

    public int getIndex(String functionName) {
        for (int i = 0; i < constants.size(); i++) {
            if (constants.get(i).value.equals(functionName))
                return i;
        }
        return 0;
    }
}
