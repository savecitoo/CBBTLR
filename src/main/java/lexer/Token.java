package lexer;

import org.jetbrains.annotations.NotNull;

/**
 * This record represent a token produced by the lexer.
 * @param type the token type.
 * @param value the token lexeme.
 */
public record Token(@NotNull TokenType type, @NotNull String value) {

}
