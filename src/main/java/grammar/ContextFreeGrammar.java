package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a Context Free Grammar for a Formal Language.
 */
public class ContextFreeGrammar {

    // TODO: scrivere metodi che trovano simboli non produttivi, non raggiungibili e poi il semplificatore della grammatica.

    /** This field contains the non-Terminal symbols of the grammar. */
    public final @NotNull Set<@NotNull String> N;

    /** This field contains the Terminal symbols of the grammar. */
    public final @NotNull Set<@NotNull String> T;

    /** This field contains the productions of the grammar. */
    public final @NotNull Productions P;

    /** This field contains the starting symbols of the grammar. */
    public final @NotNull String S;

    /**
     * This is a constructor for a grammar given the sets of terminals and non-terminals, the productions and the starting symbol.
     * @param N the set of non-terminal symbols.
     * @param T the set of terminal symbols.
     * @param P the set of productions.
     * @param S the startingl symbol.
     * @throws IllegalArgumentException if the sets are empty or the sets are not disjuncted, if the starting symbol is blank
     *                                  or is not in the non-terminal
     *                                  symbols or if the productions have more symbols than they are specified.
     */
    public ContextFreeGrammar(@NotNull Set<@NotNull String> N, @NotNull Set<@NotNull String> T, @NotNull Productions P, @NotNull String S) throws IllegalArgumentException {
        if (S.isBlank())
            throw new IllegalArgumentException("The starting symbol cannot be blank");
        if (N.isEmpty() || T.isEmpty())
            throw new IllegalArgumentException("Non-terminals and terminals must have at least one symbol");
        if (!N.contains(S))
            throw new IllegalArgumentException("The starting symbol must be in N.");
        for (String v : N)
            if (T.contains(v))
                throw new IllegalArgumentException("The two sets must be disjoint.");
        Set<String> union = new HashSet<>(N);
        union.addAll(T);
        if (!union.containsAll(P.symbols()))
            throw new IllegalArgumentException("The set of symbols in the production must be contained in the union between N and T");
        this.N = Set.copyOf(N);
        this.T = Set.copyOf(T);
        this.P = P;
        this.S = S;
    }


    /**
     * This method returns a Context Free Grammar starting from a string.<br>
     * This method will read every symbol on the left-hand side as non-terminal, every other symbol will be considered a terminal one.<br>
     * The starting symbol will be the one on the left-hand side of the first production rule.
     * @param grammar the string representing the grammar.
     * @return an instance of the corresponding grammar.
     * @throws IllegalArgumentException if the string is blank or bad formatted.
     */
    public static @NotNull ContextFreeGrammar fromString(@NotNull String grammar) throws IllegalArgumentException{
        if (grammar.isBlank())
            throw new IllegalArgumentException("The string in input is blank");
        Productions productions = Productions.fromString(grammar);
        Set<String> N = new HashSet<>();
        for (ContextFreeProduction cfp : productions)
            N.add(cfp.getLHS());
        Set<String> T = productions.symbols();
        T.removeAll(N);
        return new ContextFreeGrammar(
                N, T, productions, productions.productions().getFirst().getLHS()
        );

    }

    /**
     * This method implement a closure.<br>
     * This method create the maximum set that can be built iterating over the find method.
     * @param f this parameter is a function that let us search for items with that property.
     * @return the maximum set of items with the property searched.
     * @param <T> the paramenter of the function and the findMethod.
     */
    private @NotNull <T> Set<@NotNull T> closure(FindMethod<T> f) {
        Set<T> current = f.find(new HashSet<>());

        Set<T> past = new HashSet<>();

        while (!current.equals(past)) {
            past = Set.copyOf(current);
            current = f.find(current);
        }
        return current;
    }


    /**
     * This method returns a set of the symbols containing the set in input and the symbols that can product the symbols in the input.<br>
     * If the input is void the set will be initialized with the terminal symbols.
     * @param prod a set of symbols.
     * @return a set of symbols containing the input and the symbols that can product the symbols in input.
     */
    private @NotNull Set<@NotNull String> productive(@NotNull Set<@NotNull String> prod) {
        Set<String> productive;
        if (prod.isEmpty())
            productive = new HashSet<>(this.T); // if it is the first step we start from terminals
        else
            productive = new HashSet<>(prod);

        for (ContextFreeProduction cfp : this.P) {
            // if cfp.lhs is in productive we can skip
            if (productive.contains(cfp.getLHS()))
                continue;
            // else we search if a production rule is entirely contained in productive.
            for (List<String> rhs : cfp) {
                if (prod.containsAll(rhs)) {
                    productive.add(cfp.getLHS());
                }
            }
        }
        return productive;
    }

    /**
     * This method returns a set containing all the productive Symbols in {@code this} grammar.
     * @return a set containing all the productive symbols in {@code this} grammar.
     */
    public @NotNull Set<@NotNull String> productive() {
        return closure(
                this::productive
        );
    }

    /**
     * This method returns a set containing all the non-reachable Symbols in {@code this} grammar.
     * @return a set containing all the non-reachable symbols in {@code this} grammar
     */
    public @NotNull Set<@NotNull String> nonReachable() {
        return Set.of();
    }

    /**
     * This method returns a new Grammar derived from {@code this} grammar
     * such that all the non-productive and non-reachable symbols are removed.
     * @return a grammar without non-productive and non-reachable symbols.
     */
    public @NotNull ContextFreeGrammar simplifyGrammar() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("N = ").append(N.toString()).append("\n");
        sb.append("T = ").append(T.toString()).append("\n");
        sb.append("P = {\n").append(P).append("\n\t}\n");
        return sb.append("S = ").append(S).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof ContextFreeGrammar g) {
            return g.N.equals(N) && g.T.equals(T) && g.P.equals(P) && g.S.equals(S);
        }
        return false;
    }
}
