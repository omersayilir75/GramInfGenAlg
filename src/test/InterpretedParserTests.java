package test;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.analysis.LeftRecursiveRuleTransformer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ATNSerializer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.GrammarParserInterpreter;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.GrammarRootAST;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// tests the output of an interpreted parser versus that of a generated parser
public class InterpretedParserTests {
    //most complicated desk sample
    @Test
    public void testInterpreterParserSimple() throws RecognitionException {
        Grammar g = new Grammar("grammar desk;\n" +
                "\n" +
                "desk: 'print' e (c)? EOF;\n" +
                "e : f | e '+' f;\n" +
                "c : 'where' ds;\n" +
                "f : ID | NUM;\n" +
                "ds : d | ds ';' d;\n" +
                "d : ID '=' NUM;\n" +
                "ID : [a-z]+;\n" +
                "NUM :[0-9]+;\n" +
                "\n" +
                "Whitespace\n" +
                "\t\t:   [ \\t]+\n" +
                "\t\t-> skip\n" +
                "\t\t;");

        CharStream input = CharStreams.fromString("print 8982 + 6 + 444 + 516 + 15414349 + i + dk where yk = 9 ;" +
                " z = 2 ; m = 814655 ; pdnezr = 4 ; bdba = 906197 ; lr = 0");
        LexerInterpreter lexEngine = g.createLexerInterpreter(input);
        CommonTokenStream tokens = new CommonTokenStream(lexEngine);

        GrammarParserInterpreter parser = new GrammarParserInterpreter(g,
                new ATNDeserializer().deserialize(ATNSerializer.getSerialized(g.getATN()).toArray()), tokens);
        ParseTree t = parser.parse(g.rules.get("desk").index);
        String treeText = t.toStringTree(parser).replaceAll(":\\d", "");
        String expectedTreeText = "(desk print (e (e (e (e (e (e (e (f 8982)) + (f 6)) + (f 444)) + (f 516)) + (f 15414349)) + (f i)) + (f dk)) (c where (ds (ds (ds (ds (ds (ds (d yk = 9)) ; (d z = 2)) ; (d m = 814655)) ; (d pdnezr = 4)) ; (d bdba = 906197)) ; (d lr = 0))) <EOF>)";

        assertEquals(expectedTreeText, treeText);
    }

