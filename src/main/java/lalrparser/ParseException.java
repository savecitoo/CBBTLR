package lalrparser;

import java.util.List;

/**
 * Exception thrown when the LALR(1) parser encounters a token that is not valid
 * in the current parser state.
 *
 * <p>This exception provides structured information about the parse error,
 * allowing users of the parser generator to build custom, human-readable
 * error messages rather than relying on a fixed format.
 *
 * <p>Example usage:
 * <pre>{@code
 * try {
 *     parser.parse(tokens);
 * } catch (ParseException e) {
 *     System.err.printf("Syntax error at line %d, column %d%n",
 *             e.getLine(), e.getColumn());
 *     System.err.printf("Got: %s, expected one of: %s%n",
 *             e.getUnexpectedToken(),
 *             String.join(", ", e.getExpectedTokens()));
 * }
 * }</pre>
 */
public class ParseException extends RuntimeException {

    /**
     * The string representation of the token that caused the parse error,
     * as returned by the lexer.
     */
    private final String unexpectedToken;

    /**
     * The list of token types that would have been valid in the current
     * parser state.
     */
    private final List<String> expectedTokens;

    /** The line number (1-based) where the unexpected token was found. */
    private final int line;

    /** The column number (1-based) where the unexpected token was found. */
    private final int column;

    /**
     * Constructs a new {@code ParseException} with full context about the error.
     *
     * @param message        a default human-readable description of the error
     * @param unexpectedToken the string representation of the token that caused the error
     * @param expectedTokens  the list of token type names that were valid at this point;
     *                        derived from the ACTION table of the LALR(1) parser
     * @param line           the 1-based line number of the unexpected token
     * @param column         the 1-based column number of the unexpected token
     */
    public ParseException(String message, String unexpectedToken,
                          List<String> expectedTokens, int line, int column) {
        super(message);
        this.unexpectedToken = unexpectedToken;
        this.expectedTokens = expectedTokens;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the string representation of the token that caused the parse error.
     *
     * @return the unexpected token as a string
     */
    public String getUnexpectedToken() {
        return unexpectedToken;
    }

    /**
     * Returns the list of token type names that were valid in the parser state
     * where the error occurred.
     *
     * <p>These correspond to the terminal symbols with a non-null entry in the
     * ACTION table row of the current LALR(1) state, and can be used to build
     * a descriptive error message for end users.
     *
     * @return an unmodifiable view of the expected token types; never {@code null}
     */
    public List<String> getExpectedTokens() {
        return expectedTokens;
    }

    /**
     * Returns the 1-based line number where the unexpected token was found.
     *
     * @return the line number of the parse error
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the 1-based column number where the unexpected token was found.
     *
     * @return the column number of the parse error
     */
    public int getColumn() {
        return column;
    }
}