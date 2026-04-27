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
                "S -> E    \n" +
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
                "S -> A    \n" +
                        "A -> a A | b B\n" +
                        "B -> c B | d\n"
        );
        Parser parser = new Parser(g);
        assertThat(parser.isRecognized(tokens)).isEqualTo(result);
    }

    @Test
    void testParseBase() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E   \n" +
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
                "S -> E   \n" +
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
                "S -> E   \n" +
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

    @ParameterizedTest
    @CsvSource({
            // --- un solo numero ---
            "n, true",

            // --- addizione e sottrazione ---
            "n + n, true",
            "n - n, true",
            "n + n + n, true",                          // associatività sinistra +
            "n - n - n, true",                          // associatività sinistra -
            "n + n - n, true",                          // misto + e -

            // --- moltiplicazione e divisione ---
            "n * n, true",
            "n / n, true",
            "n * n * n, true",                          // associatività sinistra *
            "n / n / n, true",                          // associatività sinistra /
            "n * n / n, true",                          // misto * e /

            // --- precedenza: * e / prima di + e - ---
            "n + n * n, true",
            "n * n + n, true",
            "n + n * n - n, true",
            "n * n + n / n, true",
            "n - n / n * n, true",

            // --- parentesi ---
            "( n ), true",
            "( n + n ), true",
            "( n * n ), true",
            "( ( n ) ), true",
            "( n + n ) * n, true",                      // parentesi che alterano precedenza
            "n * ( n + n ), true",
            "( n + n ) * ( n - n ), true",
            "( n * ( n + n ) ), true",
            "( ( n + n ) * ( n - n ) ), true",
            "n + ( n * ( n - n ) ) / n, true",
            "( n + n ) * ( n / ( n - n ) ), true",

            // --- espressioni più lunghe e annidate ---
            "n + n * n - n / n, true",
            "( n + n ) * n - n / ( n - n ), true",
            "( ( n + n ) ) * ( ( n ) ), true",
            "n * n + n * n + n * n, true",

            // --- operatori mancanti o doppi ---
            "n n, false",                               // due operandi senza operatore
            "n + + n, false",                           // operatore doppio
            "n * * n, false",
            "n + - n, false",                           // unario non supportato
            "* n, false",                               // inizia con operatore
            "/ n, false",
            "+ n, false",
            "- n, false",
            "n +, false",                               // termina con operatore
            "n -,false",
            "n *,false",
            "n /,false",
            "n + n *, false",                           // operatore finale dopo espressione valida

            // --- parentesi sbilanciate ---
            "( n, false",                               // aperta non chiusa
            "n ), false",                               // chiusa senza apertura
            "( n + n, false",
            "n + n ), false",
            "( ( n ), false",
            "( n ) ), false",
            "( n + ( n * n ), false",
            "( n + n ) * ( n, false",

            // --- parentesi vuote ---
            "( ), false",
            "n + ( ), false",
            "( ) * n, false",

            // --- simboli fuori alfabeto ---
            "x, false",
            "n + x, false",
            "f e, false",
    })
    void testArithmetic(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S' -> S \n" +
                        "S -> E\n" +
                        "E -> E + T | E - T | T\n" +
                        "T -> T * F | T / F | F\n" +
                        "F -> ( E ) | n"
        );
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));
        Parser parser = new Parser(g);
        assertThat(parser.isRecognized(tokens))
                .isEqualTo(result);
    }

    @ParameterizedTest
    @CsvSource({
            // --- casi base ---
            "id, true",
            "num, true",

            // --- unary minus ---
            "- id, true",
            "- - id, true",

            // --- potenza (associatività destra) ---
            "id ^ id, true",
            "id ^ id ^ id, true",

            // --- precedenze ---
            "id * id ^ id, true",
            "- id * num ^ num, true",

            // --- parentesi ---
            "( id ), true",
            "( id + num ) * id, true",

            // --- errori ---
            "id ^, false",
            "^ id, false",
            "id * ^ id, false",
            "-, false",
    })
    void testGrammar1(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S' -> S\n" +
                        "S -> E\n" +
                        "E -> E + T | E - T | T\n" +
                        "T -> T * F | T / F | F\n" +
                        "F -> - F | P ^ F | P\n" +
                        "P -> ( E ) | id | num"
        );

        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));

        Parser parser = new Parser(g);

        assertThat(parser.isRecognized(tokens))
                .isEqualTo(result);
    }

    @ParameterizedTest
    @CsvSource({
            // --- assegnazioni base ---
            "id = num, true",
            "id = id + num, true",
            // --- if / else (matched) ---
            "if id then id = num else id = num, true",
            "if id then if id then id = num else id = num, true",
            // --- while ---
            "while id do id = num, true",
            // --- blocchi ---
            "{ id = num }, true",
            "{ id = num ; id = num }, true",
            // --- unmatched if (senza else) ---
            "if id then id = num, true",
            "if id then if id then id = num, true",
            // --- combinazioni ---
            "if id then { id = num ; id = num } else id = num, true",
            "while id do if id then id = num else id = num, true",
            // --- errori ---
            "if id then, false",
            "if then id = num, false",
            "{ }, false",
            "id =, false",
            "else id = num, false",
    })
    void testIfElseGrammar(String input, boolean expected) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S' -> S\n" +
                        "S -> M | U\n" +
                        "M -> if E then M else M | while E do M | { L } | A\n" +
                        "U -> if E then S | if E then M else U\n" +
                        "L -> L ; S | S\n" +
                        "A -> id = E\n" +
                        // E stratificata: * lega più forte di +, entrambi left-assoc
                        "E -> E + T | T\n" +
                        "T -> T * F | F\n" +
                        "F -> ( E ) | id | num"
        );
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));
        Parser parser = new Parser(g);
        assertThat(parser.isRecognized(tokens))
                .isEqualTo(expected);
    }

}
