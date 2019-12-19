package pers.thy.C0.tokenizer;

import pers.thy.C0.utils.Pair;
import pers.thy.C0.error.Cerror;

import java.io.*;
import java.math.BigInteger;
import java.util.*;


//TODO: 优化级 考虑单例模式的错误处理方式 先用mini的思路写
public class Tokenizer {

    Map<String,Token.TokenType> table = new HashMap<String,Token.TokenType>();

    String maxInt = Integer.toString(Integer.MAX_VALUE);
    String maxHex = "7fffffff";

    Pair<Optional<Token>,Optional<Cerror>> NextToken(){
        if(!initialized)
            readAll();
        //TODO: 遗漏级 考虑文件指针错误情况
        if(isEOF())
            return new Pair<>(Optional.empty(),Optional.of(new Cerror(0,0,Cerror.ErrorCode.ErrEOF)));
        Pair<Optional<Token>,Optional<Cerror>> p = nextToken();
        if(p.getSecond().isPresent()){//没有错误
            return new Pair<>(p.getFirst(),p.getSecond());
        }
        //标识符的合法性在这里检查 如遇到123abc这种 nextToken()只负责解析出来 在这里判断
        Optional<Cerror> err = checkToken(p.getFirst().get());
        if(err.isPresent()){
            return new Pair<>(p.getFirst(), Optional.of(err.get()));
        }
        return new Pair<>(p.getFirst(),Optional.empty());

    }

    public ArrayList<Pair<Optional<Token>,Optional<Cerror> > > AllTokens(){
        ArrayList<Pair<Optional<Token>,Optional<Cerror> > > result = new ArrayList<>();
        while(true){
            Pair<Optional<Token>,Optional<Cerror>> p = NextToken();
            if(p.getSecond().isPresent()){
                if(p.getSecond().get().getCode() == Cerror.ErrorCode.ErrEOF){
                    return result;
                }
            }
            result.add(p);
        }
    }

    Pair<Optional<Token>,Optional<Cerror>> getIdentifier(StringBuffer ss,Pair<Integer,Integer> pos){
        String tokenString = ss.toString();
        if(table.get(tokenString)!=null){
            return new Pair<>(Optional.of(new Token(table.get(tokenString),tokenString,pos,currentPos())),Optional.empty());
        }else{
            return new Pair<>(Optional.of(new Token(Token.TokenType.IDENTIFIER,tokenString,pos,currentPos())),Optional.empty());
        }
    }

