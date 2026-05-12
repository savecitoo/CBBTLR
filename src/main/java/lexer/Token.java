package lexer;

import org.jetbrains.annotations.NotNull;

/**
 * This record represent a token produced by the lexer.
 * @param token_id the ID of the token in the SymbolTable.
 * @param value the token lexeme.
 * @param line the line where the token appeared.
 * @param col the column where the token appeared.
 */
public record Token(@NotNull Integer token_id, @NotNull String value,
                    int line, int col) {
    public static Token createEOFToken(int eofId) { return new Token(eofId, "#", -1, -1); }
}
