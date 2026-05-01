package grammar;

import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.*;

/**
 * This class models all the production that a grammar has. <br>
 * Using the constructor doesn't guarantee that the key correspond to the left-hand side of the production
 */
public class Productions implements Iterable<ContextFreeProduction> {

    private final @NotNull List<@NotNull ContextFreeProduction> productions;

    private final @NotNull Map<@NotNull String, @NotNull ContextFreeProduction> lhs_prods;

    public Productions(@NotNull List<@NotNull ContextFreeProduction> productions) {
        productions = List.copyOf(productions);
        this.productions = productions;
        Map<String, ContextFreeProduction> lhs_prods = new HashMap<>();
        for (ContextFreeProduction p : productions)
            lhs_prods.put(p.getLHS(), p);
        this.lhs_prods = lhs_prods;
    }

    /**
     * This method returns all the symbols that appear in {@code this} Productions.
     *
     * @return the set of symbols that appear in the productions.
     */
    public @NotNull Set<@NotNull String> symbols() {
        Set<String> symbols = new HashSet<>();
        for (ContextFreeProduction cfp : productions)
            symbols.addAll(cfp.symbols());
        return symbols;
    }

    /**
     * This method returns the production with the given id.
     * @param prod_id the id of the production.
     * @return the production corresponding to the given id.
     * @throws NoSuchElementException if no production has the id.
     */
    public @NotNull ContextFreeProduction fromProdId(int prod_id) throws NoSuchElementException {
        for (ContextFreeProduction p : productions) {
            if (p.hasProdId(prod_id)) return p;
        }
        throw new NoSuchElementException("No production has that id.");
    }

    public @NotNull ContextFreeProduction fromLHS(@NotNull String lhs) throws IllegalArgumentException {
        if (!lhs_prods.containsKey(lhs))
            throw new IllegalArgumentException("No left-hand side match the input");
        return lhs_prods.get(lhs);
    }

    /**
     * This method given a grammar specified only by its production rules, returns a Productions class that contains
     * all the production of the grammar.<br>
     * The grammar must be specified with a production rule divided in different rows.<br>
     * If a non-Terminal symbol has more than one possible production it will be divided by the ASCII symbol "|".<br>
     * If you use this method to build the {@code Productions} then the order of the productions will be the same as they appear
     * in the string.
     *
     * @param grammar the grammar.
     * @return the productions specified by the grammar.
     * @throws IllegalArgumentException if the grammar is blank or if a non-terminal symbol appear in more than one left-hand side.
     */
    public static @NotNull Productions fromString(@NotNull String grammar) throws IllegalArgumentException {

        if (grammar.isBlank())
            throw new IllegalArgumentException("The gramar must not be blank.");
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

    public int size() {
        int res = 0;
        for (ContextFreeProduction p : productions) res += p.rhsSize();
        return res;
    }

    /**
     * This method returns the production of the starting symbol.
     * @return the production of the starting symbol.
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
