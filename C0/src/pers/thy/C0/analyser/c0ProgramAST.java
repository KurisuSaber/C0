package pers.thy.C0.analyser;

import pers.thy.C0.error.Cerror;
import pers.thy.C0.symboltable.*;
import pers.thy.C0.utils.Pair;

import java.util.ArrayList;
import java.util.Optional;


//<C0-program> ::= {<variable-declaration>}{<function-definition>}
public class c0ProgramAST extends AST{
    ArrayList<variableDeclarationAST> variableDeclaration;
    ArrayList<functionDefinitionAST> functionDefinition;

    public c0ProgramAST() {
    }

    public c0ProgramAST(ArrayList<variableDeclarationAST> variableDeclaration, ArrayList<functionDefinitionAST> functionDefinition) {
        this.variableDeclaration = variableDeclaration;
        this.functionDefinition = functionDefinition;
    }

    public void generate(){
        for(int i=0;i<variableDeclaration.size();i++)
            variableDeclaration.get(i).generate();
        for(int i=0;i<functionDefinition.size();i++)
            functionDefinition.get(i).generate();
    }
}

//<variable-declaration> ::=
//    [<const-qualifier>]<type-specifier><init-declarator-list>';'
class variableDeclarationAST extends AST{
    Optional<String> constQualifier;//'const'
    String typeSpecifier;//'void'|'int'
    initDeclaratorListAST initDeclaratorList;

    public variableDeclarationAST(Optional<String> constQualifier, String typeSpecifier, initDeclaratorListAST initDeclarationList) {
        this.constQualifier = constQualifier;
        this.typeSpecifier = typeSpecifier;
        this.initDeclaratorList = initDeclarationList;
    }

    void generate(){
        initDeclaratorList.generate(constQualifier,typeSpecifier);
    }
}

//<init-declarator-list> ::=
//    <init-declarator>{','<init-declarator>}
class initDeclaratorListAST extends AST{
    ArrayList<initDeclaratorAST> initDeclarator;

    public initDeclaratorListAST(ArrayList<initDeclaratorAST> initDeclarator) {
        this.initDeclarator = initDeclarator;
    }

    void generate(Optional<String> constQualifier,String typeSpecifier){
        for(int i=0;i<initDeclarator.size();i++){
            initDeclarator.get(i).generate(constQualifier,typeSpecifier);
        }
    }

}

//<init-declarator> ::=
//    <identifier>[<initializer>]
class initDeclaratorAST extends AST{
    String identifier;
    Optional<initializerAST> initializer;

    public initDeclaratorAST(String identifier, Optional<initializerAST> initializer) {
        this.identifier = identifier;
        this.initializer = initializer;
    }

    void generate(Optional<String> constQualifier,String typeSpecifier){
        boolean isInitialized = initializer.isPresent();
        boolean isConstant = constQualifier.isPresent();
        if(isConstant && !isInitialized)
            cerror.RError(Cerror.ErrorCode.ErrNotInitialized);
        Variable variable = new Variable(typeSpecifier,identifier,isConstant,isInitialized);
        if(!initializer.isPresent()){
            Order order = new Order("ipush");
            order.addOperands(0);
            if(FunctionTable.getFunctionTable().functions.isEmpty()) {//GLOBAL
                StartCodeTable.getStartCodeTable().orders.add(order);
                StartCodeTable.getStartCodeTable().variables.add(variable);
            }
            else{//LOCAL
                Function function = FunctionTable.getFunctionTable().functions.get(FunctionTable.getFunctionTable().functions.size()-1);
                String functionName = function.getName();
                variable.setFunctionName(functionName);
                function.addOrder(order);
                function.addVariable(variable);
            }
        }else{
            initializer.get().generate();
            if(FunctionTable.getFunctionTable().functions.isEmpty()) {//GLOBAL
                StartCodeTable.getStartCodeTable().variables.add(variable);
            }
            else{//LOCAL
                Function function = FunctionTable.getFunctionTable().functions.get(FunctionTable.getFunctionTable().functions.size()-1);
                String functionName = function.getName();
                variable.setFunctionName(functionName);
                function.addVariable(variable);
            }
        }
    }
}

