package pers.thy.C0.generator;

import pers.thy.C0.symboltable.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Sgenerator {
    String outputFile;

    public Sgenerator(String outputFile) {
        this.outputFile = outputFile;
    }

    public void generate(){
        try {
            FileWriter fr = new FileWriter(outputFile);
            BufferedWriter bw = new BufferedWriter(fr);
            writeConstant(bw);
            writeStart(bw);
            writeFunctions(bw);
            writeCodes(bw);
            bw.close();
            fr.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void writeConstant(BufferedWriter bw){
        try {
            bw.write(".constants:\n");
            ConstantTable constantTable = ConstantTable.getConstantTable();
            for (int i = 0; i < constantTable.constants.size(); i++) {
                String output = String.format("%-4d S  \"%s\"\n",i,constantTable.constants.get(i).getValue());
                bw.write(output);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeStart(BufferedWriter bw){
        try {
            bw.write(".start:\n");
            StartCodeTable startCodeTable = StartCodeTable.getStartCodeTable();
            for(int i=0;i<startCodeTable.orders.size();i++){
                Order order = startCodeTable.orders.get(i);
                String output = String.format("%-4d %s %-3s\n",i,order.getOpcode(),order.getOperands());
                bw.write(output);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeFunctions(BufferedWriter bw){
        try {
            bw.write(".functions:\n");
            FunctionTable functionTable = FunctionTable.getFunctionTable();
            for(int i=0;i<functionTable.functions.size();i++){
                Function function = functionTable.functions.get(i);
                bw.write(i+" "+i+" "+function.getParamsSize()+" 1\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeCodes(BufferedWriter bw){
        try {
            FunctionTable functionTable = FunctionTable.getFunctionTable();
            for(int i=0;i<functionTable.functions.size();i++){
                Function function = functionTable.functions.get(i);
                bw.write(".F"+i+":\n");
                for(int j=0;j<function.orders.size();j++){
                    Order order = function.orders.get(j);
                    String output = String.format("%-4d %s %-3s\n",j,order.getOpcode(),order.getOperands());
                    bw.write(output);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
