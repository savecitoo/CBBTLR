package utils;

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

    /**
     * This is a constructor for a parsing tree with no children, i.e. a leaf.
     * @param symbol the symbol in the root node.
     * @param isTerminal if the symbol is terminal or not.
     */
    public ParseTree(@NotNull String symbol, boolean isTerminal) {
        this.symbol = symbol;
        this.isTerminal = isTerminal;
        this.children = new ArrayList<>();
    }

    /**
     * This is a constructor for a parsing tree with children.
     * @param symbol the symbol in the root node.
     * @param children the children of the root.
     */
    public ParseTree(@NotNull String symbol, @NotNull List<@NotNull ParseTree> children) {
        this.symbol = symbol;
        this.isTerminal = children.isEmpty();
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
                    this.children.equals(tree.children);
        }
        return false;
    }

    @Override
    public String toString() {
        return toStringHelper("", "");
    }

    private String toStringHelper(String prefix, String childPrefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(symbol).append(isTerminal ? " [Terminal]" : "").append("\n");
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