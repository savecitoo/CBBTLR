package lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.NoSuchElementException;

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

    @Test
    void testBaseTokenize() {
        Lexer lexer = Lexer.fromString(
                "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n"
        );
        assertThat(lexer.tokenize("93   \n np_hard 9 //ciao"))
                .isEqualTo(
                        List.of(
                                new Token(TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"), "93"),
                                new Token(TokenType.fromString("ID      :   [a-zA-Z_]+"), "np_hard"),
                                new Token(TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"), "9"),
                                new Token(TokenType.fromString("COMMENT : //.*\n"), "//ciao")
                                )
                );
    }

    @Test
    void testWhitespaceSkip() {
        Lexer lexer = Lexer.fromString(
                "ID : [a-z]+\nWS : [ \\t\\n]+ skip\n"
        );

        assertThat(lexer.tokenize("abc   def\tghi"))
                .isEqualTo(
                        List.of(
                                new Token(TokenType.fromString("ID : [a-z]+"), "abc"),
                                new Token(TokenType.fromString("ID : [a-z]+"), "def"),
                                new Token(TokenType.fromString("ID : [a-z]+"), "ghi")
                        )
                );
    }

    @Test
    void testPriorityKeywordVsId() {
        Lexer lexer = Lexer.fromString(
                "IF : if\nID : [a-z]+\nWS : [ \\t\\n]+ skip\n"
        );

        assertThat(lexer.tokenize("if else"))
                .isEqualTo(
                        List.of(
                                new Token(TokenType.fromString("IF : if"), "if"),
                                new Token(TokenType.fromString("ID : [a-z]+"), "else")
                        )
                );
    }

    @Test
    void testLongestMatch() {
        Lexer lexer = Lexer.fromString(
                "EQ : ==\nASSIGN : =\nWS : [ \\t\\n]+ skip\n"
        );

        assertThat(lexer.tokenize("== ="))
                .isEqualTo(
                        List.of(
                                new Token(TokenType.fromString("EQ : =="), "=="),
                                new Token(TokenType.fromString("ASSIGN : ="), "=")
                        )
                );
    }

    @Test
    void testNumberWithDecimal() {
        Lexer lexer = Lexer.fromString(
                "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n]+ skip\n"
        );

        assertThat(lexer.tokenize("10 3.14 42"))
                .isEqualTo(
                        List.of(
                                new Token(TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"), "10"),
                                new Token(TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"), "3.14"),
                                new Token(TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"), "42")
                        )
                );
    }

    @Test
    void testComments() {
        Lexer lexer = Lexer.fromString(
                "COMMENT : //.*\nID : [a-z]+\nWS : [ \\t\\n]+ skip\n"
        );

        assertThat(lexer.tokenize("abc //hello\nxyz"))
                .isEqualTo(
                        List.of(
                                new Token(TokenType.fromString("ID : [a-z]+"), "abc"),
                                new Token(TokenType.fromString("COMMENT : //.*\n"), "//hello"),
                                new Token(TokenType.fromString("ID : [a-z]+"), "xyz")
                        )
                );
    }

    @Test
    void testInvalidToken() {
        Lexer lexer = Lexer.fromString(
                "ID : [a-z]+\nWS : [ \\t\\n]+ skip\n"
        );

        assertThrows(NoSuchElementException.class, () ->
                lexer.tokenize("abc 123")
        );
    }

    @Test
    void testEmptyInput() {
        Lexer lexer = Lexer.fromString(
                "ID : [a-z]+\nWS : [ \\t\\n]+ skip\n"
        );

        assertThat(lexer.tokenize(""))
                .isEqualTo(List.of());
    }

    @Test
    void testAdjacentTokens() {
        Lexer lexer = Lexer.fromString(
                "NUMBER : \\d+\nPLUS : \\+\nWS : [ \\t\\n]+ skip\n"
        );

        assertThat(lexer.tokenize("1+2+3"))
                .isEqualTo(
                        List.of(
                                new Token(TokenType.fromString("NUMBER : \\d+"), "1"),
                                new Token(TokenType.fromString("PLUS : \\+"), "+"),
                                new Token(TokenType.fromString("NUMBER : \\d+"), "2"),
                                new Token(TokenType.fromString("PLUS : \\+"), "+"),
                                new Token(TokenType.fromString("NUMBER : \\d+"), "3")
                        )
                );
    }

}
