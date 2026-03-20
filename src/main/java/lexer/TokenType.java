package lexer;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TokenType {

    /**
     * This field represent the token identifier.
     */
    public final @NotNull String identifier;

    /**
     * This field is the regular expression of the TokenType.
     */
    public final @NotNull String regex;

    /**
     * This is the patter built from the regex.
     */
    public final @NotNull Pattern pattern;

    /**
     * If this token must be skipped in the tokenization process.
     */
    public final boolean skip;

    /**
     * This is a constructor for a Token Type.
     * @param identifier the identifier of the Token Type.
     * @param regex the regular expression recognized by the Token Type.
     * @param skip if the TokenType is a skip token.
     * @throws IllegalArgumentException if the identifier or the regular expression is blank, or if the regex
     * is not correct.
     */
    public TokenType(@NotNull String identifier, @NotNull String regex, boolean skip) throws IllegalArgumentException {
        if (identifier.isBlank())
            throw new IllegalArgumentException("The identifier cannot be blank");
        if (regex.isBlank())
            throw new IllegalArgumentException("The regex cannot be blank");
        this.identifier = identifier;
        this.regex = regex;
        try {
            this.pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid regex: " + e.getMessage(), e);
        }
        this.skip = skip;
    }

    /**
     * This method starting from a string returns an instance of the corresponding TokenType.<br>
     * To be recognized you have to write the string in following way:<br>
     *      [a-zA-Z0-9_]+ : (.+?)(?:+(skip))?
     * If the TokenType must be skipped write it after the regex.
     * @param line the line that describes the TokenType.
     * @return the corresponding TokenType.
     * @throws IllegalArgumentException if the string is not well-formed.
     */
    public static @NotNull TokenType fromString(@NotNull String line) throws IllegalArgumentException {

        // (.+?) is a lazy group and not a greedy one, so the skip is not captured by this group.
        Matcher matcher = Pattern.compile("\\s*([a-zA-Z0-9_]+)\\s*:\\s*(.+?)(?:\\s+(skip))?\\s*").matcher(line);
        if (matcher.matches()){
            return new TokenType(
                    matcher.group(1),
                    matcher.group(2),
                    matcher.group(3) != null
            );
        }
        throw new IllegalArgumentException("The line specified for the token is not well formed.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof TokenType t){
            return t.skip == skip && t.identifier.equals(identifier) && t.regex.equals(regex);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(skip, identifier, regex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("identifier: ").append(identifier).append(" ");
        sb.append(regex);
        if (skip) {
            sb.append(" skip");
        }
        return sb.toString();
    }

}