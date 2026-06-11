package ast;

import ast.constant.*;
import ast.operation.*;
import ast.program.*;
import ast.expression.*;
import org.jetbrains.annotations.NotNull;
import tree.ParseTree;

import java.util.*;

public class CstToAstVisitor {

    public static AstNode visit(ParseTree node) {
        return switch (node.rule_tag) {
            case "start", "item_list" -> visitStart(node);
            case "binary_expr"  -> visitBinExpr(node);
            case "unary_expr"   -> visitNotExpr(node);
            case "group"        -> visitGroup(node);
            case "identifier"   -> visitIdentifier(node);
            case "int"          -> visitIntLiteral(node);
            case "body"         -> visitBody(node);
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

    private static @NotNull AstNode visitStart(@NotNull ParseTree node) {
        Set<String> dimension      = new HashSet<>(); // simboli di unità di base (es. "m")
        Set<String> dimension_name = new HashSet<>(); // nomi di dimensione (es. "Lunghezza")
        Map<String, AstNode> unit  = new HashMap<>();
        Map<String, AstNode> functions = new HashMap<>();

        visitProgramItems(node, dimension, dimension_name, unit, functions);

        return new ProgramNode(dimension, unit, functions, node.line, node.col);
    }

    private static void visitProgramItems(
            @NotNull ParseTree node,
            @NotNull Set<String> dimension,
            @NotNull Set<String> dimension_name,
            @NotNull Map<String, AstNode> unit,
            @NotNull Map<String, AstNode> functions) {
        switch (node.rule_tag) {
            case "start":
                visitItem(node.getChildren().getFirst(), dimension, dimension_name, unit, functions);
                break;
            case "item_list":
                visitProgramItems(node.getChildren().getFirst(), dimension, dimension_name, unit, functions);
                visitItem(node.getChildren().getLast(), dimension, dimension_name, unit, functions);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " for the program part.");
        }
    }

    private static void visitItem(
            @NotNull ParseTree itemNode,
            @NotNull Set<String> dimension,
            @NotNull Set<String> dimension_name,
            @NotNull Map<String, AstNode> unit,
            @NotNull Map<String, AstNode> functions) {
        ParseTree node = itemNode;
        while (node.rule_tag.isEmpty() && node.getChildren().size() == 1) {
            node = node.getChildren().getFirst();
        }
        switch (node.rule_tag) {
            case "dim_item":
                visitDimItem(node, dimension, dimension_name, unit);
                break;
            case "unit_item":
                visitUnitItem(node, dimension, dimension_name, unit);
                break;
            case "fun_item":
                visitFunItem(node, functions, dimension, unit);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " for a program item.");
        }
    }

    private static void visitDimItem(
            @NotNull ParseTree node,
            @NotNull Set<String> dimension,
            @NotNull Set<String> dimension_name,
            @NotNull Map<String, AstNode> unit) {
        List<ParseTree> children = node.getChildren();

        String name = children.get(1).symbol;
        int nameLine = children.get(1).line;
        int nameCol  = children.get(1).col;
        if (!dimension_name.add(name) || dimension.contains(name) || unit.containsKey(name)) {
            throw new IllegalArgumentException(String.format(
                    "The dimension name %s at line %d and column %d was already defined",
                    name, nameLine, nameCol));
        }

        String symb = children.get(4).symbol;
        int line = children.get(4).line;
        int col  = children.get(4).col;
        if (unit.containsKey(symb))
            throw new IllegalArgumentException(String.format(
                    "The dimension unit %s at line %d and column %d was already defined in the program as a unit.",
                    symb, line, col));
        if (dimension_name.contains(symb))
            throw new IllegalArgumentException(String.format(
                    "The dimension unit %s at line %d and column %d was already defined in the program as a dimension name.",
                    symb, line, col));
        if (dimension.contains(symb))
            throw new IllegalArgumentException(String.format(
                    "The dimension unit %s at line %d and column %d was already defined in the program as a dimension.",
                    symb, line, col));

        dimension.add(symb);
    }

    private static void visitUnitItem(
            @NotNull ParseTree node,
            @NotNull Set<String> dimension,
            @NotNull Set<String> dimension_name,
            @NotNull Map<String, AstNode> unit) {
        List<ParseTree> children = node.getChildren();

        String symb = children.get(1).symbol;
        int line = children.get(1).line;
        int col  = children.get(1).col;
        if (unit.containsKey(symb))
            throw new IllegalArgumentException(String.format(
                    "The unit %s at line %d and column %d was already defined in the program.", symb, line, col));
        if (dimension.contains(symb))
            throw new IllegalArgumentException(String.format(
                    "The unit %s at line %d and column %d was already defined in the program as a dimension.", symb, line, col));
        if (dimension_name.contains(symb))
            throw new IllegalArgumentException(String.format(
                    "The unit %s at line %d and column %d was already defined in the program as a dimension name.", symb, line, col));

        ParseTree exprNode = children.get(3);

        // Una 'unit' è una espressione di unità (classe U): può contenere letterali interi,
        // quindi qui controlliamo SOLO che le dimensioni/unit usate siano già state dichiarate.
        checkExpressionDimensions(exprNode, dimension, unit, String.format("The unit %s", symb), line, col);

        unit.put(symb, visit(exprNode));
    }

