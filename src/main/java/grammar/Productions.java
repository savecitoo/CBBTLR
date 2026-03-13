package grammar;

import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.*;

/**
 * This class models all the production that a grammar has. <br>
 * Using the constructor doesn't guarantee that the key correspond to the left-hand side of the production
 * @param productions this field represents the productions where the keys are the left-hand side of a production.
 */
public record Productions(@NotNull Map<@NotNull String, @NotNull ContextFreeProduction> productions) implements Iterable<ContextFreeProduction> {

    public Productions {
        productions = Map.copyOf(productions);
    }

    /**
     * This method given a grammar specified only by its production rules, returns a Productions class that contains
     * all the production of the grammar.<br>
     * The grammar must be specified with a production rule divided in different rows.<br>
     * If a non-Terminal symbol has more than one possible production it will be divided by the ASCII symbol "|".
     *
     * @param grammar the grammar.
     * @return the productions specified by the grammar.
     * @throws IllegalArgumentException if the grammar is blank or if a non-terminal symbol appear in more than one left-hand side.
     */
    public static @NotNull Productions fromString(@NotNull String grammar) throws IllegalArgumentException {

        if (grammar.isBlank())
            throw new IllegalArgumentException("The gramar must not be blank.");
        Map<String, ContextFreeProduction> productions = new HashMap<>();
        Scanner s = new Scanner(new StringReader(grammar));
        while (s.hasNext()) {
            ContextFreeProduction prod = ContextFreeProduction.fromString(s.nextLine());
            if (productions.containsKey(prod.getLHS()))
                throw new IllegalArgumentException
                        ("The left-hand side of the productions must appear in only one line as starting symbol for the production rule");
            productions.put(prod.getLHS(), prod);
        }
        s.close();
        return new Productions(productions);
    }

    @Override
    public @NotNull Iterator<ContextFreeProduction> iterator() {
        return new Iterator<>() {

            private final Iterator<Map.Entry< String, ContextFreeProduction>> it =
                    productions.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ContextFreeProduction next() {
                return it.next().getValue();
            }
        };
    }
}
