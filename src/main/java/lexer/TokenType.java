package lexer;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Describes a single token type that a {@link Lexer} can recognize.
 *
 * <p>A token type is defined by three properties:
 * <ul>
 *   <li><b>identifier</b> — a unique name used to refer to this token type (e.g. {@code "NUMBER"}).</li>
 *   <li><b>regex</b> — the Java regular expression whose matches are recognized as this token.</li>
 *   <li><b>skip</b> — when {@code true}, matches are consumed from the input but not emitted
 *       as {@link Token} objects (useful for whitespace or comments).</li>
 * </ul>
 *
 * <p>Both {@code identifier} and {@code regex} must be non-blank, and {@code regex} must be
 * a valid Java regular expression; the constructor enforces these invariants eagerly.
 *
 * <p>Instances are compared by value (record semantics) and are serializable.
 *
 * @param identifier unique name of this token type; must not be blank.
 * @param regex      Java regular expression matched by this token type; must not be blank
 *                   and must be a valid pattern.
 * @param skip       {@code true} if matches should be consumed but not emitted.
 *
 * @see Lexer
 */
public record TokenType(@NotNull String identifier, @NotNull String regex, boolean skip)
        implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@code TokenType} and validates its arguments.
     *
     * @param identifier unique name of this token type; must not be blank.
     * @param regex      Java regular expression for this token type; must not be blank.
     * @param skip       {@code true} if the lexer should consume but not emit this token.
     * @throws IllegalArgumentException if {@code identifier} or {@code regex} is blank,
     *                                  or if {@code regex} is not a valid Java regular expression.
     */
    public TokenType {
        if (identifier.isBlank())
            throw new IllegalArgumentException("The identifier cannot be blank");
        if (regex.isBlank())
            throw new IllegalArgumentException("The regex cannot be blank");
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex: " + e.getMessage(), e);
        }
    }

    /**
     * Returns a compiled {@link Pattern} for this token type's regular expression.
     *
     * <p><b>Note:</b> the pattern is recompiled on every call. Callers that invoke
     * this method repeatedly in a hot path should cache the result.
     *
     * @return a freshly compiled {@link Pattern} equivalent to {@code Pattern.compile(regex())}.
     */
    public Pattern getPattern() {
        return Pattern.compile(regex);
    }

    /**
     * Parses a single specification line and returns the corresponding {@code TokenType}.
     *
     * <p>The expected format is:
     * <pre>
     *     identifier : regex
     *     identifier : regex skip
     * </pre>
     * where {@code identifier} matches {@code [a-zA-Z0-9_]+}, {@code regex} is any
     * non-empty string, and the optional trailing {@code skip} keyword marks the token
     * type as a skip token. Leading and trailing whitespace is ignored.
     *
     * @param line the specification line to parse; must not be {@code null}.
     * @return the {@code TokenType} described by {@code line}.
     * @throws IllegalArgumentException if {@code line} does not match the expected format,
     *                                  or if the extracted regex is invalid.
     */
    public static @NotNull TokenType fromString(@NotNull String line) {
        // The regex group (.+?) is intentionally lazy so the optional trailing
        // "skip" keyword is not captured as part of the token regex.
        Matcher matcher = Pattern
                .compile("\\s*([a-zA-Z0-9_+\\-*/^()\\s]+?)\\s*:\\s*(.+?)(?:\\s+(skip))?\\s*")
                .matcher(line);
        if (matcher.matches()) {
            return new TokenType(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3) != null);
        }
        throw new IllegalArgumentException(
                "Malformed token specification: '" + line + "'. " +
                        "Expected format: 'identifier : regex [skip]'");
    }

    /**
     * Returns a string representation in the same format accepted by {@link #fromString}.
     *
     * @return a string of the form {@code "identifier: <id> <regex> [skip]"}.
     */
    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder()
                .append("identifier: ").append(identifier).append(" ")
                .append(regex);
        if (skip) sb.append(" skip");
        return sb.toString();
    }
}