grammar desk;
r0: 'print' 'ID' '+' (r1)* 'ID' EOF;
Whitespace
		:   [ \t]+
		-> skip
		;