package grammar;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ContextFreeGrammarTest {

    @Test
    void testFromString() {

        assertThat(ContextFreeGrammar.fromString(
                "A -> A a | a | B c\nB -> b | D\nD -> d\nF -> f")).isEqualTo(
            new ContextFreeGrammar(
                    Set.of("A", "B", "D", "F"),
                    Set.of("a", "b", "c", "d", "f"),
                    Productions.fromString("A -> A a | a | B c\nB -> b | D\nD -> d\nF -> f"),
                    "A"
                    )
        );
    }

    @Test
    void testProductive1() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | D E\nA -> a\nB -> b C\nC -> c\n D -> d F\n E -> e\n F -> f D"
        );
        assertThat(g.productive()).isEqualTo(Set.of("A", "B", "C", "E", "S", "a", "b", "c", "d", "e", "f"));
    }

    @Test
    void testProductive2() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString("A -> A a | a | B c\nB -> b | D\nD -> d\nF -> f");
        Set<String> symbols = new HashSet<>(g.N);
        symbols.addAll(g.T);
        assertThat(g.productive()).isEqualTo(symbols);
    }

    @Test
    void testReachable1() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | D E\nA -> a\nB -> b C\nC -> c\n D -> d F\n E -> e\n F -> f D"
        );
        Set<String> symbols = new HashSet<>(g.N);
        symbols.addAll(g.T);
        assertThat(g.reachable()).isEqualTo(symbols);
    }



    @Test
    void testReachable2() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | D \nA -> a\nB -> b C\nC -> c\n D -> d F\n E -> e\n F -> f D"
        );
        assertThat(g.reachable()).isEqualTo(Set.of("A", "B", "C", "D", "F", "S", "a", "b", "c", "d", "f"));
    }

    @Test
    void testReachable3() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | E \nA -> a\nB -> b C\nC -> c\n D -> d F\n E -> e\n F -> f D"
        );
        assertThat(g.reachable()).isEqualTo(Set.of("A", "B", "C", "E", "S", "a", "b", "c","e"));
    }

}
