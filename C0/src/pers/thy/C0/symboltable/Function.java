package pers.thy.C0.symboltable;

import java.util.ArrayList;

public class Function {
    //{index} {name_index} {params_size} {level}
    int index;
    int nameIndex;
    int paramsSize;
    int level=1;
    String name;
    String type;
    public ArrayList<Order> orders = new ArrayList<>();
    public ArrayList<Variable> variables = new ArrayList<>();

    public Function(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public int getParamsSize() {
        return paramsSize;
    }

    public void setParamsSize(int paramsSize) {
        this.paramsSize = paramsSize;
    }

    public void addOrder(Order order){
        orders.add(order);
    }

    public void addVariable(Variable variable){
        variables.add(variable);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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
