package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This class represents a Context Free Grammar for a Formal Language.
 */
public class ContextFreeGrammar {

    private final @NotNull Set<@NotNull String> non_terminals;

    private final @NotNull Set<@NotNull String> terminals;

    private final @NotNull Productions productions;

    private final @NotNull String starting;

    // TODO: AGGIUNGERE CONTROLLI AL COSTRUTTORE
    public ContextFreeGrammar(@NotNull Set<@NotNull String> non_terminals, @NotNull Set<@NotNull String> terminals, @NotNull Productions productions, @NotNull String starting) {
        this.non_terminals = non_terminals;
        this.terminals = terminals;
        this.productions = productions;
        this.starting = starting;
    }

    /**
     * This method returns a Context Free Grammar starting from a string.
     * @param grammar the string representing the grammar.
     * @return an instance of the corresponding grammar.
     * @throws IllegalArgumentException if the string is blank or bad formatted.
     */
    public static @NotNull ContextFreeGrammar from_string(@NotNull String grammar) throws IllegalArgumentException{
        if (grammar.isBlank())
            throw new IllegalArgumentException("The string in input is blank");
        String[] productions = grammar.split("\n");
        //TODO: CONTINUARE LA COSTRUZIONE A PARTIRE DA UNA STRINGA, GESTIRE LE PRODUZIONI.
        return null;
    }

}
