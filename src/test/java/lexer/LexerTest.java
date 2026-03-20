package lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LexerTest {

    @ParameterizedTest
    @CsvSource({
            "X",
            "INVALID",
            "123"
    })
    void testFromStringInvalid(String input) {
        assertThrows(IllegalArgumentException.class, () -> TokenType.fromString(input));
    }

    @Test
    void testFromStringBase() {
        assertThat(
                Lexer.fromString(
                        "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip"
                )
        ).isEqualTo(
                new Lexer(
                        List.of(
                                TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"),
                                TokenType.fromString("WS : [ \\t\\n\\r]+ skip")
                                )
                )
        );
    }

    @Test
    void testFromStringComplex() {
        assertThat(
                Lexer.fromString(
                        "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n"
                )
        ).isEqualTo(
                new Lexer(
                        List.of(
                                TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"),
                                TokenType.fromString("WS : [ \\t\\n\\r]+ skip"),
                                TokenType.fromString("COMMENT : //.*"),
                                TokenType.fromString(  "ID      :   [a-zA-Z_]+  ")
                        )
                )
        );
    }

    @Test
    void testFromStringNotEquals() {
        assertThat(
                Lexer.fromString(
                        "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n"
                )
        ).isNotEqualTo(
                new Lexer(
                        List.of(
                                TokenType.fromString("WS : [ \\t\\n\\r]+ skip"),
                                TokenType.fromString("COMMENT : //.*"),
                                TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"),
                                TokenType.fromString(  "ID      :   [a-zA-Z_]+  ")
                        )
                )
        );
    }

}
