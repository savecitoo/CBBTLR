package lroneparser;

import grammar.ContextFreeGrammar;
import grammar.ContextFreeProduction;
import org.jetbrains.annotations.NotNull;
import utils.ParseTree;

import java.util.*;

/**
 * This class implements an LR(1) Parser.
 */
public class Parser {

    /*
        In the table if the array is encoded in the following way:
        - The first element is the code of the operation:
            0 is the shift.
            1 is the reduce.
            2 is the acceptance.
        - The second element is the state where we have to go.
     */

    private final @NotNull Map<@NotNull Integer, @NotNull Map<@NotNull String, @NotNull Integer>> GOTO;

    private final @NotNull Map<@NotNull Integer, @NotNull Map<@NotNull String, int[]>> ACTION;

    private final @NotNull ContextFreeGrammar grammar;

    // probably we have to keep trace of the states where we reduce with the encoding.

    /**
     * This is a constructor for an LR(1) Parser.
     * @param grammar the grammar to be parsed.
     */
    public Parser(@NotNull ContextFreeGrammar grammar) throws IllegalArgumentException {
        Automaton automaton = Automaton.fromGrammar(grammar);

        // Construction of the GOTO table.

        Map<Integer, Map<String, Integer>> GOTO = new HashMap<>();

        Map<Integer, Map<String, int[]>> ACTION = new HashMap<>();

        for (State state : automaton.states) { // we iterate over each state on the automaton

            // SHIFT
            if (automaton.transitions.containsKey(state)) {
                Map<String, State> transitions = automaton.transitions.get(state);
                for (String symbol : transitions.keySet()) {

                    State next = transitions.get(symbol);
                    int from = automaton.states_encoding.get(state);
                    int to = automaton.states_encoding.get(next);

                    if (grammar.N.contains(symbol)) { // NON terminal → GOTO
                        GOTO.computeIfAbsent(from, k -> new HashMap<>()).put(symbol, to);
                    } else if (grammar.T.contains(symbol)) {
                        ACTION.computeIfAbsent(from, k -> new HashMap<>()).put(symbol, new int[]{0, to});
                    }
                }
            }


            // REDUCE
            for (Item it : state) {
                if (it.isComplete()) {

                    String look_ahead = it.look_ahead();
                    Map<String, int[]> row =
                            ACTION.computeIfAbsent(automaton.states_encoding.get(state), k -> new HashMap<>());

                    if (row.containsKey(look_ahead))
                        throw new IllegalArgumentException("A conflict has been raised");

                    row.put(look_ahead, new int[]{1, it.production_id()});

                    // Special case for acceptance
                    if (it.production_id() == grammar.P.fromLHS(grammar.S).getStartingProd_id() &&
                            it.look_ahead().equals("#")) {
                        row = ACTION.get(automaton.states_encoding.get(state));
                        row.put("#", new int[]{2});
                    }
                }
            }
        }
        this.GOTO = GOTO;

        this.ACTION = ACTION;

        this.grammar = grammar;
    }

