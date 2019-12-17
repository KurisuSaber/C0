package pers.thy.C0.symboltable;

import java.util.ArrayList;

public class StartCodeTable {
    public ArrayList<Order> orders = new ArrayList<>();
    public ArrayList<Variable> variables = new ArrayList<>();

    private static StartCodeTable startCodeTable = new StartCodeTable();

    private StartCodeTable(){}

    public static StartCodeTable getStartCodeTable() { return startCodeTable; }

    public boolean isDeclared(String identifier) {
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).name.equals(identifier))
                return true;
        }
        return false;
    }

    public boolean isConst(String identifier) {
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).name.equals(identifier))
                return variables.get(i).isConst;
        }
        return false;
    }

    public int getIndex(String identifier) {
        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).name.equals(identifier))
                return i;
        }
        return 0;
    }
}
