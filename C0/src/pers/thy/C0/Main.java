package pers.thy.C0;

import argparser.*;
import pers.thy.C0.analyser.AST;
import pers.thy.C0.analyser.Analyser;
import pers.thy.C0.analyser.c0ProgramAST;
import pers.thy.C0.error.Cerror;
import pers.thy.C0.generator.Ogenerator;
import pers.thy.C0.generator.Sgenerator;
import pers.thy.C0.symboltable.ConstantTable;
import pers.thy.C0.symboltable.Function;
import pers.thy.C0.symboltable.FunctionTable;
import pers.thy.C0.symboltable.StartCodeTable;
import pers.thy.C0.utils.Pair;
import pers.thy.C0.tokenizer.*;

import java.util.ArrayList;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {

//        StringHolder options = new StringHolder();
//        StringHolder ofile = new StringHolder();
//        options.value = "111";
//
//        // create the parser and specify the allowed options ...
//
//        ArgParser parser = new ArgParser("cc0 [options] input [-o file]\n" +
//                "or \n" +
//                "  cc0 [-h]");
//        parser.addOption ("-s %s #translate input c0 code to text assembly file", options);
//        parser.addOption ("-c %s #translate input c0 code to binary target file", options);
//        parser.addOption("-h %h #show this help message and exit",options);
//        parser.addOption("-o %s #file output to specified file",ofile);
//
//        String[] help = new String[]{"-h"};
//        parser.matchAllArgs(help);
//
//        System.out.println ("options=" + options.value);
//        System.out.println ("file=" + ofile.value);
        Tokenizer tokenizer = new Tokenizer("./src/pers/thy/C0/test.txt");
        ArrayList<Pair<Optional<Token>,Optional<Cerror> > > tokens = tokenizer.AllTokens();
        ArrayList<Token> token = new ArrayList<>();
        for(int i=0;i<tokens.size();i++) {
//            if(tokens.get(i).getSecond().isPresent()) {
//                Cerror cerror = tokens.get(i).getSecond().get();
//                System.err.printf("Line: {%d} Column: {%d} Error: {\'%s\'}\n", cerror.getPos().getFirst(), cerror.getPos().getSecond(), cerror.getCode());
//            }else if(tokens.get(i).getFirst().isPresent()) {
//                Token token = tokens.get(i).getFirst().get();
//                System.out.printf("Line: {%d} Column: {%d} Type: {\'%s\'} Value: {%s}\n", token.getStartPos().getFirst(), token.getStartPos().getSecond(), token.getType(), token.getValue());
//            }
            token.add(tokens.get(i).getFirst().get());
        }
        Analyser analyser = new Analyser(token);
        AST c0Program = analyser.analyseC0Program();
        c0ProgramAST c0ProgramAST = (c0ProgramAST)c0Program;
        c0ProgramAST.generate();
        Sgenerator sgenerator = new Sgenerator("./src/pers/thy/C0/output.txt");
        sgenerator.generate();
        Ogenerator ogenerator = new Ogenerator("./src/pers/thy/C0/out.o0");
        ogenerator.generate();
        System.out.println("finished");
    }
}
