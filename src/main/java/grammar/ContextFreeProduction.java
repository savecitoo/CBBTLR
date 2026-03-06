package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class represents a production rule, for example:
 *  <pre>
 *  A -> a | Bc | AAcc
 *  </pre>
 */
public class ContextFreeProduction {

    /** This field represents the left hand side of the production. */
   private final Symbol lhs;

    /** This field represents the right hand side of the production. */
   private final @NotNull List<@NotNull List <@NotNull Symbol>> rhs;


    public ContextFreeProduction(Symbol lhs, @NotNull List<@NotNull List<@NotNull Symbol>> rhs) {
        if (!lhs.isTerminal())
            throw new IllegalArgumentException("The lhs of a Context-Free production rule must have a non-Terminal symbol.");
        this.lhs = lhs;
        this.rhs = rhs;
    }



}
