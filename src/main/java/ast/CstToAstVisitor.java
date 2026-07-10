package ast;

import ast.constant.*;
import ast.operation.*;
import ast.program.*;
import ast.expression.*;
import org.jetbrains.annotations.NotNull;
import tree.ParseTree;

import java.util.*;

/**
 * This is a utility class that help to go from a Concrete Sintax Tree of a CoDiNE program, returns the corresponding
 * Abstract Sintax Tree.<br>
 * In this transformation we check also that no global identifiers are  and that the type expressions,
 * the ones that we find in the function declaration are correct.<br>
 * Here we check also if the main function is declared.
 */
public class CstToAstVisitor {

    /**
     * Given a ParseTree of a CoDine program it returns the corresponding Abstract Sintax Tree.
     * @param node the parse tree.
     * @return the corresponding AST.
     * @throws IllegalArgumentException if the rule tag of the node is unknown.
     */
    public static AstNode visit(ParseTree node) throws IllegalArgumentException{
        return switch (node.rule_tag) {
            case "start", "item_list" -> visitStart(node);
            case "binary_expr"  -> visitBinExpr(node);
            case "unary_expr"   -> visitNotExpr(node);
            case "group"        -> visitGroup(node);
            case "identifier"   -> visitIdentifier(node);
            case "int"          -> visitIntLiteral(node);
            case "true"         -> new TrueNode(node.line, node.col);
            case "false"        -> new FalseNode(node.line, node.col);
            case "func_call"    -> visitFuncCall(node);
            case "num"          -> new NumNode(node.line, node.col);
            case ""             -> visit(node.getChildren().getFirst());
            default             -> throw new IllegalStateException("Unknown rule_tag: " + node.rule_tag);
        };
    }

    // ---------------------------------------------------------------
    // program -> item #start | program item #item_list
    // item    -> dim_item | unit_item | fun_item
    // ---------------------------------------------------------------


    /**
     * Given a ParseTree of a CoDiNE well-formed program returns its corresponding Abstract Sintax Tree.
     * @param node the ParseTree.
     * @return the ProgramNode, root of the AST.
     * @throws IllegalArgumentException if the node is not the starting node of the program.
     */
    public static @NotNull ProgramNode visitStart(@NotNull ParseTree node) throws IllegalArgumentException {
        if (!node.rule_tag.equals("item_list") && !node.rule_tag.equals("start"))
            throw new IllegalArgumentException("The given parse tree must have as a root a starting node.");
        Set<String> dimension      = new HashSet<>(); // simboli di unità di base (es. "m")
        Set<String> dimension_name = new HashSet<>(); // nomi di dimensione (es. "Lunghezza")
        Map<String, AstNode> unit  = new HashMap<>();
        Map<String, FunctionNode> functions = new HashMap<>();

        // Map useful to avoid that global_id are overridden.
        Map<String, String> global_id = new HashMap<>();

        visitProgramItems(node, dimension, dimension_name, unit, functions, global_id);
        if (!functions.containsKey("main"))
            throw new IllegalArgumentException("The program doesn't contain a \"main\" function.");
        return new ProgramNode(dimension, dimension_name, unit, functions, node.line, node.col);
    }

    // This method adds to the gloalIDs the string given
    // and checks if there is no collisions.
    private static void declareGlobal(
            @NotNull Map<String, String> global_id,
            @NotNull String name, @NotNull String kind,
            int line, int col) {
        String previous = global_id.putIfAbsent(name, kind);
        if (previous != null)
            throw new IllegalArgumentException(String.format(
                    "The %s '%s' at line %d and column %d was already declared in the program as a %s.",
                    kind, name, line, col, previous));
    }

