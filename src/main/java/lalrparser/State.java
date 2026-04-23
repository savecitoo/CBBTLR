package lalrparser;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This record represents a state of an automaton.
 * @param cores the states that are possible in {@code this} state.
 * @param items a map that maps the LR(0) core to the corresponding LALR(1) item.
 */
public record State(@NotNull Set<@NotNull CoreItem> cores, @NotNull Map<@NotNull CoreItem, @NotNull ItemLALR> items) implements Iterable<CoreItem> {

    public State(@NotNull Set<@NotNull CoreItem> cores, @NotNull Map<@NotNull CoreItem, @NotNull ItemLALR> items) {
        this.cores = Set.copyOf(cores);
        this.items = new HashMap<>(items);
    }

    /**
     * This method builds a state from the corresponding set of LR(0) core.
     * @param cores the set of LR(0) core of the state.
     * @return the corresponding state.
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
    
    @Override
    public @NotNull Iterator<CoreItem> iterator() {
        return cores.iterator();
    }
}
