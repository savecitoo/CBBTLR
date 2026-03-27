package lexer;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class keeps trace of the priority of the tokens.
 */
public class TokenRegister {

    /**
     * This field saves the priorities between tokens.
     */
    private final @NotNull Map<@NotNull String, @NotNull Integer> register = new HashMap<>();

    /**
     * This is a constructor for a TokenRegister.
     */
    public TokenRegister(){}

    /**
     * This method adds to the register a new Token, it will have the lowest priority among all the token previously added.
     * @param token the token to be added in the register.
     * @return true if it is a new token in the register, false otherwise.
     */
    public boolean add(@NotNull TokenType token) throws IllegalArgumentException {
        if (register.containsKey(token.identifier())) return false;
        register.put(token.identifier(), register.size());
        return true;
    }

    /**
     * This method returns the priority of the token, if it is not in the register it returns -1.
     * @param token the token you are looking for priority.
     * @return the priority of the token if it is in the register, -1 otherwise.
     */
    public int priority(@NotNull TokenType token) {
        return register.getOrDefault(token.identifier(), -1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof TokenRegister reg)
            return register.equals(reg.register);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(register);
    }
}
