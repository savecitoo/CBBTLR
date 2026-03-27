package lexer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

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

    @Test
    void SerializeTest() throws Exception {
        TokenType tt = TokenType.fromString("Das : a+, Das, a+, false");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(tt);
        out.close();

        byte[] data = bos.toByteArray();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        TokenType copy = (TokenType) in.readObject();
        in.close();
        assertThat(tt).isEqualTo(copy);

    }

}
