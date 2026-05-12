package lalrparser;

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
 * <p>This record is <em>immutable</em>: the {@code rhs} list is defensively copied
 * on construction, so external modifications to the original list have no effect.
 *
 * @param lhs           the non-terminal on the left-hand side of the production rule
 *                      (e.g. {@code "E"}).
 * @param rhs           the ordered list of grammar symbols (terminals and non-terminals)
 *                      on the right-hand side of the production rule
 *                      (e.g. {@code ["E", "+", "T"]}).
 * @param dot_position  the zero-based index indicating the position of the dot
 *                      <em>before</em> the symbol at that index.
 *                      A value of {@code 0} places the dot before the first symbol;
 *                      a value equal to {@code rhs.size()} places it at the end,
 *                      marking a <em>complete</em> item.
 * @param production_id a unique numeric identifier for the underlying production rule,
 *                      used to distinguish items that share the same {@code lhs} and
 *                      {@code rhs} but originate from different grammar productions.
 */
public record CoreItem(@NotNull String lhs,
                       @NotNull List<@NotNull String> rhs,
                       int dot_position,
                       int production_id) {

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
     * <p>The supplied {@code rhs} list is defensively copied via {@link List#copyOf},
     * so the constructed item is unaffected by later mutations to the original list.
     *
     * @param lhs           the non-terminal on the left-hand side; must not be {@code null}.
     * @param rhs           the list of grammar symbols on the right-hand side;
     *                      must not be {@code null} and must not contain {@code null} elements.
     * @param dot_position  the position of the dot; must satisfy
     *                      {@code 0 <= dot_position <= rhs.size()}.
     * @param production_id the unique identifier of the underlying production rule.
     * @throws IllegalArgumentException if {@code dot_position} is negative or
     *                                  greater than {@code rhs.size()}.
     */
    public CoreItem(@NotNull String lhs, @NotNull List<@NotNull String> rhs, int dot_position, int production_id) {
        if (dot_position < 0 || dot_position > rhs.size())
            throw new IllegalArgumentException("The position of the dot must be between 0 and rhs.size()");
        this.rhs = List.copyOf(rhs);
        this.lhs = lhs;
        this.dot_position = dot_position;
        this.production_id = production_id;
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
        return rhs.get(dot_position);
    }

    /**
     * Returns whether this item is a <em>complete</em> (or <em>reduce</em>) item.
     *
     * <p>An item is complete when the dot has reached the end of the right-hand side,
     * i.e. when all symbols of the production have been consumed. Formally, this
     * corresponds to an item of the form {@code A → α ·} (with an empty {@code β}).
     * A complete item triggers a <em>reduce</em> action in the LR automaton.
     *
     * @return {@code true} if {@code dot_position == rhs.size()}, {@code false} otherwise.
     */
    public boolean isComplete() {
        return dot_position == rhs.size();
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
     * @return a new {@code CoreItem} with {@code dot_position} incremented by one;
     *         never {@code null}.
     * @throws IllegalArgumentException if the item is already complete and the dot
     *                                  cannot be advanced further. Callers should
     *                                  check {@link #isComplete()} before invoking
     *                                  this method.
     */
    public @NotNull CoreItem advanceDot() {
        if (isComplete())
            throw new IllegalArgumentException("The dot cannot be advanced since it's already at the end of it");
        return new CoreItem(
                lhs, rhs, dot_position + 1, production_id
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
        return (dot_position + 1) < rhs.size();
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
        return rhs.get(dot_position + 1);
    }

    /**
     * Factory method that builds an LR(0) core item from a {@link ContextFreeProduction}.
     *
     * <p>This is a convenience entry point that avoids extracting the left-hand side,
     * the right-hand side at a given index, and the production identifier manually.
     *
     * @param production    the context-free production rule from which to derive the item;
     *                      must not be {@code null}.
     *                      {@code production} (productions may have multiple alternatives).
     * @param dot_position  the initial position of the dot in the new item;
     *                      must satisfy {@code 0 <= dot_position <= rhs.size()}.
     * @return a new {@code CoreItem} corresponding to the specified production alternative
     *         with the dot placed at {@code dot_position}.
     * @throws IllegalArgumentException if {@code dot_position} is out of range (delegated
     *                                  to the canonical constructor).
     */
    public static CoreItem fromProduction(@NotNull ContextFreeProduction production, int dot_position) {
        return new CoreItem(production.lhs, production.getRHS(),
                dot_position, production.getProd_id());
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(lhs).append(" -> ");

        for (int i = 0; i < rhs.size(); i++) {
            if (i == dot_position) {
                sb.append("· ");
            }
            sb.append(rhs.get(i)).append(" ");
        }

        // caso: punto alla fine
        if (dot_position == rhs.size()) {
            sb.append("·");
        }

        return sb.toString().trim();
    }

}