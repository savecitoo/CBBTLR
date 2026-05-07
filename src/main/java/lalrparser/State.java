package lalrparser;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Represents a state in the LALR(1) automaton used during parser construction.
 *
 * <p>Each state is defined by a non-empty set of LR(0) {@link CoreItem core items}
 * (the <em>kernel</em> of the state) and a map that associates every core item with
 * its full {@link ItemLALR} representation, which additionally carries the LALR(1)
 * lookahead sets computed during closure.
 *
 * <p>Two automaton states are considered identical when their {@code cores} sets are
 * equal. The {@code items} map is mutable so that lookahead sets can be
 * propagated and updated in place without creating new state instances.
 *
 * @param cores a snapshot of the LR(0) core items that form the kernel of this state.
 * @param items a map from each {@link CoreItem} to its {@link ItemLALR}
 *              counterpart, which holds the associated LALR(1) lookahead set.
 */
public record State(@NotNull Set<@NotNull CoreItem> cores, @NotNull Map<@NotNull CoreItem, @NotNull ItemLALR> items) implements Iterable<CoreItem> {

    /**
     * Canonical constructor for a {@code State}.
     *
     * @param cores the LR(0) core items that constitute the kernel of this state.
     * @param items the initial mapping from core items to their LALR(1) counterparts.
     */
    public State(@NotNull Set<@NotNull CoreItem> cores, @NotNull Map<@NotNull CoreItem, @NotNull ItemLALR> items) {
        this.cores = Set.copyOf(cores);
        this.items = new HashMap<>(items);
    }

    /**
     * Factory method that builds a {@code State} from a set of LR(0) core items,
     * initialising each core with an empty LALR(1) lookahead set.
     *
     * @param cores the set of LR(0) core items that form the kernel of the new state.
     * @return a new {@code State} whose {@code items} map contains one {@link ItemLALR}
     *         per core item, each with an initially empty lookahead set.
     */
    public static State fromCore(@NotNull Set<@NotNull CoreItem> cores) {
        Map<CoreItem, ItemLALR> items = new HashMap<>();
        for (CoreItem core : cores)
            items.put(core, new ItemLALR(core));
        return new State(cores, items);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");

        cores.stream()
                .map(CoreItem::toString)
                .sorted()
                .forEach(ci -> sb.append("  ").append(ci).append("\n"));

        sb.append("}");

        return sb.toString();
    }

    public @NotNull Iterator<CoreItem> iterator() {
        return cores.iterator();
    }
}