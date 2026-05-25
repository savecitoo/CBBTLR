package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a context-free production rule of the form:
 * <pre>
 *     A → α
 * </pre>
 * where {@code A} is a non-terminal symbol and α is a non-empty sequence
 * of grammar symbols.
 */
public class ContextFreeProduction implements Iterable<String> {

    /** The left-hand side non-terminal symbol of {@code this} production rule. */
    public final String lhs;

    /** The right-hand side {@code this} production rule, it is immutable. */
    private final @NotNull List <@NotNull String> rhs;

    /** The production id of the rule.  */
    public final int prod_id;

    /** The tag of the production, if it is empty then it is assumed that has no tag. */
    public final @NotNull String prod_tag;

    /*
        Constructors
     */

    /**
     * Constructs a context-free production rule.
     *
     * @param lhs     the left-hand side symbol; must not be blank
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

    /*
        FACTORY METHODS
     */

    /**
     * Parses a string representation of a production rule and returns a list of the corresponding instances.
     *
     * <p>The expected format is:</p>
     * <pre>
     *     A -> α₁ [#tag]? | α₂[#tag]? | ... | αₙ [#tag]?
     * </pre>
     * where {@code A} is the left-hand side non-terminal, and each αᵢ is a sequence
     * of space-separated symbols. The left-hand side must be a single symbol with no spaces.
     * The optional {@code #tag} suffix can be applied to all the alternative.
     *
     * <p>Two productions are considered equal if and only if they share the same left-hand side
     * and the same right-hand side sequence of symbols. The tag is purely semantic and is
     * not part of the equality criterion.</p>
     *
     * <p>Occurrences of {@code ε} (epsilon) in a right-hand side are filtered out unless
     * the entire alternative consists solely of epsilon symbols, in which case the right-hand
     * side is normalized to a single {@code ε}. For example:</p>
     * <ul>
     *   <li>{@code ε B C} is parsed as {@code [B, C]}</li>
     *   <li>{@code ε ε} is parsed as {@code [ε]}</li>
     *   <li>{@code ε} is parsed as {@code [ε]}</li>
     * </ul>
     *
     * @param production the string representation of the production rule; must not be blank
     *                   and must conform to the expected format
     * @param prod_id    the numeric identifier assigned to the first alternative;
     *                   subsequent alternatives are assigned consecutive ids
     * @return a List of {@link ContextFreeProduction} instances corresponding to the given string
     *         in the order specified by the string
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

        Set<ContextFreeProduction> seen = new HashSet<>();

        for (String s : rhs_s) {
            String[] rule = s.trim().split("#");
            String rule_tag = rule.length == 2 ? rule[1].trim() : "";
            List<String> rule_symb = new ArrayList<>();
            for (String symb : rule[0].split(" ")) {
                if (!symb.equals("ε")) {
                    rule_symb.add(symb);
                }
            }

            if (rule_symb.isEmpty()) {
                rule_symb.add("ε");
            }
            ContextFreeProduction p =new ContextFreeProduction(
                    lhs, rule_symb, prod_id, rule_tag
            );
            if (seen.add(p)) {
                productions.add(p);
            }
            prod_id++;
        }

        return productions;
    }

    /*
        Instance Methods
     */

    /**
     * Tells if the production is equal to A -> ε.
     * @return if {@code this} production produces ε.
     */
    public boolean isEpsilon() {
        return rhs.getFirst().equals("ε");
    }

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
     * Returns the size of right-hand side of {@code this} production rule.
     *
     * @return the size of the rhs of {@code this} production rule.
     */
    public int rhsSize() {
        return isEpsilon() ? 0 :  rhs.size();
    }

    /**
     * Returns the symbol at the given index in the right-hand side of this production.
     *
     * @param i the index of the symbol to retrieve
     * @return the symbol at position {@code i}
     * @throws IllegalArgumentException if this is an epsilon production or {@code i} is out of bounds
     */
    public String rhsGet(int i) {
        if (isEpsilon())
            throw new IllegalArgumentException("Epsilon production has no symbols");
        else if (i < 0 || i >= rhsSize())
            throw new IllegalArgumentException("The index should be between 0 and rhsSize().");
        return rhs.get(i);
    }

    /**
     * Returns an unmodifiable view of the right-hand side of this production.
     * If this is an epsilon production, returns an empty list.
     *
     * @return an unmodifiable {@link List} of the right-hand side symbols,
     *         or an empty list if this is an epsilon production
     */
    public List<String> rhs() {
        return isEpsilon() ? List.of() : rhs;
    }

    /**
     * Returns the set of all symbols that appear in {@code this} production rule,
     * the left-hand side symbol and all the symbols in the right-hand side.
     *
     * @return the set of symbols appearing in {@code this} production
     */
    public @NotNull Set<String> symbols() {
        Set<String> symbols = new HashSet<>(rhs);
        symbols.add(lhs);
        return symbols;
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return rhs.iterator();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(lhs).append(" -> ");
        for (String s : rhs)
            sb.append(s);
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
