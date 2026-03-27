package utils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SymbolTableTest {



    @Test
    void serializeTest() throws Exception{
        SymbolTable<String> table = new SymbolTable<>();
        for (String s : Set.of("ciao", "java", "set", "immutabile"))
            table.add(s);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(table);
        out.close();

        byte[] data = bos.toByteArray();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
        SymbolTable copy = (SymbolTable) in.readObject();
        in.close();

        assertThat(table).isEqualTo(copy);

    }

}
