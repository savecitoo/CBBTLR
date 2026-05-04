package generated;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneratedTest {

    @Test
    void testGenerated1() {
        MyGrammarParser parser = new MyGrammarParser();
        System.out.println(parser.parse("9 * 9"));
    }

    @Test
    void testGenerated2() {
        MyGrammarParser parser = new MyGrammarParser();
        System.out.println(parser.parse("(9 * 9) + (x ^ 3 - 5)"));
    }

}
