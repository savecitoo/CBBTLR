package lexer;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class implements a Lexer.
 */
public class Lexer {

    /** This field keeps saved all the tokens that the lexer recognize. */
    private final @NotNull Set<@NotNull TokenType> tokens;

    /**
     * This field contains the token's priority.
     */
    private final @NotNull TokenRegister reg = new TokenRegister();

    /**
     * This is a constructor for a Lexer starting from a set of TokenType.<br>
     * The order of the tokens will be their priority in case of two TokenTypes recognizes the same string.
     * @param tokens the list of tokens that the Lexer will recognize.
     * @throws IllegalArgumentException if the same token appears more than once.
     */
    public Lexer(@NotNull List<@NotNull TokenType> tokens) {
        Set<String> seen = new HashSet<>();
        for (TokenType tt : tokens) {
            if (!seen.add(tt.identifier))
                throw new IllegalArgumentException("The identifier " + tt.identifier + " appears more than once");
            reg.add(tt);
        }
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
     * Every token has to be specified with the following scheme, after a token you have to put
     * a new line to add another one:<br>
     * TokenType : regex (skip)?<br>
     * The addition of skip is necessary if the lexer has to skip the regular expression recognized by the Token.<br>
     * If some token can recognize the same word it will be assigned to the first one written in the string.
     * @param lexer the string that describes the lexer.
     * @return the lexer represented by the string.
     * @throws IllegalArgumentException if the string is not well-formed or if an identifier appears more than once.
     */
    public static @NotNull Lexer fromString(@NotNull String lexer) throws IllegalArgumentException {
        List<TokenType> tokens = new LinkedList<>();
        Set<String> identifiers = new HashSet<>();
        Scanner s = new Scanner(lexer);
        while (s.hasNext()){
            TokenType t = TokenType.fromString(s.nextLine());
            if (!identifiers.add(t.identifier))
                throw new IllegalArgumentException("This identifier " + t.identifier + "appears twice in the string.");
            tokens.add(t);
        }
        s.close();
        return new Lexer(tokens);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Lexer lexer) {
            return reg.equals(lexer.reg) && tokens.equals(lexer.tokens);
        }
        return false;
    }
}
