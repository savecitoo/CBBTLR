package basics;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AppTest {
    interface Servizio { String saluta(); }

    @Test
    void testConMockito() {
        Servizio mockServizio = mock(Servizio.class);
        when(mockServizio.saluta()).thenReturn("Ciao test!");
        assertEquals("Ciao test!", mockServizio.saluta());
    }
}
