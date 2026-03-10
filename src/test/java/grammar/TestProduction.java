package grammar;

import org.junit.jupiter.api.Test;

import java.util.List;

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
    void testFromString() {
        assertThat(ContextFreeProduction.fromString("A -> a")).isEqualTo(new ContextFreeProduction(
                "A",
                List.of(List.of("a"))
        ));
    }

}
