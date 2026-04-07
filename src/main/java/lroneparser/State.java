package lroneparser;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;

/**
 * This record represents a state of an automaton.
 * @param states the states that are possible in {@code this} state.
 */
public record State(@NotNull Set<@NotNull Item> states) implements Iterable<Item> {

    public State(@NotNull Set<@NotNull Item> states) {
        this.states = Set.copyOf(states);
    }

    @Override
    public @NotNull Iterator<Item> iterator() {
        return states.iterator();
    }
}
