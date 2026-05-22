package lalrparser;

import grammar.ContextFreeGrammar;
import grammar.ContextFreeProduction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents an LR(0) core item used in the construction of LALR(1) parsing tables.
 *
 * <p>An LR(0) item is a production rule augmented with a <em>dot</em> (·) that marks
 * how far the parser has progressed through the right-hand side during a parse.
 * For example, the item {@code A → α · B β} indicates that the symbols in {@code α}
 * have already been recognised, and the parser is now expecting {@code B} followed
 * by {@code β}.
 *
 * @param production the context-free production augmented in {@code this} CoreItem.
 * @param dot_position  the 0-based index indicating the position of the dot
 *                      <em>before</em> the symbol at that index.
 *                      A value of {@code 0} places the dot before the first symbol;
 *                      a value equal to {@code rhs.size()} places it at the end,
 *                      marking a <em>complete</em> item.
 */
public record CoreItem( @NotNull ContextFreeProduction production,
                       int dot_position) {

    /**
     * Canonical constructor for an LR(0) core item.
     *
     * <p>The dot position is interpreted as the index of the symbol that
     * immediately <em>follows</em> the dot on the right-hand side:
     * <ul>
     *   <li>{@code dot_position == 0} — the dot is before the first symbol
     *       (the item has consumed nothing yet).</li>
     *   <li>{@code dot_position == k} — the dot is before the {@code k}-th symbol
     *       (symbols at indices {@code 0 .. k-1} have been consumed).</li>
     *   <li>{@code dot_position == rhs.size()} — the dot is at the end of the
     *       right-hand side (the item is <em>complete</em>, i.e. a reduce item).</li>
     * </ul>
     *
     * @param production the production of {@code this} CoreItem.
     * @param dot_position  the position of the dot; must satisfy
     *                      {@code 0 <= dot_position <= rhs.size()}.
      @throws IllegalArgumentException if {@code dot_position} is negative or
     *                                  greater than {@code rhs.size()}.
     */
    public CoreItem(@NotNull ContextFreeProduction production, int dot_position) {
        if (dot_position < 0 || dot_position > production.rhsSize())
            throw new IllegalArgumentException("The position of the dot must be between 0 and rhs.size()");
        this.dot_position = dot_position;
        this.production = production;

    }

    /**
     * Returns the grammar symbol immediately after the dot.
     *
     * <p>In an item of the form {@code A → α · X β}, this method returns {@code X}.
     *
     * @return the first symbol after the dot.
     * @throws IllegalArgumentException if the item is complete (i.e. the dot is at
     *                                  the end of the right-hand side and there is
     *                                  no symbol after it). Callers should check
     *                                  {@link #isComplete()} before invoking this method.
     */
    public @NotNull String afterDot() throws IllegalArgumentException {
        if (isComplete())
            throw new IllegalArgumentException("There is no symbol after the dot");
        return production.rhs.get(dot_position);
    }

    /**
     * Returns whether this item is a <em>complete</em> (or <em>reduce</em>) item.
     *
     * <p>An item is complete when the dot has reached the end of the right-hand side,
     * i.e. when all symbols of the production have been consumed. Formally, this
     * corresponds to an item of the form {@code A → α ·}.
     *
     * @return {@code true} if {@code dot_position == rhs.size()}, {@code false} otherwise.
     */
    public boolean isComplete() {
        return dot_position == production.rhsSize();
    }

    /**
     * Returns a new {@code CoreItem} with the dot advanced by one position.
     *
     * <p>Given an item of the form {@code A → α · X β}, this method produces
     * {@code A → α X · β}, representing the state of the parser after successfully
     * shifting (or reducing to) the symbol {@code X}.
     *
     * <p>The original item is not modified; a fresh instance is returned.
     *
     * @return a new {@code CoreItem} with {@code dot_position} incremented by one.
     * @throws IllegalArgumentException if the item is already complete and the dot
     *                                  cannot be advanced further. Callers should
     *                                  check {@link #isComplete()} before invoking
     *                                  this method.
     */
    public @NotNull CoreItem advanceDot() {
        if (isComplete())
            throw new IllegalArgumentException("The dot cannot be advanced since it's already at the end of it");
        return new CoreItem(
                production, dot_position + 1
        );
    }

    /**
     * Returns whether this item has a non-empty {@code β} suffix after the symbol
     * following the dot.
     *
     * <p>Formally, returns {@code true} if and only if the item is of the form
     * {@code [A → α · B β]} where {@code β} is non-empty, i.e. there exists at
     * least one symbol beyond {@code B} in the right-hand side. This predicate is
     * useful when computing FIRST sets for LALR(1) lookahead propagation.
     *
     * @return {@code true} if {@code dot_position + 1 < rhs.size()},
     *         {@code false} otherwise.
     */
    public boolean hasFirstOfBeta() {
        return (dot_position + 1) < production.rhsSize();
    }

    /**
     * Returns the first symbol of the {@code β} suffix in an item of the form
     * {@code [A → α · B β]}.
     *
     * <p>Concretely, this is the symbol at index {@code dot_position + 1} in the
     * right-hand side, i.e. the symbol immediately after the one following the dot.
     *
     * @return the first symbol of {@code β}.
     * @throws IllegalArgumentException if {@code β} is empty — that is, when the
     *                                  dot is at, or one position before, the end of
     *                                  the right-hand side. Callers should check
     *                                  {@link #hasFirstOfBeta()} before invoking
     *                                  this method.
     */
    public @NotNull String firstOfBeta() throws IllegalArgumentException {
        if (!hasFirstOfBeta())
            throw new IllegalArgumentException(
                    "You can't have the symbol after beta because the dot is before one of the last two symbols");
        return production.rhs.get(dot_position + 1);
    }

    /**
     * Factory method that builds an LR(0) core item from a {@link ContextFreeProduction}.
     *
     * <p>This is a convenience entry point that avoids extracting the left-hand side,
     * the right-hand side at a given index, and the production identifier manually.
     *
     * @param production    the context-free production rule from which to derive the item.
     *
     * @param dot_position  the position of the dot in the new item;
     *                      must satisfy {@code 0 <= dot_position <= rhs.size()}.
     * @return a new {@code CoreItem} corresponding to the specified production alternative
     *         with the dot placed at {@code dot_position}.
     * @throws IllegalArgumentException if {@code dot_position} is out of range (delegated
     *                                  to the canonical constructor).
     */
    public static @NotNull CoreItem fromProduction(@NotNull ContextFreeProduction production, int dot_position) {
        return new CoreItem(production,
                dot_position);
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(production.lhs).append(" -> ");

        for (int i = 0; i < production.rhsSize(); i++) {
            if (i == dot_position) {
                sb.append("· ");
            }
            sb.append(production.rhs.get(i)).append(" ");
        }

        // caso: punto alla fine
        if (dot_position == production.rhs.size()) {
            sb.append("·");
        }

        return sb.toString().trim();
    }

}