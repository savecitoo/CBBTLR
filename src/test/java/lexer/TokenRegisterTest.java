package lexer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenRegisterTest {

    @Test
    void testSerializable() throws Exception {
        TokenRegister reg = new TokenRegister();
        for (String s :
                "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n"
                        .split("\n"))
            reg.add(TokenType.fromString(s));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(reg);
        out.close();

        byte[] data = bos.toByteArray();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        TokenRegister copy = (TokenRegister) in.readObject();
        in.close();

        assertThat(reg).isEqualTo(copy);
    }

}
