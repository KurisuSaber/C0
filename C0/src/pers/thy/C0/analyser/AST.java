package pers.thy.C0.analyser;

import jdk.nashorn.internal.runtime.options.Option;
import pers.thy.C0.utils.Pair;

import java.awt.print.Printable;
import java.beans.Expression;
import java.util.ArrayList;
import java.util.Optional;

public class AST {

}

//<C0-program> ::= {<variable-declaration>}{<function-definition>}
class c0ProgramAST extends AST{
    ArrayList<variableDeclarationAST> variableDeclaration;
    ArrayList<functionDefinitionAST> functionDefinition;

    public c0ProgramAST() {
    }

    public c0ProgramAST(ArrayList<variableDeclarationAST> variableDeclaration, ArrayList<functionDefinitionAST> functionDefinition) {
        this.variableDeclaration = variableDeclaration;
        this.functionDefinition = functionDefinition;
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
}

//<init-declarator-list> ::=
//    <init-declarator>{','<init-declarator>}
class initDeclaratorListAST extends AST{
    ArrayList<initDeclaratorAST> initDeclarator;

    public initDeclaratorListAST(ArrayList<initDeclaratorAST> initDeclarator) {
        this.initDeclarator = initDeclarator;
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
}

//<initializer> ::=
//    '='<expression>
class initializerAST extends AST{
    expressionAST expression;

    public initializerAST(expressionAST expression) {
        this.expression = expression;
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
}

//<parameter-clause> ::=
//    '(' [<parameter-declaration-list>] ')'
class parameterClauseAST extends AST{
    Optional<parameterDeclarationListAST> parameterDeclarationList;

    public parameterClauseAST(Optional<parameterDeclarationListAST> parameterDeclarationList) {
        this.parameterDeclarationList = parameterDeclarationList;
    }
}
//<parameter-declaration-list> ::=
//    <parameter-declaration>{','<parameter-declaration>}
class parameterDeclarationListAST extends AST{
    ArrayList<parameterDeclarationAST> parameterDeclaration;

    public parameterDeclarationListAST(ArrayList<parameterDeclarationAST> parameterDeclaration) {
        this.parameterDeclaration = parameterDeclaration;
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
}

//<statement-seq> ::=
//	{<statement>}
class statementSeqAST extends AST{
    ArrayList<statementAST> statement;

    public statementSeqAST(ArrayList<statementAST> statement) {
        this.statement = statement;
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
}
//<jump-statement> ::=
//    <return-statement>
class jumpStatementAST extends AST{
    returnStatementAST returnStatement;

    public jumpStatementAST(returnStatementAST returnStatement) {
        this.returnStatement = returnStatement;
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
}
//<scan-statement> ::=
//    'scan' '(' <identifier> ')' ';'
class scanStatementAST extends AST{
    String identifier;

    public scanStatementAST(String identifier) {
        this.identifier = identifier;
    }
}
//<print-statement> ::=
//    'print' '(' [<printable-list>] ')' ';'
class printStatementAST extends AST{
    Optional<printableListAST> printableList;

    public printStatementAST(Optional<printableListAST> printableList) {
        this.printableList = printableList;
    }
}
//<printable-list>  ::=
//    <printable> {',' <printable>}
class printableListAST extends AST{
    ArrayList<printableAST> printable;

    public printableListAST(ArrayList<printableAST> printable) {
        this.printable = printable;
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
}
//<expression> ::=
//    <additive-expression>
class expressionAST extends AST{
    additiveExpressionAST additiveExpression;

    public expressionAST(additiveExpressionAST additiveExpression) {
        this.additiveExpression = additiveExpression;
    }
}
//<additive-expression> ::=
//     <multiplicative-expression>{<additive-operator><multiplicative-expression>}
class additiveExpressionAST extends AST{
    multiplicativeExpressionAST multiplicativeExpression;
    ArrayList<Pair<String,multiplicativeExpressionAST> > add_multi;

    public additiveExpressionAST(multiplicativeExpressionAST multiplicativeExpression, ArrayList<Pair<String, multiplicativeExpressionAST>> add_multi) {
        this.multiplicativeExpression = multiplicativeExpression;
        this.add_multi = add_multi;
    }
}
//<multiplicative-expression> ::=
//     <unary-expression>{<multiplicative-operator><unary-expression>}
class multiplicativeExpressionAST extends AST{
    unaryExpressionAST unaryExpression;
    ArrayList<Pair<String,unaryExpressionAST> > mul_unary;

    public multiplicativeExpressionAST(unaryExpressionAST unaryExpression, ArrayList<Pair<String, unaryExpressionAST>> mul_unary) {
        this.unaryExpression = unaryExpression;
        this.mul_unary = mul_unary;
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
}
//<expression-list> ::=
//    <expression>{','<expression>}
class expressionListAST extends AST{
    ArrayList<expressionAST> expression;

    public expressionListAST(ArrayList<expressionAST> expression) {
        this.expression = expression;
    }
}


