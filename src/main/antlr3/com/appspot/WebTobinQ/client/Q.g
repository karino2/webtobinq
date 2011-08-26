grammar Q;
options {output=AST;}
// start
//prog	:	END_OF_INPUT			{ return 0; }
//	|	'\n'				{ return xxvalue(NULL,2,NULL); }
//	|	expr_or_assign '\n'			{ return xxvalue($1,3,&@1); }
//	|	expr_or_assign ';'			{ return xxvalue($1,4,&@1); }
//	|	error	 			{ YYABORT; }
//	;

tokens {
AND='&';
AND2='&&';
BREAK='break';
ELSE='else';
EQ='==';
EQ_ASSIGN='=';
FUNCTION='function';
FOR='for';
GE='>=';
GT='>';
IN='in';
IF='if';
LBB='[[';
LEFT_ASSIGN='<-';
LT='<';
LE='<=';
NE='!=';
NEXT='next';
NS_GET='::';
NS_GET_INT=':::';
OR='|';
OR2='||';
RIGHT_ASSIGN='->';
WHILE='while';
REPEAT='repeat';
}

@header {
       package com.appspot.WebTobinQ.client;
}
@lexer::header{
       package com.appspot.WebTobinQ.client;
}



// LEXER
// These might be not compatible

//NUM_CONST
HexLiteral : '0' ('x'|'X') HexDigit+ IntegerTypeSuffix? ;

DecimalLiteral : ('0' | '1'..'9' '0'..'9'*) IntegerTypeSuffix? ;

OctalLiteral : '0' ('0'..'7')+ IntegerTypeSuffix? ;

fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
IntegerTypeSuffix : ('l'|'L') ;

FloatingPointLiteral
    :   ('0'..'9')+ '.' ('0'..'9')* Exponent? FloatTypeSuffix?
    |   '.' ('0'..'9')+ Exponent? FloatTypeSuffix?
    |   ('0'..'9')+ Exponent FloatTypeSuffix?
    |   ('0'..'9')+ FloatTypeSuffix
    ;

fragment
Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
FloatTypeSuffix : ('f'|'F'|'d'|'D') ;

NUM_CONST : integerLiteral
	| FloatingPointLiteral
	| 'NA'
	| 'TRUE'
	| 'FALSE'
	| 'Inf'
	| 'NaN'
	| 'NA'
	| 'NA_integer_'
	| 'NA_real_'
	| 'NA_character_'
	| 'NA_complex_'
	;

// end NUM_CONST
// STR_CONST
STR_CONST
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    |   '\'' ( EscapeSequence | ~('\''|'\\') )* '\''
    ;

fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UnicodeEscape
    |   OctalEscape
    ;

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;


// end STR_CONST
NULL_CONST
	: 'NULL'
	;

// SYMBOL
fragment
Letter
    :  '\u0024' |
       '\u0041'..'\u005a' |
       '\u005f' |
       '\u0061'..'\u007a' |
       '\u00c0'..'\u00d6' |
       '\u00d8'..'\u00f6' |
       '\u00f8'..'\u00ff' |
       '\u0100'..'\u1fff' |
       '\u3040'..'\u318f' |
       '\u3300'..'\u337f' |
       '\u3400'..'\u3d2d' |
       '\u4e00'..'\u9fff' |
       '\uf900'..'\ufaff'
    ;

fragment
JavaIDDigit
    :  '\u0030'..'\u0039' |
       '\u0660'..'\u0669' |
       '\u06f0'..'\u06f9' |
       '\u0966'..'\u096f' |
       '\u09e6'..'\u09ef' |
       '\u0a66'..'\u0a6f' |
       '\u0ae6'..'\u0aef' |
       '\u0b66'..'\u0b6f' |
       '\u0be7'..'\u0bef' |
       '\u0c66'..'\u0c6f' |
       '\u0ce6'..'\u0cef' |
       '\u0d66'..'\u0d6f' |
       '\u0e50'..'\u0e59' |
       '\u0ed0'..'\u0ed9' |
       '\u1040'..'\u1049'
   ;


SYMBOL
	:   Letter (Letter|JavaIDDigit)*
	;

// end SYMBOL

SPECIAL
	: '%' ~('%')* '%'
	;

// end LEXER

// main
//	|	error
prog	:	'\n'
	|	expr_or_assign '\n'
	|	expr_or_assign ';'
	;

expr_or_assign  :    expr
                |    equal_assign
                ;

equal_assign    :    expr EQ_ASSIGN expr_or_assign
                ;


// end unaryExpression

expr	: 	NUM_CONST
	|	STR_CONST
	|	NULL_CONST
	|	SYMBOL

	|	'{' exprlist '}'
	|	'(' expr_or_assign ')'

	|	'-' expr
	|	'+' expr
	|	'!' expr
	|	'~' expr
	|	'?' expr


	|	expr ':'  expr
	|	expr '+'  expr
	|	expr '-' expr
	|	expr '*' expr
	|	expr '/' expr
	|	expr '^' expr
	|	expr SPECIAL expr
	|	expr '%' expr
	|	expr '~' expr
	|	expr '?' expr
	|	expr LT expr
	|	expr LE expr
	|	expr EQ expr
	|	expr NE expr
	|	expr GE expr
	|	expr GT expr
	|	expr AND expr
	|	expr OR expr
	|	expr AND2 expr
	|	expr OR2 expr

	|	expr LEFT_ASSIGN expr
	|	expr RIGHT_ASSIGN expr
	|	FUNCTION '(' formlist ')' cr expr_or_assign
	|	expr '(' sublist ')'
	|	IF ifcond expr_or_assign
	|	IF ifcond expr_or_assign ELSE expr_or_assign
	|	FOR forcond expr_or_assign
	|	WHILE cond expr_or_assign
	|	REPEAT expr_or_assign
	|	expr LBB sublist ']' ']'
	|	expr '[' sublist ']'
	|	SYMBOL NS_GET SYMBOL
	|	SYMBOL NS_GET STR_CONST
	|	STR_CONST NS_GET SYMBOL
	|	STR_CONST NS_GET STR_CONST
	|	SYMBOL NS_GET_INT SYMBOL
	|	SYMBOL NS_GET_INT STR_CONST
	|	STR_CONST NS_GET_INT SYMBOL
	|	STR_CONST NS_GET_INT STR_CONST
	|	expr '$' SYMBOL
	|	expr '$' STR_CONST
	|	expr '@' SYMBOL
	|	expr '@' STR_CONST
	|	NEXT
	|	BREAK
	;


cond	:	'(' expr ')'
	;

ifcond	:	'(' expr ')'
	;

forcond :	'(' SYMBOL IN expr ')'
	;


exprlist:
	|	expr_or_assign
	|	exprlist ';' expr_or_assign
	|	exprlist ';'
	|	exprlist '\n' expr_or_assign
	|	exprlist '\n'
	;

sublist	:	sub
	|	sublist cr ',' sub
	;

sub	:
	|	expr
	|	SYMBOL EQ_ASSIGN
	|	SYMBOL EQ_ASSIGN expr
	|	STR_CONST EQ_ASSIGN
	|	STR_CONST EQ_ASSIGN expr
	|	NULL_CONST EQ_ASSIGN
	|	NULL_CONST EQ_ASSIGN expr
	;

formlist:
	|	SYMBOL
	|	SYMBOL EQ_ASSIGN expr
	|	formlist ',' SYMBOL
	|	formlist ',' SYMBOL EQ_ASSIGN expr
	;

cr	: '\r'? '\n'
	;


