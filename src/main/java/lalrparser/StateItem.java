package lalrparser;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a pair of an automaton state and an LALR(1) item within that state.
 *
 * <p>During LALR(1) construction, the same {@link CoreItem} may appear in
 * multiple states with different lookahead sets. A {@code StateItem} uniquely
 * identifies an item within a specific state, and is used as the key in the
 * lookahead and propagation maps of the LALR(1) algorithm:
 *
 * <pre>
 *   Map&lt;StateItem, Set&lt;String&gt;&gt;     lookaheads
 *   Map&lt;StateItem, Set&lt;StateItem&gt;&gt;  propagate
 * </pre>
 *
 * <p>Equality and hashing are based on both the state and the item,
 * so two {@code StateItem} instances are equal if and only if they refer
 * to the same item in the same state.
 *
 * @param state the automaton state containing the item
 * @param item  the LALR(1) item within the state
 *
 * @see ItemLALR
 */
public record StateItem(@NotNull State state, @NotNull ItemLALR item) {

    /**
     * Compact constructor that validates neither component is null.
     *
     * @throws NullPointerException if {@code state} or {@code item} is {@code null}
     */
    public StateItem {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(item,  "item must not be null");
    }
}