    private static void visitProgramItems(
            @NotNull ParseTree node,
            @NotNull Set<String> dimension,
            @NotNull Set<String> dimension_name,
            @NotNull Map<String, AstNode> unit,
            @NotNull Map<String, FunctionNode> functions,
            @NotNull Map<String, String> global_id) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "start":
                // children.getFirst().getChildren().getFirst() -> every item has just one non-terminal in each
                // production.
                //item -> dim_item | unit_item | fun_item
                visitItem(children.getFirst().getChildren().getFirst(), dimension, dimension_name, unit, functions, global_id);
                break;
            case "item_list":
                visitProgramItems(children.getFirst(), dimension, dimension_name, unit, functions, global_id);
                visitItem(children.getLast().getChildren().getFirst(), dimension, dimension_name, unit, functions, global_id);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " for the program part.");
        }
    }

    private static void visitItem(
            @NotNull ParseTree node,
            @NotNull Set<String> dimension,
            @NotNull Set<String> dimension_name,
            @NotNull Map<String, AstNode> unit,
            @NotNull Map<String, FunctionNode> functions,
            @NotNull Map<String, String> global_id) {
        switch (node.rule_tag) {
            case "dim_item":
                visitDimItem(node, dimension, dimension_name, global_id);
                break;
            case "unit_item":
                visitUnitItem(node, dimension, unit, global_id);
                break;
            case "fun_item":
                visitFunItem(node, functions, dimension, unit, global_id);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " for a program item.");
        }
    }

    private static void visitDimItem(
            @NotNull ParseTree node,
            @NotNull Set<String> dimension,
            @NotNull Set<String> dimension_name,
            @NotNull Map<String, String> global_id) {
        List<ParseTree> children = node.getChildren();

        // dimension name
        ParseTree dim_name = children.get(1);
        declareGlobal(global_id, dim_name.symbol, "dimension", dim_name.line, dim_name.col);
        dimension_name.add(dim_name.symbol);

        // unit of the dimension.
        ParseTree dim_unit = children.get(4);
        dimension.add(dim_unit.symbol);
    }

    private static void visitUnitItem(
            @NotNull ParseTree node,
            @NotNull Set<String> dimension,
            @NotNull Map<String, AstNode> unit,
            @NotNull Map<String, String> global_id) {
        List<ParseTree> children = node.getChildren();

        ParseTree symb_name = children.get(1);
        String symb = symb_name.symbol;
        declareGlobal(global_id, symb, "unit", symb_name.line, symb_name.col);

        ParseTree unit_expr = children.get(3);

        unit.put(symb, visit(unit_expr));
    }

    private static void visitFunItem(
            @NotNull ParseTree node,
            @NotNull Map<String, FunctionNode> functions,
            @NotNull Set<String> dimension,
            @NotNull Map<String, AstNode> unit,
            @NotNull Map<String, String> global_id) {
        List<ParseTree> children = node.getChildren();
        ParseTree func_name = children.get(1);
        String name = func_name.symbol;
        declareGlobal(global_id, name, "function", func_name.line, func_name.col);

        // with this set we check if the variable are already defined in the function.
        Set<String> local_namespace = new HashSet<>();
        List<ParameterNode> par = new ArrayList<>();
        visitParameterList(children.get(3), par, dimension, unit, name, local_namespace, global_id);

        ParseTree returnTypeNode = children.get(6);
        checkNoIntegerLiterals(returnTypeNode,
                String.format("The return type of function %s", name),
                returnTypeNode.line, returnTypeNode.col);

        AstNode body = visitBody(children.get(8), local_namespace, name);

        functions.put(name, new FunctionNode(
                new IdentifierNode(name, func_name.line, func_name.col),
                par, visit(returnTypeNode), body, node.line, node.col));
    }

    // ---------------------------------------------------------------
    // Controlli semantici sulle espressioni di tipo/unit
    // ---------------------------------------------------------------


    private static void checkNoIntegerLiterals(@NotNull ParseTree node, @NotNull String context, int line, int col) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "int":
                throw new IllegalArgumentException(String.format(
                        "%s at line %d and column %d cannot contain integer literals outside of an exponent (^).",
                        context, line, col));
            case "binary_expr": {
                String op = children.get(1).symbol;
                if (!op.equals("^")) {
                    checkNoIntegerLiterals(children.get(0), context, line, col);
                    checkNoIntegerLiterals(children.get(2), context, line, col);
                }
                break;
            }
            case "group":
                checkNoIntegerLiterals(children.get(1), context, line, col);
                break;
            case "identifier", "num":
                break;
            case "":
                checkNoIntegerLiterals(children.getFirst(), context, line, col);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " inside a type type.");
        }
    }

    private static void visitParameterList(
            ParseTree node, List<ParameterNode> par,
            Set<String> dimension, Map<String, AstNode> unit, String func_name,
            Set<String> local_namespace,
            @NotNull Map<String, String> global_id) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "empty":
                break;
            case "start_parameter_list":
                visitParameterList(children.getFirst(), par, dimension, unit, func_name, local_namespace, global_id);
                break;
            case "parameter_list":
                if (children.size() == 1) {
                    visitParameterList(children.getFirst(), par, dimension, unit, func_name, local_namespace, global_id);
                } else {
                    visitParameterList(children.getFirst(), par, dimension, unit, func_name, local_namespace, global_id);
                    visitParameterList(children.getLast(), par, dimension, unit, func_name, local_namespace, global_id);
                }
                break;
            case "parameter_item": {
                ParseTree var_name = children.getFirst();
                if (!local_namespace.add(var_name.symbol)) {
                    throw new IllegalArgumentException(String.format(
                            "The parameter %s of function %s at line %d and column %d was already declared.",
                            var_name.symbol, func_name, var_name.line, var_name.col));
                } else if (global_id.containsKey(var_name.symbol)) {
                    throw new IllegalArgumentException(String.format(
                            "The parameter %s of function %s at line %d and column %d was already declared as " + global_id.get(var_name.symbol) +  ".",
                            var_name.symbol, func_name, var_name.line, var_name.col));
                }
                ParseTree type = children.get(2);
                checkNoIntegerLiterals(type,
                        String.format("The parameter %s of function %s", var_name.symbol, func_name),
                        var_name.line, var_name.col);
                par.add(new ParameterNode(
                        new IdentifierNode(var_name.symbol, var_name.line, var_name.col),
                        visit(type), node.line, node.col));
                break;
            }
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " for the parameter list part.");
        }
    }

    private static AstNode visitBody(ParseTree node, Set<String> local_namespace, String func_name) {
        List<AstNode> decl = new ArrayList<>();
        List<AstNode> stat = new ArrayList<>();
        visitDeclNode(node.getChildren().getFirst(), decl, local_namespace, func_name);
        visitStatNode(node.getChildren().getLast(), stat);
        return new BodyNode(decl, stat, node.line, node.col);
    }

    private static void visitDeclNode(@NotNull ParseTree node, List<AstNode> decl, Set<String> local_namespace,
                                      String func_name) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "empty":
                break;
            case "var_list":
                visitDeclNode(children.getFirst(), decl, local_namespace, func_name);
                visitDeclNode(children.getLast(), decl, local_namespace, func_name);
                break;
            case "var_item": {
                ParseTree var_name = children.get(1);
                if (!local_namespace.add(var_name.symbol)) {
                    throw new IllegalArgumentException(String.format(
                            "The variable %s in function %s at line %d and column %d was already declared.",
                            var_name.symbol, func_name, var_name.line, var_name.col));
                }

                decl.add(new DeclarationNode(
                        new IdentifierNode(var_name.symbol, var_name.line, var_name.col),
                        visit(children.getLast()),
                        node.line, node.col));
                break;
            }
            default:
                throw new IllegalArgumentException("Unexpected rule tag " + node.rule_tag + " for the variable declaration part.");
        }
    }

    private static void visitStatNode(@NotNull ParseTree node, List<AstNode> stat) {
        switch (node.rule_tag) {
            case "empty":
                break;
            case "instruction_list":
                visitStatNode(node.getChildren().getFirst(), stat);
                visitStatNode(node.getChildren().getLast(), stat);
                break;
            case "return":
                stat.add(visitReturnNode(node));
                break;
            case "if":
                stat.add(visitIfNode(node));
                break;
            case "while":
                stat.add(visitWhileNode(node));
                break;
            case "assignment":
                stat.add(visitAssignmentNode(node));
                break;
            default:
                throw new IllegalArgumentException("Unexpected rule tag " + node.rule_tag + " for the instruction part.");
        }
    }

    private static AstNode visitReturnNode(@NotNull ParseTree node) {
        return new ReturnNode(visit(node.getChildren().get(1)), node.line, node.col);
    }

    private static AstNode visitAssignmentNode(@NotNull ParseTree node) {
        List<ParseTree> children = node.getChildren();
        return new AssignmentNode(
                new IdentifierNode(children.get(0).symbol, children.get(0).line, children.get(0).col),
                visit(children.get(2)), node.line, node.col
        );
    }

    private static AstNode visitWhileNode(@NotNull ParseTree node) {
        List<ParseTree> children = node.getChildren();
        AstNode condition = visit(children.get(1));
        List<AstNode> instruction = new ArrayList<>();
        visitStatNode(children.get(3), instruction);
        return new WhileNode(condition, instruction, node.line, node.col);
    }

    private static AstNode visitIfNode(@NotNull ParseTree node) {
        List<ParseTree> children = node.getChildren();
        AstNode condition = visit(children.get(1));
        List<AstNode> true_branch = new ArrayList<>();
        visitStatNode(children.get(3), true_branch);
        List<AstNode> false_branch = new ArrayList<>();
        ParseTree else_node = children.get(5);
        if (else_node.rule_tag.equals("else")) {
            visitStatNode(else_node.getChildren().get(2), false_branch);
        }
        return new IfNode(condition, true_branch, false_branch, node.line, node.col);
    }

    // ---------------------------------------------------------------
    // Espressioni
    // ---------------------------------------------------------------

    private static @NotNull PowNode getPowNode(ParseTree node) {
        List<ParseTree> children = node.getChildren();
        IdentifierNode b = new IdentifierNode(children.getFirst().symbol, node.line, node.col);
        IntNode exp;
        ParseTree expNode = children.get(2);
        List<ParseTree> exp_children = expNode.getChildren();
        if (exp_children.size() == 1) {
            exp = new IntNode(Long.parseLong(exp_children.getFirst().symbol), expNode.line, expNode.col);
        } else {
            ParseTree int_name = exp_children.getLast();

            exp = new IntNode(- Long.parseLong(int_name.symbol), int_name.line, int_name.col);

        }
        return new PowNode(b, exp, node.line, node.col);
    }

    private static AstNode visitIntLiteral(ParseTree node) {
        // term -> INT #int | - INT #int
        List<ParseTree> children = node.getChildren();
        if (children.size() == 1) {
            ParseTree int_name = children.getFirst();
            return new IntNode(Long.parseLong(int_name.symbol), int_name.line, int_name.col);
        } else {
            ParseTree int_name = children.getLast();
            return new IntNode(-Long.parseLong(int_name.symbol), int_name.line, int_name.col);
        }
    }

    private static AstNode visitIdentifier(ParseTree node) {
        return new IdentifierNode(node.getChildren().getFirst().symbol, node.line, node.col);
    }

    private static AstNode visitGroup(ParseTree node) {
        return visit(node.getChildren().get(1));
    }

    private static AstNode visitFuncCall(ParseTree node) {
        List<ParseTree> children = node.getChildren();
        String func = children.getFirst().symbol;
        List<AstNode> args = new ArrayList<>();
        visitFuncCallArgs(children.get(2), args);
        return new FuncCallNode(
                new IdentifierNode(func, children.getFirst().line, children.getFirst().col),
                args, node.line, node.col);
    }

    private static void visitFuncCallArgs(ParseTree node, List<AstNode> args) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "empty":
                break;
            case "arg_list":
                if (children.size() == 1) {
                    visitFuncCallArgs(children.getFirst(), args);
                } else {
                    visitFuncCallArgs(children.getFirst(), args);
                    args.add(visit(children.getLast()));
                }
                break;
            case "arg_list_item":
                args.add(visit(children.getFirst()));
                break;
            default:
                throw new IllegalArgumentException("Unexpected rule tag for the func call arguments: " + node.rule_tag);
        }
    }

    private static AstNode visitBinExpr(ParseTree node) {
        List<ParseTree> children = node.getChildren();
        String op = children.get(1).symbol;
        if (op.equals("^")) {
            return getPowNode(node);
        }
        AstNode left = visit(children.get(0));
        AstNode right = visit(children.get(2));
        return switch (op) {
            case "+"   -> new AddNode(left, right, node.line, node.col);
            case "-"   -> new SubNode(left, right, node.line, node.col);
            case "*"   -> new MulNode(left, right, node.line, node.col);
            case "/"   -> new DivNode(left, right, node.line, node.col);
            case "or"  -> new OrNode(left, right, node.line, node.col);
            case "and" -> new AndNode(left, right, node.line, node.col);
            case "<"   -> new LtNode(left, right, node.line, node.col);
            case "<="  -> new LeNode(left, right, node.line, node.col);
            case ">"   -> new GtNode(left, right, node.line, node.col);
            case ">="  -> new GeNode(left, right, node.line, node.col);
            default    -> throw new IllegalArgumentException("Unknown operation id: " + op);
        };
    }

    private static AstNode visitNotExpr(ParseTree node) {
        return new NotNode(visit(node.getChildren().get(1)), node.line, node.col);
    }
}