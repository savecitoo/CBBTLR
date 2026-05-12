package grammar;

import org.jetbrains.annotations.NotNull;
import utils.Finder;

import java.util.*;

/**
 * Represents a context-free grammar (CFG) for a formal language.
 *
 * <p>A CFG is defined by a 4-tuple (N, T, P, S) where N is the set of non-terminal
 * symbols, T is the set of terminal symbols, P is the set of production rules,
 * and S ∈ N is the start symbol.</p>
 *
 * <p>This implementation does not support ε-productions; every right-hand side
 * must contain at least one symbol.</p>
 */
public class ContextFreeGrammar implements Iterable<String> {

    /** This field contains the non-Terminal symbols of the grammar. */
    public final @NotNull Set<@NotNull String> N;

    /** This field contains the Terminal symbols of the grammar. */
    public final @NotNull Set<@NotNull String> T;

    /** This field contains the productions of the grammar. */
    public final @NotNull Productions P;

    /** This field contains the starting symbols of the grammar. */
    public final @NotNull String S;

    /**
     * Constructs a context-free grammar from its fundamental components.
     *
     * @param N the set of non-terminal symbols; must not be empty
     * @param T the set of terminal symbols; must not be empty and must be disjoint from {@code N}
     * @param P the set of production rules; every symbol appearing in any right-hand side
     *          must belong to either {@code N} or {@code T}
     * @param S the start symbol; must not be blank and must be an element of {@code N}
     * @throws IllegalArgumentException if {@code N} or {@code T} is empty, if {@code N} and {@code T}
     *                                  are not disjoint, if {@code S} is blank or not in {@code N},
     *                                  or if any production references a symbol not in {@code N} ∪ {@code T}
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
     * Parses a string representation of a context-free grammar and returns the corresponding instance.
     *
     * <p>Symbols are classified as follows:</p>
     * <ul>
     *   <li>Every symbol appearing on the left-hand side of a production is treated as <em>non-terminal</em>.</li>
     *   <li>Every other symbol is treated as <em>terminal</em>.</li>
     * </ul>
     * The start symbol is the left-hand side symbol of the first production rule.
     *
     * @param grammar the string representation of the grammar; must not be blank or malformed
     * @return a {@link ContextFreeGrammar} instance corresponding to the given string
     * @throws IllegalArgumentException if {@code grammar} is blank or does not conform to the expected format
     */
    public static @NotNull ContextFreeGrammar fromString(@NotNull String grammar) throws IllegalArgumentException{
        if (grammar.isBlank())
            throw new IllegalArgumentException("The string in input is blank");
        Productions productions = Productions.fromString(grammar);
        Set<String> N = new HashSet<>();
        for (ContextFreeProduction cfp : productions)
            N.add(cfp.lhs);
        Set<String> T = productions.symbols();
        T.removeAll(N);
        return new ContextFreeGrammar(
                N, T, productions, productions.getStarting().lhs
        );

    }

