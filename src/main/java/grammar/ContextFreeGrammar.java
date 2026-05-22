package grammar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.Finder;

import javax.swing.*;
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

    // PRE-COMPUTED FIELDS due to efficiency reasons.


    /** This field contains all the nullable symbols in the grammar */
     private @Nullable Set<@NotNull String> nullable;

     /** This field contains a map from a non-terminal to all the possible first of its. */
    private @Nullable Map<@NotNull String, @NotNull Set<@NotNull String>> first;

    /** This field contains the follow of every non-terminal of the grammar. */
    private @Nullable Map<@NotNull String, @NotNull Set<@NotNull String>> follow;

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
     * Computes all the follow set for the non-terminals in the grammar.
     * @return a map with as key a non-terminal symbol, and as a value the follow set of the symbol.
     */
    private @NotNull Map<@NotNull String, @NotNull Set<@NotNull String>> computeFollow() {
        Map<String, Set<String>> follow = new HashMap<>();
        for (String symbol : N) {
            follow.put(symbol, new HashSet<>());
        }
        follow.get(S).add("#");
        boolean changed;
        do {
            changed = false;
            for (ContextFreeProduction production : P) {
                for (int i = 0; i < production.rhsSize(); i++) {
                    String X = production.rhsGet(i);

                    // if it is a non-terminal we start to compute

                    if (N.contains(X)) {
                        Set<String> fol = follow.get(X);

                        boolean epsilonInAll = true;

                        for (int j = i + 1; j < production.rhsSize(); j++) {
                            Set<String> firstJ = new HashSet<>(first(production.rhsGet(j)));
                            boolean hasEpsilon = firstJ.remove("ε");
                            changed = fol.addAll(firstJ) || changed;

                            if (!hasEpsilon) {
                                epsilonInAll = false;
                                break;
                            }
                        }

                        if (epsilonInAll) {
                            changed = fol.addAll(follow.get(production.lhs)) || changed;
                        }
                    }
                }

            }
        } while (changed);
        return follow;
    }

    /**
     * Computes all the first set for the symbols in the grammar.
     * @return a map with as key a grammar symbol, and as a value the first set of the symbol.
     */
    private @NotNull Map<@NotNull String, @NotNull Set<@NotNull String>> computeFirst() {
        Map<String, Set<String>> first = new HashMap<>();

        // initialization

        for (String t : T) {
            first.put(t, new HashSet<>(Set.of(t)));
        }
        first.put("ε", new HashSet<>(Set.of("ε")));
        for (String n : N) {
            first.put(n, new HashSet<>());
        }
        boolean changed;
        do {
            changed = false;
            for (ContextFreeProduction production : P) {
                Set<String> fst = first.get(production.lhs);
                boolean epsilonInAll = true;

                for (int i = 0; i < production.rhsSize(); i++) {
                    Set<String> firstI = new HashSet<>(first.get(production.rhsGet(i)));
                    boolean hasEpsilon = firstI.remove("ε");
                    changed = fst.addAll(firstI) || changed;

                    if (!hasEpsilon) {
                        epsilonInAll = false;
                        break;
                    }
                }

                // Tutti i simboli della RHS possono derivare ε
                if (epsilonInAll) {
                    changed = fst.add("ε") || changed;
                }
            }
        } while (changed);
        return first;
    }

    /**
     * This method computes all the nullable symbols in the grammar.
     * @return a set containing all the nullable symbols in the grammar.
     */
    private @NotNull Set<String> computeNullable() {
        Set<String> nullable = new HashSet<>();
        for (String nt : N) {
            if (P.containsProd(nt)) {
                for (ContextFreeProduction p : P.fromLHS(nt)) {
                    if (p.isEpsilon()) {
                        nullable.add(p.lhs);
                        break; // you cannot have a two nullable prod for the same LHS.
                    }
                }
            }
        }

        boolean changed;

        do {
            changed = false;
            for (ContextFreeProduction p : P) {
                // if it is already nullable we delete it
                if (nullable.contains(p.lhs))
                    continue;

                boolean allNullable = true;

                for (String symb : p) {
                    if (!nullable.contains(symb)) {
                        allNullable = false;
                        break;
                    }
                }

                if (allNullable) {
                    nullable.add(p.lhs);
                    changed = true;
                }
            }
        } while (changed);

        return Set.copyOf(nullable);
    }


    /**
     * Parses a textual representation of a context-free grammar and returns the
     * corresponding {@link ContextFreeGrammar} instance.
     *
     * <p>Symbols are classified as follows:</p>
     * <ul>
     *   <li>Every symbol appearing on the left-hand side of a production is
     *       considered a non-terminal.</li>
     *   <li>Every other symbol is considered a terminal.</li>
     * </ul>
     *
     * <p>The start symbol is the left-hand side of the first production rule.</p>
     *
     * <p>The grammar string must contain one production per line using the
     * following format:</p>
     *
     * <pre>
     * A -> α1 #tag | α2
     * B -> β1 | β2 #anotherTag
     * </pre>
     *
     * <p>where:</p>
     * <ul>
     *   <li>{@code A}, {@code B}, ... are non-terminal symbols;</li>
     *   <li>each alternative on the right-hand side is separated by {@code |};</li>
     *   <li>symbols inside a production must be separated by whitespace;</li>
     *   <li>an optional production tag can be appended using the
     *       {@code #tag} syntax;</li>
     * </ul>
     *
     * @param grammar the textual representation of the grammar;
     *                must not be blank or malformed
     * @return the parsed {@link ContextFreeGrammar}
     * @throws IllegalArgumentException if {@code grammar} is blank or does not
     *                                  conform to the expected format
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
                N, T, productions, productions.getStarting().getFirst().lhs
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
            if (prod.containsAll(cfp.rhs())) {
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
                    P.put(i, new ContextFreeProduction(p.lhs, p.rhs(), i, p.prod_tag));
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


    public boolean isNullable(@NotNull String symbol) {
        if (nullable == null) nullable = computeNullable();
        return nullable.contains(symbol);
    }

    public @NotNull Set<@NotNull String> first(@NotNull String symbol) throws IllegalArgumentException {
        if (first == null) first = computeFirst();
        if (!first.containsKey(symbol))
            throw new IllegalArgumentException("The symbol does not belong to the grammar.");
        return first.get(symbol);
    }

    public @NotNull Set<@NotNull String> follow(@NotNull String symbol) throws IllegalArgumentException {
        if (follow == null) follow = computeFollow();
        if (!follow.containsKey(symbol))
            throw new IllegalArgumentException("The symbol is not a non-terminal symbol.");
        return follow.get(symbol);
    }


    public Set<String> firstOfSymbols(@NotNull List<String> symbols) {
        Set<String> result = new HashSet<>();
        for (String sym : symbols) {
            Set<String> firstSym = new HashSet<>(first(sym));
            boolean hasEpsilon = firstSym.remove("ε");
            result.addAll(firstSym);
            if (!hasEpsilon) return result;
        }
        result.add("ε");
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("N = ").append(N.toString()).append("\n");
        sb.append("T = ").append(T).append("\n");
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
