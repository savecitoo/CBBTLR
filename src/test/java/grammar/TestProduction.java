package grammar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProduction {

    @Test
    void testToStringBase() {
        String prod = "A -> a";
        ContextFreeProduction CFprod = new ContextFreeProduction(
                "A",
                List.of(List.of("a"))
        );
        assertThat(CFprod.toString()).isEqualTo(prod);
    }

    @Test
    void testToStringMultipleProduction() {
        String prod = "A -> a | aB | CD";
        ContextFreeProduction CFprod = new ContextFreeProduction(
                "A",
                List.of(
                        List.of("a"),
                        List.of("a", "B"),
                        List.of("C", "D")
                        )
        );
        assertThat(CFprod.toString()).isEqualTo(prod);
    }

    @Test
    void testFromString1() {
        assertThat(ContextFreeProduction.fromString("A -> a")).isEqualTo(new ContextFreeProduction(
                "A",
                List.of(List.of("a"))
        ));
    }

    @Test
    void testFromString2() {
        assertThat(ContextFreeProduction.fromString("A -> a | a B | C D")).isEqualTo(
                new ContextFreeProduction(
                        "A",
                        List.of(
                                List.of("a"),
                                List.of("a", "B"),
                                List.of("C", "D")
                        )
                )
        );
    }


    @ParameterizedTest
    @CsvSource({
            "A -> a | a B | C D, a, true",
            "A -> a | a B | C D, d, false",
            "A -> a , d, false",
            "A -> d , d, true"
    })
    void testContains(String prod, String symb, boolean res) {
        assertThat(ContextFreeProduction.fromString(prod).contains(symb)).isEqualTo(res);
    }

    @Test
    void testSymbolsBase() {
        assertThat(
            ContextFreeProduction.fromString("A -> d | C").symbols()
        ).isEqualTo(Set.of("A" , "d", "C"));
    }

    @Test
    void testSymbols() {
        assertThat(
                ContextFreeProduction.fromString("A -> a | a B | C D").symbols()
        ).isEqualTo(Set.of("a" , "B", "D", "A", "C"));
    }



}
