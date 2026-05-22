package grammar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProduction {

    @Test
    void testToStringBase() {
        String prod = "A -> a [A_tag]";
        ContextFreeProduction CFprod = new ContextFreeProduction(
                "A",
                (List.of("a")),
                0,
                "A_tag"
        );
        assertThat(CFprod.toString()).isEqualTo(prod);
    }

    @Test
    void testFromString1() {
        assertThat(ContextFreeProduction.fromString("A -> a", 0).getFirst()).isEqualTo(new ContextFreeProduction(
                "A",
                List.of("a"),0, ""
        ));
    }

    @Test
    void testFromString2() {
        assertThat(ContextFreeProduction.fromString("A -> a | a B | C D #dai", 0)).isEqualTo(
                List.of(
                        new ContextFreeProduction("A", List.of("a"), 0, ""),
                        new ContextFreeProduction("A", List.of("a", "B"), 1, ""),
                        new ContextFreeProduction("A", List.of("C","D"), 2, "dai")
                )
        );
    }



    @Test
    void testSymbolsBase() {
        Set<String> symbols = new HashSet<>();
        for (ContextFreeProduction p : ContextFreeProduction.fromString("A -> d | C", 0))
            symbols.addAll(p.symbols());
        assertThat(
           symbols
        ).isEqualTo(Set.of("A" , "d", "C"));
    }

    @Test
    void testSymbols() {
        Set<String> symbols = new HashSet<>();
        for (ContextFreeProduction p : ContextFreeProduction.fromString("A -> a | a B | C D", 0))
            symbols.addAll(p.symbols());
        assertThat(
                symbols
        ).isEqualTo(Set.of("a" , "B", "D", "A", "C"));
    }

    @Test
    void testEpsilonProductions() {
        assertThat(ContextFreeProduction.fromString("A -> ε | ε B | ε ε | ε ε B C #tag", 0)).isEqualTo(
                List.of(
                        new ContextFreeProduction("A", List.of("ε"), 0, ""),
                        new ContextFreeProduction("A", List.of("B"), 1, ""),
                        new ContextFreeProduction("A", List.of("B", "C"), 2, "tag")
                )
        );
    }

}
