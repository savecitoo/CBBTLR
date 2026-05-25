package lexer;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * Tracks the declaration-order priority of {@link TokenType} instances.
 *
 * <p>Each token type is assigned an integer priority equal to its insertion order,
 * starting from {@code 0}.
 * <p>Priorities are keyed by {@link TokenType#identifier()}.
 */
public class TokenRegister implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Maps each token-type identifier to its priority (0 = highest).
     * The priority of a new entry is always {@code register.size()} at insertion time,
     * which guarantees a strict total order consistent with declaration order.
     */
    private final @NotNull Map<@NotNull String, @NotNull Integer> register = new HashMap<>();

    /**
     * Creates an empty {@code TokenRegister} with no entries.
     */
    public TokenRegister() {}

    /**
     * Registers a token type with the next available (lowest) priority.
     *
     * <p>If a token type with the same {@link TokenType#identifier() identifier}
     * is already present, the register is left unchanged and {@code false} is returned.
     *
     * @param token the token type to register; must not be {@code null}.
     * @return {@code true} if the token type was newly added,
     *         {@code false} if it was already present.
     */
    public boolean add(@NotNull TokenType token) {
        if (register.containsKey(token.identifier())) return false;
        register.put(token.identifier(), register.size());
        return true;
    }

    /**
     * Returns the priority of the given token type.
     *
     * <p>Lower values indicate higher priority. Returns {@code -1} if the token
     * type has not been registered.
     *
     * @param token the token type to look up; must not be {@code null}.
     * @return the token type's priority, or {@code -1} if not registered.
     */
    public int priority(@NotNull TokenType token) {
        return register.getOrDefault(token.identifier(), -1);
    }

    /**
     * Two {@code TokenRegister} instances are equal if they contain the same
     * identifier-to-priority mappings.
     *
     * @param o the object to compare.
     * @return {@code true} if {@code o} is a {@code TokenRegister} with identical entries.
     */
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