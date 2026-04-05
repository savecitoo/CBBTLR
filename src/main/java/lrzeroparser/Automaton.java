package lrzeroparser;

import grammar.ContextFreeGrammar;
import grammar.ContextFreeProduction;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class represents an LR(0) automaton.
 */
public class Automaton {

    private final @NotNull Map<@NotNull State, @NotNull Map<@NotNull String, @NotNull State>> transitions;

    private final @NotNull Map<@NotNull State, @NotNull Integer> states_encoding;

    private final @NotNull Set<@NotNull State> states;

    /**
     * This is a constructor for an LR(0) automaton.
     * @param transitions the transitions of the automata.
     * @param states the states of the automata.
     * @throws IllegalArgumentException if state or transitions are empty.
     */
    private Automaton(@NotNull Map<@NotNull State, @NotNull Map<@NotNull String, @NotNull State>> transitions,
                      @NotNull Map<@NotNull State, @NotNull Integer> statesEncoding,
                      @NotNull Set<@NotNull State> states) {
        if (states.isEmpty() || statesEncoding.isEmpty())
            throw new IllegalArgumentException("The states cannot be empty.");
        if (transitions.isEmpty())
            throw new IllegalArgumentException("The transitions cannot be empty.");
        this.transitions = transitions;
        this.states = states;
        this.states_encoding = statesEncoding;
    }

    /**
     * This method given a grammar and a starting set of items turn back the closure of these items.
     * @param grammar the grammar.
     * @param start the starting items.
     * @param cache_item a cache that helps to use always the same items, instead of building new ones.
     * @return the closure of the starting set
     */
    private static @NotNull Set<@NotNull Item> closure(@NotNull ContextFreeGrammar grammar,
                                                @NotNull Set<Item> start,
                                                @NotNull Map<@NotNull Item, @NotNull Item> cache_item) {
        Set<Item> closure = new HashSet<>(start);
        boolean changed = true;
        do {
            Set<Item> toAdd = new HashSet<>();
            for (Item item : closure) {
                if (!item.isComplete()) {
                    String A = item.afterDot();
                    if (grammar.N.contains(A)) {
                        ContextFreeProduction production = grammar.P.fromLHS(A);
                        for (int i = 0; i < production.rhsSize(); i++) {
                            Item new_item = Item.fromProduction(production, i, 0);
                            new_item = cache_item.computeIfAbsent(new_item, k -> k);
                            if (!closure.contains(new_item))
                                toAdd.add(new_item);
                        }
                    }
                }
            }
            if (toAdd.isEmpty()){
                changed = false;
            }
            closure.addAll(toAdd);
        } while (changed);
        return closure;
    }

    /**
     * This method build an LR(0) automaton from a grammar.
     * @param grammar the grammar that has to be transformed in an automaton.
     * @return the corresponding LR(0) automaton.
     */
    public static @NotNull Automaton fromGrammar(@NotNull ContextFreeGrammar grammar) {
        if (grammar.P.fromLHS(grammar.S).rhsSize() != 1)
            throw new IllegalArgumentException
                    ("The grammar should be augmented, this means the starting symbol should be wrapped by another symbol.");

        // items needed by the constructor
        Map<State, Map<String, State>> transition = new HashMap<>();
        Set<State> states = new HashSet<>();
        Map<State, Integer> state_encoding = new HashMap<>();

        // useful for avoiding loops and add memory efficiency
        Map<Item, Item> cache_item = new HashMap<>();
        Queue<State> queue = new ArrayDeque<>();
        Set<State> seen = new HashSet<>();

        // the start of the building
        Item curr = Item.fromProduction(grammar.P.fromLHS(grammar.S), 0, 0);
        cache_item.put(curr, curr);
        State s = new State(closure(grammar, Set.of(curr), cache_item));
        queue.add(s);
        seen.add(s);

        while (!queue.isEmpty()) {
            s = queue.remove();
            for (String symb : grammar) { // search for all symbols

                Set<Item> v = new HashSet<>(); // cration of the new state for the symbol v
                for (Item it : s) { //search for every item in the current state which one is going to be in the new
                                    // state
                    if (!it.isComplete() && it.afterDot().equals(symb)) {
                        v.add(it.advanceDot());
                    }
                }

                if (!v.isEmpty()) { // if v is not empty we do the closure and eventually create the new state
                    State new_state = new State(closure(grammar, v, cache_item));
                    if (seen.add(new_state)) {

                        // the new state is added to the queue to be expanded after
                        queue.add(new_state);
                    }

                    // creation of the transition in the automaton.
                    Map<String, State> arrow = transition.getOrDefault(
                            s, new HashMap<>()
                    );
                    arrow.put(symb, new_state);
                    transition.put(s,arrow);

                }
            }

            states.add(s);
            state_encoding.put(s, state_encoding.size());
        }
        return new Automaton(
                transition,
                state_encoding,
                states
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("LR(0) Automaton\n");
        sb.append("====================\n\n");

        // Stati
        sb.append("States:\n");
        for (State s : states) {
            int id = states_encoding.getOrDefault(s, -1);
            sb.append("I").append(id).append(":\n");

            for (Item it : s) {
                sb.append("  ").append(it).append("\n");
            }
            sb.append("\n");
        }

        // Transizioni
        sb.append("Transitions:\n");
        for (Map.Entry<State, Map<String, State>> entry : transitions.entrySet()) {
            State from = entry.getKey();
            int fromId = states_encoding.getOrDefault(from, -1);

            for (Map.Entry<String, State> t : entry.getValue().entrySet()) {
                String symb = t.getKey();
                State to = t.getValue();
                int toId = states_encoding.getOrDefault(to, -1);

                sb.append("I")
                        .append(fromId)
                        .append(" -- ")
                        .append(symb)
                        .append(" --> I")
                        .append(toId)
                        .append("\n");
            }
        }

        return sb.toString();
    }
}
