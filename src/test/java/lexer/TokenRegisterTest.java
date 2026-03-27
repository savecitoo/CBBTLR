package lexer;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TokenRegisterTest {

    @Test
    void testSerializable() throws Exception {
        TokenRegister reg = new TokenRegister();
        for (String s :
                "NUMBER : \\d+(\\.\\d+)?\nWS : [ \\t\\n\\r]+ skip\nCOMMENT : //.*\n  ID      :   [a-zA-Z_]+  \n"
                        .split("\n"))
            reg.add(TokenType.fromString(s));


        // serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(reg);
        out.close();

        byte[] data = bos.toByteArray();

        // deserialize
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        TokenRegister copy = (TokenRegister) in.readObject();
        in.close();

        // TEST
        System.out.println(reg.equals(copy));
    }

}
