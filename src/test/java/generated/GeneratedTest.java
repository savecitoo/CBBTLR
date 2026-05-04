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

}