//<initializer> ::=
//    '='<expression>
class initializerAST extends AST{
    expressionAST expression;

    public initializerAST(expressionAST expression) {
        this.expression = expression;
    }

    void generate(){
        expression.generate();
    }
}

///////////////////////////////////////////////////////////////////////


//<function-definition> ::=
//    <type-specifier><identifier><parameter-clause><compound-statement>
class functionDefinitionAST extends AST{
    String typeSpecifier;
    String identifier;
    parameterClauseAST parameterClause;
    compoundStatementAST compoundStatement;

    public functionDefinitionAST(String typeSpecifier, String identifier, parameterClauseAST parameterClause, compoundStatementAST compoundStatement) {
        this.typeSpecifier = typeSpecifier;
        this.identifier = identifier;
        this.parameterClause = parameterClause;
        this.compoundStatement = compoundStatement;
    }

    void generate(){//考虑是否声明过这个函数
        if(FunctionTable.getFunctionTable().isDeclared(identifier))
            cerror.RError(Cerror.ErrorCode.ErrFunctionDeclared);

        Function function = new Function(typeSpecifier,identifier);
        FunctionTable.getFunctionTable().functions.add(function);

        Constant functionName = new Constant("String",identifier);
        ConstantTable.getConstantTable().constants.add(functionName);

        parameterClause.generate();
        compoundStatement.generate();
    }
}

//<parameter-clause> ::=
//    '(' [<parameter-declaration-list>] ')'
class parameterClauseAST extends AST{
    Optional<parameterDeclarationListAST> parameterDeclarationList;

    public parameterClauseAST(Optional<parameterDeclarationListAST> parameterDeclarationList) {
        this.parameterDeclarationList = parameterDeclarationList;
    }

    void generate(){
        if(!parameterDeclarationList.isPresent()){
            Function currentFunction = FunctionTable.getFunctionTable().getCurrentFuction();
            currentFunction.setParamsSize(0);
            return;
        }
        parameterDeclarationList.get().generate();
    }

}
//<parameter-declaration-list> ::=
//    <parameter-declaration>{','<parameter-declaration>}
class parameterDeclarationListAST extends AST{
    ArrayList<parameterDeclarationAST> parameterDeclaration;

    public parameterDeclarationListAST(ArrayList<parameterDeclarationAST> parameterDeclaration) {
        this.parameterDeclaration = parameterDeclaration;
    }

    void generate(){
        Function currentFunction = FunctionTable.getFunctionTable().getCurrentFuction();
        currentFunction.setParamsSize(parameterDeclaration.size());
        for(int i=0;i<parameterDeclaration.size();i++){
            parameterDeclaration.get(i).generate();
        }
    }
}
//<parameter-declaration> ::=
//    [<const-qualifier>]<type-specifier><identifier>
class parameterDeclarationAST extends AST{
    Optional<String> constQualifier;
    String typeSpecifier;
    String identifier;

    public parameterDeclarationAST(Optional<String> constQualifier, String typeSpecifier, String identifier) {
        this.constQualifier = constQualifier;
        this.typeSpecifier = typeSpecifier;
        this.identifier = identifier;
    }

    void generate(){
        if(FunctionTable.getFunctionTable().getCurrentFuction().isDeclared(identifier))
            cerror.RError(Cerror.ErrorCode.ErrIdentifierDeclared);
        Variable variable = new Variable(typeSpecifier,identifier,constQualifier.isPresent(),false);
        FunctionTable.getFunctionTable().getCurrentFuction().addVariable(variable);
    }
}

///////////////////////////////////////////////////////////////////////

//<compound-statement> ::=
//    '{' {<variable-declaration>} <statement-seq> '}'
class compoundStatementAST extends AST{
    ArrayList<variableDeclarationAST> variableDeclaration;
    statementSeqAST statementSeq;

    public compoundStatementAST(ArrayList<variableDeclarationAST> variableDeclaration, statementSeqAST statementSeq) {
        this.variableDeclaration = variableDeclaration;
        this.statementSeq = statementSeq;
    }

    void generate(){
        for (int i=0;i<variableDeclaration.size();i++)
            variableDeclaration.get(i).generate();
        statementSeq.generate();
    }
}

