package lrzeroparser;

import grammar.ContextFreeProduction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class represent an LR(0) core.
 * @param lhs the left-hand side of the production rule that this item models.
 * @param rhs the right-hand side of the production rule that this item models.
 * @param dot_position the position of the dot in the production rule
 */
public record CoreItem(@NotNull String lhs,
                       @NotNull List<@NotNull String> rhs,
                       int dot_position,
                       int production_id) {

    /**
     * This is a constructor for an LR(0) core item.<br>
     * The position of the dot is interpreted before the position on the rhs. It means that if dot_position is 0
     * then it is before the first symbol, if it is 7 it is before the seventh symbol and if dot_position is equal to
     * rhs.size() it is at the end of the right-hand side.
     * @param lhs the left-hand side of the itemized production rule.
     * @param rhs the right-hand side of the itemized production rule.
     * @param dot_position the position of the dot in the item.
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
     * This method returns an LR(0) core that shifts ahead the dot.
     * @return an item with the dot shifted.
     * @throws IllegalArgumentException if the dot is already at the end of the production.
     */
    public @NotNull CoreItem advanceDot() {
        if (isComplete())
            throw new IllegalArgumentException("The dot cannot be advanced since it's already at the end of it");
        return new CoreItem(
          lhs, rhs, dot_position+1, production_id
        );
    }

    /**
     * This method builds an LR(0) core from a Production.
     * @param production the production rule of the item.
     * @param rhs_index the index of the right-hand side of the production rule.
     * @param dot_position the position of the dot in the item.
     * @return the corresponding item starting from given production and the position of the dot.
     */
    public static CoreItem fromProduction(@NotNull ContextFreeProduction production, int rhs_index, int dot_position) {
        return new CoreItem(production.getLHS(), production.getRHS(rhs_index),
                dot_position, production.getProd_id(production.getRHS(rhs_index)));
    }

    @Override
    public String toString() {
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