    //词法分析
    Pair<Optional<Token>,Optional<Cerror>> nextToken(){
        //存储已经读到的组成当前token的字符
        StringBuffer ss = new StringBuffer();
        //分析token结果 作为返回值
        Pair<Optional<Token>,Optional<Cerror>> result;
        //当前token第一个字符的位置
        Pair<Integer,Integer> pos = new Pair<>(0,0);
        //自动机状态
        DFAstate currentState = DFAstate.INITIAL_STATE;

        while(true){
            Optional<Character> currentChar = nextChar();

            switch (currentState){
                case INITIAL_STATE:{
                    //读到文件尾
                    if(!currentChar.isPresent()){
                        //返回空token 和ErrEOF
                        return new Pair<>(Optional.empty(),Optional.of(new Cerror(0,0,Cerror.ErrorCode.ErrEOF)));
                    }
                    char ch = currentChar.get();
                    boolean invalid = false;

                    if(Character.isWhitespace(ch))//空白符 维持初始态
                        currentState = DFAstate.INITIAL_STATE;
                    else if(Character.isISOControl(ch))//非可见字符 不合法
                        invalid = true;
                    else if(Character.isDigit(ch) && ch=='0')//0开头 16进制数
                        currentState = DFAstate.ZERO_STATE;
                    else if(Character.isDigit(ch) && ch!='0')//非0开头 10进制数
                        currentState = DFAstate.DECIMAL_STATE;
                    else if(Character.isAlphabetic(ch))//字母开头 标识符
                        currentState = DFAstate.IDENTIFIER_STATE;
                    else{
                        switch (ch){
                            case ':':
                                currentState = DFAstate.COLON_STATE;
                                break;

                            case '(':
                                currentState = DFAstate.LEFT_BRACKET_STATE;
                                break;

                            case ')':
                                currentState = DFAstate.RIGHT_BRACKET_STATE;
                                break;

                            case '{':
                                currentState = DFAstate.LEFT_BRACE_STATE;
                                break;

                            case '}':
                                currentState = DFAstate.RIGHT_BRACE_STATE;
                                break;

                            case ',':
                                currentState = DFAstate.COMMA_STATE;
                                break;

                            case ';':
                                currentState = DFAstate.SEMICOLON_STATE;
                                break;

                            case '!':
                                currentState = DFAstate.NOT_STATE;
                                break;

                            case '<':
                                currentState = DFAstate.LESS_STATE;
                                break;

                            case '>':
                                currentState = DFAstate.GREATER_STATE;
                                break;

                            case '=':
                                currentState = DFAstate.ASSIGN_STATE;
                                break;

                            case '-':
                                currentState = DFAstate.MINUS_SIGN_STATE;
                                break;

                            case '/':
                                currentState = DFAstate.DIVIDE_SIGN_STATE;
                                break;

                            case '+':
                                currentState = DFAstate.PLUS_SIGN_STATE;
                                break;

                            case '*':
                                currentState = DFAstate.MULTIPLY_SIGN_STATE;
                                break;

                            default:
                                invalid  =true;
                                break;
                        }
                    }


                    // 如果读到的字符导致了状态的转移，说明它是一个token的第一个字符
                    if(currentState != DFAstate.INITIAL_STATE)
                        pos = previousPos();//记录位置

                    //TODO: 遗漏级 报错逻辑问题修正 这里的pos永远是0,0

                    //不合法字符
                    if(invalid){
                        unreadLast();
                        return new Pair<>(Optional.empty(),Optional.of(new Cerror(pos,Cerror.ErrorCode.ErrInvalidInput)));
                    }

                    if(currentState != DFAstate.INITIAL_STATE)
                        ss.append(ch);

                    break;
                }

                case DECIMAL_STATE:{
                    //读到了文件尾 转换为十进制数
                    if(!currentChar.isPresent()){
                        String tokenValue = ss.toString();
                        if(tokenValue.length()>=10 && tokenValue.compareTo(maxInt) > 0)
                            cerror.DieAndPrint("Number is too big!");
                        return new Pair<>(Optional.of(new Token(Token.TokenType.DECIMAL,tokenValue,pos,currentPos())),Optional.empty());
                    }

                    char ch = currentChar.get();
                    //是数字则存储
                    if(Character.isDigit(ch))
                        ss.append(ch);

                    //是字母 存储 跳到标识符
                    //回退 解析
                    else{
                        unreadLast();
                        String tokenValue = ss.toString();
                        if(tokenValue.length()>=10 && tokenValue.compareTo(maxInt) > 0)
                            cerror.DieAndPrint("Number is too big!");
                        return new Pair<>(Optional.of(new Token(Token.TokenType.DECIMAL,tokenValue,pos,currentPos())),Optional.empty());
                    }

                    break;
                }

                case IDENTIFIER_STATE:{
                    //文件尾 解析 考虑关键字
                    if(!currentChar.isPresent())
                        return getIdentifier(ss,pos);

                    //如果读到的是字符 存储
                    char ch = currentChar.get();
                    if(Character.isDigit(ch) || Character.isAlphabetic(ch)){
                        ss.append(ch);
                    }

                    else{
                        unreadLast();
                        String tokenString = ss.toString();
                        return getIdentifier(ss,pos);
                    }
                    break;
                }

                case ZERO_STATE:{
                    if(!currentChar.isPresent()) {
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.DECIMAL, tokenValue, pos, currentPos())), Optional.empty());
                    }

                    char ch = currentChar.get();
                    if(ch == 'x' || ch == 'X'){
                        ss.append(ch);
                        currentState = DFAstate.HEXADECIMAL_STATE;
                    }


                    else{
                        unreadLast();
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.DECIMAL, tokenValue, pos, currentPos())), Optional.empty());
                    }
                    break;

                }

                case HEXADECIMAL_STATE:{
                    if(!currentChar.isPresent()){
                        String tokenValue = ss.toString().substring(2);
                        if(tokenValue.length() >= 8 && tokenValue.compareTo(maxHex) > 0)
                            cerror.DieAndPrint("Number is too big");
                        tokenValue = new BigInteger(tokenValue, 16).toString(10);
                        return new Pair<>(Optional.of(new Token(Token.TokenType.HEXADECIMAL, tokenValue, pos, currentPos())), Optional.empty());
                    }

                    char ch = currentChar.get();
                    if(Character.isDigit(ch) || ('a'<=ch && ch<='f') || ('A'<=ch && ch<='F')){
                        ss.append(ch);
                    }

                    else{
                        unreadLast();
                        String tokenValue = ss.toString().substring(2).toLowerCase();
                        if(tokenValue.length() >= 8 && tokenValue.compareTo(maxHex) > 0)
                            cerror.DieAndPrint("Number is too big");
                        tokenValue = new BigInteger(tokenValue, 16).toString(10);
                        return new Pair<>(Optional.of(new Token(Token.TokenType.HEXADECIMAL, tokenValue, pos, currentPos())), Optional.empty());

                    }
                    break;
                }

                //TODO: 提醒级 这里把报错放在了checkToken !认为也是一种token
                case NOT_STATE:{
                    if (!currentChar.isPresent()) {
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.NOT, tokenValue, pos, currentPos())), Optional.empty());
                    }

                    char ch = currentChar.get();
                    if(ch == '='){
                        ss.append(ch);
                        currentState = DFAstate.NOTEQUAL_STATE;
                    }
                    //在基础C0中 !之后其他字符 错误状态
                    else{
                        unreadLast();
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.NOT, tokenValue, pos, currentPos())), Optional.empty());
                    }
                    break;
                }

                case LESS_STATE:{
                    if(!currentChar.isPresent()){
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.LESS, tokenValue, pos, currentPos())), Optional.empty());
                    }

                    char ch = currentChar.get();
                    if(ch == '='){
                        ss.append(ch);
                        currentState = DFAstate.LESS_EQUAL_STATE;
                    }

                    else{
                        unreadLast();
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.LESS, tokenValue, pos, currentPos())), Optional.empty());
                    }
                    break;
                }

                case LESS_EQUAL_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.LESS_EQUAL, "<=", pos, currentPos())), Optional.empty());
                }

                case GREATER_STATE:{
                    if(!currentChar.isPresent()){
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.GREATER, tokenValue, pos, currentPos())), Optional.empty());
                    }

                    char ch = currentChar.get();
                    if(ch == '='){
                        ss.append(ch);
                        currentState = DFAstate.GREATER_EQUAL_STATE;
                    }

                    else{
                        unreadLast();
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.GREATER, tokenValue, pos, currentPos())), Optional.empty());
                    }
                    break;
                }

                case GREATER_EQUAL_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.GREATER_EQUAL, ">=", pos, currentPos())), Optional.empty());
                }

                case ASSIGN_STATE:{
                    if(!currentChar.isPresent()){
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.ASSIGN, tokenValue, pos, currentPos())), Optional.empty());
                    }

                    char ch = currentChar.get();
                    if(ch == '='){
                        ss.append(ch);
                        currentState = DFAstate.EQUAL_STATE;
                    }

                    else{
                        unreadLast();
                        String tokenValue = ss.toString();
                        return new Pair<>(Optional.of(new Token(Token.TokenType.ASSIGN, tokenValue, pos, currentPos())), Optional.empty());
                    }
                    break;
                }

                case EQUAL_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.EQUAL, "==", pos, currentPos())), Optional.empty());
                }

                case NOTEQUAL_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.NOTEQUAL, "!=", pos, currentPos())), Optional.empty());
                }

                case COLON_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.COLON, ":", pos, currentPos())), Optional.empty());
                }

                case LEFT_BRACKET_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.LEFT_BRACKET, "(", pos, currentPos())), Optional.empty());
                }

                case RIGHT_BRACKET_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.RIGHT_BRACKET, ")", pos, currentPos())), Optional.empty());
                }

                case LEFT_BRACE_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.LEFT_BRACE, "{", pos, currentPos())), Optional.empty());
                }

                case RIGHT_BRACE_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.RIGHT_BRACE, "}", pos, currentPos())), Optional.empty());
                }

                case COMMA_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.COMMA, ",", pos, currentPos())), Optional.empty());
                }

                case SEMICOLON_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.SEMICOLON, ";", pos, currentPos())), Optional.empty());
                }

                case MINUS_SIGN_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.MINUS_SIGN, "-", pos, currentPos())), Optional.empty());
                }

                case PLUS_SIGN_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.PLUS_SIGN, "+", pos, currentPos())), Optional.empty());
                }

                case MULTIPLY_SIGN_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.MULTIPLY_SIGN, "*", pos, currentPos())), Optional.empty());
                }

                case DIVIDE_SIGN_STATE:{
                    unreadLast();
                    return new Pair<>(Optional.of(new Token(Token.TokenType.DIVIDE_SIGN, "/", pos, currentPos())), Optional.empty());
                }

                default:
                    cerror.DieAndPrint("unhandled state.");
                    break;

            }
        }

        //TODO: 提醒级 助教的mini加了return
    }

    enum DFAstate{
        INITIAL_STATE,
        DECIMAL_STATE,
        ZERO_STATE,
        HEXADECIMAL_STATE,
        IDENTIFIER_STATE,
        RESERVED_STATE,
        SINGLE_QUOTE_STATE,
        CHAR_STATE,
        DOUBLEQUOTE_STATE,
        STRING_STATE,
        COLON_STATE,
        LEFT_BRACKET_STATE,
        RIGHT_BRACKET_STATE,
        RIGHT_BRACE_STATE,
        LEFT_BRACE_STATE,
        COMMA_STATE,
        SEMICOLON_STATE,
        NOT_STATE,
        NOTEQUAL_STATE,
        LESS_STATE,
        LESS_EQUAL_STATE,
        GREATER_STATE,
        GREATER_EQUAL_STATE,
        ASSIGN_STATE,
        EQUAL_STATE,
        MINUS_SIGN_STATE,
        PLUS_SIGN_STATE,
        MULTIPLY_SIGN_STATE,
        DIVIDE_SIGN_STATE,
        SINGLE_COMMENT_STATE,
        MORE_COMMENT_STATE,
        COMMENT_STATE,
    }


    //没有初始化则readAll
    boolean initialized;
    //要读的文件
    FileReader fr;
    //指向下一个要读取的字符
    Pair<Integer,Integer> ptr;
    //按行为基础的缓冲区设计
    ArrayList<String> linesBuffer = new ArrayList<>();
    //错误
    Cerror cerror = new Cerror();

    public Tokenizer(String filename) {
        try {
            File f = new File(filename);
            if(f.exists() && f.isFile())
                f.delete();
            if(f.isDirectory()) {
                System.out.println("this is a directory, are you sure you typed the correct output file?");
                System.exit(0);
            }
            if(!f.exists()){
                System.out.println("file does not exist!");
                System.exit(0);
            }
            fr = new FileReader(filename);
        }catch (Exception e){
            e.printStackTrace();
        }
        table.put("const", Token.TokenType.CONST);
        table.put("void", Token.TokenType.VOID);
        table.put("int", Token.TokenType.INT);
        table.put("char", Token.TokenType.CHAR);
        table.put("double", Token.TokenType.DOUBLE);
        table.put("struct", Token.TokenType.STRUCT);
        table.put("if", Token.TokenType.IF);
        table.put("else", Token.TokenType.ELSE);
        table.put("switch", Token.TokenType.SWITCH);
        table.put("case", Token.TokenType.CASE);
        table.put("default", Token.TokenType.DEFAULT);
        table.put("while", Token.TokenType.WHILE);
        table.put("for", Token.TokenType.FOR);
        table.put("do",Token.TokenType.DO);
        table.put("return",Token.TokenType.RETURN);
        table.put("break",Token.TokenType.BREAK);
        table.put("continue",Token.TokenType.CONTINUE);
        table.put("print",Token.TokenType.PRINT);
        table.put("scan",Token.TokenType.SCAN);
    }

    //检查标识符合法性 和!问题
    Optional<Cerror> checkToken(Token t){
        switch (t.getType()){
            case IDENTIFIER:{
                String val = t.getValue();
                if(Character.isDigit(val.charAt(0))){
                    return Optional.of(new Cerror(t.getStartPos().getFirst(),t.getStartPos().getSecond(),Cerror.ErrorCode.ErrInvalidIdentifier));
                }
                break;
            }
            case NOT:
                return Optional.of(new Cerror(t.getStartPos().getFirst(),t.getStartPos().getSecond(),Cerror.ErrorCode.ErrInvalidInput));

            case HEXADECIMAL:{
                String val = t.getValue();
                if(val.length() ==2 ){
                    return Optional.of(new Cerror(t.getStartPos().getFirst(),t.getStartPos().getSecond(),Cerror.ErrorCode.ErrInvalidHexdecimal));
                }
                break;
            }
            default: break;
        }
        return Optional.empty();
    }

    //全部读进行缓冲里
    void readAll(){
        if(initialized)
            return;
        initialized = true;
        ptr = new Pair<>(0,0);
        try {
            BufferedReader bf = new BufferedReader(fr);
            String str;
            while ((str = bf.readLine()) != null){
                linesBuffer.add(str+"\n");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    //下一个要读的字符的位置
    Pair<Integer,Integer> nextPos(){
        if(ptr.getFirst() >= linesBuffer.size()){
            cerror.DieAndPrint("advance after EOF");
        }
        if(ptr.getSecond() == linesBuffer.get(ptr.getFirst()).length()-1)
            return new Pair<>(ptr.getFirst()+1,0);
        else
            return new Pair<>(ptr.getFirst(),ptr.getSecond()+1);
    }

    Pair<Integer,Integer> currentPos(){
        return ptr;
    }

    Pair<Integer,Integer> previousPos(){
        if(ptr.getFirst() == 0 && ptr.getSecond() == 0) {
            cerror.DieAndPrint("privious position from beginning");
        }
        if(ptr.getSecond() == 0)
            return new Pair<>(ptr.getFirst()-1,linesBuffer.get(ptr.getFirst()-1).length()-1);
        else
            return new Pair<>(ptr.getFirst(),ptr.getSecond()-1);
    }

    Optional<Character> nextChar(){
        if(isEOF())
            return Optional.empty();
        char result = linesBuffer.get(ptr.getFirst()).charAt(ptr.getSecond());
        ptr = nextPos();
        return Optional.of(result);
    }

    boolean isEOF(){
        return ptr.getFirst() >= linesBuffer.size();
    }

    void unreadLast(){
        ptr = previousPos();
    }


}
