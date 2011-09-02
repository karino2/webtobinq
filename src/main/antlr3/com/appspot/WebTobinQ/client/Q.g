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
// imaginary, from gram.y function name in R source.
XXVALUE;
XXBINARY;
XXEXPRLIST;
XXPAREN;
XXUNARY;
XXBINARY;
XXDEFUN;
XXFUNCALL;
XXIF;
XXIFELSE;
XXWHILE;
XXREPEAT;
XXSUBSCRIPT;
XXBINARY;
XXNXTBRK;
XXCOND;
XXIFCOND;
XXFOR;
XXFORCOND;
XXEXPRLIST0;
XXEXPRLIST1;
XXEXPRLIST2;
XXSUBLIST;
XXSUB0;
XXSUB1;
XXSYMSUB0;
XXSYMSUB1;
XXNULLSUB0;
XXNULLSUB1;
XXFORMAL0;
XXFORMAL1;
XXFORMALLIST;
}

@header {
       package com.appspot.WebTobinQ.client;
}
@lexer::header{
       package com.appspot.WebTobinQ.client;
}



// main
//	|	error
/*
prog	:	EOF
	|	'\n'
	|	expr_or_assign ('\n' | ';')
	;
	*/
	
prog : prog_begin prog_continue* ('\n' | ';')*
	;

prog_begin : ('\n' | ';')* expr_or_assign
		-> ^(XXVALUE expr_or_assign)
		;
		
prog_continue: 
		('\n' | ';')+ expr_or_assign 
		-> ^(XXVALUE expr_or_assign)
		;
		
expr_or_assign  :    (expr->expr) (EQ_ASSIGN expr_or_assign -> ^(XXBINARY EQ_ASSIGN expr expr_or_assign))?
                ;

symbol_or_conststr 
	: SYMBOL | STR_CONST;

unary_op 
	:	 ('-'| '+' | '!' | '~' | '?')
	;

formalarg : SYMBOL -> ^(XXFORMAL0 SYMBOL)
	| SYMBOL EQ_ASSIGN expr -> ^(XXFORMAL1 SYMBOL expr)
	;

// I add EOF for debug.
formlist:(formalarg (',' formalarg)*)? EOF?
	  -> ^(XXFORMALLIST formalarg*)
	;


lexpr : 	num_const
	|	STR_CONST
	|	NULL_CONST
	|	SYMBOL
	|	'{' expr_or_assign ((';' expr_or_assign?) | ('\n' expr_or_assign?))* '}'
		-> ^(XXEXPRLIST expr_or_assign+)
	|	'(' expr_or_assign ')'
		-> ^(XXPAREN expr_or_assign)
	|  unary_op expr
		-> ^(XXUNARY unary_op expr)
	|	FUNCTION '(' formlist ')' cr expr_or_assign
		-> ^(XXDEFUN formlist expr_or_assign)
	|	IF ifcond ifexp=expr_or_assign (ELSE elexp=expr_or_assign)?
		-> ^(XXIFELSE ifcond $ifexp $elexp?)
	|	FOR forcond expr_or_assign
		-> ^(XXFOR forcond expr_or_assign)
	|	WHILE cond expr_or_assign
		-> ^(XXWHILE cond expr_or_assign)
	|	REPEAT expr_or_assign
		-> ^(XXREPEAT expr_or_assign)
	|	(fs=symbol_or_conststr) NS_GET (ss=symbol_or_conststr)
		-> ^(XXBINARY NS_GET $fs $ss)
	|	(fs=symbol_or_conststr) NS_GET_INT (ss=symbol_or_conststr)
		-> ^(XXBINARY NS_GET_INT $fs $ss)
	|	NEXT
		-> ^(XXNXTBRK NEXT)
	|	BREAK
		-> ^(XXNXTBRK BREAK)
	;
	
binary_op
	:(':'| '+'| '-'| '*' |  '/' | '^' | SPECIAL | '%' | '~' 
			| '?' | LT | LE | EQ | NE | GE | GT | AND | OR | AND2 | OR2 
			| LEFT_ASSIGN | RIGHT_ASSIGN)
	;

refer	: (lexpr -> lexpr)
	    ('(' sublist ')'
		  -> ^(XXFUNCALL lexpr sublist)
		| LBB sublist ']' ']'
		  -> ^(XXSUBSCRIPT LBB lexpr sublist)
		| '[' sublist  ']'
		  -> ^(XXSUBSCRIPT '[' lexpr sublist)
		| ('$' | '@') symbol_or_conststr
		  -> ^(XXBINARY '$'? '@'? lexpr symbol_or_conststr)
	    )?
	  ;
	  
expr	: (refer ->refer)
	    (
		binary_op expr
		  -> ^(XXBINARY binary_op refer expr)
	    )?
	;


cond	:	'(' expr ')'
	->^(XXCOND expr)
	;

ifcond	:	'(' expr ')'
	-> ^(XXIFCOND expr)
	;

forcond :	'(' SYMBOL IN expr ')'
	-> ^(XXFORCOND SYMBOL expr)
	;


/*
exprlist:
	|	expr_or_assign
	|	exprlist ';' expr_or_assign
	|	exprlist ';'
	|	exprlist '\n' expr_or_assign
	|	exprlist '\n'
	;
*/

fragment
sublist	:	sub (cr ',' sub)*
		-> ^(XXSUBLIST sub*)
	;

fragment
sub	: -> ^(XXSUB0)
	|	expr -> ^(XXSUB1 expr)
	|	(symbol_or_conststr | NULL_CONST) EQ_ASSIGN -> ^(XXSYMSUB0 symbol_or_conststr? NULL_CONST?)
	|	(symbol_or_conststr | NULL_CONST) EQ_ASSIGN expr ->^(XXSYMSUB1 symbol_or_conststr? NULL_CONST? expr)
	;

/*
formlist:
	|	SYMBOL
	|	SYMBOL EQ_ASSIGN expr
	|	formlist ',' SYMBOL
	|	formlist ',' SYMBOL EQ_ASSIGN expr
	;
*/

cr	:
	;

// LEXER
// These might be not compatible

//num_const
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

integerLiteral : HexLiteral
    |   OctalLiteral
    |   DecimalLiteral
	;


num_const
	: integerLiteral
	| FloatingPointLiteral
	| 'NA'
	| 'TRUE'
	| 'FALSE'
	| 'Inf'
	| 'NaN'
	| 'NA_integer_'
	| 'NA_real_'
	| 'NA_character_'
	| 'NA_complex_'
	;



// end num_const
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
    :  
	// '\u0024' |  $ is special character in R
	'\u002e' | // . is normal character in R
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


