package grammar;

import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.*;

/**
 * Represents the complete set of production rules of a context-free grammar.
 *
 * <p>Productions are stored in the order they are added, and the first production
 * is assumed to belong to the start symbol of the grammar. Each left-hand side
 * symbol must appear in exactly one production rule.</p>
 *
 * <p>Note: using the constructor directly does not guarantee that the keys of the
 * internal map correspond to the left-hand side of the associated production.</p>
 */
public class Productions implements Iterable<ContextFreeProduction> {

    /** A list of the production rules in the order they were passed in the initialization. */
    private final @NotNull List<@NotNull ContextFreeProduction> productions;

    /** A map that goes from the lhs of the production to the corresponding {@code ContextFreeProduction}. */
    private final @NotNull Map<@NotNull String, @NotNull ContextFreeProduction> lhs_prods;

    /**
     * Constructs a {@code Productions} instance from the given list of production rules.
     *
     * <p>Note: the order of the productions is preserved, and the first element
     * is treated as the production of the start symbol.</p>
     *
     * @param productions the list of production rules; must not be {@code null}
     */
    public Productions(@NotNull List<@NotNull ContextFreeProduction> productions) {
        productions = List.copyOf(productions);
        this.productions = productions;
        Map<String, ContextFreeProduction> lhs_prods = new HashMap<>();
        for (ContextFreeProduction p : productions)
            lhs_prods.put(p.getLHS(), p);
        this.lhs_prods = lhs_prods;
    }

    /**
     * Returns the set of all symbols that appear in any production rule,
     * including all left-hand side and right-hand side symbols.
     *
     * @return the set of all symbols appearing in this collection of productions
     */
    public @NotNull Set<@NotNull String> symbols() {
        Set<String> symbols = new HashSet<>();
        for (ContextFreeProduction cfp : productions)
            symbols.addAll(cfp.symbols());
        return symbols;
    }

    /**
     * Returns the production rule that contains the given production id.
     *
     * @param prod_id the production id to look up
     * @return the {@link ContextFreeProduction} whose id range includes {@code prod_id}
     * @throws NoSuchElementException if no production rule owns {@code prod_id}
     */
    public @NotNull ContextFreeProduction fromProdId(int prod_id) throws NoSuchElementException {
        for (ContextFreeProduction p : productions) {
            if (p.hasProdId(prod_id)) return p;
        }
        throw new NoSuchElementException("No production has that id.");
    }

    /**
     * Returns the production rule whose left-hand side matches the given symbol.
     *
     * @param lhs the left-hand side symbol to look up; must not be {@code null}
     * @return the {@link ContextFreeProduction} with left-hand side equal to {@code lhs}
     * @throws IllegalArgumentException if no production rule has {@code lhs} as its left-hand side
     */
    public @NotNull ContextFreeProduction fromLHS(@NotNull String lhs) throws IllegalArgumentException {
        if (!lhs_prods.containsKey(lhs))
            throw new IllegalArgumentException("No left-hand side match the input");
        return lhs_prods.get(lhs);
    }

    /**
     * Parses a string representation of a set of production rules and returns
     * the corresponding {@code Productions} instance.
     *
     * <p>Each line must contain exactly one production rule in the form:</p>
     * <pre>
     *     A → α₁ | α₂ | ... | αₙ
     * </pre>
     * Each left-hand side symbol must appear on exactly one line. The order of
     * the productions is preserved, and the first line is treated as the production
     * of the start symbol.
     *
     * @param grammar the string representation of the production rules; must not be blank
     * @return a {@code Productions} instance corresponding to the given string
     * @throws IllegalArgumentException if {@code grammar} is blank, any line is malformed,
     *                                  or a left-hand side symbol appears on more than one line
     */
    public static @NotNull Productions fromString(@NotNull String grammar) throws IllegalArgumentException {

        if (grammar.isBlank())
            throw new IllegalArgumentException("The grammar must not be blank.");
        List<ContextFreeProduction> productions = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Scanner s = new Scanner(new StringReader(grammar));
        int i = 0;
        while (s.hasNext()) {
            ContextFreeProduction prod = ContextFreeProduction.fromString(s.nextLine(), i);
            if (!seen.add(prod.getLHS()))
                throw new IllegalArgumentException
                        ("The left-hand side of the productions must appear in only one line as starting symbol for the production rule");
            productions.add(prod);
            i += prod.rhsSize();
        }
        s.close();
        return new Productions(productions);
    }

    /**
     * Returns the total number of right-hand side alternatives across all production rules.
     *
     * @return the sum of {@link ContextFreeProduction#rhsSize()} over all productions
     */
    public int size() {
        int res = 0;
        for (ContextFreeProduction p : productions) res += p.rhsSize();
        return res;
    }

    /**
     * Returns the production rule of the start symbol, i.e. the first production
     * in the order they were added.
     *
     * @return the production rule of the start symbol
     */
    public @NotNull ContextFreeProduction getStarting() {
        return productions.getFirst();
    }

    @Override
    public @NotNull Iterator<ContextFreeProduction> iterator() {
        return productions.iterator();
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < productions.size() - 1; i++) {
            sb.append(productions.get(i)).append("\n");
        }
        sb.append(productions.getLast());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
       if (o == this) return true;
       if (o instanceof Productions prods) // do not check lhs_prod because is built from productions.
           return prods.productions.equals(this.productions);
       return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(productions);
    }

}
