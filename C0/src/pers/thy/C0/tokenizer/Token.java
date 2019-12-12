package pers.thy.C0.tokenizer;

import pers.thy.C0.utils.Pair;

public class Token {
    public enum TokenType{
        NULLTOKEN,
        DECIMAL,
        ZERO,
        HEXADECIMAL,
        IDENTIFIER,
        RESERVED,
        SINGLE_QUOTE,
        CHAR,
        DOUBLE_QUOTE,
        STRING,
        COLON,
        LEFT_BRACKET,
        RIGHT_BRACKET,
        RIGHT_BRACE,
        LEFT_BRACE,
        COMMA,
        SEMICOLON,
        NOT,
        NOTEQUAL,
        LESS,
        LESS_EQUAL,
        GREATER,
        GREATER_EQUAL,
        ASSIGN,
        EQUAL,
        MINUS_SIGN,
        PLUS_SIGN,
        MULTIPLY_SIGN,
        DIVIDE_SIGN,
        SINGLE_COMMENT,
        MORE_COMMENT,
        COMMENT,
        CONST,
        VOID,
        INT,
        DOUBLE,
        STRUCT,
        IF,
        ELSE,
        SWITCH,
        CASE,
        DEFAULT,
        WHILE,
        FOR,
        DO,
        RETURN,
        BREAK,
        CONTINUE,
        PRINT,
        SCAN
    }

    TokenType type;
    String value;
    Pair<Integer,Integer> startPos;
    Pair<Integer,Integer> endPos;

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Pair<Integer, Integer> getStartPos() {
        return startPos;
    }

    public Pair<Integer, Integer> getEndPos() {
        return endPos;
    }

    public Token() {
    }

    public Token(TokenType type, String value, Pair<Integer, Integer> startPos, Pair<Integer, Integer> endPos) {
        this.type = type;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
    }
}