    private static void visitFunItem(
            @NotNull ParseTree node,
            @NotNull Map<String, AstNode> functions,
            @NotNull Set<String> dimension,
            @NotNull Map<String, AstNode> unit) {
        List<ParseTree> children = node.getChildren();
        String name = children.get(1).symbol;
        if (functions.containsKey(name)) {
            throw new IllegalArgumentException(String.format(
                    "The function %s at line %d and column %d was already defined in the program.",
                    name, children.get(1).line, children.get(1).col));
        }

        List<AstNode> par = new ArrayList<>();
        visitParameterList(children.get(3), par, dimension, unit, name);

        ParseTree returnTypeNode = children.get(6);
        // Il tipo di ritorno è una espressione dimensionale (classe D): niente letterali interi
        // fuori dall'esponente di ^, e solo dimensioni/unit già dichiarate.
        checkTypeExpression(returnTypeNode, dimension, unit,
                String.format("The return type of function %s", name),
                returnTypeNode.line, returnTypeNode.col);

        functions.put(
                name,
                new FunctionNode(
                        new IdentifierNode(name, children.get(1).line, children.get(1).col),
                        par,
                        visit(returnTypeNode),
                        visit(children.get(8)),
                        node.line, node.col
                )
        );
    }

    // ---------------------------------------------------------------
    // Controlli semantici sulle espressioni di tipo/unit
    // ---------------------------------------------------------------

    /** Controllo comune a 'unit' e 'tipo': ogni identificatore usato deve essere
     *  già una dimensione o una unit dichiarata in precedenza nel programma. */
    private static void checkExpressionDimensions(
            @NotNull ParseTree exprNode,
            @NotNull Set<String> dimension,
            @NotNull Map<String, AstNode> unit,
            @NotNull String context, int line, int col) {
        Set<String> used = new LinkedHashSet<>();
        validateUnitOrTypeExpression(exprNode, used, context, line, col);
        for (String id : used) {
            if (!dimension.contains(id) && !unit.containsKey(id)) {
                throw new IllegalArgumentException(String.format(
                        "%s at line %d and column %d uses the undefined dimension/unit '%s'.",
                        context, line, col, id));
            }
        }
    }

    /** Controllo specifico per i 'tipi' (parametri e tipo di ritorno, classe D):
     *  oltre alla disponibilità delle dimensioni, niente letterali interi
     *  fuori dall'esponente di ^. */
    private static void checkTypeExpression(
            @NotNull ParseTree exprNode,
            @NotNull Set<String> dimension,
            @NotNull Map<String, AstNode> unit,
            @NotNull String context, int line, int col) {
        checkExpressionDimensions(exprNode, dimension, unit, context, line, col);
        checkNoIntegerLiterals(exprNode, context, line, col);
    }

