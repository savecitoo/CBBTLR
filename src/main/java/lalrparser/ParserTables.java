package lalrparser;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the parsing tables required by an LALR(1) parser at runtime.
 *
 * <p>Implementations of this interface are typically generated automatically
 * by the parser generator from a ContextFreeGrammar, and contain
 * all the information needed to drive the LR parsing algorithm without
 * retaining the original grammar object.</p>
 *
 * <p>The parsing tables consist of:
 * <ul>
 *   <li><b>ACTION</b>: determines what action to take given a state and a terminal symbol</li>
 *   <li><b>GOTO</b>: determines the next state after a reduction, given a state and a non-terminal</li>
 *   <li><b>PRODUCTIONS</b>: encodes each production rule as {lhs_id, rhs_length}, used during reductions</li>
 *   <li><b>PROD_TAGS</b>: the production tag associated with each rule.</li>
 *   <li><b>SYMBOLS</b>: decodes each symbol id used in the tables to the corresponding terminal or non-terminal.</li>
 * </ul>
 */
public interface ParserTables {

    /**
     * Returns the ACTION table used by the LALR(1) parser.
     *
     * <p>{@code ACTION[state][symbol_id]} encodes the action to take:</p>
     * <ul>
     *   <li>{@code {0, s}} — SHIFT to state {@code s}</li>
     *   <li>{@code {1, p}} — REDUCE using production {@code p}</li>
     *   <li>{@code {2}}    — ACCEPT</li>
     *   <li>{@code null}   — no action (syntax error)</li>
     * </ul>
     *
     * @return a 2D array of int arrays indexed by [state][symbol_id],
     *         where null entries represent missing (error) entries
     */
    int @NotNull [] @NotNull [] @NotNull [] getACTION();

    /**
     * Returns the GOTO table used by the LALR(1) parser.
     *
     * <p>{@code GOTO[state][symbol_id]} contains the next state to transition to
     * after reducing to the non-terminal identified by {@code symbol_id}.
     *
     * @return a 2D array indexed by [state][symbol_id].
     */
    int @NotNull [] @NotNull [] getGOTO();

    /**
     * Returns the production rules encoded for use during reductions.
     *
     * <p>{@code PRODUCTIONS[production_id]} is an array {@code {lhs_id, rhs_length}} where:</p>
     * <ul>
     *   <li>{@code lhs_id} is the symbol ID of the left-hand side non-terminal</li>
     *   <li>{@code rhs_length} is the number of symbols in the right-hand side,
     *       i.e. how many elements to pop from the stack during a reduction</li>
     * </ul>
     * The index of each production corresponds exactly to the production ID
     * used in the ACTION table's REDUCE entries.
     *
     * @return a 2D array indexed by [production_id], each entry being {lhs_id, rhs_length}
     */
    int @NotNull [] @NotNull [] getPRODUCTIONS();

    /**
     * Returns the symbol array used to encode terminals and non-terminals
     * in the ACTION and GOTO tables.
     *
     * <p>The array maps each integer id to its corresponding symbol string.
     * Specifically, {@code getSymbols()[id]} returns the symbol whose encoding
     * is {@code id}. This encoding is consistent with the indices used in
     * {@link #getACTION()} and {@link #getGOTO()}: if a terminal {@code t}
     * has id {@code k}, then {@code ACTION[s][k]} holds the action for state
     * {@code s} on input {@code t}, and similarly for non-terminals in GOTO.
     *
     * <p>The array must contain all terminals, all non-terminals, and the
     * end-of-input marker {@code "#"}, in the order they were assigned ids
     * during table generation. The array is treated as read-only by the parser.
     *
     * @return a non-null, non-empty array of symbol strings indexed by their id.
     */
    @NotNull String @NotNull[] getSYMBOLS();

    /**
     * Returns the production tags associated with each production rule.
     *
     * <p>{@code PROD_TAGS[production_id]} is the tag string attached to the
     * production at that index, where the index corresponds exactly to the
     * production ID used in the ACTION table's REDUCE entries and in
     * {@link #getPRODUCTIONS()}.</p>
     *
     * <p>Productions that carry no tag are represented by an empty string
     * {@code ""}.</p>
     *
     * @return a non-null array indexed by [production_id], each entry being
     *         the tag string of the corresponding production.
     */
    @NotNull String @NotNull [] getPROD_TAGS();
}
