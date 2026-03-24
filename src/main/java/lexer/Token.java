package lexer;

import org.jetbrains.annotations.NotNull;

/**
 * This record represent a token produced by the lexer.
 * @param token_id the ID of the token in the SymbolTable.
 * @param value the token lexeme.
 */
public record Token(@NotNull Integer token_id, @NotNull String value) {
}
