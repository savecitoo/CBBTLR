package lexer;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class keeps trace of the priority of the tokens.
 */
public class TokenRegister {

    private final Map<String, Integer> register = new HashMap<>();

    public TokenRegister(){}

    public boolean add(@NotNull TokenType token) throws IllegalArgumentException {
        return false;
    }

    public int priority(@NotNull TokenType token) {
        return 0;
    }

}
