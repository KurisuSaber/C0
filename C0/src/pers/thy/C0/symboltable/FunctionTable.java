package pers.thy.C0.symboltable;

import java.util.ArrayList;

public class FunctionTable {
    public ArrayList<Function> functions = new ArrayList<>();

    private FunctionTable() {
    }

    private static FunctionTable functionTable = new FunctionTable();

    public static FunctionTable getFunctionTable() { return functionTable; }

    public Function getCurrentFuction(){
        return FunctionTable.getFunctionTable().functions.get(FunctionTable.getFunctionTable().functions.size()-1);
    }

    public boolean isDeclared(String identifier) {
        for (int i = 0; i < functions.size(); i++) {
            if (functions.get(i).name.equals(identifier))
                return true;
        }
        return false;
    }
}