    /**
     * This method implement a closure.<br>
     * This method create the maximum set that can be built iterating over the find method.
     * @param f this parameter is a function that let us search for items with that property.
     * @return the maximum set of items with the property searched.
     * @param <T> the paramenter of the function and the findMethod.
     */
    private @NotNull <T> Set<@NotNull T> closure(Finder<T> f) {
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
            if (productive.contains(cfp.lhs))
                continue;
            // else we search if a production rule is entirely contained in productive.
            if (prod.containsAll(cfp.getRHS())) {
                    productive.add(cfp.lhs);
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

    private @NotNull Set<@NotNull String> reachable(@NotNull Set<@NotNull String> reach) {
        Set<String> reachable;
        if (reach.isEmpty()) { // if reach is empty we start from the starting symbol
            reachable = new HashSet<>();
            reachable.add(S);
        } else {
            reachable = new HashSet<>(reach);
        }

        for (ContextFreeProduction cfp : this.P) {
            // we iterate over the productions if the lhs of cfp is in reach then all
            // the symbols in the rhs can be reached.
            if (reachable.contains(cfp.lhs))
                reachable.addAll(cfp.symbols());    // since the lhs is already in reachable we
                                                    // can add all the symbols in the production to it
        }
        return reachable;
    }

    /**
     * This method returns a set containing all the non-reachable Symbols in {@code this} grammar.
     * @return a set containing all the non-reachable symbols in {@code this} grammar
     */
    public @NotNull Set<@NotNull String> reachable() {
        return closure(
                this::reachable
        );
    }

    /**
     * This method returns a new grammar starting from the first one that keeps just the symbols in input. <br>
     * The resulting grammar has just the specified symbols, if any of the symbols are not in the Grammar they will be ignored.
     * @param symbols the set of symbol to be removed.
     * @return the resulting grammar.
     */
    public @NotNull ContextFreeGrammar restrictTo(@NotNull Set<@NotNull String> symbols) {

        Set<String> N = new HashSet<>();
        for (String s : symbols)
            if (this.N.contains(s))
                N.add(s);

        Set<String> T = new HashSet<>();
        for (String s : symbols)
            if (this.T.contains(s))
                T.add(s);

        Map<Integer, ContextFreeProduction> P = new HashMap<>();

        int i = 0;

        for (ContextFreeProduction p : this.P) {
            if (symbols.contains(p.lhs)) {

                boolean ok = true;
                for (String s : p) {
                    if (!symbols.contains(s)) {
                        ok = false ;
                        break;
                    }
                }

                if (ok) { // if there is at least one rule we add it.
                    P.put(i, new ContextFreeProduction(p.lhs, p.getRHS(), i, p.prod_tag));
                    i++;
                }

            }
        }
        return new ContextFreeGrammar(N, T, new Productions(P), this.S);
    }

    /**
     * This method returns a new Grammar derived from {@code this} grammar
     * such that all the non-productive and non-reachable symbols are removed.
     * @return a grammar without non-productive and non-reachable symbols.
     */
    public @NotNull ContextFreeGrammar simplifyGrammar() {
        // First we have to restrict the grammar to the productive symbols and later on to the reachable symbols.
        ContextFreeGrammar g = restrictTo(productive());
        return g.restrictTo(g.reachable());
    }

    /**
     * Computes the FIRST set of the given symbol, guarding against left-recursion.
     *
     * <p>FIRST(X) is the set of terminals that can appear as the first symbol
     * of some string derived from X. This implementation does not support
     * nullable symbols (ε-productions).</p>
     *
     * @param symbol the terminal or non-terminal symbol whose FIRST set is to be computed
     * @param seen   the set of symbols already visited during the current recursion;
     *               prevents infinite loops caused by left-recursive productions
     * @return an unmodifiable set of terminal symbols in FIRST({@code symbol})
     */
    private @NotNull Set<@NotNull String> first(@NotNull String symbol, @NotNull Set<@NotNull String> seen) {
        if (T.contains(symbol)) {
            return Set.of(symbol);
        }
        if (seen.contains(symbol)) {
            return Set.of();
        }
        seen.add(symbol);
        Set<String> result = new HashSet<>();
        for (ContextFreeProduction prod : P.fromLHS(symbol)) {
            result.addAll(first(prod.getRHS().getFirst(), seen));
        }
        return result;
    }

    /**
     * Computes the FIRST set of the given symbol.
     *
     * <p>FIRST(α) is the set of terminals that can appear as the first symbol
     * of some string derived from α. ε-productions are not supported.</p>
     *
     * @param symbol the terminal or non-terminal symbol whose FIRST set is to be computed;
     *               must belong to the grammar
     * @return an unmodifiable set of terminal symbols in FIRST({@code symbol})
     * @throws IllegalArgumentException if {@code symbol} is not part of the grammar
     */
    public @NotNull Set<@NotNull String> first(@NotNull String symbol) throws IllegalArgumentException {
        if (!T.contains(symbol) && !N.contains(symbol)) {
            throw new IllegalArgumentException("The symbol is not in the grammar");
        }
        return first(symbol, new HashSet<>());
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

    @Override
    public @NotNull Iterator<String> iterator() {
        Set<String> symbols = new HashSet<>(N);
        symbols.addAll(T);
        return symbols.iterator();
    }
}
