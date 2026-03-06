package grammar;

import org.jetbrains.annotations.NotNull;

/**
 * This class represents a non-Terminal Symbol.
 */
public class NonTerminal extends Symbol{


    /**
     * This is a constructor for a non-Terminal Symbol.
     *
     * @param symbol the String representing the symbol.
     */
    protected NonTerminal(@NotNull String symbol) {
        super(symbol);
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean isNotTerminal() {
        return true;
    }
}
