package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class represents a production rule, for example:
 *  <pre>
 *  A -> a | Bc | AAcc
 *  </pre>
 */
public class ContextFreeProduction implements Iterable<List<String>> {

    /** This field represents the left-hand side of the production. */
   private final String lhs;

    /** This field represents the right-hand side of the production. */
   private final @NotNull List<@NotNull List <@NotNull String>> rhs;

    /**
     * This is a constructor for a Context-Free production.
     * @param lhs a non-terminal symbol.
     * @param rhs the possible productions of the lhs.
     * @throws IllegalArgumentException if the strings that represent the symbols are blank or the production rule is empty.
     */
    public ContextFreeProduction(String lhs, @NotNull List<@NotNull List<@NotNull String>> rhs) throws IllegalArgumentException{
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
    }

    /**
     * This method returns the number of production of the right-hand side.
     * @return the size of the rhs.
     */
    public int rhsSize() { return rhs.size(); }

    /**
     * This method returns the left-hand side of the production rule.
     * @return the left hand side.
     */
    public @NotNull String getLHS() { return lhs;}

    /**
     * This method returns the right-hand side production at the specified index.
     * @param i the index of the desired right-hand side production.
     * @return the right-hand side production in the i-th position.
     * @throws IllegalArgumentException if i is less than zero or greater equal to rhsSize().
     */
    public @NotNull List<String> getRHS(int i) throws IllegalArgumentException {
        if (i < 0 || i >= rhs.size())
            throw new IllegalArgumentException("The index must go from 0 to rhsSize() - 1.");
        return rhs.get(i);
    }

    /**
     * This method returns if the symbol given in input is contained in the right-hand side of the production.
     * @param symbol the symbol we want to see if it is in the right-hand side of the production.
     * @return true if the symbol is in the right-hand side of the production, false otherwise.
     */
    public boolean contains(@NotNull String symbol) {
        for (List<String> prods : rhs)
            for (String symb : prods)
                if (symbol.equals(symb))
                    return true;
        return false;
    }

    /**
     * This method returns all the symbols that appear in {@code this} Production.
     * @return the set of symbols that appear in the production.
     */
    public @NotNull Set<String> symbols() {
        Set<String> symbols = new HashSet<>();
        symbols.add(lhs);
        for (List<String> l : rhs)
            symbols.addAll(l);
        return symbols;
    }

    public static @NotNull ContextFreeProduction fromString(@NotNull String production) {
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
        return new ContextFreeProduction(lhs, rhs);
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
