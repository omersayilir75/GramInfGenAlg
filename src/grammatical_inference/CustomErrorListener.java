package grammatical_inference;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.tool.GrammarParserInterpreter;

import java.util.HashMap;
import java.util.List;

public class CustomErrorListener extends BaseErrorListener {
    private int syntaxErrors = 0;
    private CommonToken lastLegalToken;

    private int firstError = -1;
    private int lastError =  -1;


    // cut anything we do not need from the syntaxError handler
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e)  {
        // code for keeping track of substring with lexerrors
        if (e instanceof LexerNoViableAltException) {
            LexerNoViableAltException ex = (LexerNoViableAltException) e;
            if (firstError == -1) {
                firstError = ex.getStartIndex();
                lastError = ex.getStartIndex(); // case only one token...
            }
            lastError = ex.getStartIndex();
        }

        // code for getting the last legal token for the Graminf_mut operator.
        if (recognizer instanceof GrammarParserInterpreter && syntaxErrors < 1) {
            GrammarParserInterpreter parser = (GrammarParserInterpreter) recognizer;

            CommonTokenStream tokens = (CommonTokenStream) parser.getTokenStream();
            List<Token> tokenArrayList = tokens.getTokens();
            int offendingSymbolIndex = tokenArrayList.indexOf(offendingSymbol);
            if (offendingSymbolIndex != 0 ) { // not interested in doing this for negative samples
                lastLegalToken = (CommonToken) tokenArrayList.get(offendingSymbolIndex - 1);
            }
        }
        syntaxErrors++;
    }

    public int getNumberOfSyntaxErrors(){
        return this.syntaxErrors;
    }

    public CommonToken getLastLegalToken() {
        return this.lastLegalToken;
    }

    public int getFirstError() { return this.firstError; }

    public int getLastError() { return this.lastError; }

}