    //most complicated Oberon-0 sample
    @Test
    public void testInterpreterParserAdvanced() throws RecognitionException {
        Grammar g = new Grammar("grammar OberonGrammar;\n" +
                "\n" +
                "moduleDefinition:\n" +
                "\t\tMODULE n=ID ';'\n" +
                "\t\tdeclarations\n" +
                "\t\tblock\n" +
                "\t\t'.' EOF\n" +
                "\t\t;\n" +
                "\n" +
                "declarations:\n" +
                "\t\t( procedureDeclaration | localDeclaration ) *\n" +
                "\t\t;\n" +
                "\n" +
                "\n" +
                "\n" +
                "procedureDeclaration:\n" +
                "\t\tPROCEDURE name=ID (pps=procedureParameters)? (export=STAR)? ';'\n" +
                "\t\t(procedureDeclaration|localDeclaration)*\n" +
                "\t\tendname=block\n" +
                "\t\t';'\n" +
                "\t\t;\n" +
                "\n" +
                "procedureParameters :\n" +
                "\t\t'(' (p+=procedureParameter ';') * p+=procedureParameter ')'\n" +
                "\t\t;\n" +
                "\n" +
                "procedureParameter\n" +
                "\t\t:\n" +
                "\t\tVAR? (names+=ID ',' ) * names+=ID   ':' t=typeName\n" +
                "\t\t;\n" +
                "\n" +
                "typeName\n" +
                "\t\t: ID\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t# simpleTypeName\n" +
                "\t\t| ARRAY e=expression OF t=typeName\t\t# arrayType\n" +
                "\t\t| RECORD r=recordTypeNameElements END\t\t\t\t\t\t# recordTypeName\n" +
                "\t\t;\n" +
                "\n" +
                "recordTypeNameElements\n" +
                "\n" +
                "\t\t: recordElement (';' recordElement)*\n" +
                "\t\t;\n" +
                "\n" +
                "recordElement\n" +
                "\t\t: (ids+=ID ',')* ids+=ID ':' t=typeName\n" +
                "\t\t;\n" +
                "\n" +
                "localDeclaration\n" +
                "\t\t: variableDeclaration\n" +
                "\t\t| constDeclaration\n" +
                "\t\t| typeDeclaration\n" +
                "\t\t;\n" +
                "\n" +
                "typeDeclaration:\n" +
                "\t\tTYPE\n" +
                "\t\t  singleTypeDeclaration+\n" +
                "\t\t  ;\n" +
                "\n" +
                "singleTypeDeclaration:\n" +
                "\t\t  id=ID export=STAR? '=' t=typeName ';'\n" +
                "\t\t  ;\n" +
                "\n" +
                "variableDeclaration:\n" +
                "\t\tVAR\n" +
                "\t\t  singleVariableDeclaration+\n" +
                "\t\t  ;\n" +
                "\n" +
                "singleVariableDeclaration:\n" +
                "\t\t\t(v+=exportableID ',')* v+=exportableID  ':' t=typeName ';'\n" +
                "\t\t\t;\n" +
                "\n" +
                "exportableID:\n" +
                "\t\t\tID (export=STAR)?\n" +
                "\t\t\t;\n" +
                "\n" +
                "constDeclaration:\n" +
                "\t\tCONST\n" +
                "\t\t  constDeclarationElement+\n" +
                "\t\t  ;\n" +
                "\n" +
                "constDeclarationElement:\n" +
                "\t\tc=ID export=STAR? '=' e=expression ';'\n" +
                "\t\t;\n" +
                "\n" +
                "block\n" +
                "\t\t: (BEGIN statements)? END ID\n" +
                "\t\t;\n" +
                "\n" +
                "statements:\n" +
                "\t\tstatement\n" +
                "\t\t( ';' statement )*\n" +
                "\t\t;\n" +
                "\n" +
                "statement\n" +
                "\t\t: assign_statement\n" +
                "\t\t| procCall_statement\n" +
                "\t\t| while_statement\n" +
                "\t\t| repeat_statement\n" +
                "\t\t| if_statement\n" +
                "\t\t|\n" +
                "\t\t;\n" +
                "\n" +
                "procCall_statement\n" +
                "\t\t: id=ID ('(' cp=callParameters ')')?\n" +
                "\t\t;\n" +
                "\n" +
                "assign_statement\n" +
                "\t\t: id=ID s=selector ':=' r=expression\n" +
                "\t\t;\n" +
                "\n" +
                "while_statement\n" +
                "\t\t: WHILE r=expression DO\n" +
                "\t\t  statements\n" +
                "\t\t  END\n" +
                "\t\t;\n" +
                "\n" +
                "repeat_statement\n" +
                "\t\t: REPEAT\n" +
                "\t\t  statements\n" +
                "\t\t  UNTIL r=expression\n" +
                "\t\t;\n" +
                "\n" +
                "if_statement\n" +
                "\t\t: IF c+=expression THEN\n" +
                "\t\t\t  statements\n" +
                "\t\t  ( ELSIF c+=expression THEN\n" +
                "\t\t\t  statements\n" +
                "\t\t  )*\n" +
                "\t\t  (ELSE\n" +
                "\t\t\t  statements\n" +
                "\t\t  )?\n" +
                "\t\t  END\n" +
                "\t\t;\n" +
                "\n" +
                "\n" +
                "expression\n" +
                "\t: op=(NOT | MINUS) e=expression\t\t#exprNotExpression\n" +
                "\t| l=expression op=(STAR | DIV | MOD | AND) r=expression\t\t\t\t\t#exprMultPrecedence\n" +
                "\t| l=expression op=('+' | '-' | OR)  r=expression\t\t\t\t\t\t#exprFactPrecedence\n" +
                "\t| l=expression op=('<' | '<=' | '>' | '>=' | '=' | '#') r=expression\t#exprRelPrecedence\n" +
                "\t| id=ID\n" +
                "\t\ts=selector\t\t\t\t\t\t#exprSingleId\n" +
                "\t| id=ID '(' cp=callParameters? ')'\t\t\t\t\t\t\t\t\t\t#exprFuncCall\n" +
                "\t| '(' e=expression ')'\t\t\t\t\t\t\t\t\t\t\t\t\t#exprEmbeddedExpression\n" +
                "\t| c=Constant\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t#exprConstant\n" +
                "\t| s=STRING_LITERAL\t\t\t\t\t\t\t\t\t\t\t\t\t\t#exprStringLiteral\n" +
                "\t;\n" +
                "\n" +
                "callParameters\n" +
                "\t\t: p+=expression (',' p+=expression)*\n" +
                "\t\t;\n" +
                "\n" +
                "selector\n" +
                "\t: i+=arrayOrRecordSelector*\n" +
                "\t;\n" +
                "\n" +
                "arrayOrRecordSelector\n" +
                "\t: '[' e=expression ']'\t\t# arraySelector\n" +
                "\t| '.' ID\t\t\t\t\t\t\t\t\t# recordSelector\n" +
                "\t;\n" +
                "\n" +
                "STRING_LITERAL\n" +
                "   : '\\'' ('\\'\\'' | ~ ('\\''))* '\\''\n" +
                "   ;\n" +
                "\n" +
                "Constant\n" +
                "\t:   IntegerConstant\n" +
                "\t;\n" +
                "\n" +
                "IntegerConstant: DigitSequence;\n" +
                "\n" +
                "\n" +
                "fragment\n" +
                "Sign\n" +
                "\t:   '+' | '-'\n" +
                "\t;\n" +
                "\n" +
                "fragment\n" +
                "DigitSequence\n" +
                "\t:   Digit+\n" +
                "\t;\n" +
                "\n" +
                "fragment\n" +
                "Digit: [0-9];\n" +
                "\n" +
                "Whitespace\n" +
                "\t\t:   [ \\t]+\n" +
                "\t\t-> skip\n" +
                "\t\t;\n" +
                "\n" +
                "Newline\n" +
                "\t\t:   (   '\\r' '\\n'?\n" +
                "\t\t|   '\\n'\n" +
                "\t\t)\n" +
                "\t\t-> skip\n" +
                "\t\t;\n" +
                "\n" +
                "BlockComment\n" +
                "\t\t:   '(*' (BlockComment|.)*? '*)'\n" +
                "\t\t-> skip\n" +
                "\t\t;\n" +
                "\n" +
                "\n" +
                "SEMI:\t\t';';\n" +
                "COLON:\t\t':';\n" +
                "DOT:\t\t'.';\n" +
                "LPAREN:\t\t'(';\n" +
                "RPAREN:\t\t')';\n" +
                "COMMA:\t\t',';\n" +
                "PLUS:\t\t'+';\n" +
                "AND:\t\t'&';\n" +
                "MINUS:\t\t'-';\n" +
                "NOTEQUAL:\t'#';\n" +
                "EQUAL:\t\t'=';\n" +
                "STAR:\t\t'*';\n" +
                "NOT:\t\t'~';\n" +
                "LT:\t\t\t'<';\n" +
                "LE:\t\t\t'<=';\n" +
                "GT:\t\t\t'>';\n" +
                "GE:\t\t\t'>=';\n" +
                "Assign:\t\t':=';\n" +
                "\n" +
                "MODULE:\t\t'MODULE';\n" +
                "IMPORT:\t\t'IMPORT';\n" +
                "VAR:\t\t'VAR';\n" +
                "BEGIN:\t\t'BEGIN';\n" +
                "CONST:\t\t'CONST';\n" +
                "END:\t\t'END';\n" +
                "PROCEDURE:\t'PROCEDURE';\n" +
                "TYPE:\t\t'TYPE';\n" +
                "ARRAY:\t\t'ARRAY';\n" +
                "OF:\t\t\t'OF';\n" +
                "OR:\t\t\t'OR';\n" +
                "RECORD:\t\t'RECORD';\n" +
                "WHILE:\t\t'WHILE';\n" +
                "DO:\t\t\t'DO';\n" +
                "IF:\t\t\t'IF';\n" +
                "THEN:\t\t'THEN';\n" +
                "ELSE:\t\t'ELSE';\n" +
                "ELSIF:\t\t'ELSIF';\n" +
                "REPEAT:\t\t'REPEAT';\n" +
                "UNTIL:\t\t'UNTIL';\n" +
                "DIV:\t\t'DIV';\n" +
                "MOD:\t\t'MOD';\n" +
                "\n" +
                "ID:         [a-zA-Z_] [_a-zA-Z0-9]*;\n" +
                "\n" +
                "ErrorChar : . ;");

        CharStream input = CharStreams.fromString("MODULE TestOberon0;\n" +
                "  VAR n: INTEGER;\n" +
                "    a: ARRAY 10 OF INTEGER;\n" +
                "\n" +
                "  PROCEDURE perm(k: INTEGER);\n" +
                "    VAR i, x: INTEGER;\n" +
                "  BEGIN\n" +
                "    IF k = 0 THEN i := 0;\n" +
                "      WHILE i < n DO WriteInt(a[i], 5); i := i+1 END ;\n" +
                "      WriteLn;\n" +
                "    ELSE perm(k-1); i := 0;\n" +
                "      WHILE i < k-1 DO\n" +
                "        x := a[i]; a[i] := a[k-1]; a[k-1] := x;\n" +
                "        perm(k-1);\n" +
                "        x := a[i]; a[i] := a[k-1]; a[k-1] := x;\n" +
                "        i := i+1\n" +
                "      END\n" +
                "    END\n" +
                "  END perm;\n" +
                "\n" +
                "  PROCEDURE Permutations*;\n" +
                "  BEGIN OpenInput; n := 0;\n" +
                "    WHILE ~eot() DO ReadInt(a[n]); n := n+1 END ;\n" +
                "    perm(n)\n" +
                "  END Permutations;\n" +
                "\n" +
                "  PROCEDURE MagicSquares*;  (*magic square of order 3, 5, 7, ... *)\n" +
                "    VAR i, j, x, nx, nsq, n: INTEGER;\n" +
                "      M: ARRAY 13 OF ARRAY 13 OF INTEGER;\n" +
                "  BEGIN OpenInput;\n" +
                "    IF ~eot() THEN\n" +
                "      ReadInt(n); nsq := n*n; x := 0;\n" +
                "      i := n DIV 2; j := n-1;\n" +
                "      WHILE x < nsq DO\n" +
                "        nx := n + x; j := (j-1) MOD n; x := x+1; M[i][j] := x;\n" +
                "        WHILE x < nx DO\n" +
                "          i := (i+1) MOD n; j := (j+1) MOD n;\n" +
                "          x := x+1; M[i][j] := x\n" +
                "        END\n" +
                "      END ;\n" +
                "      i := 0;\n" +
                "      WHILE i < n DO\n" +
                "        j := 0;\n" +
                "        WHILE j < n DO WriteInt(M[i][j], 6); j := j+1 END ;\n" +
                "        i := i+1; WriteLn\n" +
                "      END\n" +
                "    END\n" +
                "  END MagicSquares;\n" +
                "\n" +
                "  PROCEDURE PrimeNumbers*;\n" +
                "    VAR i, k, m, x, inc, lim, sqr: INTEGER; prim: BOOLEAN;\n" +
                "      p: ARRAY 400 OF INTEGER;\n" +
                "      v: ARRAY 20 OF INTEGER;\n" +
                "  BEGIN OpenInput; ReadInt(n);\n" +
                "    x := 1; inc := 4; lim := 1; sqr := 4; m := 0; i := 3;\n" +
                "    WHILE i <= n DO\n" +
                "      REPEAT x := x + inc; inc := 6 - inc;\n" +
                "        IF sqr <= x THEN  (*sqr = p[lim]^2*)\n" +
                "          v[lim] := sqr; lim := lim + 1; sqr := p[lim]*p[lim]\n" +
                "        END ;\n" +
                "        k := 2; prim := TRUE;\n" +
                "        WHILE prim & (k < lim) DO\n" +
                "          k := k+1;\n" +
                "          IF v[k] < x THEN v[k] := v[k] + p[k] END ;\n" +
                "          prim := x # v[k]\n" +
                "        END\n" +
                "      UNTIL prim;\n" +
                "      p[i] := x; WriteInt(x, 5); i := i+1;\n" +
                "      IF m = 10 THEN WriteLn; m := 0 ELSE m := m+1 END\n" +
                "    END ;\n" +
                "    IF m > 0 THEN WriteLn END\n" +
                "  END PrimeNumbers;\n" +
                "\n" +
                "  PROCEDURE Fractions*;  (* Tabulate fractions 1/n*)\n" +
                "    CONST Base = 10; N = 32;\n" +
                "    VAR i, j, m, r, n: INTEGER;\n" +
                "      d: ARRAY N OF INTEGER;  (*digits*)\n" +
                "      x: ARRAY N OF INTEGER;  (*index*)\n" +
                "  BEGIN OpenInput;\n" +
                "    IF ~eot() THEN\n" +
                "      ReadInt(n); i := 2;\n" +
                "      WHILE i <= n DO j := 0;\n" +
                "        WHILE j < i DO x[j] := 0; j := j+1 END ;\n" +
                "        m := 0; r := 1;\n" +
                "        WHILE x[r] = 0 DO\n" +
                "          x[r] := m; r := Base*r; d[m] := r DIV i; r := r MOD i; m := m+1\n" +
                "        END ;\n" +
                "        WriteInt(i, 5); WriteChar(9); WriteChar(46); j := 0;\n" +
                "        WHILE j < x[r] DO WriteChar(d[j] + 48); j := j+1 END ;\n" +
                "        WriteChar(32);  (*blank*)\n" +
                "        WHILE j < m DO WriteChar(d[j] + 48); j := j+1 END ;\n" +
                "        WriteLn; i := i+1\n" +
                "      END\n" +
                "    END\n" +
                "  END Fractions;\n" +
                "\n" +
                "  PROCEDURE Powers*;\n" +
                "    CONST N = 32; M = 11;  (*M ~ N*log2*)\n" +
                "    VAR i, k, n, exp: INTEGER;\n" +
                "      c, r, t: INTEGER;\n" +
                "      d: ARRAY M OF INTEGER;\n" +
                "      f: ARRAY N OF INTEGER;\n" +
                "  BEGIN OpenInput;\n" +
                "    IF ~eot() THEN\n" +
                "      ReadInt(n); d[0] := 1; k := 1; exp := 1;\n" +
                "      WHILE exp < n DO\n" +
                "        (*compute d = 2^exp*)\n" +
                "        c := 0;  (*carry*) i := 0;\n" +
                "        WHILE i < k DO\n" +
                "          t := 2*d[i] + c;\n" +
                "          IF t < 10 THEN d[i] := t; c := 0 ELSE d[i] := t - 10; c := 1 END ;\n" +
                "          i := i+1\n" +
                "        END ;\n" +
                "        IF c = 1 THEN d[k] := 1; k := k+1 END ;\n" +
                "        (*write d*) i := M;\n" +
                "        WHILE i > k DO i := i-1; WriteChar(32) (*blank*) END ;\n" +
                "        WHILE i > 0 DO i := i-1; WriteChar(d[i] + 48) END ;\n" +
                "        WriteInt(exp, M);\n" +
                "        (*compute  f = 2^-exp*)\n" +
                "        WriteChar(9);; WriteChar(46); r := 0; i := 1;\n" +
                "        WHILE i < exp DO\n" +
                "          r := 10*r + f[i]; f[i] := r DIV 2; r := r MOD 2;\n" +
                "          WriteChar(f[i] + 48); i := i+1\n" +
                "        END ;\n" +
                "        f[exp] := 5; WriteChar(53); (*5*) WriteLn; exp := exp + 1\n" +
                "      END\n" +
                "    END\n" +
                "  END Powers;\n" +
                "\n" +
                "END TestOberon0.");
        LexerInterpreter lexEngine = g.createLexerInterpreter(input);
        CommonTokenStream tokens = new CommonTokenStream(lexEngine);

        GrammarParserInterpreter parser = new GrammarParserInterpreter(g,
                new ATNDeserializer().deserialize(ATNSerializer.getSerialized(g.getATN()).toArray()), tokens);
        ParseTree t = parser.parse(g.rules.get("moduleDefinition").index);
        String treeText = t.toStringTree(parser).replaceAll(":\\d", "");
        String expectedTreeText = "(moduleDefinition MODULE TestOberon0 ; (declarations (localDeclaration (variableDeclaration VAR (singleVariableDeclaration (exportableID n) : (typeName INTEGER) ;) (singleVariableDeclaration (exportableID a) : (typeName ARRAY (expression 10) OF (typeName INTEGER)) ;))) (procedureDeclaration PROCEDURE perm (procedureParameters ( (procedureParameter k : (typeName INTEGER)) )) ; (localDeclaration (variableDeclaration VAR (singleVariableDeclaration (exportableID i) , (exportableID x) : (typeName INTEGER) ;))) (block BEGIN (statements (statement (if_statement IF (expression (expression k selector) = (expression 0)) THEN (statements (statement (assign_statement i selector := (expression 0))) ; (statement (while_statement WHILE (expression (expression i selector) < (expression n selector)) DO (statements (statement (procCall_statement WriteInt ( (callParameters (expression a (selector (arrayOrRecordSelector [ (expression i selector) ]))) , (expression 5)) ))) ; (statement (assign_statement i selector := (expression (expression i selector) + (expression 1))))) END)) ; (statement (procCall_statement WriteLn)) ; statement) ELSE (statements (statement (procCall_statement perm ( (callParameters (expression (expression k selector) - (expression 1))) ))) ; (statement (assign_statement i selector := (expression 0))) ; (statement (while_statement WHILE (expression (expression i selector) < (expression (expression k selector) - (expression 1))) DO (statements (statement (assign_statement x selector := (expression a (selector (arrayOrRecordSelector [ (expression i selector) ]))))) ; (statement (assign_statement a (selector (arrayOrRecordSelector [ (expression i selector) ])) := (expression a (selector (arrayOrRecordSelector [ (expression (expression k selector) - (expression 1)) ]))))) ; (statement (assign_statement a (selector (arrayOrRecordSelector [ (expression (expression k selector) - (expression 1)) ])) := (expression x selector))) ; (statement (procCall_statement perm ( (callParameters (expression (expression k selector) - (expression 1))) ))) ; (statement (assign_statement x selector := (expression a (selector (arrayOrRecordSelector [ (expression i selector) ]))))) ; (statement (assign_statement a (selector (arrayOrRecordSelector [ (expression i selector) ])) := (expression a (selector (arrayOrRecordSelector [ (expression (expression k selector) - (expression 1)) ]))))) ; (statement (assign_statement a (selector (arrayOrRecordSelector [ (expression (expression k selector) - (expression 1)) ])) := (expression x selector))) ; (statement (assign_statement i selector := (expression (expression i selector) + (expression 1))))) END))) END))) END perm) ;) (procedureDeclaration PROCEDURE Permutations * ; (block BEGIN (statements (statement (procCall_statement OpenInput)) ; (statement (assign_statement n selector := (expression 0))) ; (statement (while_statement WHILE (expression ~ (expression eot ( ))) DO (statements (statement (procCall_statement ReadInt ( (callParameters (expression a (selector (arrayOrRecordSelector [ (expression n selector) ])))) ))) ; (statement (assign_statement n selector := (expression (expression n selector) + (expression 1))))) END)) ; (statement (procCall_statement perm ( (callParameters (expression n selector)) )))) END Permutations) ;) (procedureDeclaration PROCEDURE MagicSquares * ; (localDeclaration (variableDeclaration VAR (singleVariableDeclaration (exportableID i) , (exportableID j) , (exportableID x) , (exportableID nx) , (exportableID nsq) , (exportableID n) : (typeName INTEGER) ;) (singleVariableDeclaration (exportableID M) : (typeName ARRAY (expression 13) OF (typeName ARRAY (expression 13) OF (typeName INTEGER))) ;))) (block BEGIN (statements (statement (procCall_statement OpenInput)) ; (statement (if_statement IF (expression ~ (expression eot ( ))) THEN (statements (statement (procCall_statement ReadInt ( (callParameters (expression n selector)) ))) ; (statement (assign_statement nsq selector := (expression (expression n selector) * (expression n selector)))) ; (statement (assign_statement x selector := (expression 0))) ; (statement (assign_statement i selector := (expression (expression n selector) DIV (expression 2)))) ; (statement (assign_statement j selector := (expression (expression n selector) - (expression 1)))) ; (statement (while_statement WHILE (expression (expression x selector) < (expression nsq selector)) DO (statements (statement (assign_statement nx selector := (expression (expression n selector) + (expression x selector)))) ; (statement (assign_statement j selector := (expression (expression ( (expression (expression j selector) - (expression 1)) )) MOD (expression n selector)))) ; (statement (assign_statement x selector := (expression (expression x selector) + (expression 1)))) ; (statement (assign_statement M (selector (arrayOrRecordSelector [ (expression i selector) ]) (arrayOrRecordSelector [ (expression j selector) ])) := (expression x selector))) ; (statement (while_statement WHILE (expression (expression x selector) < (expression nx selector)) DO (statements (statement (assign_statement i selector := (expression (expression ( (expression (expression i selector) + (expression 1)) )) MOD (expression n selector)))) ; (statement (assign_statement j selector := (expression (expression ( (expression (expression j selector) + (expression 1)) )) MOD (expression n selector)))) ; (statement (assign_statement x selector := (expression (expression x selector) + (expression 1)))) ; (statement (assign_statement M (selector (arrayOrRecordSelector [ (expression i selector) ]) (arrayOrRecordSelector [ (expression j selector) ])) := (expression x selector)))) END))) END)) ; (statement (assign_statement i selector := (expression 0))) ; (statement (while_statement WHILE (expression (expression i selector) < (expression n selector)) DO (statements (statement (assign_statement j selector := (expression 0))) ; (statement (while_statement WHILE (expression (expression j selector) < (expression n selector)) DO (statements (statement (procCall_statement WriteInt ( (callParameters (expression M (selector (arrayOrRecordSelector [ (expression i selector) ]) (arrayOrRecordSelector [ (expression j selector) ]))) , (expression 6)) ))) ; (statement (assign_statement j selector := (expression (expression j selector) + (expression 1))))) END)) ; (statement (assign_statement i selector := (expression (expression i selector) + (expression 1)))) ; (statement (procCall_statement WriteLn))) END))) END))) END MagicSquares) ;) (procedureDeclaration PROCEDURE PrimeNumbers * ; (localDeclaration (variableDeclaration VAR (singleVariableDeclaration (exportableID i) , (exportableID k) , (exportableID m) , (exportableID x) , (exportableID inc) , (exportableID lim) , (exportableID sqr) : (typeName INTEGER) ;) (singleVariableDeclaration (exportableID prim) : (typeName BOOLEAN) ;) (singleVariableDeclaration (exportableID p) : (typeName ARRAY (expression 400) OF (typeName INTEGER)) ;) (singleVariableDeclaration (exportableID v) : (typeName ARRAY (expression 20) OF (typeName INTEGER)) ;))) (block BEGIN (statements (statement (procCall_statement OpenInput)) ; (statement (procCall_statement ReadInt ( (callParameters (expression n selector)) ))) ; (statement (assign_statement x selector := (expression 1))) ; (statement (assign_statement inc selector := (expression 4))) ; (statement (assign_statement lim selector := (expression 1))) ; (statement (assign_statement sqr selector := (expression 4))) ; (statement (assign_statement m selector := (expression 0))) ; (statement (assign_statement i selector := (expression 3))) ; (statement (while_statement WHILE (expression (expression i selector) <= (expression n selector)) DO (statements (statement (repeat_statement REPEAT (statements (statement (assign_statement x selector := (expression (expression x selector) + (expression inc selector)))) ; (statement (assign_statement inc selector := (expression (expression 6) - (expression inc selector)))) ; (statement (if_statement IF (expression (expression sqr selector) <= (expression x selector)) THEN (statements (statement (assign_statement v (selector (arrayOrRecordSelector [ (expression lim selector) ])) := (expression sqr selector))) ; (statement (assign_statement lim selector := (expression (expression lim selector) + (expression 1)))) ; (statement (assign_statement sqr selector := (expression (expression p (selector (arrayOrRecordSelector [ (expression lim selector) ]))) * (expression p (selector (arrayOrRecordSelector [ (expression lim selector) ]))))))) END)) ; (statement (assign_statement k selector := (expression 2))) ; (statement (assign_statement prim selector := (expression TRUE selector))) ; (statement (while_statement WHILE (expression (expression prim selector) & (expression ( (expression (expression k selector) < (expression lim selector)) ))) DO (statements (statement (assign_statement k selector := (expression (expression k selector) + (expression 1)))) ; (statement (if_statement IF (expression (expression v (selector (arrayOrRecordSelector [ (expression k selector) ]))) < (expression x selector)) THEN (statements (statement (assign_statement v (selector (arrayOrRecordSelector [ (expression k selector) ])) := (expression (expression v (selector (arrayOrRecordSelector [ (expression k selector) ]))) + (expression p (selector (arrayOrRecordSelector [ (expression k selector) ]))))))) END)) ; (statement (assign_statement prim selector := (expression (expression x selector) # (expression v (selector (arrayOrRecordSelector [ (expression k selector) ]))))))) END))) UNTIL (expression prim selector))) ; (statement (assign_statement p (selector (arrayOrRecordSelector [ (expression i selector) ])) := (expression x selector))) ; (statement (procCall_statement WriteInt ( (callParameters (expression x selector) , (expression 5)) ))) ; (statement (assign_statement i selector := (expression (expression i selector) + (expression 1)))) ; (statement (if_statement IF (expression (expression m selector) = (expression 10)) THEN (statements (statement (procCall_statement WriteLn)) ; (statement (assign_statement m selector := (expression 0)))) ELSE (statements (statement (assign_statement m selector := (expression (expression m selector) + (expression 1))))) END))) END)) ; (statement (if_statement IF (expression (expression m selector) > (expression 0)) THEN (statements (statement (procCall_statement WriteLn))) END))) END PrimeNumbers) ;) (procedureDeclaration PROCEDURE Fractions * ; (localDeclaration (constDeclaration CONST (constDeclarationElement Base = (expression 10) ;) (constDeclarationElement N = (expression 32) ;))) (localDeclaration (variableDeclaration VAR (singleVariableDeclaration (exportableID i) , (exportableID j) , (exportableID m) , (exportableID r) , (exportableID n) : (typeName INTEGER) ;) (singleVariableDeclaration (exportableID d) : (typeName ARRAY (expression N selector) OF (typeName INTEGER)) ;) (singleVariableDeclaration (exportableID x) : (typeName ARRAY (expression N selector) OF (typeName INTEGER)) ;))) (block BEGIN (statements (statement (procCall_statement OpenInput)) ; (statement (if_statement IF (expression ~ (expression eot ( ))) THEN (statements (statement (procCall_statement ReadInt ( (callParameters (expression n selector)) ))) ; (statement (assign_statement i selector := (expression 2))) ; (statement (while_statement WHILE (expression (expression i selector) <= (expression n selector)) DO (statements (statement (assign_statement j selector := (expression 0))) ; (statement (while_statement WHILE (expression (expression j selector) < (expression i selector)) DO (statements (statement (assign_statement x (selector (arrayOrRecordSelector [ (expression j selector) ])) := (expression 0))) ; (statement (assign_statement j selector := (expression (expression j selector) + (expression 1))))) END)) ; (statement (assign_statement m selector := (expression 0))) ; (statement (assign_statement r selector := (expression 1))) ; (statement (while_statement WHILE (expression (expression x (selector (arrayOrRecordSelector [ (expression r selector) ]))) = (expression 0)) DO (statements (statement (assign_statement x (selector (arrayOrRecordSelector [ (expression r selector) ])) := (expression m selector))) ; (statement (assign_statement r selector := (expression (expression Base selector) * (expression r selector)))) ; (statement (assign_statement d (selector (arrayOrRecordSelector [ (expression m selector) ])) := (expression (expression r selector) DIV (expression i selector)))) ; (statement (assign_statement r selector := (expression (expression r selector) MOD (expression i selector)))) ; (statement (assign_statement m selector := (expression (expression m selector) + (expression 1))))) END)) ; (statement (procCall_statement WriteInt ( (callParameters (expression i selector) , (expression 5)) ))) ; (statement (procCall_statement WriteChar ( (callParameters (expression 9)) ))) ; (statement (procCall_statement WriteChar ( (callParameters (expression 46)) ))) ; (statement (assign_statement j selector := (expression 0))) ; (statement (while_statement WHILE (expression (expression j selector) < (expression x (selector (arrayOrRecordSelector [ (expression r selector) ])))) DO (statements (statement (procCall_statement WriteChar ( (callParameters (expression (expression d (selector (arrayOrRecordSelector [ (expression j selector) ]))) + (expression 48))) ))) ; (statement (assign_statement j selector := (expression (expression j selector) + (expression 1))))) END)) ; (statement (procCall_statement WriteChar ( (callParameters (expression 32)) ))) ; (statement (while_statement WHILE (expression (expression j selector) < (expression m selector)) DO (statements (statement (procCall_statement WriteChar ( (callParameters (expression (expression d (selector (arrayOrRecordSelector [ (expression j selector) ]))) + (expression 48))) ))) ; (statement (assign_statement j selector := (expression (expression j selector) + (expression 1))))) END)) ; (statement (procCall_statement WriteLn)) ; (statement (assign_statement i selector := (expression (expression i selector) + (expression 1))))) END))) END))) END Fractions) ;) (procedureDeclaration PROCEDURE Powers * ; (localDeclaration (constDeclaration CONST (constDeclarationElement N = (expression 32) ;) (constDeclarationElement M = (expression 11) ;))) (localDeclaration (variableDeclaration VAR (singleVariableDeclaration (exportableID i) , (exportableID k) , (exportableID n) , (exportableID exp) : (typeName INTEGER) ;) (singleVariableDeclaration (exportableID c) , (exportableID r) , (exportableID t) : (typeName INTEGER) ;) (singleVariableDeclaration (exportableID d) : (typeName ARRAY (expression M selector) OF (typeName INTEGER)) ;) (singleVariableDeclaration (exportableID f) : (typeName ARRAY (expression N selector) OF (typeName INTEGER)) ;))) (block BEGIN (statements (statement (procCall_statement OpenInput)) ; (statement (if_statement IF (expression ~ (expression eot ( ))) THEN (statements (statement (procCall_statement ReadInt ( (callParameters (expression n selector)) ))) ; (statement (assign_statement d (selector (arrayOrRecordSelector [ (expression 0) ])) := (expression 1))) ; (statement (assign_statement k selector := (expression 1))) ; (statement (assign_statement exp selector := (expression 1))) ; (statement (while_statement WHILE (expression (expression exp selector) < (expression n selector)) DO (statements (statement (assign_statement c selector := (expression 0))) ; (statement (assign_statement i selector := (expression 0))) ; (statement (while_statement WHILE (expression (expression i selector) < (expression k selector)) DO (statements (statement (assign_statement t selector := (expression (expression (expression 2) * (expression d (selector (arrayOrRecordSelector [ (expression i selector) ])))) + (expression c selector)))) ; (statement (if_statement IF (expression (expression t selector) < (expression 10)) THEN (statements (statement (assign_statement d (selector (arrayOrRecordSelector [ (expression i selector) ])) := (expression t selector))) ; (statement (assign_statement c selector := (expression 0)))) ELSE (statements (statement (assign_statement d (selector (arrayOrRecordSelector [ (expression i selector) ])) := (expression (expression t selector) - (expression 10)))) ; (statement (assign_statement c selector := (expression 1)))) END)) ; (statement (assign_statement i selector := (expression (expression i selector) + (expression 1))))) END)) ; (statement (if_statement IF (expression (expression c selector) = (expression 1)) THEN (statements (statement (assign_statement d (selector (arrayOrRecordSelector [ (expression k selector) ])) := (expression 1))) ; (statement (assign_statement k selector := (expression (expression k selector) + (expression 1))))) END)) ; (statement (assign_statement i selector := (expression M selector))) ; (statement (while_statement WHILE (expression (expression i selector) > (expression k selector)) DO (statements (statement (assign_statement i selector := (expression (expression i selector) - (expression 1)))) ; (statement (procCall_statement WriteChar ( (callParameters (expression 32)) )))) END)) ; (statement (while_statement WHILE (expression (expression i selector) > (expression 0)) DO (statements (statement (assign_statement i selector := (expression (expression i selector) - (expression 1)))) ; (statement (procCall_statement WriteChar ( (callParameters (expression (expression d (selector (arrayOrRecordSelector [ (expression i selector) ]))) + (expression 48))) )))) END)) ; (statement (procCall_statement WriteInt ( (callParameters (expression exp selector) , (expression M selector)) ))) ; (statement (procCall_statement WriteChar ( (callParameters (expression 9)) ))) ; statement ; (statement (procCall_statement WriteChar ( (callParameters (expression 46)) ))) ; (statement (assign_statement r selector := (expression 0))) ; (statement (assign_statement i selector := (expression 1))) ; (statement (while_statement WHILE (expression (expression i selector) < (expression exp selector)) DO (statements (statement (assign_statement r selector := (expression (expression (expression 10) * (expression r selector)) + (expression f (selector (arrayOrRecordSelector [ (expression i selector) ])))))) ; (statement (assign_statement f (selector (arrayOrRecordSelector [ (expression i selector) ])) := (expression (expression r selector) DIV (expression 2)))) ; (statement (assign_statement r selector := (expression (expression r selector) MOD (expression 2)))) ; (statement (procCall_statement WriteChar ( (callParameters (expression (expression f (selector (arrayOrRecordSelector [ (expression i selector) ]))) + (expression 48))) ))) ; (statement (assign_statement i selector := (expression (expression i selector) + (expression 1))))) END)) ; (statement (assign_statement f (selector (arrayOrRecordSelector [ (expression exp selector) ])) := (expression 5))) ; (statement (procCall_statement WriteChar ( (callParameters (expression 53)) ))) ; (statement (procCall_statement WriteLn)) ; (statement (assign_statement exp selector := (expression (expression exp selector) + (expression 1))))) END))) END))) END Powers) ;)) (block END TestOberon0) . <EOF>)";
        assertEquals(expectedTreeText, treeText);
    }

    // LR rewrite test
    @Test
    public void testrulenameextractor() {
        final String string = "(r2)+";
//        boolean match = string.matches("\\(r\\d+\\)\\*|\\(r\\d+\\)\\+|\\(r\\d+\\)\\?");
//        boolean othermatch = string.matches("\\((r\\d+|\\([^\\)]*\\))\\*|\\((r\\d+|\\([^\\)]*\\))\\+|\\((r\\d+|\\([^\\)]*\\))\\?");
        boolean match =  isMatch(string);
        String extr = string.replace("(", "")
                .replace(")", "")
                .replace("+", "")
                .replace("?", "")
                .replace("*", "");
        System.out.println(extr);

        String correctedPart = string.replace(extr, "r11");
        System.out.println(correctedPart);
    }

    public boolean isMatch(String str) {
        if (str.matches("\\(r\\d+\\)\\*|\\(r\\d+\\)\\+|\\(r\\d+\\)\\?")) {
            return true;
        } else {
            int openIndex = str.indexOf('(');
            int closeIndex = str.lastIndexOf(')');
            if (openIndex == -1 || closeIndex == -1 || closeIndex < openIndex) {
                return false;
            } else {
                return isMatch(str.substring(openIndex + 1, closeIndex));
            }
        }
    }


}