//<statement-seq> ::=
//	{<statement>}
class statementSeqAST extends AST{
    ArrayList<statementAST> statement;

    public statementSeqAST(ArrayList<statementAST> statement) {
        this.statement = statement;
    }

    void generate(){
        for(int i=0;i<statement.size();i++)
            statement.get(i).generate();
    }
}
//<statement> ::=
//     '{' <statement-seq> '}'
//    |<condition-statement>
//    |<loop-statement>
//    |<jump-statement>
//    |<print-statement>
//    |<scan-statement>
//    |<assignment-expression>';'
//    |<function-call>';'
//    |';'
class statementAST extends AST{
    AST substatement;

    public statementAST(AST substatement) {
        this.substatement = substatement;
    }

    void generate(){
        if(substatement instanceof statementSeqAST){
            statementSeqAST statementSeq = (statementSeqAST)substatement;
            statementSeq.generate();
        }else if(substatement instanceof conditionStatementAST){
            conditionStatementAST conditionStatement = (conditionStatementAST)substatement;
            conditionStatement.generate();
        }else if(substatement instanceof loopStatementAST){
            loopStatementAST loopStatement = (loopStatementAST)substatement;
            loopStatement.generate();
        }else if(substatement instanceof jumpStatementAST){
            jumpStatementAST jumpStatement = (jumpStatementAST)substatement;
            jumpStatement.generate();
        }else if(substatement instanceof printStatementAST){
            printStatementAST printStatement = (printStatementAST)substatement;
            printStatement.generate();
        }else if(substatement instanceof scanStatementAST){
            scanStatementAST scanStatement = (scanStatementAST)substatement;
            scanStatement.generate();
        }else if(substatement instanceof assignmentExpressionAST){
            assignmentExpressionAST assignmentExpression = (assignmentExpressionAST)substatement;
            assignmentExpression.generate();
        }else if(substatement instanceof functionCallAST){
            functionCallAST functionCall = (functionCallAST)substatement;
            functionCall.generate();
        }
    }
}

//<condition> ::=
//     <expression>[<relational-operator><expression>]
//
class conditionAST extends AST{
    expressionAST expression;
    Optional<Pair<String,expressionAST>> re_exp;

    public conditionAST(expressionAST expression, Optional<Pair<String, expressionAST>> re_exp) {
        this.expression = expression;
        this.re_exp = re_exp;
    }

    void generate(){
        expression.generate();
        if(re_exp.isPresent()){
            re_exp.get().getSecond().generate();
            Order icmp = new Order("icmp");
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(icmp);
            Order jump = new Order("");
            jump.setType("jump");
            switch (re_exp.get().getFirst()){
                case "<":{
                    jump.setOpcode("jge");
                    break;
                }
                case "<=":{
                    jump.setOpcode("jg");
                    break;
                }
                case ">":{
                    jump.setOpcode("jle");
                    break;
                }
                case ">=":{
                    jump.setOpcode("jl");
                    break;
                }
                case "!=":{
                    jump.setOpcode("je");
                    break;
                }
                case "==":{
                    jump.setOpcode("jne");
                    break;
                }
            }
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(jump);
        }else{
            Order ipush = new Order("ipush");
            ipush.addOperands(0);
            Order icmp = new Order("icmp");
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(ipush);
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(icmp);
            Order jump = new Order("je");
            jump.setType("jump");
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(jump);
        }
    }
}
//<condition-statement> ::=
//     'if' '(' <condition> ')' <statement> ['else' <statement>]
//
class conditionStatementAST extends AST{
    conditionAST condition;
    statementAST statement;
    Optional<statementAST> elseStatement;

    public conditionStatementAST(conditionAST condition, statementAST statement, Optional<statementAST> elseStatement) {
        this.condition = condition;
        this.statement = statement;
        this.elseStatement = elseStatement;
    }

    void generate(){
        condition.generate();
        statement.generate();
        //回填jmp的index
        int index = FunctionTable.getFunctionTable().getCurrentFuction().orders.size();
        Function currentFunction = FunctionTable.getFunctionTable().getCurrentFuction();
        for(int i=currentFunction.orders.size()-1;i>=0;i--){
            if(currentFunction.orders.get(i).getType().equals("jump")){
                currentFunction.orders.get(i).addOperands(index);
                break;
            }
        }
        if(elseStatement.isPresent()) elseStatement.get().generate();
    }
}
//<loop-statement> ::=
//    'while' '(' <condition> ')' <statement>
//
class loopStatementAST extends AST{
    conditionAST condition;
    statementAST statement;

