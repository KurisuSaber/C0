package pers.thy.C0.error;

import pers.thy.C0.utils.Pair;

public class Cerror {
    public void DieAndPrint(String condition){
        System.out.println("Exception: "+condition);
        System.out.println("The program should not reach here.");
        System.out.println("Please check your program carefully.\n");
        //TODO: 优化级 throw exception
    }

    public enum ErrorCode{
        ErrNoError, // Should be only used internally.
        ErrStreamError,
        ErrEOF,
        ErrInvalidInput,
        ErrInvalidIdentifier,
        ErrInvalidHexdecimal,
        ErrIntegerOverflow, // int32_t overflow.
        ErrNoBegin,
        ErrNoEnd,
        ErrNeedIdentifier,
        ErrConstantNeedValue,
        ErrNoSemicolon,
        ErrInvalidVariableDeclaration,
        ErrIncompleteExpression,
        ErrNotDeclared,
        ErrAssignToConstant,
        ErrDuplicateDeclaration,
        ErrNotInitialized,
        ErrInvalidAssignment,
        ErrInvalidPrint
    }

    ErrorCode err;
    int line;
    int column;
    Pair<Integer,Integer> pos;

    public ErrorCode getCode(){ return err;}

    public Pair<Integer, Integer> getPos() {
        return pos;
    }

    public Cerror() {
    }

    public Cerror(int line, int column, ErrorCode err) {
        this.err = err;
        this.line = line;
        this.column = column;
        pos = new Pair<>(line,column);
    }

    public Cerror(Pair<Integer, Integer> pos, ErrorCode err) {
        this.err = err;
        this.pos = pos;
    }
}
