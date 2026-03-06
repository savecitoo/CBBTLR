package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * This class represents a production rule, for example:
 *  <pre>
 *  A -> a | Bc | AAcc
 *  </pre>
 */
public class ContextFreeProduction implements Iterable<List<Symbol>> {

    /** This field represents the left-hand side of the production. */
   private final Symbol lhs;

    /** This field represents the right-hand side of the production. */
   private final @NotNull List<@NotNull List <@NotNull Symbol>> rhs;

    /**
     *
     * @param lhs
     * @param rhs
     * @throws IllegalArgumentException
     */
    public ContextFreeProduction(Symbol lhs, @NotNull List<@NotNull List<@NotNull Symbol>> rhs) throws IllegalArgumentException{
        if (!lhs.isTerminal())
            throw new IllegalArgumentException("The lhs of a Context-Free production rule must have a non-Terminal symbol.");
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
    public @NotNull Symbol getLHS() {return lhs;}

    /**
     * This method returns the right-hand side production at the specified index.
     * @param i the index of the desired right-hand side production.
     * @return the right hand side production in the i-th position.
     * @throws IllegalArgumentException if i is less than zero or greater equal to rhsSize().
     */
    public @NotNull List<Symbol> getRHS(int i) throws IllegalArgumentException {
        if (i<0 || i >= rhs.size())
            throw new IllegalArgumentException("The index must go from 0 to rhsSize() - 1.");
        return rhs.get(i);
    }

    @Override
    public @NotNull Iterator<List<Symbol>> iterator() {
        return rhs.iterator();
    }
}
