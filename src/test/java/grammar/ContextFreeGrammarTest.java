package grammar;

import org.junit.jupiter.api.Test;

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
}
