grammar Q;
options {output=AST;}
tokens { MULT; } // imaginary token
@header {
       package com.appspot.WebTobinQ.client;
}
@lexer::header{
       package com.appspot.WebTobinQ.client;
}


poly: term ('+'^ term)*
    ;

term: INT ID  -> ^(MULT["*"] INT ID)
    | INT exp -> ^(MULT["*"] INT exp)
    | exp
    | INT
	| ID
    ;

exp : ID '^'^ INT
    ;
    
ID	: 'a'..'z'+ ;

INT	: '0'..'9'+ ;

WS	: (' '|'\t'|'\r'|'\n')+ {skip();} ;
