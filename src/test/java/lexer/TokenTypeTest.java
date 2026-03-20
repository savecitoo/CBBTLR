package lexer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenTypeTest {

    @ParameterizedTest
    @CsvSource({
            "Das : a+, Das, a+, false",
            "'  ID   :   [a-zA-Z_]+  ', ID, [a-zA-Z_]+, false",
            "'NUMBER : \\d+(\\.\\d+)?', NUMBER, \\d+(\\.\\d+)?, false",
            "'WS : [ \\t\\n\\r]+ skip', WS, [ \\t\\n\\r]+, true",
            "'COMMENT : //.*', COMMENT, //.*, false",
            "'STRING : \"(\\\\.|[^\"])*\"', STRING, \"(\\\\.|[^\"])*\", false",
            "'TOKEN_123 : abc123+', TOKEN_123, abc123+, false",
            "'OP : (\\+|\\-|\\*|/)', OP, (\\+|\\-|\\*|/), false",
            "'HEX : 0[xX][0-9a-fA-F]+', HEX, 0[xX][0-9a-fA-F]+, false",
            "'WS:[ ]+ skip', WS, [ ]+, true"
    })
    void fromString(String line, String identifier, String regex, boolean skip) {
        assertThat(TokenType.fromString(line))
                .isEqualTo(new TokenType(identifier, regex, skip));
    }

}
