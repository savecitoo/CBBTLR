package lalrparser;

import grammar.ContextFreeGrammar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.ParseTree;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestParser {

    @ParameterizedTest
    @CsvSource({
            "n, true",
            "n - n, true",
            "n - n - n, true",
            "( n ), true",
            "( n - n ), true",
            "( ( n ) ), true",
            "( n - ( n ) ), true",
            "( n - ( n ) ) - n, true",
            "( n - n, false",                // parentesi non chiusa
            "n -, false",                    // operatore senza operando
            "- n, false",                    // inizio non valido
            "n n, false",                    // due terminali consecutivi
            "( ), false",                    // vuoto dentro parentesi
            "( n ) n, false",                // simboli consecutivi non validi
            "n - ( ), false",                // parentesi vuote
            "( n - ( n ) ) - n (, false",    // parentesi extra
            "( n - ( n ) ) ) - n, false",    // parentesi sbilanciate
            "f e, false",                    // simboli non nel linguaggio
            "n - ( n - ), false",            // espressione incompleta
            "( n - ( n ) ) - ( n ), true",
            "( ( n - n ) - ( n ) ), true"
    })
    void testRecognizer(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E $\n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        List<String> tokens = Arrays.asList(input.split(" "));
        Parser parser = new Parser(g);
        assertThat(parser.isRecognized(tokens))
                .isEqualTo(result);
    }

    @Test
    void testConstructionNotLR() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A a | b A c | d c | b d a\nA -> d"
        );
        assertThatThrownBy(
                () -> new Parser(g)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "a a b d, true",
            "a a b c d, true",
            "b d, true",
            "b c c d, true",

            "a b d, true",
            "a a a b c c d, true",

            "a a, false",
            "b, false",
            "a b, false",
            "a a b c, false",
            "c d, false",
            "a d, false",

            "a a b d d, false",
            "b c d d, false"
    })
    void testLR0Parser(String input, boolean result) {
        List<String> tokens = Arrays.asList(input.split(" "));
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A $\n" +
                "A -> a A | b B\n" +
                "B -> c B | d\n"
        );
        Parser parser = new Parser(g);
        assertThat(parser.isRecognized(tokens)).isEqualTo(result);
    }

    @Test
    void testParseBase() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E $\n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        Parser p = new Parser(g);
        ParseTree tree = new ParseTree(
                "E",
                List.of( new ParseTree("T",
                        List.of(
                        new ParseTree("(", true),
                        new ParseTree("E", List.of(
                                new ParseTree("T", List.of(new ParseTree("n", true)))
                        )

                        ),
                        new ParseTree(")", true)
                )))
        );
        List<String> tokens = Arrays.asList("( n )".split(" "));
        assertThat(p.parse(tokens)).isEqualTo(tree);
    }

    @Test
    void testParseComplex() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E $\n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        Parser p = new Parser(g);

        ParseTree tree = new ParseTree("E", List.of(
                new ParseTree("E", List.of(
                        new ParseTree("T", List.of(
                                new ParseTree("n", true)
                        ))
                )),
                new ParseTree("-", true),
                new ParseTree("T", List.of(
                        new ParseTree("(", true),
                        new ParseTree("E", List.of(
                                new ParseTree("E", List.of(
                                        new ParseTree("T", List.of(
                                                new ParseTree("n", true)
                                        ))
                                )),
                                new ParseTree("-", true),
                                new ParseTree("T", List.of(
                                        new ParseTree("n", true)
                                ))
                        )),
                        new ParseTree(")", true)
                ))
        ));

        List<String> tokens = Arrays.asList("n - ( n - n )".split(" "));
        assertThat(p.parse(tokens)).isEqualTo(tree);
    }

    @Test
    void testParseUnrecognizedThrows() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E $\n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        Parser p = new Parser(g);

        // parentesi non chiusa
        assertThatThrownBy(() -> p.parse(Arrays.asList("( n".split(" "))))
                .isInstanceOf(RuntimeException.class);

        // operatore senza operando destro
        assertThatThrownBy(() -> p.parse(Arrays.asList("n -".split(" "))))
                .isInstanceOf(RuntimeException.class);

        // token sconosciuto
        assertThatThrownBy(() -> p.parse(Arrays.asList("n + n".split(" "))))
                .isInstanceOf(RuntimeException.class);
    }

}
