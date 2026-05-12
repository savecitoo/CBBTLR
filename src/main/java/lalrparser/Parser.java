package lalrparser;

import grammar.ContextFreeGrammar;
import grammar.ContextFreeProduction;
import lexer.Token;
import org.jetbrains.annotations.NotNull;
import tree.ParseTree;
import utils.SymbolTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An LALR(1) parser that recognizes strings and builds parse trees
 * for a given context-free grammar.
 *
 * <p>Instances are created by supplying a {@link ParserTables} object,
 * which encodes the ACTION and GOTO tables for the grammar.
 * Use {@link #generateParser} to produce those tables from a
 * {@link ContextFreeGrammar} and write the corresponding Java source files.
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

    /** The tables the parser has to use to parse. */
    private final @NotNull ParserTables tables;

    /**
     * Creates a parser backed by the given tables.
     *
     * @param tables the ACTION, GOTO, and production tables for the grammar.
     */
    public Parser(@NotNull ParserTables tables) throws IllegalArgumentException {
        this.tables = tables;
    }

    /**
     * Returns whether the given sequence of symbol names belongs to the language
     * defined by the grammar.
     *
     * @param input the sequence of terminal symbols to test.
     * @return {@code true} if the sequence is recognized; {@code false} otherwise.
     */
    public boolean isRecognizedString(List<String> input) {
        try {
            parseStrings(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (NoSuchElementException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * Returns whether the given token sequence belongs to the language
     * defined by the grammar.
     *
     * @param input the token sequence to test.
     * @return {@code true} if the sequence is recognized; {@code false} otherwise.
     */
    public boolean isRecognized(List<Token> input) {
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
     * Parses a sequence of symbol names and returns the corresponding parse tree.
     *
     * @param input the sequence of terminal symbols to parse.
     * @return the root of the parse tree for the input.
     * @throws IllegalArgumentException if the sequence is not recognized by the grammar.
     */
    public ParseTree parseStrings(@NotNull List<String> input) throws IllegalArgumentException {
        input = new ArrayList<>(input);
        input.add("#");
        Stack<Integer> states = new Stack<>();
        Stack<ParseTree> symbols = new Stack<>();
        states.push(0);
        int i = 0;

        int[][][] ACTION = tables.getACTION();
        int[][] GOTO = tables.getGOTO();
        int[][] PRODUCTIONS = tables.getPRODUCTIONS();
        String[] SYMBOLS = tables.getSymbols();

        while (true) {
            int s = states.peek();
            String token = input.get(i);

            int next = -1;
            for (int k = 0; k < SYMBOLS.length; k++) {
                if (SYMBOLS[k].equals(token)) { next = k; break; }
            }

            if (next < 0 || next >= ACTION[s].length || ACTION[s][next] == null)
                throw new IllegalArgumentException("The word is not recognized");

            int[] action = ACTION[s][next];
            switch (action[0]) {
                case 0: // shift
                    states.push(action[1]);
                    symbols.push(new ParseTree(token, true));
                    i++;
                    break;
                case 1: // reduce
                    int[] production = PRODUCTIONS[action[1]];
                    int lhs_id = production[0];
                    int rhs_len = production[1];
                    List<ParseTree> children = new LinkedList<>();
                    for (int j = 0; j < rhs_len; j++) {
                        states.pop();
                        children.addFirst(symbols.pop());
                    }
                    s = states.peek();
                    states.push(GOTO[s][lhs_id]);
                    symbols.push(new ParseTree(SYMBOLS[lhs_id], children));
                    break;
                case 2: // accept
                    return symbols.peek();
                default:
                    throw new IllegalArgumentException("The word is not recognized");
            }
        }
    }

    /**
     * Parses a token sequence and returns the corresponding parse tree.
     *
     * @param input the token sequence to parse.
     * @return the root of the parse tree for the input.
     * @throws IllegalArgumentException if the sequence is not recognized by the grammar.
     */
    public ParseTree parse(@NotNull List<Token> input) throws IllegalArgumentException {
        input = new ArrayList<>(input);
        Stack<Integer> states = new Stack<>();
        Stack<ParseTree> symbols = new Stack<>();
        states.push(0);
        int i = 0;

        int[][][] ACTION = tables.getACTION();
        int[][] GOTO = tables.getGOTO();
        int[][] PRODUCTIONS = tables.getPRODUCTIONS();
        String[] SYMBOLS = tables.getSymbols();
        input.add(Token.createEOFToken(SYMBOLS.length-1));

        while (true) {
            int s = states.peek();

            int next = input.get(i).token_id();

            if (next >= ACTION[s].length || ACTION[s][next] == null)
                throw new IllegalArgumentException("The word is not recognized");

            int[] action = ACTION[s][next];
            switch (action[0]) {
                case 0: // shift
                    states.push(action[1]);
                    symbols.push(new ParseTree(input.get(i).value(), true));
                    i++;
                    break;
                case 1: // reduce
                    int[] production = PRODUCTIONS[action[1]];
                    int lhs_id = production[0];
                    int rhs_len = production[1];
                    List<ParseTree> children = new LinkedList<>();
                    for (int j = 0; j < rhs_len; j++) {
                        states.pop();
                        children.addFirst(symbols.pop());
                    }
                    s = states.peek();
                    states.push(GOTO[s][lhs_id]);
                    symbols.push(new ParseTree(SYMBOLS[lhs_id], children));
                    break;
                case 2: // accept
                    return symbols.peek();
                default:
                    throw new IllegalArgumentException("The word is not recognized");
            }
        }
    }

    /**
     * Generates an LALR(1) parser for the given grammar and writes the
     * corresponding Java source files to the specified output directory.
     *
     * <p>Two files are produced: {@code <className>ParserTables.java},
     * containing the precomputed ACTION, GOTO, and production tables,
     * and {@code <className>Parser.java}, a ready-to-use parser class
     * that wraps them.
     *
     * @param grammar          the context-free grammar to generate a parser for.
     * @param symbol_encoding  a symbol table used to assign numeric IDs to grammar symbols.
     * @param packageName      the Java package of the generated classes.
     * @param className        the base name for the generated classes.
     * @param outputDir        the directory where the source files will be written.
     * @throws IllegalArgumentException if the grammar is ambiguous or the output cannot be written.
     */
    public static void generateParser(@NotNull ContextFreeGrammar grammar,
                                      @NotNull SymbolTable<@NotNull String> symbol_encoding,
                                      @NotNull String packageName,
                                      @NotNull String className,
                                      @NotNull String outputDir) {
        Automaton automaton = Automaton.fromGrammar(grammar);
        Map<Integer, Map<Integer, Integer>> GOTO = new HashMap<>();
        Map<Integer, Map<Integer, int[]>> ACTION = new HashMap<>();
        buildingTables(grammar, symbol_encoding, automaton, GOTO, ACTION);
        try {
            Path dir = Path.of(outputDir);
            writeTo(packageName, className, dir, GOTO, ACTION, symbol_encoding, grammar);
            writeParserFile(packageName, className, dir);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "An exception has been raised with the following output:" + e.getMessage());
        }
    }

    /**
     * Registers all terminal and non-terminal symbols of the grammar
     * into the symbol table, appending the end-of-input marker {@code "#"}.
     *
     * @param grammar         the grammar whose symbols are to be registered.
     * @param symbolEncoding  the symbol table to populate.
     * @throws IllegalArgumentException if the grammar already uses the reserved symbol {@code "#"}.
     */
    private static void populateSymbolEncoding(@NotNull ContextFreeGrammar grammar,
                                               @NotNull SymbolTable<@NotNull String> symbolEncoding) {
        for (String s : grammar.T) symbolEncoding.add(s);
        for (String s : grammar.N) symbolEncoding.add(s);
        if (!symbolEncoding.add("#"))
            throw new IllegalArgumentException("The symbol \"#\" must not be used.");
    }

    /**
     * Fills the ACTION and GOTO tables from the given LALR(1) automaton.
     *
     * @param grammar          the context-free grammar being parsed.
     * @param symbol_encoding  the symbol-to-integer mapping for the grammar.
     * @param automaton        the LALR(1) automaton for the grammar.
     * @param GOTO             the GOTO table to populate.
     * @param ACTION           the ACTION table to populate.
     * @throws IllegalArgumentException if a shift/reduce or reduce/reduce conflict is detected.
     */
    private static void buildingTables(@NotNull ContextFreeGrammar grammar,
                                       @NotNull SymbolTable<@NotNull String> symbol_encoding,
                                       @NotNull Automaton automaton,
                                       @NotNull Map<@NotNull Integer,
                                               @NotNull Map<@NotNull Integer, @NotNull Integer>> GOTO,
                                       @NotNull Map<@NotNull Integer,
                                               @NotNull Map<@NotNull Integer, int[]>> ACTION) {
        populateSymbolEncoding(grammar,symbol_encoding);
        for (State state : automaton.states) {
            // SHIFT
            if (automaton.transitions.containsKey(state)) {
                Map<String, State> transitions = automaton.transitions.get(state);
                for (String symbol : transitions.keySet()) {
                    int id_symbol = symbol_encoding.getId(symbol);
                    State next = transitions.get(symbol);
                    int from = automaton.states_encoding.get(state);
                    int to = automaton.states_encoding.get(next);

                    if (grammar.N.contains(symbol)) {
                        GOTO.computeIfAbsent(from, k -> new HashMap<>()).put(id_symbol, to);
                    } else if (grammar.T.contains(symbol)) {
                        Map<Integer, int[]> row = ACTION.computeIfAbsent(from, k -> new HashMap<>());
                        if (row.containsKey(id_symbol)) {
                            throw new IllegalArgumentException("SHIFT/REDUCE CONFLICT");
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
                        if (it.production_id() == grammar.P.fromLHS(grammar.S).getFirst().getProd_id() && symbol.equals("#")) {
                            row.put(symbol_encoding.getId("#"), new int[]{2});
                        } else {
                            row.put(id_symbol, new int[]{1, it.production_id()});
                        }
                    }
                }
            }
        }
    }

    /**
     * Writes the {@code <className>ParserTables.java} source file containing
     * the precomputed ACTION, GOTO, and production tables for the grammar.
     *
     * @param packageName      the Java package of the generated class.
     * @param className        the base name of the generated class.
     * @param outputDir        the directory where the file will be written.
     * @param GOTO             the GOTO table.
     * @param ACTION           the ACTION table.
     * @param symbol_encoding  the symbol-to-integer mapping used to build the tables.
     * @param grammar          the grammar the tables were derived from.
     * @throws IOException if the file cannot be written.
     */
    private static void writeTo(
            @NotNull String packageName,
            @NotNull String className,
            @NotNull Path outputDir,
            @NotNull Map<@NotNull Integer, @NotNull Map<@NotNull Integer, @NotNull Integer>> GOTO,
            @NotNull Map<@NotNull Integer, @NotNull Map<@NotNull Integer, int[]>> ACTION,
            @NotNull SymbolTable<@NotNull String> symbol_encoding,
            @NotNull ContextFreeGrammar grammar
    ) throws IOException {
        StringBuilder sb = new StringBuilder();

        int nStates = 0;
        for (int state : ACTION.keySet())
            if (state + 1 > nStates) nStates = state + 1;
        for (int state : GOTO.keySet())
            if (state + 1 > nStates) nStates = state + 1;

        int nSymbols = symbol_encoding.size();

        // ── Header ──────────────────────────────────────────────────────────
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import java.util.Arrays;\n");
        sb.append("import lalrparser.ParserTables;\n");
        sb.append("import org.jetbrains.annotations.NotNull;\n\n");
        sb.append("/**\n");
        sb.append(" * LALR parser tables generated for the ").append(className).append(" grammar.\n");
        sb.append(" *\n");
        sb.append(" * <p>Contains the precomputed {@code ACTION}, {@code GOTO}, {@code PRODUCTIONS},\n");
        sb.append(" * and {@code SYMBOLS} tables used by the LALR parser at runtime.</p>\n");
        sb.append(" *\n");
        sb.append(" * <p>This class is auto-generated — do not edit manually.</p>\n");
        sb.append(" */\n");
        sb.append("public class ").append(className).append("ParserTables implements ParserTables {\n\n");

        // ── PRODUCTIONS ─────────────────────────────────────────────────────
        sb.append("    public static final int[][] PRODUCTIONS = {\n");
        for (int i = 0; i < grammar.P.size(); i++) {
            ContextFreeProduction p = grammar.P.fromProdId(i);
            int lhs_id = symbol_encoding.getId(p.lhs);
            int rhs_len = p.rhsSize();
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

        // ── SYMBOLS ──────────────────────────────────────────────────────────
        sb.append("    public static final String[] SYMBOLS = {\n");
        for (int i = 0; i < symbol_encoding.size(); i++) {
            sb.append("        \"")
                    .append(symbol_encoding.getSymbol(i).replace("\"", "\\\""))
                    .append("\",\n");
        }
        sb.append("    };\n\n");

        // ── Getters ──────────────────────────────────────────────────────────
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

        sb.append("    @Override\n");
        sb.append("    public @NotNull String @NotNull [] getSymbols() { return SYMBOLS; }\n\n");

        sb.append("}\n");

        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve(className + "ParserTables.java"), sb.toString());
    }

    /**
     * Writes the {@code <className>Parser.java} source file — a thin facade
     * that instantiates {@code <className>ParserTables} and delegates to {@link Parser}.
     *
     * <p>The generated class exposes {@link Parser#parse} and
     * {@link Parser#isRecognizedString} without requiring the caller to manage
     * tables or symbol encoding manually.
     *
     * @param packageName the Java package of the generated class.
     * @param className   the base name of the generated class.
     * @param outputDir   the directory where the file will be written.
     * @throws IOException if the file cannot be written.
     */
    private static void writeParserFile(
            @NotNull String packageName,
            @NotNull String className,
            @NotNull Path outputDir
    ) throws IOException {

        String src = """
    package %s;
                
    import lalrparser.Parser;
    import lexer.Lexer;
    import tree.ParseTree;
    import java.io.IOException;
    import java.io.ObjectInputStream;
    import java.util.List;
    import java.util.Objects;
    import lexer.Token;
                
    /**
     * Parser for the %s language.
     *
     * <p>Combines a lexer and an LALR parser to process input strings
     * and produce parse trees according to the %s grammar.</p>
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * %sParser parser = new %sParser();
     * ParseTree tree = parser.parse("your input here");
     * }</pre>
     */
    public class %sParser {
                
        private final Parser parser;
        private final Lexer lexer;
                
        /**
         * Constructs a new {@code %sParser}, loading the precompiled
         * lexer and parser tables from the embedded resources.
         *
         * @throws IllegalStateException if the lexer resource cannot be loaded
         */
        public %sParser() {
            this.parser = new Parser(new %sParserTables());
            try (ObjectInputStream ois = new ObjectInputStream(
                    Objects.requireNonNull(
                        %sParser.class.getResourceAsStream("%sLexer.ser")
                    )
            )) {
                this.lexer = (Lexer) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load lexer for %s", e);
            }
        }
                
        /**
         * Parses a pre-tokenized input provided as a list of token strings,
         * bypassing the lexing phase.
         *
         * @param input the list of token strings to parse
         * @return the resulting {@link ParseTree}
         */
        public ParseTree parseStrings(List<String> input) {
            return parser.parseStrings(input);
        }
                
        /**
         * Tokenizes and parses the given input string.
         *
         * @param input the raw input string to lex and parse
         * @return the resulting {@link ParseTree}
         */
        public ParseTree parse(String input) {
            List<Token> tokenized = lexer.tokenize(input);
            return parser.parse(tokenized);
        }
                
        /**
         * Checks whether a pre-tokenized input is recognized by the grammar,
         * without building a parse tree.
         *
         * @param input the list of token strings to check
         * @return {@code true} if the input is valid, {@code false} otherwise
         */
        public boolean isRecognizedString(List<String> input) {
            return parser.isRecognizedString(input);
        }
        
        /**
         * Checks whether the given input string is recognized by the grammar,
         * without building a parse tree.
         *
         * @param input the raw input string to lex and check
         * @return {@code true} if the input is valid, {@code false} otherwise
         */
        public boolean isRecognized(String input) {
            List<Token> tokenized = lexer.tokenize(input);
            return parser.isRecognized(tokenized);
        }
    }
    """.formatted(packageName, className, className, className, className, className, className,
                className, className, className, className, className, className);

        Files.writeString(outputDir.resolve(className + "Parser.java"), src);
    }

    /**
     * An LALR(1) automaton derived from a context-free grammar.
     *
     * <p>The automaton is built once from the grammar and then used to
     * populate the ACTION and GOTO tables. It can also be printed for
     * debugging purposes.
     */
    public static class Automaton implements Iterable<State> {

        /** The edges of the automaton. */
        private final @NotNull Map<@NotNull State, @NotNull Map<@NotNull String, @NotNull State>> transitions;

        /** A map that encodes every state in an integer. */
        private final @NotNull Map<@NotNull State, @NotNull Integer> states_encoding;

        /** The set of all the states created in the map. */
        private final @NotNull Set<@NotNull State> states;

        /**
         * Creates an automaton with the given states and transitions.
         *
         * @param transitions    the labeled edges between states.
         * @param statesEncoding a mapping from each state to its numeric identifier.
         * @param states         the complete set of states.
         * @throws IllegalArgumentException if the state set or transitions are empty.
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
         * Returns the closure of the given set of LR(0) items under the grammar.
         *
         * @param grammar    the context-free grammar.
         * @param start      the initial set of items.
         * @param cache_item a shared cache ensuring item identity across the automaton.
         * @return the closure of {@code start}.
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
                            List<ContextFreeProduction> production = grammar.P.fromLHS(A);
                            for (ContextFreeProduction p : production) {
                                CoreItem new_item = CoreItem.fromProduction(p, 0);
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
         * Builds an LALR(1) automaton from a context-free grammar.
         *
         * <p>The grammar must be augmented: its start symbol must have exactly
         * one production whose left-hand side does not appear elsewhere.
         *
         * @param grammar the augmented context-free grammar.
         * @return the LALR(1) automaton for the grammar.
         * @throws IllegalArgumentException if the grammar is not augmented.
         */
        public static @NotNull Automaton fromGrammar(@NotNull ContextFreeGrammar grammar) {
            if (grammar.P.fromLHS(grammar.S).size() != 1)
                throw new IllegalArgumentException
                        ("The grammar should be augmented, this means the starting symbol should be wrapped by another symbol.");

            Map<State, Map<String, State>> transition = new HashMap<>();
            Set<State> states = new HashSet<>();
            Map<State, Integer> state_encoding = new HashMap<>();

            State first = automatonBuilding(grammar, transition, states, state_encoding);

            Map<StateItem, Set<StateItem>> propagate  = new HashMap<>();

            CoreItem start = CoreItem.fromProduction(grammar.P.fromLHS(grammar.S).getFirst(), 0);
            first.items().get(start).addLookahead("#");

            channelBuilding(grammar, states, propagate, transition);

            lookaheadsPropagation(propagate);

            return new Automaton(
                    transition,
                    state_encoding,
                    states
            );
        }

        /**
         * Constructs the LR(0) automaton for the given grammar.
         *
         * @param grammar        the context-free grammar.
         * @param transition     the edge map to populate.
         * @param states         the state set to populate.
         * @param state_encoding the state-to-integer map to populate.
         * @return the initial state of the automaton.
         */
        private static @NotNull State automatonBuilding(@NotNull ContextFreeGrammar grammar,
                                                        Map<State, Map<String, State>> transition,
                                                        Set<State> states,
                                                        Map<State, Integer> state_encoding) {
            Map<CoreItem, CoreItem> cache_item = new HashMap<>();
            Map<State, State> cache_state = new HashMap<>();
            Queue<State> queue = new ArrayDeque<>();
            Set<State> seen = new HashSet<>();

            CoreItem curr = CoreItem.fromProduction(grammar.P.fromLHS(grammar.S).getFirst(), 0);
            cache_item.put(curr, curr);

            State s = State.fromCore(closure(grammar, Set.of(curr), cache_item));
            cache_state.put(s, s);
            State first = s;
            queue.add(s);
            seen.add(s);

            while (!queue.isEmpty()) {
                s = queue.remove();
                for (String symb : grammar) {

                    Set<CoreItem> v = new HashSet<>();
                    for (CoreItem it : s) {
                        if (!it.isComplete() && it.afterDot().equals(symb)) {
                            v.add(it.advanceDot());
                        }
                    }

                    if (!v.isEmpty()) {
                        State new_state = State.fromCore(closure(grammar, v, cache_item));
                        if (seen.add(new_state)) {
                            queue.add(new_state);
                            cache_state.put(new_state,new_state);
                        } else {
                            new_state = cache_state.get(new_state);
                        }

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
         * Computes lookahead propagation channels for each item in the automaton,
         * upgrading the LR(0) automaton to LALR(1).
         *
         * @param grammar    the context-free grammar.
         * @param states     the states of the LR(0) automaton.
         * @param propagate  the propagation map to populate.
         * @param transition the transitions of the automaton.
         */
        private static void channelBuilding(@NotNull ContextFreeGrammar grammar,
                                            @NotNull Set<@NotNull State> states,
                                            @NotNull Map<@NotNull StateItem, @NotNull Set<@NotNull StateItem>> propagate,
                                            @NotNull Map<@NotNull State, @NotNull Map< @NotNull String, @NotNull State>> transition) {
            for (State state : states) {
                for (CoreItem item : state) {
                    if (!item.isComplete()) {

                        String B = item.afterDot();

                        if (grammar.N.contains(B)) {
                            /*
                            * If the symbol after the dot is non-terminal then we iterate on every core item
                            * of the state and if the lhs of the item1 is equal to B then we can:
                            *   1. Add the first of the item into the lookaheads of item to the LALR(1) item of item1,
                            *      if item1 has the first of beta.
                            *   2. Create a channel that goes from item to item1.
                            */
                            for (CoreItem item1 : state) {
                                if (item1.lhs().equals(B)) {
                                    if (item.hasFirstOfBeta()) {
                                        Set<String> lookahead_spont = grammar.first(item.firstOfBeta());
                                        state.items().get(item1).addAllLookaheads(lookahead_spont);
                                    } else {
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

                for (CoreItem item : state) {
                    /*
                    * If the item in the state is not complete then we add a transition that goes from
                    * the item in the current state to the state that has the advanced item.
                    */
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
         * Propagates lookaheads through the automaton until a fixed point is reached.
         *
         * @param propagate the propagation channels between LALR(1) items.
         */
        private static void lookaheadsPropagation(Map<StateItem, Set<StateItem>> propagate) {
            boolean changes = true;
            while (changes) {
                /*
                * Until no lookaheads are propagated the algorithm continue
                * to propagate through all the channels the lookaheads.
                */
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

        private static void printPropagationGraph(@NotNull Map<@NotNull StateItem, @NotNull Set<@NotNull StateItem>> propagate,
                                                  @NotNull Map<@NotNull State, @NotNull Integer> states_encoding) {
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

            sb.append("States:\n");
            for (State s : states) {
                int id = states_encoding.getOrDefault(s, -1);
                sb.append("I").append(id).append(":\n");

                for (CoreItem it : s) {
                    sb.append("  ").append(s.items().get(it)).append("\n");
                }
                sb.append("\n");
            }

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