package pers.thy.C0.symboltable;

import pers.thy.C0.utils.Pair;

import java.util.ArrayList;

public class Order {
    int index;
    String opcode;
    String type = new String();
    ArrayList<Integer> operands = new ArrayList<>();

    public Order(int index, String opcode) {
        this.index = index;
        this.opcode = opcode;
    }

    public Order(String opcode) {
        this.index = index;
        this.opcode = opcode;
    }

    public void addOperands(int number){
        operands.add(number);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperands(){
        String result = "";
        for(int i=0;i<operands.size();i++){
            result += operands.get(i);
            if(i!=operands.size()-1) result += ", ";
        }
        return result;
    }

    public Pair<Integer,Integer> getOperandsInt(){
        if(operands.isEmpty()) return new Pair<>(0,0);
        if(operands.size() == 1) return new Pair<>(operands.get(0),0);
        if(operands.size() == 2) return new Pair<>(operands.get(0),operands.get(1));
        return new Pair<>(0,0);
    }
}
