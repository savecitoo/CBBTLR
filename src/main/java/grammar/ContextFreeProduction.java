package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a context-free production rule of the form:
 * <pre>
 *     A → α
 * </pre>
 * where {@code A} is a non-terminal symbol and α is a non-empty sequence
 * of grammar symbols. ε-productions are not supported.
 */
public class ContextFreeProduction implements Iterable<String> {

    /** The left-hand side non-terminal symbol of {@code this} production rule. */
    public final String lhs;

    /** The right-hand side {@code this} production rule, it is immutable. */
    public final @NotNull List <@NotNull String> rhs;

    /** The production id of the rule.  */
    public final int prod_id;

    /** The tag of the production, if it is empty then it is assumed that has no type. */
    public final @NotNull String prod_tag;

    /**
     * Constructs a context-free production rule.
     *
     * @param lhs     the left-hand side non-terminal symbol; must not be blank
     * @param rhs     the right hand side of {@code this} production rule.
     * @param prod_id the numeric identifier assigned to {@code this} production.
     * @param prod_tag the tag that specifies the semantic mean of the rule, it can be the empty string.
     * @throws IllegalArgumentException if {@code lhs} is blank, if {@code rhs} is empty,
     *                                  or contains a blank symbol
     */
    public ContextFreeProduction(String lhs, @NotNull List<@NotNull String> rhs,
                                 int prod_id, @NotNull String prod_tag) throws IllegalArgumentException{
        if (lhs.isBlank())
            throw new IllegalArgumentException("The string passed must not be blank.");
        if (rhs.isEmpty())
            throw new IllegalArgumentException("The right-hand side must not be void.");

        for (String s : rhs){
            if (s.isBlank())
                throw new IllegalArgumentException("The string passed must not be blank.");
        }

        this.lhs = lhs;
        this.prod_tag = prod_tag;
        this.rhs = List.copyOf(rhs);
        this.prod_id = prod_id;
    }

    /**
     * Returns the size of right-hand side of {@code this} production rule.
     *
     * @return the size of the rhs of {@code this} production rule.
     */
    public int rhsSize() { return rhs.size(); }

    /**
     * Returns whether the given symbol appears in the right-hand side
     * of {@code this} production rule.
     *
     * @param symbol the symbol to look up
     * @return {@code true} if {@code symbol} appears in the right-hand side;
     *         {@code false} otherwise
     */
    public boolean contains(@NotNull String symbol) {
        for (String symb : rhs)
            if (symbol.equals(symb))
                return true;
        return false;
    }

    /**
     * Returns the set of all symbols that appear in {@code this} production rule,
     * the left-hand side and in the right-hand side.
     *
     * @return the set of symbols appearing in {@code this} production
     */
    public @NotNull Set<String> symbols() {
        Set<String> symbols = new HashSet<>(rhs);
        symbols.add(lhs);
        return symbols;
    }

    /**
     * Parses a string representation of a production rule and returns a list of the corresponding instances.
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
     * @return a List of {@link ContextFreeProduction} instances corresponding to the given string
     *          in the order specified by the string.
     * @throws IllegalArgumentException if {@code production} is blank, malformed,
     *                                  or the left-hand side contains more than one symbol
     */
    public static @NotNull List<ContextFreeProduction> fromString(@NotNull String production, int prod_id) {
        String[] prod = production.trim().split(" -> ");
        if (prod.length != 2)
            throw new IllegalArgumentException("The productions must be separeted by just one  \"->\" and must to be not blank");
        String lhs = prod[0].trim();
        if (lhs.contains(" "))
            throw new IllegalArgumentException("The left-hand side must have only one symbol");

        List<ContextFreeProduction> productions = new LinkedList<>();

        String[] rhs_s = prod[1].trim().split("\\|");

        for (String s : rhs_s) {
            String[] rule = s.trim().split("#");
            String rule_tag = rule.length == 2 ? rule[1].trim() : "";
            productions.add(
                    new ContextFreeProduction(
                            lhs, Arrays.asList(rule[0].split(" ")), prod_id, rule_tag
                    )
            );
            prod_id++;
        }

        return productions;
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return rhs.iterator();
    }

    private @NotNull String stringOfRHS() {
        StringBuilder sb = new StringBuilder();
        for (String s : rhs)
            sb.append(s);
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(lhs).append(" -> ").append(stringOfRHS());
        if (!prod_tag.isBlank())
            sb.append(" [").append(prod_tag).append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ContextFreeProduction CFprod) {
            return lhs.equals(CFprod.lhs) && rhs.equals(CFprod.rhs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, rhs);
    }

}
