package grammatical_inference;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class CustomErrorListener extends BaseErrorListener {
    private int syntaxErrors = 0;

    //todo also report location of errors for operators where this would be relevant information.

    // cut anything we do not need from the syntaxError handler
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e)  {
        syntaxErrors++;
    }

    public int getSyntaxErrors(){
        return this.syntaxErrors;
    }
}


