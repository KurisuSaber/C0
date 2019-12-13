package pers.thy.C0.analyser;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import pers.thy.C0.error.Cerror;
import pers.thy.C0.tokenizer.Token;
import pers.thy.C0.utils.Pair;
import pers.thy.C0.analyser.*;
import sun.jvm.hotspot.debugger.cdbg.Sym;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Optional;

public class Analyser {
    private ArrayList<Token> tokens = new ArrayList<>();
    private int offset=0;
    private Pair<Integer,Integer> currentPos;
    private Cerror cerror = new Cerror();
    public ArrayList<Cerror> errors = new ArrayList<>();
    public ArrayList<Symbol> globalVariableTable = new ArrayList<>();
    public ArrayList<Symbol> localVariableTable = new ArrayList<>();
    public ArrayList<Symbol> functionTable = new ArrayList<>();
    private int countVG = 0;
    private int countVL = 0;
    private int countF = 0;

    private ArrayList<Token.TokenType> statements = new ArrayList<>();

    //<digit> ::=
    //    '0'|<nonzero-digit>
    //<nonzero-digit> ::=
    //    '1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'
    //<hexadecimal-digit> ::=
    //    <digit>|'a'|'b'|'c'|'d'|'e'|'f'|'A'|'B'|'C'|'D'|'E'|'F'
    //
    //<integer-literal> ::=
    //    <decimal-literal>|<hexadecimal-literal>
    //<decimal-literal> ::=
    //    '0'|<nonzero-digit>{<digit>}
    //<hexadecimal-literal> ::=
    //    ('0x'|'0X')<hexadecimal-digit>{<hexadecimal-digit>}
    //
    //
    //<nondigit> ::=    'a'|'b'|'c'|'d'|'e'|'f'|'g'|'h'|'i'|'j'|'k'|'l'|'m'|'n'|'o'|'p'|'q'|'r'|'s'|'t'|'u'|'v'|'w'|'x'|'y'|'z'|'A'|'B'|'C'|'D'|'E'|'F'|'G'|'H'|'I'|'J'|'K'|'L'|'M'|'N'|'O'|'P'|'Q'|'R'|'S'|'T'|'U'|'V'|'W'|'X'|'Y'|'Z'
    //
    //<identifier> ::=
    //    <nondigit>{<nondigit>|<digit>}
    //<reserved-word> ::=
    //     'const'
    //    |'void'   |'int'    |'char'   |'double'
    //    |'struct'
    //    |'if'     |'else'
    //    |'switch' |'case'   |'default'
    //    |'while'  |'for'    |'do'
    //    |'return' |'break'  |'continue'
    //    |'print'  |'scan'
    //
    //<unary-operator>          ::= '+' | '-'
    //<additive-operator>       ::= '+' | '-'
    //<multiplicative-operator> ::= '*' | '/'
    //<relational-operator>     ::= '<' | '<=' | '>' | '>=' | '!=' | '=='
    //<assignment-operator>     ::= '='
    //
    //
    //​
    //<type-specifier>         ::= <simple-type-specifier>
    //<simple-type-specifier>  ::= 'void'|'int'
    //<const-qualifier>        ::= 'const'
    //
    //​
    //<C0-program> ::=
    //​    {<variable-declaration>}{<function-definition>}
    public c0ProgramAST analyseC0Program(){
        ArrayList<variableDeclarationAST> vd = analyseVariableDeclaration();
        ArrayList<functionDefinitionAST> fd = analyseFunctionDefinition();

        c0ProgramAST result = new c0ProgramAST(vd,fd);
        return result;
    }
    //<variable-declaration> ::=
    //    [<const-qualifier>]<type-specifier><init-declarator-list>';'
    ArrayList<variableDeclarationAST> analyseVariableDeclaration(){
        ArrayList<variableDeclarationAST> result = new ArrayList<>();
        String typeSpecifier = "";

        //进行<variable-declaration>分析
        while (true) {
            Optional<Token> cq = nextToken();
            String constQualifier = "";
            //暴力一点 一上来就连读三个判断该不该用这个文法分析 只要任意一个地方读不了 必有错误
            if (cq.isPresent() && (cq.get().getType() == Token.TokenType.CONST ||
                    cq.get().getType() == Token.TokenType.VOID ||
                    cq.get().getType() == Token.TokenType.INT
                    )) {
                constQualifier = cq.get().getValue();
                cq = nextToken();
                if (cq.isPresent()) {
                    cq = nextToken();
                    if (cq.isPresent()) {//<function-definition>
                        if (cq.get().getValue().equals("(")) {
                            unreadToken();
                            unreadToken();
                            unreadToken();
                            return result;
                        } else{
                            unreadToken();
                            unreadToken();
                        }
                    } else {
                        cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression, currentPos);
                    }
                } else {
                    cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression, currentPos);
                }
            } else {
                if(cq.isPresent()) unreadToken();
                return result;
            }

