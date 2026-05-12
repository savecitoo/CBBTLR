package grammar;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProductions {


    @Test
    void testSymbols() {
        Productions p = Productions.fromString("A -> a | a B | C D\nD -> a | a B | C D");
        assertThat(p.symbols()).isEqualTo(Set.of("A", "B", "C", "D", "a"));
    }
}
