package generated;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratedTest {


    @ParameterizedTest
    @CsvSource({
            "x, true",
            "-123, true",

            "- t, true",
            "- - x, true",

            "y ^ x, true",
            "y ^ w ^ z, true",

            "n * x ^ t, true",
            "- n * -312 ^ -12, true",

            "( CiaoUser ), true",
            "( x + 3417 ) * factor, true",

            "x ^, false",
            "^ f, false",
            "factor * ^ da, false",
            "-, false",
    })
    void testGenerated(String input, boolean result) {
        MyGrammarParser parser = new MyGrammarParser();
        assertThat(parser.isRecognized(input)).isEqualTo(result);
    }

    @ParameterizedTest
    @CsvSource({
            "x = 1, true",
            "abc = x + 42, true",

            "if x then y = 1 else z = 2, true",
            "if x then if y then z = 1 else w = 2, true",

            "while x do y = 3, true",

            "{ x = 1 }, true",
            "{ x = 1 ; y = 2 }, true",

            "if x then y = 1, true",
            "if x then if y then z = 1, true",

            "if x then { y = 1 ; z = 2 } else w = 3, true",
            "while x do if y then z = 1 else w = 2, true",

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
            "p, true",
            "true, true",
            "false, true",
            "myVar, true",

            "!p, true",
            "!!p, true",
            "!true, true",

            "p & q, true",
            "p & q & r, true",
            "p & !q, true",

            "p | q, true",
            "p | q | r, true",
            "p | !q, true",

            "p -> q, true",
            "p -> q -> r, true",
            "p & q -> r | s, true",

            "p <-> q, true",
            "p <-> q <-> r, true",
            "p -> q <-> r -> s, true",

            "(p), true",
            "(p & q), true",
            "(p | q) & r, true",
            "!(p & q), true",
            "(p -> q) <-> (!p | q), true",

            "p & q | r -> s <-> t, true",
            "!p & !q -> !(p | q), true",
            "(p -> q) & (q -> r) -> (p -> r), true",

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

    @Test
    void testTag() {
        ITEGrammarParser parser = new ITEGrammarParser();
        System.out.println(parser.parse("while x do y = 3"));
    }
}
