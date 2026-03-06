package grammar;

import org.jetbrains.annotations.NotNull;

/**
 * This class represents a Terminal symbol.
 */
public class Terminal extends Symbol{


    /**
     * This is a constructor for a Symbol.
     *
     * @param symbol the String representing the symbol.
     */
    protected Terminal(@NotNull String symbol) {
        super(symbol);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }

    @Override
    public boolean isNotTerminal() {
        return false;
    }
}
