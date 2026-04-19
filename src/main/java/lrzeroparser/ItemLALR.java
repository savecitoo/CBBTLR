package lrzeroparser;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a LALR(1) Item.
 */
public class ItemLALR {

    /**
     * The LR(0) core of the item.
     */
    private final @NotNull CoreItem core;

    /**
     * The lookaheads of the item.
     */
    private final @NotNull Set<@NotNull String> lookaheads = new HashSet<>();

    /**
     * The items that takes the lookaheads from this item.
     */
    private final @NotNull Set<@NotNull ItemLALR> propagations = new HashSet<>();

    /**
     * This is a constructor for an LALR(1) Item.<br>
     * The lookaheads and the propagations are set empty by default.
     * @param core the core LR(0) of the item.
     */
    public ItemLALR(@NotNull CoreItem core) {
        this.core = core;
    }

    /**
     * This method given in input a lookahead for the item adds it to the lookaheads of the item.
     * @param lookahead the lookahead to be added.
     * @throws IllegalArgumentException if the string is blank.
     */
    public void addLookahead(@NotNull String lookahead) throws IllegalArgumentException {
        if (lookahead.isBlank())
            throw new IllegalArgumentException("The lookahead cannot be blank.");
        lookaheads.add(lookahead);
    }

    /**
     * This method given in input an Item adds it to the items that receive the propagations by {@code this} item.
     * @param propagation the item to add to the propagations.
     */
    public void addPropagation(@NotNull ItemLALR propagation) {
        propagations.add(propagation);
    }

}