    public loopStatementAST(conditionAST condition, statementAST statement) {
        this.condition = condition;
        this.statement = statement;
    }

    void generate(){
        int index = FunctionTable.getFunctionTable().getCurrentFuction().orders.size();
        condition.generate();
        statement.generate();
        Order jmp = new Order("jmp");
        jmp.addOperands(index);
        FunctionTable.getFunctionTable().getCurrentFuction().orders.add(jmp);
        //回填jmp的index
        index = FunctionTable.getFunctionTable().getCurrentFuction().orders.size();
        Function currentFunction = FunctionTable.getFunctionTable().getCurrentFuction();
        for(int i=currentFunction.orders.size()-1;i>=0;i--){
            if(currentFunction.orders.get(i).getType().equals("jump")){
                currentFunction.orders.get(i).addOperands(index);
                break;
            }
        }
    }
}
//<jump-statement> ::=
//    <return-statement>
class jumpStatementAST extends AST{
    returnStatementAST returnStatement;

    public jumpStatementAST(returnStatementAST returnStatement) {
        this.returnStatement = returnStatement;
    }

    void generate(){
        returnStatement.generate();
    }
}
//<return-statement> ::=
//    'return' [<expression>] ';'
//
class returnStatementAST extends AST{
    Optional<expressionAST> expression;

    public returnStatementAST(Optional<expressionAST> expression) {
        this.expression = expression;
    }

    void generate(){
        if(expression.isPresent()){//return xxx;
            if(FunctionTable.getFunctionTable().getCurrentFuction().getType().equals("void"))
                cerror.RError(Cerror.ErrorCode.ErrReturnTypeError);
            expression.get().generate();
            Order iret = new Order("iret");
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(iret);
        }else{//return ;
            if(!FunctionTable.getFunctionTable().getCurrentFuction().getType().equals("void"))
                cerror.RError(Cerror.ErrorCode.ErrReturnTypeError);
            Order ret = new Order("ret");
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(ret);
        }
    }
}
//<scan-statement> ::=
//    'scan' '(' <identifier> ')' ';'
class scanStatementAST extends AST{
    String identifier;

    public scanStatementAST(String identifier) {
        this.identifier = identifier;
    }

    //identifier要求非const
    void generate(){
        Function currentFunction = FunctionTable.getFunctionTable().getCurrentFuction();
        if(!currentFunction.isDeclared(identifier) && !StartCodeTable.getStartCodeTable().isDeclared(identifier))
            cerror.RError(Cerror.ErrorCode.ErrIdentifierNotDefined);
        if(currentFunction.isConst(identifier))
            cerror.RError(Cerror.ErrorCode.ErrAssignToConstant);
        Order scan = new Order("iscan");
        int level = 0;
        int index = 0;
        if(FunctionTable.getFunctionTable().getCurrentFuction().isDeclared(identifier))
            index = FunctionTable.getFunctionTable().getCurrentFuction().getIndex(identifier);
        else {
            level = 1;
            index = StartCodeTable.getStartCodeTable().getIndex(identifier);
        }
        Order loadaOrder = new Order("loada");
        loadaOrder.addOperands(level);
        loadaOrder.addOperands(index);
        FunctionTable.getFunctionTable().getCurrentFuction().addOrder(loadaOrder);
        FunctionTable.getFunctionTable().getCurrentFuction().addOrder(scan);
        Order istore = new Order("istore");
        FunctionTable.getFunctionTable().getCurrentFuction().addOrder(istore);
    }
}
//<print-statement> ::=
//    'print' '(' [<printable-list>] ')' ';'
class printStatementAST extends AST{
    Optional<printableListAST> printableList;

    public printStatementAST(Optional<printableListAST> printableList) {
        this.printableList = printableList;
    }

