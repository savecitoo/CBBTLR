package lalrparser;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a pair of an automaton state and an LALR(1) item within that state.
 *
 * @param state the automaton state containing the item
 * @param item  the LALR(1) item within the state
 *
 * @see ItemLALR
 */
public record StateItem(@NotNull State state, @NotNull ItemLALR item) {
}