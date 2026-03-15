package grammar;

import org.jetbrains.annotations.NotNull;
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

    @Test
    void testRestrictToGrammarSame() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | E \nA -> a\nB -> b C\nC -> c\n D -> d F\n E -> e\n F -> f D"
        );
        Set<String> symb = new HashSet<>(g.N);
        symb.addAll(g.T);
        assertThat(g.restrictTo(symb)).isEqualTo(g);
        symb.addAll(Set.of("Aj", "jo", "fba", "das"));
        assertThat(g.restrictTo(symb)).isEqualTo(g);
    }

    @Test
    void testRestrictToGrammar() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | E \nA -> a\nB -> b C\nC -> c\n D -> d F\n E -> e\n F -> f D"
        );
        assertThat(g.restrictTo(Set.of("S", "B", "E", "C","D", "F", "a", "b", "d", "e", "f"))).isEqualTo(
                new ContextFreeGrammar(
                        Set.of("S", "B", "E", "C", "D", "F"),
                        Set.of("a", "b", "e", "f", "d"),
                        Productions.fromString("S -> E \nB -> b C\n D -> d F\n E -> e\n F -> f D"),
                        "S"
                )
        );
    }

    @Test
    void testSimplifyGrammarSame() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | E \nA -> a\nB -> b C\nC -> c D\n D -> d F\n E -> e\n F -> f"
        );
        assertThat(g.simplifyGrammar()).isEqualTo(g);
    }

    @Test
    void testSimplifyGrammar() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A B | D E \nA -> a\nB -> b C\nC -> c\n D -> d F\n E -> e\n F -> f D"
        );
        assertThat(g.simplifyGrammar()).isEqualTo(new ContextFreeGrammar(
                Set.of("A", "B", "C", "S"),
                Set.of("a", "b", "c"),
                Productions.fromString("S -> A B \nA -> a\nB -> b C\nC -> c"),
                g.S
        ));
        System.out.println(g.simplifyGrammar());
    }
}