    void generate(){
        if(!printableList.isPresent()) {//TODO:提醒级 printl
            Order printl = new Order("printl");
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(printl);
            return;
        }
        printableList.get().generate();
    }
}
//<printable-list>  ::=
//    <printable> {',' <printable>}
class printableListAST extends AST{
    ArrayList<printableAST> printable;

    public printableListAST(ArrayList<printableAST> printable) {
        this.printable = printable;
    }

    void generate(){
        for(int i=0;i<printable.size();i++) {
            printable.get(i).generate();
            Order iprint = new Order("iprint");
            FunctionTable.getFunctionTable().getCurrentFuction().addOrder(iprint);
            if(i != printable.size()-1){
                Order bipush = new Order("bipush");
                bipush.addOperands(32);
                Order printc = new Order("cprint");
                FunctionTable.getFunctionTable().getCurrentFuction().addOrder(bipush);
                FunctionTable.getFunctionTable().getCurrentFuction().addOrder(printc);
            }
        }
        Order printl = new Order("printl");
        FunctionTable.getFunctionTable().getCurrentFuction().addOrder(printl);
    }
}
//<printable> ::=
//    <expression>
//
class printableAST extends AST{
    expressionAST expression;

    public printableAST(expressionAST expression) {
        this.expression = expression;
    }

    void generate(){
        expression.generate();
    }
}
//<assignment-expression> ::=
//    <identifier><assignment-operator><expression>
//
class assignmentExpressionAST extends AST{
    String identifier;
    String assignmentOperator;
    expressionAST expression;

    public assignmentExpressionAST(String identifier, String assignmentOperator, expressionAST expression) {
        this.identifier = identifier;
        this.assignmentOperator = assignmentOperator;
        this.expression = expression;
    }

    void generate(){
        Function currentFunction = FunctionTable.getFunctionTable().getCurrentFuction();
        if(!currentFunction.isDeclared(identifier) && !StartCodeTable.getStartCodeTable().isDeclared(identifier))
            cerror.RError(Cerror.ErrorCode.ErrIdentifierNotDefined);
        if(currentFunction.isConst(identifier))
            cerror.RError(Cerror.ErrorCode.ErrAssignToConstant);
        int level = 0;
        int index = 0;
        if(FunctionTable.getFunctionTable().getCurrentFuction().isDeclared(identifier))
            index = FunctionTable.getFunctionTable().getCurrentFuction().getIndex(identifier);
        else {
            level = 1;
            index = StartCodeTable.getStartCodeTable().getIndex(identifier);
        }
        Order loadaOrder = new Order("loada");
        loadaOrder.addOperands(level);
        loadaOrder.addOperands(index);
        FunctionTable.getFunctionTable().getCurrentFuction().addOrder(loadaOrder);
        expression.generate();
        Order istore = new Order("istore");
        FunctionTable.getFunctionTable().getCurrentFuction().addOrder(istore);
    }
}
//<expression> ::=
//    <additive-expression>
class expressionAST extends AST{
    additiveExpressionAST additiveExpression;

    public expressionAST(additiveExpressionAST additiveExpression) {
        this.additiveExpression = additiveExpression;
    }

    void generate(){
        additiveExpression.generate();
    }

}
//<additive-expression> ::=
//     <multiplicative-expression>{<additive-operator><multiplicative-expression>}
class additiveExpressionAST extends AST{
    AST multiplicativeExpressionL;
    String additiveOperator;
    AST multiplicativeExpressionR;

    public additiveExpressionAST(AST multiplicativeExpressionL, String additiveOperator, AST multiplicativeExpressionR) {
        this.multiplicativeExpressionL = multiplicativeExpressionL;
        this.additiveOperator = additiveOperator;
        this.multiplicativeExpressionR = multiplicativeExpressionR;
    }

    public additiveExpressionAST(AST multiplicativeExpressionL) {
        this.multiplicativeExpressionL = multiplicativeExpressionL;
    }