    private static void validateUnitOrTypeExpression(
            @NotNull ParseTree node,
            @NotNull Set<String> ids,
            @NotNull String context, int line, int col) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "identifier":
                ids.add(children.getFirst().symbol);
                break;
            case "binary_expr": {
                String op = children.get(1).symbol;
                switch (op) {
                    case "^":
                        ids.add(children.getFirst().symbol); // base ID di ID ^ exp
                        break;
                    case "*":
                    case "/":
                        validateUnitOrTypeExpression(children.get(0), ids, context, line, col);
                        validateUnitOrTypeExpression(children.get(2), ids, context, line, col);
                        break;
                    default:
                        throw new IllegalArgumentException(String.format(
                                "%s at line %d and column %d can only use the multiplicative operators *, /, ^ (found '%s').",
                                context, line, col, op));
                }
                break;
            }
            case "group":
                validateUnitOrTypeExpression(children.get(1), ids, context, line, col);
                break;
            case "func_call":
                throw new IllegalArgumentException(String.format(
                        "%s at line %d and column %d cannot contain a function call.",
                        context, line, col));
            case "int":
            case "num":
                break;
            case "":
                validateUnitOrTypeExpression(children.getFirst(), ids, context, line, col);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " inside a unit/type expression.");
        }
    }

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
                    // l'esponente di ^ (exp -> INT | - INT) non passa mai per il tag "int",
                    // quindi qui non scendiamo affatto quando op è "^": è sempre ammesso.
                    checkNoIntegerLiterals(children.get(0), context, line, col);
                    checkNoIntegerLiterals(children.get(2), context, line, col);
                }
                break;
            }
            case "group":
                checkNoIntegerLiterals(children.get(1), context, line, col);
                break;
            case "func_call":
                checkNoIntegerLiteralsInArgs(children.get(2), context, line, col);
                break;
            case "identifier":
            case "num":
                break;
            case "":
                checkNoIntegerLiterals(children.getFirst(), context, line, col);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " inside a type expression.");
        }
    }

    private static void checkNoIntegerLiteralsInArgs(@NotNull ParseTree node, @NotNull String context, int line, int col) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "empty":
                break;
            case "arg_list":
                if (children.size() == 1) {
                    checkNoIntegerLiteralsInArgs(children.getFirst(), context, line, col);
                } else {
                    checkNoIntegerLiteralsInArgs(children.getFirst(), context, line, col);
                    checkNoIntegerLiterals(children.getLast(), context, line, col);
                }
                break;
            case "arg_list_item":
                checkNoIntegerLiterals(children.getFirst(), context, line, col);
                break;
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " in type expression function call arguments.");
        }
    }


    // ---------------------------------------------------------------
    // Parametri, corpo, dichiarazioni, istruzioni
    // ---------------------------------------------------------------

    private static void visitParameterList(
            ParseTree node, List<AstNode> par,
            Set<String> dimension, Map<String, AstNode> unit, String funcName) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "empty":
                break;
            case "start_parameter_list":
                visitParameterList(children.getFirst(), par, dimension, unit, funcName);
                break;
            case "parameter_list":
                if (children.size() == 1) {
                    visitParameterList(children.getFirst(), par, dimension, unit, funcName);
                } else {
                    visitParameterList(children.getFirst(), par, dimension, unit, funcName);
                    visitParameterList(children.getLast(), par, dimension, unit, funcName);
                }
                break;
            case "parameter_item": {
                ParseTree idTok = children.getFirst();
                ParseTree typeNode = children.get(2);
                checkTypeExpression(typeNode, dimension, unit,
                        String.format("The parameter %s of function %s", idTok.symbol, funcName),
                        idTok.line, idTok.col);
                par.add(new ParameterNode(
                        new IdentifierNode(idTok.symbol, idTok.line, idTok.col),
                        visit(typeNode),
                        node.line, node.col));
                break;
            }
            default:
                throw new IllegalStateException("Unexpected rule tag " + node.rule_tag + " for the parameter list part.");
        }
    }

    private static AstNode visitBody(ParseTree node) {
        List<AstNode> decl = new ArrayList<>();
        List<AstNode> stat = new ArrayList<>();
        visitDeclNode(node.getChildren().getFirst(), decl);
        visitStatNode(node.getChildren().getLast(), stat);
        return new BodyNode(decl, stat, node.line, node.col);
    }

    private static void visitDeclNode(@NotNull ParseTree node, List<AstNode> decl) {
        List<ParseTree> children = node.getChildren();
        switch (node.rule_tag) {
            case "empty":
                break;
            case "var_list":
                visitDeclNode(children.getFirst(), decl);
                visitDeclNode(children.getLast(), decl);
                break;
            case "var_item":
                decl.add(
                        new DeclarationNode(
                                new IdentifierNode(children.get(1).symbol, children.get(1).line, children.get(1).col),
                                visit(children.getLast()),
                                node.line, node.col)
                );
                break;
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
                new IdentifierNode(children.get(1).symbol, children.get(1).line, children.get(1).col),
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
        AstNode b = new IdentifierNode(children.getFirst().symbol, node.line, node.col);
        AstNode exp;
        ParseTree expNode = children.get(2);
        List<ParseTree> exp_children = expNode.getChildren();
        if (exp_children.size() == 1) {
            exp = new IntNode(Integer.parseInt(exp_children.getFirst().symbol), expNode.line, expNode.col);
        } else {
            ParseTree intTok = exp_children.getLast();
            exp = new MinusNode(
                    new IntNode(Integer.parseInt(intTok.symbol), intTok.line, intTok.col),
                    expNode.line, expNode.col
            );
        }
        return new PowNode(b, exp, node.line, node.col);
    }

    private static AstNode visitIntLiteral(ParseTree node) {
        // term -> INT #int | - INT #int : entrambe condividono il tag "int",
        // distinguibili dal numero di figli.
        List<ParseTree> children = node.getChildren();
        if (children.size() == 1) {
            ParseTree intTok = children.getFirst();
            return new IntNode(Integer.parseInt(intTok.symbol), intTok.line, intTok.col);
        } else {
            ParseTree intTok = children.getLast();
            return new MinusNode(
                    new IntNode(Integer.parseInt(intTok.symbol), intTok.line, intTok.col),
                    node.line, node.col
            );
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
            case "OR"  -> new OrNode(left, right, node.line, node.col);
            case "AND" -> new AndNode(left, right, node.line, node.col);
            case "<"   -> new LtNode(left, right, node.line, node.col);
            case "<="  -> new LeNode(left, right, node.line, node.col);
            case ">"   -> new GtNode(left, right, node.line, node.col);
            case ">="  -> new GeNode(left, right, node.line, node.col);
            default    -> throw new IllegalArgumentException("Unknown operation symbol: " + op);
        };
    }

    private static AstNode visitNotExpr(ParseTree node) {
        return new NotNode(visit(node.getChildren().get(1)), node.line, node.col);
    }
}