    /**
     * This method returns if the input is correct for the given grammar.
     * @param input the word to be recognized.
     * @return true if it is in the language of the grammar, false otherwise.
     */
    public boolean isRecognized(List<String> input) {
        try {
            parse(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    /**
     * This method parse list of string in input returning the corresponding parsing tree.
     * @param input the input we want to parse.
     * @return the corresponding parsing tree.
     * @throws IllegalArgumentException if the word is not recognized.
     */
    public ParseTree parse(List<String> input) throws IllegalArgumentException {
        input = new ArrayList<>(input);
        Stack<Integer> states = new Stack<>();
        Stack<ParseTree> symbols = new Stack<>();
        states.push(0);
        int i = 0;
        String next = "";
        while (true) {
            Integer s = states.peek();

            next = (i < input.size()) ? input.get(i) : "#";

            if (!ACTION.get(s).containsKey(next))
                throw new IllegalArgumentException("The word is not recognized");
            int[] action = ACTION.get(s).get(next);
            switch (action[0]) {
                case 0: // shift
                    states.push(
                            action[1]
                    );
                    if (!next.equals("#"))
                        symbols.push(new ParseTree(next, true));
                    i++;
                    break;
                case 1: // reduce
                    ContextFreeProduction production = grammar.P.fromProdId(action[1]);
                    int prod_size = production.fromProdId(action[1]).size();
                    List<ParseTree> children = new LinkedList<>();
                    for (int j = 0; j < prod_size; j++) {
                        states.pop();
                        children.addFirst(symbols.pop());
                    }
                    s = states.peek();

                    states.push(GOTO.get(s).get(production.getLHS()));
                    symbols.push(new ParseTree(production.getLHS(), children));
                    break;
                case 2: // accept
                    return symbols.peek();
                default: // error
                    throw new IllegalArgumentException("The word is not recognized");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("GOTO TABLE:\n");
        for (Map.Entry<Integer, Map<String, Integer>> entry : GOTO.entrySet()) {
            Integer state = entry.getKey();
            sb.append("State ").append(state).append(":\n");

            for (Map.Entry<String, Integer> inner : entry.getValue().entrySet()) {
                sb.append("  ").append(inner.getKey())
                        .append(" -> ").append(inner.getValue())
                        .append("\n");
            }
        }

        sb.append("\nACTION TABLE:\n");
        for (Map.Entry<Integer, Map<String, int[]>> entry : ACTION.entrySet()) {
            Integer state = entry.getKey();
            sb.append("State ").append(state).append(":\n");

            for (Map.Entry<String, int[]> inner : entry.getValue().entrySet()) {
                sb.append("  ").append(inner.getKey())
                        .append(" -> ").append(Arrays.toString(inner.getValue()))
                        .append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * This class represents an LR(1) automaton.
     */
    public static class Automaton implements Iterable<State> {

        private final @NotNull Map<@NotNull State, @NotNull Map<@NotNull String, @NotNull State>> transitions;

        private final @NotNull Map<@NotNull State, @NotNull Integer> states_encoding;

        private final @NotNull Set<@NotNull State> states;

        /**
         * This is a constructor for an LR(1) automaton.
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

                                Set<String> lookaheads;
                                if (item.hasFirstOfBeta()) { // we add the father lookahead iff the
                                                            // item has no first of beta
                                    lookaheads = grammar.first(item.firstOfBeta());
                                } else {
                                    lookaheads = Set.of(item.look_ahead());
                                }
                                for (int i = 0; i < production.rhsSize(); i++) {
                                    for (String first : lookaheads) {
                                        Item new_item = Item.fromProduction(production, first, i, 0);
                                        new_item = cache_item.computeIfAbsent(new_item, k -> k);
                                        if (!closure.contains(new_item))
                                            toAdd.add(new_item);
                                    }
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
         * This method build an LR(1) automaton from a grammar.
         * @param grammar the grammar that has to be transformed in an automaton.
         * @return the corresponding LR(1) automaton.
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
            Item curr = Item.getStartingItem(grammar);
            cache_item.put(curr, curr);
            State s = new State(closure(grammar, Set.of(curr), cache_item));
            queue.add(s);
            seen.add(s);

            while (!queue.isEmpty()) {
                s = queue.remove();
                for (String symb : grammar) { // search for all symbols

                    Set<Item> v = new HashSet<>(); // ceration of the new state for the symbol v
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
        public @NotNull Iterator<State> iterator() {
            return states.iterator();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("LR(1) Automaton\n");
            sb.append("====================\n\n");

            List<State> ordered = new ArrayList<>(states);
            ordered.sort(new Comparator<State>() {
                @Override
                public int compare(State o1, State o2) {
                   return states_encoding.get(o1).compareTo(states_encoding.get(o2));
                }
            });

            // Stati
            sb.append("States:\n");
            for (State s : ordered) {
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

}