    void generate(){
        if(multiplicativeExpressionL instanceof multiplicativeExpressionAST) {
            multiplicativeExpressionAST multiplicativeExpressionLd = (multiplicativeExpressionAST) multiplicativeExpressionL;
            multiplicativeExpressionLd.generate();
        }else if(multiplicativeExpressionL instanceof additiveExpressionAST){
            additiveExpressionAST additiveExpressionLd = (additiveExpressionAST)multiplicativeExpressionL;
            additiveExpressionLd.generate();
        }
        if(multiplicativeExpressionR!=null) {
            if(multiplicativeExpressionR instanceof multiplicativeExpressionAST) {
                multiplicativeExpressionAST multiplicativeExpressionRd = (multiplicativeExpressionAST) multiplicativeExpressionR;
                multiplicativeExpressionRd.generate();
            }else if(multiplicativeExpressionR instanceof additiveExpressionAST){
                additiveExpressionAST additiveExpressionRd = (additiveExpressionAST)multiplicativeExpressionR;
                additiveExpressionRd.generate();
            }
        }
        Order order = new Order("iadd");
        if(additiveOperator!=null) {
            if(additiveOperator.equals("-"))
                order.setOpcode("isub");
            if (FunctionTable.getFunctionTable().functions.isEmpty())//GLOBAL
                StartCodeTable.getStartCodeTable().orders.add(order);
            else//LOCAL
                FunctionTable.getFunctionTable().getCurrentFuction().addOrder(order);
        }
    }
}
//<multiplicative-expression> ::=
//     <unary-expression>{<multiplicative-operator><unary-expression>}
class multiplicativeExpressionAST extends AST{
    AST unaryExpressionL;
    String mulOperator;
    AST unaryExpressionR;

    public multiplicativeExpressionAST(AST unaryExpressionL, String mulOperator, AST unaryExpressionR) {
        this.unaryExpressionL = unaryExpressionL;
        this.mulOperator = mulOperator;
        this.unaryExpressionR = unaryExpressionR;
    }

    public multiplicativeExpressionAST(AST unaryExpressionL) {
        this.unaryExpressionL = unaryExpressionL;
    }

    void generate(){
        if(unaryExpressionL instanceof unaryExpressionAST) {
            unaryExpressionAST unaryExpressionLd = (unaryExpressionAST) unaryExpressionL;
            unaryExpressionLd.generate();
        }else if(unaryExpressionL instanceof multiplicativeExpressionAST){
            multiplicativeExpressionAST multiplicativeExpressionLd = (multiplicativeExpressionAST)unaryExpressionL;
            multiplicativeExpressionLd.generate();
        }
        if(unaryExpressionR != null) {
            if(unaryExpressionR instanceof unaryExpressionAST) {
                unaryExpressionAST unaryExpressionRd = (unaryExpressionAST) unaryExpressionR;
                unaryExpressionRd.generate();
            }else if(unaryExpressionR instanceof multiplicativeExpressionAST){
                multiplicativeExpressionAST multiplicativeExpressionRd = (multiplicativeExpressionAST)unaryExpressionR;
                multiplicativeExpressionRd.generate();
            }
        }
        Order order = new Order("imul");
        if(mulOperator!=null) {
            if(mulOperator.equals("/"))
                order.setOpcode("idiv");
            if(FunctionTable.getFunctionTable().functions.isEmpty())//GLOBAL
                StartCodeTable.getStartCodeTable().orders.add(order);
            else//LOCAL
                FunctionTable.getFunctionTable().functions.get(FunctionTable.getFunctionTable().functions.size()-1).addOrder(order);
        }
    }
}
//<unary-expression> ::=
//    [<unary-operator>]<primary-expression>
class unaryExpressionAST extends AST{
    Optional<String> unaryOperator;
    primaryExpresiionAST primaryExpresiion;

    public unaryExpressionAST(Optional<String> unaryOperator, primaryExpresiionAST primaryExpresiion) {
        this.unaryOperator = unaryOperator;
        this.primaryExpresiion = primaryExpresiion;
    }

    void generate(){
        primaryExpresiion.generate();
        if(unaryOperator.isPresent() && unaryOperator.get().equals("-")){
            Order order = new Order("ineg");

            if(FunctionTable.getFunctionTable().functions.isEmpty())//GLOBAL
                StartCodeTable.getStartCodeTable().orders.add(order);
            else//LOCAL
                FunctionTable.getFunctionTable().getCurrentFuction().addOrder(order);

        }
    }
}
//<primary-expression> ::=
//     '('<expression>')'
//    |<identifier>
//    |<integer-literal>
//    |<function-call>
//
class primaryExpresiionAST extends AST{
    Optional<expressionAST> expression;
    Optional<String> identifier;
    Optional<String> integerLiteral;
    Optional<functionCallAST> functionCall;

