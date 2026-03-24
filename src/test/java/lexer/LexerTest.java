package lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.SymbolTable;

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
                        "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip",
                        new SymbolTable<>()
                )
        ).isEqualTo(
                new Lexer(
                        List.of(
                                TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"),
                                TokenType.fromString("WS : [ \\t\\n\\r]+ skip")
                                ), new SymbolTable<>()
                )
        );
    }

    @Test
    void testFromStringComplex() {
        assertThat(
                Lexer.fromString(
                        "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n",
                        new SymbolTable<>()
                )
        ).isEqualTo(
                new Lexer(
                        List.of(
                                TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"),
                                TokenType.fromString("WS : [ \\t\\n\\r]+ skip"),
                                TokenType.fromString("COMMENT : //.*"),
                                TokenType.fromString(  "ID      :   [a-zA-Z_]+  ")
                        ), new SymbolTable<>()
                )
        );
    }

    @Test
    void testFromStringNotEquals() {
        assertThat(
                Lexer.fromString(
                        "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n",
                        new SymbolTable<>()
                )
        ).isNotEqualTo(
                new Lexer(
                        List.of(
                                TokenType.fromString("WS : [ \\t\\n\\r]+ skip"),
                                TokenType.fromString("COMMENT : //.*"),
                                TokenType.fromString("NUMBER : \\d+(\\.\\d+)?"),
                                TokenType.fromString(  "ID      :   [a-zA-Z_]+  ")
                        ), new SymbolTable<>()
                )
        );
    }

    @Test
    void testBaseTokenize() {
        Lexer lexer = Lexer.fromString(
                "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n",
                new SymbolTable<>()
        );
        assertThat(lexer.tokenize("93   \n np_hard 9 //ciao"))
                .isEqualTo(
                        List.of(
                                new Token(0, "93"),
                                new Token(3, "np_hard"),
                                new Token(0, "9"),
                                new Token(2, "//ciao")
                                )
                );
    }

    @Test
    void testWhitespaceSkip() {
        Lexer lexer = Lexer.fromString(
                "ID : [a-z]+\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThat(lexer.tokenize("abc   def\tghi"))
                .isEqualTo(
                        List.of(
                                new Token(0, "abc"),
                                new Token(0, "def"),
                                new Token(0, "ghi")
                        )
                );
    }

    @Test
    void testPriorityKeywordVsId() {
        Lexer lexer = Lexer.fromString(
                "IF : if\nID : [a-z]+\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThat(lexer.tokenize("if else"))
                .isEqualTo(
                        List.of(
                                new Token(0, "if"),
                                new Token(1, "else")
                        )
                );
    }

    @Test
    void testLongestMatch() {
        Lexer lexer = Lexer.fromString(
                "EQ : ==\nASSIGN : =\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThat(lexer.tokenize("== ="))
                .isEqualTo(
                        List.of(
                                new Token(0, "=="),
                                new Token(1, "=")
                        )
                );
    }

    @Test
    void testNumberWithDecimal() {
        Lexer lexer = Lexer.fromString(
                "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThat(lexer.tokenize("10 3.14 42"))
                .isEqualTo(
                        List.of(
                                new Token(0, "10"),
                                new Token(0, "3.14"),
                                new Token(0, "42")
                        )
                );
    }

    @Test
    void testComments() {
        Lexer lexer = Lexer.fromString(
                "COMMENT : //.*\nID : [a-z]+\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThat(lexer.tokenize("abc //hello\nxyz"))
                .isEqualTo(
                        List.of(
                                new Token(1, "abc"),
                                new Token(0, "//hello"),
                                new Token(1, "xyz")
                        )
                );
    }

    @Test
    void testInvalidToken() {
        Lexer lexer = Lexer.fromString(
                "ID : [a-z]+\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThrows(NoSuchElementException.class, () ->
                lexer.tokenize("abc 123")
        );
    }

    @Test
    void testEmptyInput() {
        Lexer lexer = Lexer.fromString(
                "ID : [a-z]+\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThat(lexer.tokenize(""))
                .isEqualTo(List.of());
    }

    @Test
    void testAdjacentTokens() {
        Lexer lexer = Lexer.fromString(
                "NUMBER : \\d+\nPLUS : \\+\nWS : [ \\t\\n]+ skip\n",
                new SymbolTable<>()
        );

        assertThat(lexer.tokenize("1+2+3"))
                .isEqualTo(
                        List.of(
                                new Token(0, "1"),
                                new Token(1, "+"),
                                new Token(0, "2"),
                                new Token(1, "+"),
                                new Token(0, "3")
                        )
                );
    }

}
