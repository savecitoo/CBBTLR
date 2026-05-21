package lalrparser;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an LALR(1) item, associating an LR(0) core with a mutable set of lookahead terminals.
 *
 * <p>An LALR(1) item is of the form [A → α • β, {a, b, ...}] where the core
 * [A → α • β] is immutable and the lookahead set evolves during the
 * fixpoint propagation phase of the LALR(1) construction algorithm.
 *
 * <p>Equality and hashing are based solely on the {@link CoreItem}, so that
 * two {@code ItemLALR} instances with the same core but different lookaheads
 * are considered equal. This allows them to be used as keys in hash-based
 * collections while their lookahead sets are mutated during construction.
 *
 * <p><strong>Note:</strong> because equality is core-based, the same core in
 * two different states would be indistinguishable. Use {@link StateItem} to
 * disambiguate items across states.
 *
 * @see CoreItem
 * @see StateItem
 */
public class ItemLALR {

    /**
     * The LR(0) core of this item. Immutable.
     */
    private final @NotNull CoreItem core;

    /**
     * The set of lookahead terminals for this item.
     * Mutated during the LALR(1) lookahead propagation fixpoint.
     */
    private final @NotNull Set<@NotNull String> lookaheads = new HashSet<>();

    /**
     * Constructs an {@code ItemLALR} with the given core and no lookaheads.
     *
     * @param core the LR(0) core of this item.
     */
    public ItemLALR(@NotNull CoreItem core) {
        this.core = core;
    }

    /**
     * Returns the LR(0) core of this item.
     *
     * @return the LR(0) core.
     */
    public @NotNull CoreItem getCore() {
        return core;
    }

    /**
     * Returns an unmodifiable view of the current lookahead set.
     *
     * @return the lookahead terminals.
     */
    public @NotNull java.util.Set<@NotNull String> getLookaheads() {
        return Set.copyOf(lookaheads);
    }

    /**
     * Adds a lookahead terminal to this item.
     *
     * @param terminal the terminal to add.
     * @return {@code true} if the lookahead set changed as a result
     */
    public boolean addLookahead(@NotNull String terminal) {
        return lookaheads.add(terminal);
    }

    /**
     * Adds all lookahead terminals from the given collection to this item.
     *
     * @param terminals the terminals to add.
     * @return {@code true} if the lookahead set changed as a result
     */
    public boolean addAllLookaheads(@NotNull Set<@NotNull String> terminals) {
        return lookaheads.addAll(terminals);
    }

    /**
     * Two {@code ItemLALR} instances are equal if and only if their cores are equal.
     * Lookaheads are intentionally excluded from equality.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemLALR other)) return false;
        return core.equals(other.core);
    }

    /**
     * Hash code based solely on the core, consistent with {@link #equals}.
     */
    @Override
    public int hashCode() {
        return core.hashCode();
    }

    @Override
    public String toString() {
        return core + ", " + lookaheads;
    }
}