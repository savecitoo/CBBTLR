package ast;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Models an abstract syntax tree node.
 */
public interface AstNode {

    /** Source position: line where this node starts (1-based). */
    int line();

    /** Source position: column where this node starts (0-based). */
    int column();

    /** Ordered list of condition nodes (leaves return an empty list). */
    @NotNull List< @NotNull AstNode> children();


    /**
     * Accept a visitor.
     * @param visitor the visitor {@code this} node must accept.
     * @return the resulting object from the visit.
     * @param <T> the type of the visitor.
     */
    default <T> T accept(AstVisitor<T> visitor) {
        throw new UnsupportedOperationException("accept() not supported.");
    }

    /**
     * This method permits to have a good view of the Abrstact Sintax Tree rooted in {@code this} node.
     * @return a string representing the AST.
     */
    default String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        prettyPrint(sb, "", true);
        return sb.toString();
    }

    private void prettyPrint(StringBuilder sb, String prefix, boolean isLast) {

        sb.append(prefix)
                .append(isLast ? "└── " : "├── ")
                .append(this)
                .append('\n');

        List<AstNode> kids = children();

        for (int i = 0; i < kids.size(); i++) {
            boolean last = i == kids.size() - 1;

            kids.get(i).prettyPrint(
                    sb,
                    prefix + (isLast ? "    " : "│   "),
                    last
            );
        }
    }
}