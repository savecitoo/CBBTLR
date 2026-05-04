package generated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratedTest {


    @ParameterizedTest
    @CsvSource({
            // --- casi base ---
            "x, true",
            "-123, true",

            // --- unary minus ---
            "- t, true",
            "- - x, true",

            // --- potenza (associatività destra) ---
            "y ^ x, true",
            "y ^ w ^ z, true",

            // --- precedenze ---
            "n * x ^ t, true",
            "- n * -312 ^ -12, true",

            // --- parentesi ---
            "( CiaoUser ), true",
            "( x + 3417 ) * factor, true",

            // --- errori ---
            "x ^, false",
            "^ f, false",
            "factor * ^ da, false",
            "-, false",
    })
    void testGenerated(String input, boolean result) {
        MyGrammarParser parser = new MyGrammarParser();
        //System.out.println(parser.parse("9 * 9"));
        assertThat(parser.isRecognized(input)).isEqualTo(result);
    }

    @ParameterizedTest
    @CsvSource({
            // --- assegnazioni base ---
            "x = 1, true",
            "abc = x + 42, true",

            // --- if / else (matched) ---
            "if x then y = 1 else z = 2, true",
            "if x then if y then z = 1 else w = 2, true",

            // --- while ---
            "while x do y = 3, true",

            // --- blocchi ---
            "{ x = 1 }, true",
            "{ x = 1 ; y = 2 }, true",

            // --- unmatched if (senza else) ---
            "if x then y = 1, true",
            "if x then if y then z = 1, true",

            // --- combinazioni ---
            "if x then { y = 1 ; z = 2 } else w = 3, true",
            "while x do if y then z = 1 else w = 2, true",

            // --- errori ---
            "if x then, false",
            "if then y = 1, false",
            "{ }, false",
            "x =, false",
            "else y = 1, false",
    })
    void testGeneratedIfThenElse(String input, boolean result) {
        ITEGrammarParser parser = new ITEGrammarParser();
        assertThat(parser.isRecognized(input)).isEqualTo(result);
    }

    @ParameterizedTest
    @CsvSource({
            // --- variabili e costanti ---
            "p, true",
            "true, true",
            "false, true",
            "myVar, true",

            // --- not ---
            "!p, true",
            "!!p, true",
            "!true, true",

            // --- and ---
            "p & q, true",
            "p & q & r, true",
            "p & !q, true",

            // --- or ---
            "p | q, true",
            "p | q | r, true",
            "p | !q, true",

            // --- implies ---
            "p -> q, true",
            "p -> q -> r, true",
            "p & q -> r | s, true",

            // --- iff ---
            "p <-> q, true",
            "p <-> q <-> r, true",
            "p -> q <-> r -> s, true",

            // --- parentesi ---
            "(p), true",
            "(p & q), true",
            "(p | q) & r, true",
            "!(p & q), true",
            "(p -> q) <-> (!p | q), true",

            // --- formule complesse ---
            "p & q | r -> s <-> t, true",
            "!p & !q -> !(p | q), true",
            "(p -> q) & (q -> r) -> (p -> r), true",

            // --- errori ---
            "p &, false",
            "p | , false",
            "-> p, false",
            "<-> p, false",
            "p <-> , false",
            "( p, false",
            "p ), false",
            "(), false",
            "p q, false",
            "!, false",
    })
    void testLogicFormulaParser(String input, boolean result) {
        LogicGrammarParser parser = new LogicGrammarParser();
        assertThat(parser.isRecognized(input)).isEqualTo(result);
    }

}
