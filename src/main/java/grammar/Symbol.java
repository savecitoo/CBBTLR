package grammar;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public abstract class Symbol {

    /**
     * This field is the string of {@code this} Symbol.
     */
    public final @NotNull String symbol;

    /**
     * This is a constructor for a Symbol.
     * @param symbol the String representing the symbol.
     */
    protected Symbol(@NotNull String symbol) {
        this.symbol = symbol;
    }

    /**
     * This method returns if {@code this} Symbol is a Terminal symbol.
     * @return true if it is a Terminal symbol, false otherwise.
     */
    public abstract boolean isTerminal();

    /**
     * This method returns if {@code this} Symbol is a non-Terminal symbol.
     * @return true if it is a non-Terminal symbol, false otherwise.
     */
    public abstract boolean isNotTerminal();

}
