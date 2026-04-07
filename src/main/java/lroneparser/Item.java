package lroneparser;

import grammar.ContextFreeGrammar;
import grammar.ContextFreeProduction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent an LR(1) item.
 * @param lhs the left-hand side of the production rule that this item models.
 * @param rhs the right-hand side of the production rule that this item models.
 * @param dot_position the position of the dot in the production rule
 */
public record Item(@NotNull String lhs,
                   @NotNull List<@NotNull String> rhs,
                   @NotNull String look_ahead,
                   int dot_position,
                   int production_id) {

    /**
     * This is a constructor for an LR(1) item.<br>
     * The position of the dot is interpreted before the position on the rhs. It means that if dot_position is 0
     * then it is before the first symbol, if it is 7 it is before the seventh symbol and if dot_position is equal to
     * rhs.size() it is at the end of the right-hand side.
     * @param lhs the left-hand side of the itemized production rule.
     * @param rhs the right-hand side of the itemized production rule.
     * @param dot_position the position of the dot in the item.
     */
    public Item(@NotNull String lhs, @NotNull List<@NotNull String> rhs, @NotNull String look_ahead, int dot_position, int production_id) {
        if (dot_position < 0 || dot_position > rhs.size())
            throw new IllegalArgumentException("The position of the dot must be between 0 and rhs.size()");
        this.rhs = List.copyOf(rhs);
        this.lhs = lhs;
        this.look_ahead = look_ahead;
        this.dot_position = dot_position;
        this.production_id = production_id;
    }

    /**
     * This method returns the first symbol after the dot.
     * @return the symbol after the dot.
     * @throws IllegalArgumentException if the dot is at the end of the rhs.
     */
    public @NotNull String afterDot() throws IllegalArgumentException {
        if (isComplete())
            throw new IllegalArgumentException("There is no symbol after the dot");
        return rhs.get(dot_position);
    }

    /**
     * This method returns if the rhs is all before the dot.
     * @return if the item has the dot at the end of the production.
     */
    public boolean isComplete() {
        return dot_position==rhs.size();
    }

    /**
     * This method returns an LR(1) Item that shifts ahead the dot.
     * @return an item with the dot shifted.
     * @throws IllegalArgumentException if the dot is already at the end of the production.
     */
    public @NotNull Item advanceDot() {
        if (isComplete())
            throw new IllegalArgumentException("The dot cannot be advanced since it's already at the end of it");
        return new Item(
          lhs, rhs, look_ahead,dot_position+1, production_id
        );
    }

    /**
     * Returns the first symbol of β in an item of the form [A → α • B β, a],
     * i.e. the symbol immediately after the symbol following the dot.
     *
     * @return the first symbol of β
     * @throws IllegalArgumentException if β is empty, i.e. the dot is on one
     *                                  of the last two positions of the production
     */
    public @NotNull String firstOfBeta() throws IllegalArgumentException {
        if (!hasFirstOfBeta())
            throw new IllegalArgumentException(
                    "You can't have the symbol after beta because the dot is before one of the last two symbols");
        return rhs.get(dot_position + 1);
    }

    /**
     * Returns whether this item has a non-empty β, i.e. whether there exists
     * a symbol after the one following the dot.
     *
     * <p>Formally, returns {@code true} if and only if the item is of the form
     * [A → α • B β, a] where β is non-empty.
     *
     * @return {@code true} if β is non-empty, {@code false} otherwise
     */
    public boolean hasFirstOfBeta() {
        return (dot_position + 1) < rhs.size();
    }

    /**
     * This method given a grammar returns the first LR(1) Item that is useful for the automata construction.
     * @param grammar the grammar where the starting Item is from.
     * @return the Item corresponding to the starting production with a dot ahead.
     */
    public static @NotNull Item getStartingItem(@NotNull ContextFreeGrammar grammar) {
        return new Item(
                grammar.S,
                grammar.P.fromLHS(grammar.S).getRHS(0),
                "#",
                0,
                grammar.P.fromLHS(grammar.S).getStartingProd_id()
        );
    }

    /**
     * This method builds an LR(1) item from a Production.
     * @param production the production rule of the item.
     * @param rhs_index the index of the right-hand side of the production rule.
     * @param dot_position the position of the dot in the item.
     * @return the corresponding item starting from given production and the position of the dot.
     */
    public static @NotNull Item fromProduction(@NotNull ContextFreeProduction production, @NotNull String look_ahead , int rhs_index, int dot_position) {
        return new Item(production.getLHS(), production.getRHS(rhs_index), look_ahead,
                dot_position, production.getProd_id(production.getRHS(rhs_index)));
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

        sb.append(" ").append("[").append(look_ahead).append("]");

        return sb.toString();
    }

}
