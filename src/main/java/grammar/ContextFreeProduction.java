package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a context-free production rule of the form:
 * <pre>
 *     A → α₁ | α₂ | ... | αₙ
 * </pre>
 * where {@code A} is a non-terminal symbol and each αᵢ is a non-empty sequence
 * of grammar symbols. ε-productions are not supported.
 */
public class ContextFreeProduction implements Iterable<List<String>> {

    /** The left-hand side non-terminal symbol of {@code this} production rule. */
    private final String lhs;

    /** The right-hand side alternatives of {@code this} production rule. */
    private final @NotNull List<@NotNull List <@NotNull String>> rhs;

    /** The production id of the first alternative of  */
    private final int prod_id;

    /**
     * Constructs a context-free production rule.
     *
     * @param lhs     the left-hand side non-terminal symbol; must not be blank
     * @param rhs     the list of right-hand side alternatives; must not be empty,
     *                and each alternative must be a non-empty list of non-blank symbols
     * @param prod_id the numeric identifier assigned to the first alternative of {@code this} production;
     *                subsequent alternatives are assigned consecutive ids
     * @throws IllegalArgumentException if {@code lhs} is blank, if {@code rhs} is empty,
     *                                  or if any alternative is empty or contains a blank symbol
     */
    public ContextFreeProduction(String lhs, @NotNull List<@NotNull List<@NotNull String>> rhs, int prod_id) throws IllegalArgumentException{
        if (lhs.isBlank())
            throw new IllegalArgumentException("The string passed must not be blank.");
        if (rhs.isEmpty())
            throw new IllegalArgumentException("The right-hand side must not be void.");
        for (List<String> prod : rhs) {
            if (prod.isEmpty()) {
                throw new IllegalArgumentException("The production rule cannot be empty.");
            } else {
                for (String s : prod){
                    if (s.isBlank())
                        throw new IllegalArgumentException("The string passed must not be blank.");
                }

            }
        }
        this.lhs = lhs;
        this.rhs = rhs;
        this.prod_id = prod_id;
    }

    /**
     * Returns the starting production id of {@code this} production rule.
     * Subsequent alternatives are assigned consecutive ids starting from {@code this} value.
     *
     * @return the id of the first right-hand side alternative
     */
    public int getStartingProd_id() {
        return prod_id;
    }

    /**
     * Returns the production id of the given right-hand side alternative.
     *
     * @param production the right-hand side alternative whose id is to be retrieved
     * @return the production id corresponding to {@code production}
     * @throws NoSuchElementException if {@code production} does not match any alternative
     *                                in {@code this} production rule
     */
    public int getProd_id(@NotNull List<@NotNull String> production) throws NoSuchElementException {
        for (int i = 0; i < rhsSize(); i++) {
            if (rhs.get(i).equals(production))
                return prod_id+i;
        }
        throw new NoSuchElementException("The is no production equal to the one in input");
    }

    /**
     * Returns the number of right-hand side alternatives of {@code this} production rule.
     *
     * @return the number of right-hand side alternatives
     */
    public int rhsSize() { return rhs.size(); }

    /**
     * Returns the left-hand side non-terminal symbol of {@code this} production rule.
     *
     * @return the left-hand side symbol
     */
    public @NotNull String getLHS() { return lhs;}

    /**
     * Returns the right-hand side alternative at the specified index.
     *
     * @param i the zero-based index of the desired alternative; must be in [{@code 0}, {@link #rhsSize()})
     * @return the right-hand side alternative at position {@code i}
     * @throws IllegalArgumentException if {@code i} is negative or greater than or equal to {@link #rhsSize()}
     */
    public @NotNull List<String> getRHS(int i) throws IllegalArgumentException {
        if (i < 0 || i >= rhs.size())
            throw new IllegalArgumentException("The index must go from 0 to rhsSize() - 1.");
        return rhs.get(i);
    }

