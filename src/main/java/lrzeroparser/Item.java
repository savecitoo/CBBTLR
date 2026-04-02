package lrzeroparser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent an LR(0) item.
 * @param lhs the left-hand side of the production rule that this item models.
 * @param rhs the right-hand side of the production rule that this item models.
 * @param dot_position the position of the dot in the production rule
 */
public record Item(@NotNull String lhs,
                   @NotNull List<@NotNull String> rhs,
                   int dot_position) {

    /**
     * This is a constructor for an LR(0) item.<br>
     * The position of the dot is interpreted before the position on the rhs. It means that if dot_position is 0
     * then it is before the first symbol, if it is 7 it is before the seventh symbol and if dot_position is equal to
     * rhs.size() it is at the end of the right-hand side.
     * @param lhs the left-hand side of the itemized production rule.
     * @param rhs the right-hand side of the itemized production rule.
     * @param dot_position the position of the dot in the item.
     */
    public Item(@NotNull String lhs, @NotNull List<@NotNull String> rhs, int dot_position) {
        if (dot_position < 0 || dot_position > rhs().size())
            throw new IllegalArgumentException("The position of the dot must be between 0 and rhs.size()");
        this.rhs = List.copyOf(rhs);
        this.lhs = lhs;
        this.dot_position = dot_position;
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
     * This method returns an LR(0) Item that shifts ahead the dot.
     * @return an item with the dot shifted.
     * @throws IllegalArgumentException if the dot is already at the end of the production.
     */
    public @NotNull Item advanceDot() {
        if (isComplete())
            throw new IllegalArgumentException("The dot cannot be advanced since it's already at the end of it");
        return new Item(
          lhs, rhs, dot_position+1
        );
    }

}