            if (!constQualifier.equals("const")) {//TODO: 遗漏级
                unreadToken();
            }


            cq = nextToken();
            if (!cq.isPresent() || (!cq.get().getValue().equals("void") && !cq.get().getValue().equals("int"))) {
                cerror.Error(Cerror.ErrorCode.ErrNeedTypeSpecifier, currentPos);
            }
            typeSpecifier = cq.get().getValue();

            initDeclaratorListAST initDeclaratorList = analyseInitDeclaratorList();
            ArrayList<initDeclaratorAST> variables = initDeclaratorList.initDeclarator;

            cq = nextToken();
            if (!cq.isPresent() || !cq.get().getValue().equals(";"))
                cerror.Error(Cerror.ErrorCode.ErrNoSemicolon, currentPos);

            if (constQualifier.equals("const")) {
                result.add(new variableDeclarationAST(Optional.of(constQualifier), typeSpecifier, initDeclaratorList));
                for(int i=0;i<variables.size();i++){
                    String identifier = variables.get(i).identifier;
                    if(functionTable.isEmpty())
                        addGlobalVariable(identifier,"const",typeSpecifier);
                    else
                        addLocalVariable(identifier,"const",typeSpecifier);
                }
            } else {
                result.add(new variableDeclarationAST(Optional.empty(), typeSpecifier, initDeclaratorList));
                for(int i=0;i<variables.size();i++){
                    String identifier = variables.get(i).identifier;
                    if(functionTable.isEmpty())
                        addGlobalVariable(identifier,"variable",typeSpecifier);
                    else
                        addLocalVariable(identifier,"variable",typeSpecifier);
                }
            }
        }
    }
    //<init-declarator-list> ::=
    //    <init-declarator>{','<init-declarator>}
    initDeclaratorListAST analyseInitDeclaratorList(){
        ArrayList<initDeclaratorAST> result = new ArrayList<>();
        initDeclaratorAST initDeclarator = analyseInitDeclarator();

        result.add(initDeclarator);

        while (true){
            Optional<Token> cq = nextToken();
            if(!cq.isPresent())
                return new initDeclaratorListAST(result);
            if(!cq.get().getValue().equals(",")) {
                unreadToken();
                return new initDeclaratorListAST(result);
            }

            initDeclarator = analyseInitDeclarator();
            result.add(initDeclarator);
        }
    }

    //<init-declarator> ::=
    //    <identifier>[<initializer>]
    initDeclaratorAST analyseInitDeclarator(){
        Optional<Token> cq = nextToken();
        if(!cq.isPresent() || cq.get().getType()!= Token.TokenType.IDENTIFIER)
            cerror.Error(Cerror.ErrorCode.ErrNeedIdentifier,currentPos);
        String identifier = cq.get().getValue();

        cq = nextToken();
        if(!cq.isPresent())
            return new initDeclaratorAST(identifier,Optional.empty());
        if(cq.get().getType()!=Token.TokenType.ASSIGN){
            unreadToken();
            return new initDeclaratorAST(identifier,Optional.empty());
        }

        unreadToken();
        initializerAST initializer = analyseInitializer();

        return new initDeclaratorAST(identifier,Optional.of(initializer));

    }
    //<initializer> ::=
    //    '='<expression>
    initializerAST analyseInitializer(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.ASSIGN)
            cerror.Error(Cerror.ErrorCode.ErrNeedAssign,currentPos);

        expressionAST expression = analyseExpression();
        return new initializerAST(expression);
    }
    //<function-definition> ::=
    //    <type-specifier><identifier><parameter-clause><compound-statement>
    //
    ArrayList<functionDefinitionAST> analyseFunctionDefinition(){
        ArrayList<functionDefinitionAST> result = new ArrayList<>();
        String typeSpecifier = "";
        String identifier = "";

        while(true) {
            Optional<Token> tk = nextToken();
            if(!tk.isPresent())
                return result;
            if ((tk.get().getType() != Token.TokenType.VOID && tk.get().getType() != Token.TokenType.INT))
                cerror.Error(Cerror.ErrorCode.ErrNeedTypeSpecifier, currentPos);
            typeSpecifier = tk.get().getValue();

            tk = nextToken();
            if (!tk.isPresent() || tk.get().getType() != Token.TokenType.IDENTIFIER)
                cerror.Error(Cerror.ErrorCode.ErrNeedIdentifier, currentPos);
            identifier = tk.get().getValue();

            addFunction(identifier,"function",typeSpecifier,0,"");

            parameterClauseAST parameterClause = analyseParameterClause();
            compoundStatementAST compoundStatement = analyseCompundStatement();

            result.add(new functionDefinitionAST(typeSpecifier, identifier, parameterClause, compoundStatement));
        }
    }
    //<parameter-clause> ::=
    //    '(' [<parameter-declaration-list>] ')'
    parameterClauseAST analyseParameterClause(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.LEFT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBracket,currentPos);

        tk = nextToken();
        if(!tk.isPresent())
            cerror.Error(Cerror.ErrorCode.ErrNoRightBracket,currentPos);
        if(tk.get().getType()!=Token.TokenType.RIGHT_BRACKET){
            unreadToken();
            parameterDeclarationListAST parameterDeclarationList = analyseParameterDeclarationList();
            tk = nextToken();
            if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.RIGHT_BRACKET)
                cerror.Error(Cerror.ErrorCode.ErrNoRightBracket,currentPos);
            return new parameterClauseAST(Optional.of(parameterDeclarationList));
        }

        return new parameterClauseAST(Optional.empty());
    }
    //<parameter-declaration-list> ::=
    //    <parameter-declaration>{','<parameter-declaration>}
    parameterDeclarationListAST analyseParameterDeclarationList(){
        ArrayList<parameterDeclarationAST> result = new ArrayList<>();
        parameterDeclarationAST parameterDeclaration = analyseParameterDeclaration();
        result.add(parameterDeclaration);

        while(true){
            Optional<Token> tk = nextToken();
            if(!tk.isPresent())
                return new parameterDeclarationListAST(result);
            if(tk.get().getType()!=Token.TokenType.COMMA){
                unreadToken();
                return new parameterDeclarationListAST(result);
            }

            parameterDeclaration = analyseParameterDeclaration();
            result.add(parameterDeclaration);
        }
    }
    //<parameter-declaration> ::=
    //    [<const-qualifier>]<type-specifier><identifier>
    parameterDeclarationAST analyseParameterDeclaration(){
        Optional<Token> tk = nextToken();
        String constQualifier = "";
        if(!tk.isPresent())
            cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression,currentPos);
        if(tk.get().getType()==Token.TokenType.CONST) {
            constQualifier = tk.get().getValue();
        }else{
            unreadToken();
        }

        tk = nextToken();
        if(!tk.isPresent() || (tk.get().getType()!=Token.TokenType.VOID && tk.get().getType()!=Token.TokenType.INT))
            cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression,currentPos);
        String typeSpecifier = tk.get().getValue();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.IDENTIFIER)
            cerror.Error(Cerror.ErrorCode.ErrNeedIdentifier,currentPos);
        String identifier = tk.get().getValue();

        if(constQualifier.equals("const")){
            addLocalVariable(identifier,"const",typeSpecifier);
            return new parameterDeclarationAST(Optional.of(constQualifier),typeSpecifier,identifier);
        }else{
            addLocalVariable(identifier,"variable",typeSpecifier);
            return new parameterDeclarationAST(Optional.empty(),typeSpecifier,identifier);
        }
    }
    //<compound-statement> ::=
    //    '{' {<variable-declaration>} <statement-seq> '}'
    compoundStatementAST analyseCompundStatement(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.LEFT_BRACE)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBrace,currentPos);

        ArrayList<variableDeclarationAST> variableDeclaration = analyseVariableDeclaration();
        statementSeqAST statementSeq = analyseStatementSeq();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.RIGHT_BRACE)
            cerror.Error(Cerror.ErrorCode.ErrNoRightBrace,currentPos);

        return new compoundStatementAST(variableDeclaration,statementSeq);
    }

    //<statement-seq> ::=
    //	{<statement>}
    statementSeqAST analyseStatementSeq(){
        ArrayList<statementAST> result = new ArrayList<>();
        while (true){
            Optional<Token> tk = nextToken();
            if(!tk.isPresent())
                return new statementSeqAST(result);
            if(!statements.contains(tk.get().getType())) {
                unreadToken();
                return new statementSeqAST(result);
            }

            unreadToken();
            statementAST statement = analyseStatement();
            result.add(statement);
        }
    }
    //<statement> ::=
    //     '{' <statement-seq> '}'
    //    |<condition-statement> if
    //    |<loop-statement> while
    //    |<jump-statement> return
    //    |<print-statement> print
    //    |<scan-statement> scan
    //    |<assignment-expression>';' identifier
    //    |<function-call>';' identifier
    //    |';'
    statementAST analyseStatement(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || !statements.contains(tk.get().getType()))
            cerror.Error(Cerror.ErrorCode.ErrStatement,currentPos);
        Token.TokenType tt = tk.get().getType();
        switch (tt){
            case LEFT_BRACE:{
                statementSeqAST statementSeq = analyseStatementSeq();

                tk = nextToken();
                if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.RIGHT_BRACE)
                    cerror.Error(Cerror.ErrorCode.ErrNoRightBrace,currentPos);

                return new statementAST(statementSeq);
            }

            case IF:{
                unreadToken();
                conditionStatementAST conditionStatement = analyseConditionStatement();
                return new statementAST(conditionStatement);
            }

            case WHILE:{
                unreadToken();
                loopStatementAST loopStatement = analyseLoopStatement();
                return new statementAST(loopStatement);
            }

            case RETURN:{
                unreadToken();
                jumpStatementAST jumpStatement = analyseJumpStatement();
                return new statementAST(jumpStatement);
            }

            case PRINT:{
                unreadToken();
                printStatementAST printStatement = analysePrintStatement();
                return new statementAST(printStatement);
            }

            case SCAN:{
                unreadToken();
                scanStatementAST scanStatement = analyseScanStatement();
                return new statementAST(scanStatement);
            }

            case IDENTIFIER:{
                tk = nextToken();
                if(!tk.isPresent())
                    cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression,currentPos);
                if(tk.get().getType() == Token.TokenType.ASSIGN){
                    unreadToken();
                    unreadToken();
                    assignmentExpressionAST assignmentExpression = analyseAssignmentExpression();

                    tk = nextToken();
                    if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.SEMICOLON)
                        cerror.Error(Cerror.ErrorCode.ErrNoSemicolon,currentPos);
                    return new statementAST(assignmentExpression);

                }else if(tk.get().getType() == Token.TokenType.LEFT_BRACKET){
                    unreadToken();
                    unreadToken();
                    functionCallAST functionCall = analyseFunctionCall();

                    tk = nextToken();
                    if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.SEMICOLON)
                        cerror.Error(Cerror.ErrorCode.ErrNoSemicolon,currentPos);

                    return new statementAST(functionCall);

                }else{
                    cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression,currentPos);
                }
            }

            case SEMICOLON:{
                return new statementAST(new AST());
            }

            default:
                cerror.Error(Cerror.ErrorCode.ErrIncompleteStatement,currentPos);
        }
        //can not reach
        return new statementAST(new AST());
    }

    //<condition> ::=
    //     <expression>[<relational-operator><expression>]
    //
    conditionAST analyseCondition(){
        expressionAST expression = analyseExpression();
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || (
                    tk.get().getType() != Token.TokenType.LESS &&
                            tk.get().getType() != Token.TokenType.LESS_EQUAL &&
                            tk.get().getType() != Token.TokenType.GREATER &&
                            tk.get().getType() != Token.TokenType.GREATER_EQUAL &&
                            tk.get().getType() != Token.TokenType.NOTEQUAL &&
                            tk.get().getType() != Token.TokenType.EQUAL
                )
        ){
            unreadToken();
            return new conditionAST(expression,Optional.empty());
        }

        String relationOperator = tk.get().getValue();
        expressionAST expression2 = analyseExpression();

        return new conditionAST(expression,Optional.of(new Pair<>(relationOperator,expression2)));
    }
    //<condition-statement> ::=
    //     'if' '(' <condition> ')' <statement> ['else' <statement>]
    //
    conditionStatementAST analyseConditionStatement(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.IF)
            cerror.Error(Cerror.ErrorCode.ErrNoIf,currentPos);

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.LEFT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBracket,currentPos);

        conditionAST condition = analyseCondition();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.RIGHT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoRightBracket,currentPos);

        statementAST statement = analyseStatement();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.ELSE)
            return new conditionStatementAST(condition,statement,Optional.empty());

        statementAST elseStatement = analyseStatement();
        return new conditionStatementAST(condition,statement,Optional.of(elseStatement));
    }
    //<loop-statement> ::=
    //    'while' '(' <condition> ')' <statement>
    //
    loopStatementAST analyseLoopStatement(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.WHILE)
            cerror.Error(Cerror.ErrorCode.ErrNoWhile,currentPos);

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.LEFT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBracket,currentPos);

        conditionAST condition = analyseCondition();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.RIGHT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoRightBracket,currentPos);

        statementAST statement = analyseStatement();

        return new loopStatementAST(condition,statement);
    }
    //<jump-statement> ::=
    //    <return-statement>
    jumpStatementAST analyseJumpStatement(){
        returnStatementAST returnStatement = analyseReturnStatement();
        return new jumpStatementAST(returnStatement);
    }
    //<return-statement> ::=
    //    'return' [<expression>] ';'
    //
    returnStatementAST analyseReturnStatement(){
        Optional<Token> tk = nextToken();
        expressionAST expression = null;
        if(!tk.isPresent() || tk.get().getType()!=Token.TokenType.RETURN)
            cerror.Error(Cerror.ErrorCode.ErrNoReturn,currentPos);

        tk = nextToken();
        if(!tk.isPresent())
            cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression,currentPos);
        if(tk.get().getType()==Token.TokenType.PLUS_SIGN ||
                tk.get().getType()==Token.TokenType.MINUS_SIGN ||
                tk.get().getType()==Token.TokenType.LEFT_BRACKET||
                tk.get().getType()==Token.TokenType.IDENTIFIER||
                Character.isDigit(tk.get().getValue().charAt(0))
        ){
            unreadToken();
            expression = analyseExpression();
        }else{
            unreadToken();
        }

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.SEMICOLON)
            cerror.Error(Cerror.ErrorCode.ErrNoSemicolon,currentPos);

        if(expression!=null)
            return new returnStatementAST(Optional.of(expression));

        return new returnStatementAST(Optional.empty());
    }
    //<scan-statement> ::=
    //    'scan' '(' <identifier> ')' ';'
    scanStatementAST analyseScanStatement(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.SCAN)
            cerror.Error(Cerror.ErrorCode.ErrNoScan,currentPos);

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.LEFT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBracket,currentPos);

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.IDENTIFIER)
            cerror.Error(Cerror.ErrorCode.ErrNeedIdentifier,currentPos);
        String identifier = tk.get().getValue();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.RIGHT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoRightBracket,currentPos);

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.SEMICOLON)
            cerror.Error(Cerror.ErrorCode.ErrNoSemicolon,currentPos);

        return new scanStatementAST(identifier);
    }
    //<print-statement> ::=
    //    'print' '(' [<printable-list>] ')' ';'
    printStatementAST analysePrintStatement(){
        Optional<Token> tk = nextToken();
        printableListAST printableList = null;
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.PRINT)
            cerror.Error(Cerror.ErrorCode.ErrNoPrint,currentPos);

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.LEFT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBracket,currentPos);

        tk = nextToken();
        if(!tk.isPresent())
            cerror.Error(Cerror.ErrorCode.ErrIncompleteStatement,currentPos);
        if(tk.get().getType()==Token.TokenType.PLUS_SIGN ||
                tk.get().getType()==Token.TokenType.MINUS_SIGN ||
                tk.get().getType()==Token.TokenType.LEFT_BRACKET||
                tk.get().getType()==Token.TokenType.IDENTIFIER||
                Character.isDigit(tk.get().getValue().charAt(0))
        ){
            unreadToken();
            printableList = analysePrintableList();
        }else{
            unreadToken();
        }

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.RIGHT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoRightBracket,currentPos);

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.SEMICOLON)
            cerror.Error(Cerror.ErrorCode.ErrNoSemicolon,currentPos);

        if(printableList!=null)
            return new printStatementAST(Optional.of(printableList));

        return new printStatementAST(Optional.empty());
    }

    //<printable-list>  ::=
    //    <printable> {',' <printable>}
    printableListAST analysePrintableList(){
        ArrayList<printableAST> result = new ArrayList<>();
        printableAST printable = analysePrintable();
        result.add(printable);

        while(true){
            Optional<Token> tk = nextToken();
            if(!tk.isPresent())
                return new printableListAST(result);
            if(tk.get().getType()!= Token.TokenType.COMMA){
                unreadToken();
                return new printableListAST(result);
            }

            printable = analysePrintable();
            result.add(printable);
        }
    }
    //<printable> ::=
    //    <expression>
    printableAST analysePrintable(){
        expressionAST expression = analyseExpression();
        return new printableAST(expression);
    }
    //<assignment-expression> ::=
    //    <identifier><assignment-operator><expression>
    assignmentExpressionAST analyseAssignmentExpression(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.IDENTIFIER)
            cerror.Error(Cerror.ErrorCode.ErrNeedIdentifier,currentPos);

        String identifier = tk.get().getValue();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType()!= Token.TokenType.ASSIGN)
            cerror.Error(Cerror.ErrorCode.ErrNeedAssign,currentPos);

        String assignOperator = tk.get().getValue();

        expressionAST expression = analyseExpression();

        return new assignmentExpressionAST(identifier,assignOperator,expression);
    }
    //<expression> ::=
    //    <additive-expression>
    expressionAST analyseExpression(){
        additiveExpressionAST additiveExpression = analyseAdditiveExpression();
        return new expressionAST(additiveExpression);
    }
    //<additive-expression> ::=
    //     <multiplicative-expression>{<additive-operator><multiplicative-expression>}
    additiveExpressionAST analyseAdditiveExpression(){
        multiplicativeExpressionAST multiplicativeExpression1 = analyseMultiplicativeExpression();
        ArrayList<Pair<String,multiplicativeExpressionAST> > result = new ArrayList<>();

        while (true){
            Optional<Token> tk = nextToken();
            if(!tk.isPresent())
                return new additiveExpressionAST(multiplicativeExpression1,result);
            if(tk.get().getType()!= Token.TokenType.PLUS_SIGN && tk.get().getType()!= Token.TokenType.MINUS_SIGN){
                unreadToken();
                return new additiveExpressionAST(multiplicativeExpression1,result);
            }

            String additiveOperator = tk.get().getValue();

            multiplicativeExpressionAST multiplicativeExpression = analyseMultiplicativeExpression();

            result.add(new Pair<>(additiveOperator,multiplicativeExpression));
        }
    }
    //<multiplicative-expression> ::=
    //     <unary-expression>{<multiplicative-operator><unary-expression>}
    multiplicativeExpressionAST analyseMultiplicativeExpression(){
        unaryExpressionAST unaryExpression1 = analyseUnaryExpression();
        ArrayList<Pair<String,unaryExpressionAST> > result = new ArrayList<>();

        while (true){
            Optional<Token> tk = nextToken();
            if(!tk.isPresent())
                return new multiplicativeExpressionAST(unaryExpression1,result);
            if(tk.get().getType()!= Token.TokenType.MULTIPLY_SIGN && tk.get().getType()!= Token.TokenType.DIVIDE_SIGN){
                unreadToken();
                return new multiplicativeExpressionAST(unaryExpression1,result);
            }

            String multiOperator = tk.get().getValue();
            unaryExpressionAST unaryExpression = analyseUnaryExpression();
            result.add(new Pair<>(multiOperator,unaryExpression));
        }
    }
    //<unary-expression> ::=
    //    [<unary-operator>]<primary-expression>
    unaryExpressionAST analyseUnaryExpression(){
        Optional<Token> tk = nextToken();
        String unaryOperator = "";
        if(!tk.isPresent())
            cerror.Error(Cerror.ErrorCode.ErrIncompleteStatement,currentPos);
        if(tk.get().getType() == Token.TokenType.PLUS_SIGN || tk.get().getType() == Token.TokenType.MINUS_SIGN){
            unaryOperator = tk.get().getValue();
        }else{
            unreadToken();
        }

        primaryExpresiionAST primaryExpresiion = analysePrimaryExpresiion();

        if(unaryOperator.equals("")){
            return new unaryExpressionAST(Optional.empty(),primaryExpresiion);
        }

        return new unaryExpressionAST(Optional.of(unaryOperator),primaryExpresiion);
    }
    //<primary-expression> ::=
    //     '('<expression>')'
    //    |<identifier>
    //    |<integer-literal>
    //    |<function-call>
    //
    primaryExpresiionAST analysePrimaryExpresiion(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent())
            cerror.Error(Cerror.ErrorCode.ErrIncompleteStatement,currentPos);
        String type = tk.get().getValue();
        switch (tk.get().getType()){
            case LEFT_BRACKET:{
                expressionAST expression = analyseExpression();
                tk = nextToken();
                if(!tk.isPresent() || tk.get().getType() == Token.TokenType.RIGHT_BRACKET)
                    cerror.Error(Cerror.ErrorCode.ErrNoRightBracket,currentPos);

                return new primaryExpresiionAST(Optional.of(expression),Optional.empty(),Optional.empty(),Optional.empty());
            }
            case IDENTIFIER:{
                tk = nextToken();
                if(!tk.isPresent())
                    return new primaryExpresiionAST(Optional.empty(),Optional.of(type),Optional.empty(),Optional.empty());

                if(tk.get().getType() != Token.TokenType.LEFT_BRACKET){
                    unreadToken();
                    return new primaryExpresiionAST(Optional.empty(),Optional.of(type),Optional.empty(),Optional.empty());
                }

                unreadToken();
                unreadToken();
                functionCallAST functionCall = analyseFunctionCall();
                return new primaryExpresiionAST(Optional.empty(),Optional.empty(),Optional.empty(),Optional.of(functionCall));
            }
            default:{
                if(Character.isDigit(type.charAt(0))){
                    return new primaryExpresiionAST(Optional.empty(),Optional.empty(),Optional.of(type),Optional.empty());
                }
                cerror.Error(Cerror.ErrorCode.ErrIncompleteStatement,currentPos);
            }
        }

        return new primaryExpresiionAST(Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty());
    }
    //<function-call> ::=
    //    <identifier> '(' [<expression-list>] ')'
    functionCallAST analyseFunctionCall(){
        Optional<Token> tk = nextToken();
        if(!tk.isPresent() || tk.get().getType() != Token.TokenType.IDENTIFIER)
            cerror.Error(Cerror.ErrorCode.ErrNeedIdentifier,currentPos);
        String identifier = tk.get().getValue();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType() != Token.TokenType.LEFT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBracket,currentPos);

        tk = nextToken();
        if(!tk.isPresent())
            cerror.Error(Cerror.ErrorCode.ErrIncompleteExpression,currentPos);
        if(tk.get().getType() == Token.TokenType.RIGHT_BRACKET)
            return new functionCallAST(identifier,Optional.empty());

        unreadToken();
        expressionListAST expressionList = analyseExpressionList();

        tk = nextToken();
        if(!tk.isPresent() || tk.get().getType() != Token.TokenType.RIGHT_BRACKET)
            cerror.Error(Cerror.ErrorCode.ErrNoLeftBracket,currentPos);

        return new functionCallAST(identifier,Optional.of(expressionList));

    }
    //<expression-list> ::=
    //    <expression>{','<expression>}
    expressionListAST analyseExpressionList(){
        ArrayList<expressionAST> result = new ArrayList<>();
        expressionAST expression = analyseExpression();
        result.add(expression);

        while (true){
            Optional<Token> tk = nextToken();
            if(!tk.isPresent())
                return new expressionListAST(result);
            if(tk.get().getType() != Token.TokenType.COMMA){
                unreadToken();
                return new expressionListAST(result);
            }

            expression = analyseExpression();
            result.add(expression);
        }
    }

    private Optional<Token> nextToken(){
        if(offset == tokens.size())
            return Optional.empty();
        currentPos = tokens.get(offset).getEndPos();
        return Optional.of(tokens.get(offset++));
    }

    private void unreadToken(){
        if(offset == 0)
            cerror.DieAndPrint("Analyser unreads token from the beginning");
        currentPos = tokens.get(offset-1).getStartPos();
        offset--;
    }

    private boolean isDeclared(String identifier){
        if(functionTable.size() == 0) {
            for (int i = 0; i < globalVariableTable.size(); i++) {
                Symbol symbol = globalVariableTable.get(i);
                if (symbol.name.equals(identifier)) return true;
            }
        }else{
            String currentFunction = functionTable.get(functionTable.size()-1).name;
            for(int i=localVariableTable.size()-1; i>=0; i--){
                Symbol symbol = globalVariableTable.get(i);
                if (symbol.name.equals(identifier) && symbol.other.equals(currentFunction)) return true;
            }
        }
        return false;
    }

    private void addGlobalVariable(String name, String kind, String type){
        Symbol symbol = new Symbol(++countVG,name,kind,type,0,"");
        globalVariableTable.add(symbol);
    }

    private void addFunction(String name, String kind, String type, int level, String other){
        Symbol symbol = new Symbol(++countF,name,kind,type,level,other);
        functionTable.add(symbol);
    }

    private void addLocalVariable(String name, String kind, String type){
        String currentFunction = functionTable.get(functionTable.size()-1).name;
        Symbol symbol = new Symbol(++countVL,name,kind,type,1,currentFunction);
        localVariableTable.add(symbol);
    }

    public Analyser() {
        statements.add(Token.TokenType.LEFT_BRACE);
        statements.add(Token.TokenType.IF);
        statements.add(Token.TokenType.WHILE);
        statements.add(Token.TokenType.RETURN);
        statements.add(Token.TokenType.PRINT);
        statements.add(Token.TokenType.SCAN);
        statements.add(Token.TokenType.IDENTIFIER);
        statements.add(Token.TokenType.SEMICOLON);
    }

    public Analyser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        statements.add(Token.TokenType.LEFT_BRACE);
        statements.add(Token.TokenType.IF);
        statements.add(Token.TokenType.WHILE);
        statements.add(Token.TokenType.RETURN);
        statements.add(Token.TokenType.PRINT);
        statements.add(Token.TokenType.SCAN);
        statements.add(Token.TokenType.IDENTIFIER);
        statements.add(Token.TokenType.SEMICOLON);
    }
}
