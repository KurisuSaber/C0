package pers.thy.C0;

import argparser.ArgParser;
import argparser.StringHolder;
import pers.thy.C0.analyser.AST;
import pers.thy.C0.analyser.Analyser;
import pers.thy.C0.analyser.c0ProgramAST;
import pers.thy.C0.error.Cerror;
import pers.thy.C0.generator.Ogenerator;
import pers.thy.C0.generator.Sgenerator;
import pers.thy.C0.utils.Pair;
import pers.thy.C0.tokenizer.*;

import java.util.ArrayList;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {

        StringHolder sInputFile = new StringHolder();
        StringHolder oInputFile = new StringHolder();
        StringHolder outputFile = new StringHolder();
        StringHolder help = new StringHolder();

        // create the parser and specify the allowed options ...

        ArgParser parser = new ArgParser("java -jar C0.jar [options] input [-o file]\n" +
                "or \n" +
                "  java -jar C0.jar [-h]");
        parser.addOption ("-s %s #translate input c0 code to text assembly file", sInputFile);
        parser.addOption ("-c %s #translate input c0 code to binary target file", oInputFile);
        parser.addOption("-h %h #show this help message and exit",help);
        parser.addOption("-o %s #file output to specified file",outputFile);

        //String[] test = new String[]{"-s","./src/pers/thy/C0/test.txt"};
        parser.matchAllArgs(args);

        String inputFilePath = "";

        if(sInputFile.value == null && oInputFile.value == null){//print help
            String[] helpArg = new String[]{"-h"};
            parser.matchAllArgs(helpArg);
        }else{//run
            inputFilePath = sInputFile.value == null ? oInputFile.value : sInputFile.value;
            Tokenizer tokenizer = new Tokenizer(inputFilePath);
            ArrayList<Pair<Optional<Token>,Optional<Cerror> > > tokens = tokenizer.AllTokens();
            ArrayList<Token> token = new ArrayList<>();
            for(int i=0;i<tokens.size();i++) {
                if(tokens.get(i).getSecond().isPresent()) {
                    Cerror cerror = tokens.get(i).getSecond().get();
                    System.err.printf("Line: {%d} Column: {%d} Error: {\'%s\'}\n", cerror.getPos().getFirst(), cerror.getPos().getSecond(), cerror.getCode());
                }
                token.add(tokens.get(i).getFirst().get());
            }
            Analyser analyser = new Analyser(token);
            AST c0Program = analyser.analyseC0Program();
            c0ProgramAST c0ProgramAST = (c0ProgramAST)c0Program;
            c0ProgramAST.generate();
            if(sInputFile.value!=null){
                Sgenerator sgenerator = new Sgenerator(outputFile.value == null ? "out" : outputFile.value);
                sgenerator.generate();
            }else if(oInputFile.value!=null){
                Ogenerator ogenerator = new Ogenerator(outputFile.value == null ? "out" : outputFile.value);
                ogenerator.generate();
            }
            System.out.println("finished!");
        }

    }
}
