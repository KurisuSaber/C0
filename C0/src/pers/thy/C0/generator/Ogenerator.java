package pers.thy.C0.generator;

import pers.thy.C0.symboltable.*;
import pers.thy.C0.utils.Pair;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Ogenerator {
    String outputFile;

    public Ogenerator(String outputFile) {
        this.outputFile = outputFile;
    }

    //指令的十六进制 第一操作数长度 第二操作数长度
    Pair<Integer,Pair<Integer,Integer> > getInstruction(String opCode){
        switch (opCode){
            case "bipush": return new Pair<>(0x01,new Pair<>(1,0));
            case "ipush": return new Pair<>(0x02,new Pair<>(4,0));
            case "loada": return new Pair<>(0x0a,new Pair<>(2,4));
            case "iload": return new Pair<>(0x10,new Pair<>(0,0));
            case "istore": return new Pair<>(0x20,new Pair<>(0,0));
            case "iadd": return new Pair<>(0x30,new Pair<>(0,0));
            case "isub": return new Pair<>(0x34,new Pair<>(0,0));
            case "imul": return new Pair<>(0x38,new Pair<>(0,0));
            case "idiv": return new Pair<>(0x3c,new Pair<>(0,0));
            case "ineg": return new Pair<>(0x40,new Pair<>(0,0));
            case "icmp": return new Pair<>(0x44,new Pair<>(0,0));
            case "jmp": return new Pair<>(0x70,new Pair<>(2,0));
            case "je": return new Pair<>(0x71,new Pair<>(2,0));
            case "jne": return new Pair<>(0x72,new Pair<>(2,0));
            case "jl": return new Pair<>(0x73,new Pair<>(2,0));
            case "jge": return new Pair<>(0x74,new Pair<>(2,0));
            case "jg": return new Pair<>(0x75,new Pair<>(2,0));
            case "jle": return new Pair<>(0x76,new Pair<>(2,0));
            case "call": return new Pair<>(0x80,new Pair<>(2,0));
            case "ret": return new Pair<>(0x88,new Pair<>(0,0));
            case "iret": return new Pair<>(0x89,new Pair<>(0,0));
            case "iprint": return new Pair<>(0xa0,new Pair<>(0,0));
            case "cprint": return new Pair<>(0xa2,new Pair<>(0,0));
            case "printl": return new Pair<>(0xaf,new Pair<>(0,0));
            case "iscan": return new Pair<>(0x72,new Pair<>(0,0));
        }
        return new Pair<>(0,new Pair<>(0,0));
    }

    byte[] intToBytes(int input){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(input);
        return buffer.array();
    }

    byte[] shortToBytes(short input){
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putShort(input);
        return buffer.array();
    }


    public void generate(){
        try {
            File f = new File(outputFile);
            if(f.exists() && f.isFile())
                f.delete();
            if(f.isDirectory()) {
                System.out.println("this is a directory, are you sure you typed the correct output file?");
                System.exit(0);
            }
            f.createNewFile();
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile, true));
            writeMagicAndVersion(out);
            writeConstants(out);
            writeStartCode(out);
            writeFunctionCode(out);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void writeMagicAndVersion(DataOutputStream out){
        try{
            int magic = 0x43303a29;
            int version = 1;

            out.write(intToBytes(magic));
            out.write(intToBytes(version));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeConstants(DataOutputStream out){
        try{
            ConstantTable constantTable = ConstantTable.getConstantTable();
            short count = (short)constantTable.constants.size();
            out.write(shortToBytes(count));
            for(int i=0;i<count;i++){
                byte type = 0;
                short length = (short)constantTable.constants.get(i).getValue().length();
                String value = constantTable.constants.get(i).getValue();
                out.write(type);
                out.write(shortToBytes(length));
                out.write(value.getBytes());
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeStartCode(DataOutputStream out){
        try{
            StartCodeTable startCodeTable = StartCodeTable.getStartCodeTable();
            short count = (short)startCodeTable.orders.size();
            out.write(shortToBytes(count));
            for(int i=0;i<count;i++){
                Order order = startCodeTable.orders.get(i);
                Pair<Integer,Pair<Integer,Integer>> instruction = getInstruction(order.getOpcode());
                byte opcode = (byte)instruction.getFirst().intValue();
                int sizeL = instruction.getSecond().getFirst();
                int sizeR = instruction.getSecond().getSecond();
                out.write(opcode);
                if(sizeL == 0) continue;
                if(sizeL == 1){
                    byte operand = (byte)order.getOperandsInt().getFirst().intValue();
                    out.write(operand);
                }else if(sizeL == 2){
                    short operand = (short) order.getOperandsInt().getFirst().intValue();
                    out.write(shortToBytes(operand));
                }else if(sizeL == 4){
                    int operand = order.getOperandsInt().getFirst().intValue();
                    out.write(intToBytes(operand));
                }

                if(sizeR == 0) continue;
                else if(sizeR == 4){
                    int operand = order.getOperandsInt().getFirst().intValue();
                    out.write(intToBytes(operand));
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void writeFunctionCode(DataOutputStream out){
        try{
            FunctionTable functionTable = FunctionTable.getFunctionTable();
            short count = (short)functionTable.functions.size();
            out.write(shortToBytes(count));
            for(int i=0;i<count;i++){
                Function function = functionTable.functions.get(i);
                short nameIndex = (short)i;
                short paramsSize = (short)function.getParamsSize();
                short level = 1;
                short instructionCount = (short)function.orders.size();
                out.write(shortToBytes(nameIndex));
                out.write(shortToBytes(paramsSize));
                out.write(shortToBytes(level));
                out.write(shortToBytes(instructionCount));
                for(int j=0;j<instructionCount;j++) {
                    Order order = function.orders.get(j);
                    Pair<Integer, Pair<Integer, Integer>> instruction = getInstruction(order.getOpcode());
                    byte opcode = (byte) instruction.getFirst().intValue();
                    int sizeL = instruction.getSecond().getFirst();
                    int sizeR = instruction.getSecond().getSecond();
                    out.write(opcode);
                    if (sizeL == 0) continue;
                    if (sizeL == 1) {
                        char operand = (char) order.getOperandsInt().getFirst().intValue();
                        out.write(operand);
                    } else if (sizeL == 2) {
                        short operand = (short) order.getOperandsInt().getFirst().intValue();
                        out.write(shortToBytes(operand));
                    } else if (sizeL == 4) {
                        int operand = order.getOperandsInt().getFirst().intValue();
                        out.write(intToBytes(operand));
                    }

                    if (sizeR == 0) continue;
                    else if (sizeR == 4) {
                        int operand = order.getOperandsInt().getFirst().intValue();
                        out.write(intToBytes(operand));
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
