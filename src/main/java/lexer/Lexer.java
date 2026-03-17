package lexer;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class implements a Lexer.
 */
public class Lexer {

    /** This field keeps saved all the tokens that the lexer recognize. */
    private final @NotNull Set<@NotNull TokenType> tokens;

    /**
     * This is a constructor for a Lexer starting from a set of TokenType.
     * @param tokens the set of tokens that the Lexer will recognize.
     */
    public Lexer(@NotNull Set<@NotNull TokenType> tokens) {
        this.tokens = Set.copyOf(tokens);
    }

    /**
     * This method starting from a string returns a List of tokens recognized by the string.
     * @param tokenized the string that has to be transformed.
     * @return a list of tokens representing the string.
     * @throws NoSuchElementException if no TokenType reflects a substring of the input.
     */
    public @NotNull List<@NotNull Token> tokenize(@NotNull String tokenized) throws NoSuchElementException {
        return List.of();
    }

    /**
     * This method starting from a string returns the lexer specified by the string. <br>
     * TODO: descrizione di come va fatto il lexer
     * @param lexer the string that describes the lexer.
     * @return the lexer represented by the string.
     * @throws IllegalArgumentException if the string is not well-formed.
     */
    public static @NotNull Lexer fromString(@NotNull String lexer) throws IllegalArgumentException {
        // TODO: implement fromString
        return new Lexer(new HashSet<>());
    }

}
