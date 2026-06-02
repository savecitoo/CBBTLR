package ast;

import tree.ParseTree;

import java.util.List;

public class CstToAstVisitor {

    public static AstNode visit(ParseTree node) {
        return switch (node.rule_tag) {
            case "binary_expr"  -> visitBinExpr(node);
            case "unary_expr"   -> visitUnaryPlus(node);
            case "group"        -> visitGroup(node);
            case "identifier"   -> visitIdentifier(node);
            case "int_literal"  -> visitIntLiteral(node);
            case ""          -> visit(node.getChildren().getFirst());
            default          -> throw new IllegalStateException("Unknown rule_tag: " + node.rule_tag);
        };
    }

    private static AstNode visitIntLiteral(ParseTree node) {
        return new IntNode(node.getChildren().getFirst().symbol, node.col, node.line);
    }

    private static AstNode visitIdentifier(ParseTree node) {
        return new IdenfierNode(node.getChildren().getFirst().symbol, node.col, node.line);
    }

    private static AstNode visitGroup(ParseTree node) {
        return visit(node.getChildren().get(1));
    }

    private static AstNode visitBinExpr(ParseTree node) {
        List<ParseTree> children = node.getChildren();
        AstNode left  = visit(children.get(0));
        String  op    = children.get(1).symbol;
        AstNode right = visit(children.get(2));
        return new BinExprNode(op, left, right, node.line, node.col);
    }

    private static AstNode visitUnaryPlus(ParseTree node) {
        AstNode child = visit(node.getChildren().get(1));
        return new UnarExprNode(node.getChildren().getFirst().symbol, child , node.line, node.col);
    }

}