    /**
     * Returns whether the given symbol appears in any right-hand side alternative
     * of {@code this} production rule.
     *
     * @param symbol the symbol to look up
     * @return {@code true} if {@code symbol} appears in at least one alternative;
     *         {@code false} otherwise
     */
    public boolean contains(@NotNull String symbol) {
        for (List<String> prods : rhs)
            for (String symb : prods)
                if (symbol.equals(symb))
                    return true;
        return false;
    }

    /**
     * Returns whether the given production id belongs to {@code this} production rule.
     *
     * @param prodId the production id to check
     * @return {@code true} if {@code prodId} is assigned to one of the alternatives
     *         of {@code this} production rule; {@code false} otherwise
     */
    public boolean hasProdId(int prodId) {
        return prodId >= prod_id && prodId <= prod_id+rhsSize()-1;
    }

    /**
     * Returns the right-hand side alternative corresponding to the given production id.
     *
     * @param prod_id the production id of the desired alternative
     * @return the right-hand side alternative assigned to {@code prod_id}
     * @throws IllegalArgumentException if {@code prod_id} does not belong to {@code this} production rule
     */
    public List<String> fromProdId(int prod_id) throws IllegalArgumentException {
        if (!hasProdId(prod_id))
            throw new IllegalArgumentException("There is no production with that id");
        return rhs.get(prod_id - this.prod_id);
    }

    /**
     * Returns the set of all symbols that appear in {@code this} production rule,
     * including the left-hand side and all right-hand side alternatives.
     *
     * @return the set of symbols appearing in {@code this} production
     */
    public @NotNull Set<String> symbols() {
        Set<String> symbols = new HashSet<>();
        symbols.add(lhs);
        for (List<String> l : rhs)
            symbols.addAll(l);
        return symbols;
    }

    /**
     * Parses a string representation of a production rule and returns the corresponding instance.
     *
     * <p>The expected format is:</p>
     * <pre>
     *     A -> α₁ | α₂ | ... | αₙ
     * </pre>
     * where {@code A} is the left-hand side non-terminal, and each αᵢ is a sequence
     * of space-separated symbols. The left-hand side must be a single symbol with no spaces.
     *
     * @param production the string representation of the production rule; must not be blank
     *                   and must conform to the expected format
     * @param prod_id    the numeric identifier assigned to the first alternative;
     *                   subsequent alternatives are assigned consecutive ids
     * @return a {@link ContextFreeProduction} instance corresponding to the given string
     * @throws IllegalArgumentException if {@code production} is blank, malformed,
     *                                  or the left-hand side contains spaces
     */
    public static @NotNull ContextFreeProduction fromString(@NotNull String production, int prod_id) {
        String[] prod = production.trim().split(" -> ");
        if (prod.length != 2)
            throw new IllegalArgumentException("The productions must be separeted by just one  \"->\" and must to be not blank");
        String lhs = prod[0].trim();
        if (lhs.contains(" "))
            throw new IllegalArgumentException("The left-hand side must have only one symbol and must not contain spaces.");
        // using a linked list because we have just to append the new part of the rhs
        List<List<String>> rhs = new LinkedList<>();

        String[] rhs_s = prod[1].trim().split("\\|");

        for (String s : rhs_s) {
            rhs.add(Arrays.asList(s.trim().split(" ")));
        }
        return new ContextFreeProduction(lhs, rhs, prod_id);
    }

    @Override
    public @NotNull Iterator<List<String>> iterator() {
        return rhs.iterator();
    }

    private @NotNull String stringOfRHS(int i) {
        StringBuilder sb = new StringBuilder();
        for (String s : rhs.get(i))
            sb.append(s);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(lhs).append(" -> ");
        int i;
        for (i = 0; i < rhs.size()-1; i++)
            sb.append(stringOfRHS(i)).append(" | ");
        return sb.append(stringOfRHS(i)).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ContextFreeProduction CFprod) {
            return lhs.equals(CFprod.lhs) && rhs.equals(CFprod.rhs);
        }
        return false;
    }

}
