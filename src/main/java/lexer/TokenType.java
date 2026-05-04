package lexer;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @param identifier This field represent the token identifier.
 * @param regex      This field is the regular expression of the TokenType.
 * @param skip       If this token must be skipped in the tokenization process.
 */
public record TokenType(@NotNull String identifier, @NotNull String regex, boolean skip) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * This is a constructor for a Token Type.
     *
     * @param identifier the identifier of the Token Type.
     * @param regex      the regular expression recognized by the Token Type.
     * @param skip       if the TokenType is a skip token.
     * @throws IllegalArgumentException if the identifier or the regular expression is blank, or if the regex
     *                                  is not correct.
     */
    public TokenType(@NotNull String identifier, @NotNull String regex, boolean skip) {
        if (identifier.isBlank())
            throw new IllegalArgumentException("The identifier cannot be blank");
        if (regex.isBlank())
            throw new IllegalArgumentException("The regex cannot be blank");
        this.identifier = identifier;
        this.regex = regex;
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex: " + e.getMessage(), e);
        }
        this.skip = skip;
    }

    public Pattern getPattern() {
        return Pattern.compile(regex);
    }

    /**
     * This method starting from a string returns an instance of the corresponding TokenType.<br>
     * To be recognized you have to write the string in following way:<br>
     * [a-zA-Z0-9_]+ : (.+?)(?:+(skip))?
     * If the TokenType must be skipped write it after the regex.
     *
     * @param line the line that describes the TokenType.
     * @return the corresponding TokenType.
     * @throws IllegalArgumentException if the string is not well-formed.
     */
    public static @NotNull TokenType fromString(@NotNull String line) throws IllegalArgumentException {

        // (.+?) is a lazy group and not a greedy one, so the skip is not captured by this group.
        Matcher matcher = Pattern.compile("\\s*([a-zA-Z0-9_]+)\\s*:\\s*(.+?)(?:\\s+(skip))?\\s*").matcher(line);
        if (matcher.matches()) {
            return new TokenType(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3) != null
            );
        }
        throw new IllegalArgumentException("The line " + line + "  specified for the token is not well formed.");
    }

    @Override
    public @NotNull String toString() {
        StringBuilder sb = new StringBuilder().append("identifier: ").append(identifier).append(" ");
        sb.append(regex);
        if (skip) {
            sb.append(" skip");
        }
        return sb.toString();
    }

}