    public primaryExpresiionAST(Optional<expressionAST> expression, Optional<String> identifier, Optional<String> integerLiteral, Optional<functionCallAST> functionCall) {
        this.expression = expression;
        this.identifier = identifier;
        this.integerLiteral = integerLiteral;
        this.functionCall = functionCall;
    }

    void generate(){
        if(expression.isPresent())
            expression.get().generate();
        else if(identifier.isPresent()){
            if(FunctionTable.getFunctionTable().functions.isEmpty()){//GLOBAL
                if(!StartCodeTable.getStartCodeTable().isDeclared(identifier.get()))
                    cerror.RError(Cerror.ErrorCode.ErrIdentifierNotDefined);
                int index = StartCodeTable.getStartCodeTable().getIndex(identifier.get());
                Order loadaOrder = new Order("loada");
                loadaOrder.addOperands(0);
                loadaOrder.addOperands(index);
                StartCodeTable.getStartCodeTable().orders.add(loadaOrder);
                Order iloadOrder = new Order("iload");
                StartCodeTable.getStartCodeTable().orders.add(iloadOrder);
            }else{//LOCAL
                if(!StartCodeTable.getStartCodeTable().isDeclared(identifier.get()) &&
                        !FunctionTable.getFunctionTable().getCurrentFuction().isDeclared(identifier.get()))
                    cerror.RError(Cerror.ErrorCode.ErrIdentifierNotDefined);
                int level = 0;
                int index = 0;
                if(FunctionTable.getFunctionTable().getCurrentFuction().isDeclared(identifier.get()))
                    index = FunctionTable.getFunctionTable().getCurrentFuction().getIndex(identifier.get());
                else {
                    level = 1;
                    index = StartCodeTable.getStartCodeTable().getIndex(identifier.get());
                }
                Order loadaOrder = new Order("loada");
                loadaOrder.addOperands(level);
                loadaOrder.addOperands(index);
                FunctionTable.getFunctionTable().getCurrentFuction().addOrder(loadaOrder);
                Order iloadOrder = new Order("iload");
                FunctionTable.getFunctionTable().getCurrentFuction().addOrder(iloadOrder);
            }
        }else if(integerLiteral.isPresent()){
            if(FunctionTable.getFunctionTable().functions.isEmpty()){//GLOBAL
                Order ipush = new Order("ipush");
                ipush.addOperands(Integer.parseInt(integerLiteral.get()));
                StartCodeTable.getStartCodeTable().orders.add(ipush);
            }else{//LOCAL
                Order ipush = new Order("ipush");
                ipush.addOperands(Integer.parseInt(integerLiteral.get()));
                FunctionTable.getFunctionTable().getCurrentFuction().addOrder(ipush);
            }
        }else
            functionCall.get().generate();
    }
}
//<function-call> ::=
//    <identifier> '(' [<expression-list>] ')'
class functionCallAST extends AST{
    String identifier;
    Optional<expressionListAST> expressionList;

    public functionCallAST(String identifier, Optional<expressionListAST> expressionList) {
        this.identifier = identifier;
        this.expressionList = expressionList;
    }

    void generate(){
        if(expressionList.isPresent()){
            expressionList.get().generate();
        }
        if(!ConstantTable.getConstantTable().isDeclared(identifier))
            cerror.RError(Cerror.ErrorCode.ErrIdentifierNotDefined);
        int index = ConstantTable.getConstantTable().getIndex(identifier);
        Order call = new Order("call");
        call.addOperands(index);
        FunctionTable.getFunctionTable().getCurrentFuction().addOrder(call);
    }
}
//<expression-list> ::=
//    <expression>{','<expression>}
class expressionListAST extends AST{
    ArrayList<expressionAST> expression;

    public expressionListAST(ArrayList<expressionAST> expression) {
        this.expression = expression;
    }

    void generate(){
        for(int i=0;i<expression.size();i++)
            expression.get(i).generate();
    }
}


