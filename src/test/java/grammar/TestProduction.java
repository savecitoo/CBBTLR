package grammar;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProduction {

    @Test
    void testToStringBase() {
        String prod = "A -> a";
        ContextFreeProduction CFprod = new ContextFreeProduction(
                new NonTerminal("A"),
                List.of(List.of(new Terminal("a")))
        );
        assertThat(CFprod.toString()).isEqualTo(prod);
    }

    @Test
    void testToStringMultipleProduction() {
        String prod = "A -> a | aB | CD";
        ContextFreeProduction CFprod = new ContextFreeProduction(
                new NonTerminal("A"),
                List.of(
                        List.of(new Terminal("a")),
                        List.of(new Terminal("a"), new NonTerminal("B")),
                        List.of(new NonTerminal("C"), new NonTerminal("D"))
                        )
        );
        assertThat(CFprod.toString()).isEqualTo(prod);
    }

}
