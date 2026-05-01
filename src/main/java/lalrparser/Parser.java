package lalrparser;

import grammar.ContextFreeGrammar;
import grammar.ContextFreeProduction;
import org.jetbrains.annotations.NotNull;
import utils.ParseTree;
import utils.SymbolTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class implements an LALR(1) Parser.
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

    private final @NotNull SymbolTable<String> symbol_encoding;

    private final @NotNull ParserTables tables;

    /**
     * This is a constructor for an LALR(1) Parser.
     * @param tables the tables needed to the parser, you can build it with the generateParser method.
     * @param symbol_encoding the symbol table that will be used to encode all the symbols in the grammar.
     */
    public Parser(@NotNull ParserTables tables, @NotNull SymbolTable<String> symbol_encoding) throws IllegalArgumentException {

        this.symbol_encoding = symbol_encoding;
        this.tables = tables;
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
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * This method parse list of string in input returning the corresponding parsing tree.
     * @param input the input we want to parse.
     * @return the corresponding parsing tree.
     * @throws IllegalArgumentException if the word is not recognized.
     */
    public ParseTree parse(@NotNull List<String> input) throws IllegalArgumentException {
        input = new ArrayList<>(input);
        input.add("#");
        Stack<Integer> states = new Stack<>();
        Stack<ParseTree> symbols = new Stack<>();
        states.push(0);
        int i = 0;

        while (true) {
            int s = states.peek();
            int next = symbol_encoding.getId(input.get(i));

            int[][][] ACTION = tables.getACTION();
            int[][] GOTO = tables.getGOTO();
            int[][] PRODUCTIONS = tables.getPRODUCTIONS();

            if (next < 0 || next >= ACTION[s].length || ACTION[s][next] == null)
                throw new IllegalArgumentException("The word is not recognized");

            int[] action = ACTION[s][next];
            switch (action[0]) {
                case 0: // shift
                    states.push(action[1]);
                    symbols.push(new ParseTree(input.get(i), true));
                    i++;
                    break;
                case 1: // reduce
                    int[] production = PRODUCTIONS[action[1]];
                    int lhs_id = production[0];
                    int rhs_len = production[1];
                    List<ParseTree> children = new ArrayList<>();
                    for (int j = 0; j < rhs_len; j++) {
                        states.pop();
                        children.add(symbols.pop());
                    }
                    s = states.peek();
                    states.push(GOTO[s][lhs_id]);
                    symbols.push(new ParseTree(symbol_encoding.getSymbol(lhs_id), children.reversed()));
                    break;
                case 2: // accept
                    return symbols.peek();
                default:
                    throw new IllegalArgumentException("The word is not recognized");
            }
        }
    }

    public static void generateParser(@NotNull ContextFreeGrammar grammar,
                               @NotNull SymbolTable<@NotNull String> symbol_encoding,
                               @NotNull String className,
                               @NotNull String outputDir) {
        Automaton automaton = Automaton.fromGrammar(grammar);
        Map<Integer, Map<Integer, Integer>> GOTO = new HashMap<>();
        Map<Integer, Map<Integer, int[]>> ACTION = new HashMap<>();
        buildingTables(grammar, symbol_encoding, automaton, GOTO, ACTION);
        try {
            writeTo(
                    className,
                    Path.of(outputDir),
                    GOTO,
                    ACTION,
                    symbol_encoding,
                    grammar
            );
        } catch (IOException e) {
            throw new IllegalArgumentException
                    ("An exception has been raised with the following output:" + e.getMessage());
        }
    }


    /**
     * This method given a ContextFreeGrammar and a SymbolTable adds to the table all the symbols in the grammar.
     * @param grammar the grammar.
     * @param symbolEncoding the table.
     */
    private static void populateSymbolEncoding(@NotNull ContextFreeGrammar grammar,
                                        @NotNull SymbolTable<@NotNull String> symbolEncoding) {
        for (String s : grammar.T) symbolEncoding.add(s);
        for (String s : grammar.N) symbolEncoding.add(s);
        symbolEncoding.add("#");
    }

    /**
     * This method builds the tables for the parser starting from the grammar.
     * @param grammar the grammar wanted to be parsed.
     * @param symbol_encoding the encoding of the symbols in the grammar.
     * @param automaton the LALR(1) automaton.
     * @param GOTO the GOTO table.
     * @param ACTION the ACTION table.
     */
    private static void buildingTables(@NotNull ContextFreeGrammar grammar,
                                       @NotNull SymbolTable<@NotNull String> symbol_encoding,
                                       @NotNull Automaton automaton,
                                       @NotNull Map<@NotNull Integer,
                                               @NotNull Map<@NotNull Integer, @NotNull Integer>> GOTO,
                                       @NotNull Map<@NotNull Integer,
                                               @NotNull Map<@NotNull Integer, int[]>> ACTION) {
        populateSymbolEncoding(grammar,symbol_encoding);
        for (State state : automaton.states) { // we iterate over each state on the automaton
            // SHIFT
            if (automaton.transitions.containsKey(state)) {
                Map<String, State> transitions = automaton.transitions.get(state);
                for (String symbol : transitions.keySet()) {
                    int id_symbol = symbol_encoding.getId(symbol);
                    State next = transitions.get(symbol);
                    int from = automaton.states_encoding.get(state);
                    int to = automaton.states_encoding.get(next);

                    if (grammar.N.contains(symbol)) { // NON terminal → GOTO
                        GOTO.computeIfAbsent(from, k -> new HashMap<>()).put(id_symbol, to);
                    } else if (grammar.T.contains(symbol)) {
                        Map<Integer, int[]> row = ACTION.computeIfAbsent(from, k -> new HashMap<>());
                        if (row.containsKey(id_symbol)) {
                            throw new IllegalArgumentException("SHIFT/REDUCE CONFLICT"); // shift/reduce
                        }
                        row.put(id_symbol, new int[]{0, to});
                    }
                }
            }

            // REDUCE
            int stateId = automaton.states_encoding.get(state);
            for (CoreItem it : state) {
                if (it.isComplete()) {
                    Set<String> lookaheads = state.items().get(it).getLookaheads();
                    Map<Integer, int[]> row = ACTION.computeIfAbsent(stateId, k -> new HashMap<>());
                    for (String symbol : lookaheads) {
                        int id_symbol = symbol_encoding.getId(symbol);
                        if (row.containsKey(id_symbol)) {
                            if (row.get(id_symbol)[0] == 0)
                                throw new IllegalArgumentException("SHIFT/REDUCE CONFLICT");
                            else
                                throw new IllegalArgumentException("REDUCE/REDUCE CONFLICT");
                        }
                        if (it.production_id() == grammar.P.fromLHS(grammar.S).getStartingProd_id() && symbol.equals("#")) {
                            row.put(symbol_encoding.getId("#"), new int[]{2});
                        } else {
                            row.put(id_symbol, new int[]{1, it.production_id()});
                        }
                    }
                }
            }
        }
    }

    private static void writeTo(
           @NotNull String className,
           @NotNull Path outputDir,
           @NotNull Map<@NotNull Integer,
                   @NotNull Map<@NotNull Integer, @NotNull Integer>> GOTO,
           @NotNull Map<@NotNull Integer,
                   @NotNull Map<@NotNull Integer, int[]>> ACTION,
           @NotNull SymbolTable<@NotNull String> symbol_encoding,
           @NotNull ContextFreeGrammar grammar  // solo per le produzioni, poi buttato
    ) throws IOException {
        StringBuilder sb = new StringBuilder();

        int nStates = 0;
        for (int state : ACTION.keySet())
            if (state + 1 > nStates) nStates = state + 1;
        for (int state : GOTO.keySet())
            if (state + 1 > nStates) nStates = state + 1;

        int nSymbols = symbol_encoding.size();

        // ── Header ──────────────────────────────────────────────────────────
        sb.append("import java.util.Arrays;\n\n");
        sb.append("import lalrparser.ParserTables;\n\n");
        sb.append("import org.jetbrains.annotations.NotNull;\n\n");
        sb.append("public class ").append(className).append("ParserTables implements ParserTables {\n\n");

        // ── PRODUCTIONS ─────────────────────────────────────────────────────
        sb.append("    public static final int[][] PRODUCTIONS = {\n");
        for (int i = 0; i < grammar.P.size(); i++) {
            ContextFreeProduction p = grammar.P.fromProdId(i);
            int lhs_id = symbol_encoding.getId(p.getLHS());
            int rhs_len = p.fromProdId(i).size();
            sb.append("        {").append(lhs_id).append(", ").append(rhs_len).append("},\n");
        }
        sb.append("    };\n\n");

        // ── ACTION ───────────────────────────────────────────────────────────
        sb.append("    public static final int[][][] ACTION = new int[")
                .append(nStates).append("][").append(nSymbols).append("][];\n");
        sb.append("    static {\n");
        for (int state : ACTION.keySet()) {
            Map<Integer, int[]> row = ACTION.get(state);
            for (int symbolId : row.keySet()) {
                int[] action = row.get(symbolId);
                sb.append("        ACTION[").append(state).append("][").append(symbolId).append("] = new int[]{");
                for (int i = 0; i < action.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(action[i]);
                }
                sb.append("};\n");
            }
        }
        sb.append("    }\n\n");

        // ── GOTO ─────────────────────────────────────────────────────────────
        sb.append("    public static final int[][] GOTO = new int[")
                .append(nStates).append("][").append(nSymbols).append("];\n");
        sb.append("    static {\n");
        sb.append("        for (int[] row : GOTO) Arrays.fill(row, -1);\n");
        for (int state : GOTO.keySet()) {
            Map<Integer, Integer> row = GOTO.get(state);
            for (int symbolId : row.keySet()) {
                int nextState = row.get(symbolId);
                sb.append("        GOTO[").append(state).append("][").append(symbolId)
                        .append("] = ").append(nextState).append(";\n");
            }
        }
        sb.append("    }\n\n");

        // ── Getters ─────────────────────────────────────────────────────────
        sb.append("    @Override\n");
        sb.append("    public int @NotNull [] @NotNull [] @NotNull [] getACTION() {\n");
        sb.append("        return ACTION;\n");
        sb.append("    }\n\n");

        sb.append("    @Override\n");
        sb.append("    public int @NotNull [] @NotNull [] getGOTO() {\n");
        sb.append("        return GOTO;\n");
        sb.append("    }\n\n");

        sb.append("    @Override\n");
        sb.append("    public int @NotNull [] @NotNull [] getPRODUCTIONS() {\n");
        sb.append("        return PRODUCTIONS;\n");
        sb.append("    }\n\n");

        sb.append("}\n");

        // ── Scrittura su file ────────────────────────────────────────────────
        Files.createDirectories(outputDir);
        Files.writeString(
                outputDir.resolve(className + "ParserTables.java"),
                sb.toString()
        );
    }


    /**
     * This class represents an LALR(1) automaton.
     */
    public static class Automaton implements Iterable<State> {

        private final @NotNull Map<@NotNull State, @NotNull Map<@NotNull String, @NotNull State>> transitions;

        private final @NotNull Map<@NotNull State, @NotNull Integer> states_encoding;

        private final @NotNull Set<@NotNull State> states;

        /**
         * This is a constructor for an LALR(1) automaton.
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
        private static @NotNull Set<@NotNull CoreItem> closure(@NotNull ContextFreeGrammar grammar,
                                                               @NotNull Set<CoreItem> start,
                                                               @NotNull Map<@NotNull CoreItem, @NotNull CoreItem> cache_item) {
            Set<CoreItem> closure = new HashSet<>(start);
            boolean changed = true;
            do {
                Set<CoreItem> toAdd = new HashSet<>();
                for (CoreItem item : closure) {
                    if (!item.isComplete()) {
                        String A = item.afterDot();
                        if (grammar.N.contains(A)) {
                            ContextFreeProduction production = grammar.P.fromLHS(A);
                            for (int i = 0; i < production.rhsSize(); i++) {
                                CoreItem new_item = CoreItem.fromProduction(production, i, 0);
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
         * This method build an LALR(1) automaton from a grammar.
         * @param grammar the grammar that has to be transformed in an automaton.
         * @return the corresponding LALR(1) automaton.
         */
        public static @NotNull Automaton fromGrammar(@NotNull ContextFreeGrammar grammar) {
            if (grammar.P.fromLHS(grammar.S).rhsSize() != 1)
                throw new IllegalArgumentException
                        ("The grammar should be augmented, this means the starting symbol should be wrapped by another symbol.");

            // items needed by the constructor
            Map<State, Map<String, State>> transition = new HashMap<>();
            Set<State> states = new HashSet<>();
            Map<State, Integer> state_encoding = new HashMap<>();

            // First part: LR(0) automaton building.

            State first = automatonBuilding(grammar, transition, states, state_encoding);

            Map<StateItem, Set<StateItem>> propagate  = new HashMap<>();  // (stato, item) → (stato, item)


            // second part of the algorithm: we construct the channels and the lookaheads

            CoreItem start = CoreItem.fromProduction(grammar.P.fromLHS(grammar.S), 0, 0);
            first.items().get(start).addLookahead("#");

            channelBuilding(grammar, states, propagate, transition);

            // third part of the algorithm: we propagate the lookaheads.

//            printPropagationGraph(propagate, state_encoding);

            lookaheadsPropagation(propagate);

            // printPropagationGraph(propagate, state_encoding);

            return new Automaton(
                    transition,
                    state_encoding,
                    states
            );
        }

        /**
         * This method build the LR(0) automaton data-structures starting from a grammar.
         * @param grammar the grammar that we use to construct the LR(0) automaton.
         * @param transition the edges of the automaton.
         * @param states the states of the automaton.
         * @param state_encoding the encoding of the states of the automaton.
         * @return the starting state of the automaton.
         */
        private static @NotNull State automatonBuilding(@NotNull ContextFreeGrammar grammar, Map<State, Map<String, State>> transition, Set<State> states, Map<State, Integer> state_encoding) {
            // useful for avoiding loops and add memory efficiency
            Map<CoreItem, CoreItem> cache_item = new HashMap<>();
            Map<State, State> cache_state = new HashMap<>();
            Queue<State> queue = new ArrayDeque<>();
            Set<State> seen = new HashSet<>();

            // the start of the building
            CoreItem curr = CoreItem.fromProduction(grammar.P.fromLHS(grammar.S), 0, 0);
            cache_item.put(curr, curr);

            State s = State.fromCore(closure(grammar, Set.of(curr), cache_item));
            cache_state.put(s, s);
            State first = s;
            queue.add(s);
            seen.add(s);

            // first part of the algorithm: in this step the LR(0) automaton is built.
            while (!queue.isEmpty()) {
                s = queue.remove();
                for (String symb : grammar) { // search for all symbols

                    Set<CoreItem> v = new HashSet<>(); // creation of the new state for the symbol v
                    for (CoreItem it : s) { //search for every item in the current state which one is going to be in the new
                        // state
                        if (!it.isComplete() && it.afterDot().equals(symb)) {
                            v.add(it.advanceDot());
                        }
                    }

                    if (!v.isEmpty()) { // if v is not empty we do the closure and eventually create the new state
                        State new_state = State.fromCore(closure(grammar, v, cache_item));
                        if (seen.add(new_state)) {
                            // the new state is added to the queue to be expanded after
                            queue.add(new_state);
                            cache_state.put(new_state,new_state);
                        } else {
                            // since every state creates its personal LALR(1) item we have to use always
                            // the same state also in the arrow map, otherwise the propagation will be erroneous.
                            new_state = cache_state.get(new_state);
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
            return first;
        }

        /**
         * This method starting from the automaton builds the channels to upgrade the automaton to an LALR(1) automaton. <br>
         * The informations will be saved in the field @code{propagate} and @code{lookaheads}.
         * @param grammar the grammar of the LALR(1) is currently being built.
         * @param states the states of the LR(0) automaton.
         * @param propagate a map where will be saved the propagations of the automata.
         * @param transition the transitions of the automata.
         */
        private static void channelBuilding(@NotNull ContextFreeGrammar grammar,
                                            @NotNull Set<@NotNull State> states,
                                            @NotNull Map<@NotNull StateItem, @NotNull Set<@NotNull StateItem>> propagate,
                                            @NotNull Map<@NotNull State, @NotNull Map< @NotNull String, @NotNull State>> transition) {
            for (State state : states) {
                for (CoreItem item : state) {
                    if (!item.isComplete()) { // check that the item has something after the dot.

                        String B = item.afterDot();
                        // This part id for spontaneous lookahead.

                        if (grammar.N.contains(B)) {
                            for (CoreItem item1 : state) {
                                if (item1.lhs().equals(B)) {
                                    if (item.hasFirstOfBeta()) {
                                        // lookahead spontaneo: aggiungi FIRST(β)
                                        Set<String> lookahead_spont = grammar.first(item.firstOfBeta());
                                        state.items().get(item1).addAllLookaheads(lookahead_spont);
                                    } else {
                                        // β è vuota: propaga sempre
                                        propagate.computeIfAbsent(
                                                new StateItem(state, state.items().get(item)),
                                                k -> new HashSet<>()
                                        ).add(new StateItem(state, state.items().get(item1)));
                                    }
                                }
                            }
                        }
                    }
                }

                // now we construct the lookaheads for the goto table.

                for (CoreItem item : state) {
                    if (!item.isComplete()) {
                        State destination = transition.get(state).get(item.afterDot());
                        Set<StateItem> propagations = propagate.computeIfAbsent(
                                new StateItem(state, state.items().get(item)),
                                k -> new HashSet<>()
                        );
                        propagations.add(new StateItem(destination, destination.items().get(item.advanceDot())));
                    }
                }
            }
        }


        /**
         * This method given the propagations and the lookaheads for each item propagates the lookaheads.
         * @param propagate the propagation for each LALR(1) item.
         */
        private static void lookaheadsPropagation(Map<StateItem, Set<StateItem>> propagate) {
            boolean changes = true;
            while (changes) {
                changes = false;
                for (Map.Entry<StateItem, Set<StateItem>> entry : propagate.entrySet()) {
                    StateItem src = entry.getKey();
                    Set<StateItem> destinations = entry.getValue();
                    for (StateItem dst : destinations) {
                        if (dst.item().addAllLookaheads(src.item().getLookaheads())) {
                            changes = true;
                        }
                    }
                }
            }
        }

        private static void printPropagationGraph(Map<StateItem, Set<StateItem>> propagate, Map<State, Integer> states_encoding) {
            System.out.println("Propagation Graph");
            System.out.println("=================");
            for (Map.Entry<StateItem, Set<StateItem>> entry : propagate.entrySet()) {
                StateItem src = entry.getKey();
                for (StateItem dst : entry.getValue()) {
                    System.out.println(
                            "I" + states_encoding.get(src.state()) + ": " + src.item() +
                                    " --> " +
                                    "I" + states_encoding.get(dst.state()) + ": " + dst.item()
                    );
                }
            }
        }

        @Override
        public @NotNull Iterator<State> iterator() {
            return states.iterator();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("LALR(1) Automaton\n");
            sb.append("====================\n\n");

            // Stati
            sb.append("States:\n");
            for (State s : states) {
                int id = states_encoding.getOrDefault(s, -1);
                sb.append("I").append(id).append(":\n");

                for (CoreItem it : s) {
                    sb.append("  ").append(s.items().get(it)).append("\n");
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
