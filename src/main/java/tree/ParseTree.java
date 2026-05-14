package tree;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is an implementation for a Parsing Tree.
 */
public class ParseTree implements Iterable<ParseTree> {

    public final @NotNull String symbol;

    private final @NotNull List<@NotNull ParseTree> children;

    public boolean isTerminal;

    public final int line;

    public final int col;

    public final @NotNull String rule_tag;

    /**
     * This is a constructor for a parsing tree with no children, i.e. a leaf.
     * @param symbol the symbol in the root node.
     * @param isTerminal if the symbol is terminal or not.
     * @param children the children of the current node.
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