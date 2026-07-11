package grammar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * Represents a context-free grammar (CFG) for a formal language.
 *
 * <p>A CFG is defined by a 4-tuple (N, T, P, S) where N is the set of non-terminal
 * symbols, T is the set of terminal symbols, P is the set of production rules,
 * and S ∈ N is the start symbol.</p>
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

    /*
        CONSTUCTORS
     */

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

    /*
        FACTORY METHODS
     */

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

    /*
        Instance Methods:
        1: Symbols Information:
     */

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
     * This method given a symbol returns if it is nullable or not.
     * @param symbol a symbol.
     * @return true if the input symbol is nullable, false otherwise.
     */
    public boolean isNullable(@NotNull String symbol) {
        if (nullable == null) nullable = computeNullable();
        return nullable.contains(symbol);
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
                if (epsilonInAll) {
                    changed = fst.add("ε") || changed;
                }
            }
        } while (changed);
        return first;
    }


    /**
     * Returns the FIRST set of a given grammar symbol.
     *
     * <p>The FIRST set of a symbol contains all terminals that can appear as the
     * first character of any string derived from that symbol. If the symbol can
     * derive the empty string, "ε" is included in the set.
     *
     * @param symbol the grammar symbol (terminal or non-terminal) whose FIRST set is requested
     * @return a non-null set of terminal strings (possibly including "ε") representing FIRST(symbol)
     * @throws IllegalArgumentException if {@code symbol} does not belong to this grammar
     */
    public @NotNull Set<@NotNull String> first(@NotNull String symbol) throws IllegalArgumentException {
        if (first == null) first = computeFirst();
        if (!first.containsKey(symbol))
            throw new IllegalArgumentException("The symbol does not belong to the grammar.");
        return first.get(symbol);
    }

    /**
     * Returns the FIRST set of a sequence of grammar symbols.
     *
     * <p>Computes FIRST(s₁ s₂ … sₙ) by iterating over the symbol list and applying
     * the standard rule:
     * <ol>
     *   <li>Add all terminals in FIRST(sᵢ) \ {ε} to the result.</li>
     *   <li>If ε ∉ FIRST(sᵢ), stop — the remaining symbols are unreachable.</li>
     *   <li>If ε ∈ FIRST(sᵢ) for every sᵢ, add ε to the result.</li>
     * </ol>
     *
     * @param symbols an ordered list of grammar symbols representing a sentential form;
     *                must not be {@code null}, but may be empty (returns {"ε"})
     * @return the FIRST set of the symbol sequence; includes "ε" only if every symbol
     *         in the list can individually derive the empty string
     */
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
     * Returns the FOLLOW set of a given non-terminal symbol.
     *
     * <p>The FOLLOW set of a non-terminal A contains all terminals that can appear
     * immediately to the right of A in any sentential form derived from the start
     * symbol. If A can appear as the rightmost symbol of a complete derivation,
     * the end-of-input marker "#" is included in the set.
     *
     * @param symbol the non-terminal symbol whose FOLLOW set is requested
     * @return a non-null set of terminal strings (possibly including "#") representing FOLLOW(symbol)
     * @throws IllegalArgumentException if {@code symbol} is not a non-terminal of this grammar
     */
    public @NotNull Set<@NotNull String> follow(@NotNull String symbol) throws IllegalArgumentException {
        if (follow == null) follow = computeFollow();
        if (!follow.containsKey(symbol))
            throw new IllegalArgumentException("The symbol is not a non-terminal symbol.");
        return follow.get(symbol);
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
