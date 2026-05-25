package lexer;

import org.jetbrains.annotations.NotNull;
import utils.SymbolIdentifier;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;

/**
 * A lexer (tokenizer) that splits an input string into a sequence of {@link Token} objects
 * according to a set of {@link TokenType} rules.
 *
 * <p>Each {@link TokenType} defines a regular expression and an identifier. When multiple
 * token types can match the same substring starting at the current position, the lexer
 * applies a two-level disambiguation strategy:
 * <ol>
 *   <li><b>Longest match</b>: the token type consuming the most characters wins.</li>
 *   <li><b>Priority</b>: if two types match the same length, the one with the lower
 *       priority value wins.</li>
 * </ol>
 *
 * <p>Token types marked as {@link TokenType#skip() skip} are consumed from the input
 * but not added to the output list (useful for whitespace or comments).
 *
 * <p>Token identifiers are interned into a {@link SymbolIdentifier} so that identity
 * comparison can be used instead of string equality during subsequent processing.
 *
 *
 * <h2>Example usage</h2>
 * <pre>{@code
 * SymbolTable<String> table = new SymbolTable<>();
 * Lexer lexer = Lexer.fromString("""
 *     NUMBER : [0-9]+
 *     PLUS   : \\+
 *     WS     : \\s+ skip
 *     """, table);
 * List<Token> tokens = lexer.tokenize("3 + 42");
 * }</pre>
 */
public class Lexer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The complete set of token types this lexer recognizes.
     * Stored as an unmodifiable set; order-sensitive priority information
     * is kept separately in {@link #reg}.
     */
    private final @NotNull Set<@NotNull TokenType> tokens;

    /**
     * Symbol table used to intern token-type identifiers.
     */
     /*
     * Interning allows O(1) identity comparisons in downstream passes
     * instead of O(n) string equality checks.
     */
    private final @NotNull SymbolIdentifier<@NotNull String> table;

    /**
     * Maps each {@link TokenType} to its declaration-order priority.
     * Lower values mean higher priority; used to break ties when two
     * token types produce a match of equal length.
     */
    private final @NotNull TokenRegister reg = new TokenRegister();

    /**
     * Creates a {@code Lexer} from an ordered list of token types and a symbol table.
     *
     * <p>The position of each {@link TokenType} in {@code tokens} determines its
     * priority: index {@code 0} has the highest priority and wins all tie-breaks
     * against later entries.
     *
     * <p>Every token-type identifier is added to {@code table} if not already present,
     * so that {@link Token} objects produced by {@link #tokenize} carry interned IDs.
     *
     * @param tokens an ordered list of token types; must not contain duplicate identifiers.
     * @param table  the symbol table used to intern token-type identifiers.
     * @throws IllegalArgumentException if any identifier appears more than once in {@code tokens}.
     */
    public Lexer(@NotNull List<@NotNull TokenType> tokens, @NotNull SymbolIdentifier<@NotNull String> table) {
        Set<String> seen = new HashSet<>();
        for (TokenType tt : tokens) {
            if (!seen.add(tt.identifier()))
                throw new IllegalArgumentException(
                        "The identifier " + tt.identifier() + " appears more than once");
            reg.add(tt);
            if (!table.contains(tt.identifier()))
                table.add(tt.identifier());
        }
        this.table = table;
        this.tokens = Set.copyOf(tokens);
    }

    /**
     * Tokenizes the entire input string and returns the resulting token list.
     *
     *
     * @param tokenized the input string to tokenize; must not be {@code null}.
     * @return an ordered, mutable list of tokens representing the full input.
     * @throws NoSuchElementException if any position in {@code tokenized} is not
     *                                matched by any registered token type.
     */
    public @NotNull List<@NotNull Token> tokenize(@NotNull String tokenized) {
        List<Token> tokenList = new LinkedList<>();

        // Build one Matcher per TokenType upfront; regions are adjusted per iteration
        // so the underlying Pattern is compiled only once per call.
        Map<TokenType, Matcher> matchers = new HashMap<>();
        for (TokenType tt : tokens) {
            matchers.put(tt, tt.getPattern().matcher(tokenized));
        }

        int line = 1;
        int col  = 1;

        for (int i = 0; i < tokenized.length(); ) {
            TokenType bestType   = null;
            int       bestLength = 0;

            // Find the token type whose pattern matches the longest prefix at position i.
            // Ties are broken by the declaration-order priority stored in `reg`.
            for (TokenType tt : tokens) {
                Matcher matcher = matchers.get(tt);
                matcher.region(i, tokenized.length());

                if (matcher.lookingAt()) {
                    int len = matcher.end() - i;

                    if (len > bestLength ||
                            (len == bestLength &&
                                    (bestType == null || reg.priority(tt) < reg.priority(bestType)))) {
                        bestType   = tt;
                        bestLength = len;
                    }
                }
            }

            // No token type matched at position i — the input is not fully covered.
            if (bestType == null) {
                throw new NoSuchElementException(
                        "No token type matches input at position " + i +
                                " (line " + line + ", col " + col + "): '" +
                                tokenized.substring(i, Math.min(i + 20, tokenized.length())) + "'");
            }

            String consumed = tokenized.substring(i, i + bestLength);

            // Skip tokens (e.g. whitespace) are consumed but not added to the output list.
            if (!bestType.skip()) {
                tokenList.add(new Token(
                        table.getId(bestType.identifier()), consumed, line, col));
            }

            // Update line/column tracking character by character.
            for (char c : consumed.toCharArray()) {
                if (c == '\n') {
                    line++;
                    col = 1;
                } else {
                    col++;
                }
            }
            i += bestLength;
        }
        return tokenList;
    }

    /**
     * Parses a plain-text lexer specification and returns the corresponding {@code Lexer}.
     *
     * <p>The specification consists of one token-type definition per line, in the form:
     * <pre>
     *     Identifier : regex
     *     Identifier : regex skip
     * </pre>
     * where {@code Identifier} is the token name, {@code regex} is a Java regular
     * expression, and the optional {@code skip} keyword instructs the lexer to consume
     * but discard matches (e.g. for whitespace or comments).
     *
     * <p>Lines are processed in order; earlier lines have higher priority when two
     * patterns match a string of equal length.
     *
     * <p>Leading and trailing whitespace in the specification string is ignored.
     *
     * @param lexer the plain-text specification string; must not be {@code null}.
     * @param table the symbol table used to intern token-type identifiers.
     * @return a fully configured {@code Lexer} representing the specification.
     * @throws IllegalArgumentException if any line is malformed, or if the same
     *                                  identifier appears more than once.
     */
    public static @NotNull Lexer fromString(
            @NotNull String lexer,
            @NotNull SymbolIdentifier<@NotNull String> table) {

        List<TokenType> tokenList   = new LinkedList<>();
        Set<String>     identifiers = new HashSet<>();
        Scanner         s           = new Scanner(lexer.trim());

        while (s.hasNextLine()) {
            TokenType t = TokenType.fromString(s.nextLine());
            if (!identifiers.add(t.identifier()))
                throw new IllegalArgumentException(
                        "Identifier '" + t.identifier() + "' appears twice in the specification.");
            tokenList.add(t);
        }
        s.close();
        return new Lexer(tokenList, table);
    }

    /**
     * Two {@code Lexer} instances are equal if and only if they have the same token set and
     * the same priority register.
     *
     * @param o the object to compare.
     * @return {@code true} if {@code o} is a {@code Lexer} equal to this one.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Lexer other) {
            return reg.equals(other.reg)
                    && tokens.equals(other.tokens);
        }
        return false;
    }
}