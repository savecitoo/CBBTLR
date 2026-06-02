package tree;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This is an implementation for a Parsing Tree.
 */
public class ParseTree implements Iterable<ParseTree> {

    /** The symbol of the grammar that generated this node. */
    public final @NotNull String symbol;

    /** The children of the current node, it could be an empty list.  */
    private final @NotNull List<@NotNull ParseTree> children;

    /** The information that tell if this node is terminal. */
    public final boolean isTerminal;

    /** The line where the first character of the token is found. */
    public final int line;

    /** The column where the first character of the token is found. */
    public final int col;

    /** The rule that represents the semantic meaning of the grammar rule that generates
     * this node and its children. */
    public final @NotNull String rule_tag;

    /**
     * This is a constructor for a parsing tree with no children, i.e. a leaf.
     * @param symbol the symbol in the root node.
     * @param isTerminal if the symbol is terminal or not.
     * @param children the children of the current node.
     * @param line the line where the first character of  current node is found in the text.
     * @param col the column where the first character of  current node is found in the text.
     * @param expr_tag the tag of the rule that generated this nodes and its eventual children.
     */
    public ParseTree(@NotNull String symbol, boolean isTerminal, @NotNull List<@NotNull ParseTree> children,
                     int line, int col, @NotNull String expr_tag) {
        this.symbol = symbol;
        this.isTerminal = isTerminal;
        this.line = line;
        this.col = col;
        this.rule_tag = expr_tag;
        this.children = children;
    }

    public @NotNull List<@NotNull ParseTree> getChildren() {
        return List.copyOf(children);
    }

    @Override
    public @NotNull Iterator<ParseTree> iterator() {
        return children.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof ParseTree tree) {
            return this.symbol.equals(tree.symbol) &&
                    this.isTerminal == tree.isTerminal &&
                    this.children.equals(tree.children) &&
                    this.col == tree.col && this.line == tree.line &&
                    this.rule_tag.equals(tree.rule_tag);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, isTerminal, children, line, col, rule_tag);
    }

    @Override
    public String toString() {
        return toStringHelper("", "");
    }

    private String toStringHelper(String prefix, String childPrefix) {
        StringBuilder sb = new StringBuilder();
        String label = rule_tag.isEmpty()
                ? symbol
                : "(" + rule_tag + ") " + symbol;
        sb.append(prefix).append(label).append(isTerminal ? " [Terminal]" : "").append("\n");
        for (int i = 0; i < children.size(); i++) {
            ParseTree child = children.get(i);
            if (i < children.size() - 1) {
                sb.append(child.toStringHelper(childPrefix + "├── ", childPrefix + "│   "));
            } else {
                sb.append(child.toStringHelper(childPrefix + "└── ", childPrefix + "    "));
            }
        }
        return sb.toString();
    }

}