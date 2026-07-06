package ast.visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.FalseNode;
import ast.constant.IdentifierNode;
import ast.constant.TrueNode;
import ast.expression.*;
import ast.operation.*;
import ast.program.FunctionNode;
import ast.program.ParameterNode;
import ast.program.ProgramNode;
import org.jetbrains.annotations.NotNull;
import utils.DimensionalVector;
import utils.DimensionalVectorBuilder;

import java.util.*;

public class TypeCheckerVisitor implements AstVisitor<DimensionalVector> {

    private final Map<String, DimensionalVector> unitVectors;
    private final Map<String, List<DimensionalVector>> functionParams;
    private final Map<String, DimensionalVector> functionReturns;
    private final Map<String, Integer> dimIndex;

    private Map<String, DimensionalVector> localScope = new HashMap<>();
    private DimensionalVector expectedReturn;

    public TypeCheckerVisitor(DimensionalVectorVisitor dvv) {
        this.unitVectors     = dvv.getUnitVectors();
        this.functionParams  = dvv.getFunctionParams();
        this.functionReturns = dvv.getFunctionReturns();
        this.dimIndex        = dvv.getDimIndex();
    }

    // ---------------------------------------------------------------
    // Helper: formatta la posizione di un nodo
    // ---------------------------------------------------------------
    private String pos(AstNode node) {
        return "[riga " + node.line() + ", col " + node.column() + "]";
    }

    // ---------------------------------------------------------------
    // Visitors
    // ---------------------------------------------------------------

    @Override
    public @NotNull DimensionalVector visitProgram(@NotNull ProgramNode node) {
        for (AstNode fn : node.functions().values())
            fn.accept(this);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitFunction(@NotNull FunctionNode node) {
        localScope = new HashMap<>();
        expectedReturn = functionReturns.get(node.name().id());

        List<DimensionalVector> params = functionParams.get(node.name().id());
        List<ParameterNode> paramNodes = node.parameter();
        for (int i = 0; i < paramNodes.size(); i++)
            localScope.put(paramNodes.get(i).id().id(), params.get(i));

        node.body().accept(this);
        return expectedReturn;
    }

    @Override
    public @NotNull DimensionalVector visitDeclaration(@NotNull DeclarationNode node) {
        DimensionalVector vec = node.assignment().accept(this);
        localScope.put(node.var().id(), vec);
        return vec;
    }

    @Override
    public @NotNull DimensionalVector visitAssignment(@NotNull AssignmentNode node) {
        if (!localScope.containsKey(node.var().id()))
            throw new IllegalArgumentException(String.format("Use of a not declared variable %s at line %d and column %d",
                    node.var().id(), node.line(), node.column()));
        DimensionalVector vec = node.expression().accept(this);
        if (!vec.equals(localScope.get(node.var().id())))
            throw new IllegalArgumentException(
                    String.format("The assignment at line %d and column %d has a different type with respect to the one declared.",
                            node.line(), node.column())
            );
        return vec;
    }

    @Override
    public @NotNull DimensionalVector visitReturn(@NotNull ReturnNode node) {
        DimensionalVector vec = node.ret_stat().accept(this);
        if (!vec.equals(expectedReturn))
            // TODO: Create TypeException
            throw new IllegalArgumentException(pos(node)
                    + " Tipo di ritorno incompatibile: atteso "
                    + expectedReturn + ", trovato " + vec);
        return vec;
    }

    @Override
    public @NotNull DimensionalVector visitFuncCall(@NotNull FuncCallNode node) {
        String fnName = node.func().id();
        List<DimensionalVector> expected = functionParams.get(fnName);
        List<AstNode> args = node.args();

        for (int i = 0; i < args.size(); i++) {
            AstNode arg = args.get(i);
            DimensionalVector argVec = arg.accept(this);
            if (!argVec.equals(expected.get(i)))
                // TODO: TypeException
                throw new IllegalArgumentException(pos(arg)
                        + " Argomento " + i + " di '" + fnName
                        + "' incompatibile: atteso " + expected.get(i)
                        + ", trovato " + argVec);
        }
        return functionReturns.get(fnName);
    }

    @Override
    public @NotNull DimensionalVector visitAdd(@NotNull AddNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            // TODO: TypeException
            throw new IllegalArgumentException(pos(node)
                    + " Operandi di '+' incompatibili: " + left + " e " + right);
        return left;
    }

    @Override
    public @NotNull DimensionalVector visitSub(@NotNull SubNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            // TODO: TypeException
            throw new IllegalArgumentException(pos(node)
                    + " Operandi di '-' incompatibili: " + left + " e " + right);
        return left;
    }

    @Override
    public @NotNull DimensionalVector visitMul(@NotNull MulNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        return left.add(right);
    }

    @Override
    public @NotNull DimensionalVector visitDiv(@NotNull DivNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        return left.sub(right);
    }

    @Override
    public @NotNull DimensionalVector visitPow(@NotNull PowNode node) {
        DimensionalVector base = node.base().accept(this);
        return base.scale(node.exp().num());
    }

    @Override
    public @NotNull DimensionalVector visitIdentifier(@NotNull IdentifierNode node) {
        String name = node.id();

        if (localScope.containsKey(name))
            return localScope.get(name);

        if (unitVectors.containsKey(name))
            return unitVectors.get(name);

        if (dimIndex.containsKey(name)) {
            DimensionalVectorBuilder builder = new DimensionalVectorBuilder(dimIndex);
            builder.add(name, 1);
            return builder.build();
        }
        throw new IllegalStateException(pos(node)
                + " Identificatore sconosciuto: '" + name + "'");
    }

    @Override
    public @NotNull DimensionalVector visitOr(@NotNull OrNode node) {
        node.left().accept(this);
        node.right().accept(this);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitAnd(@NotNull AndNode node) {
        node.left().accept(this);
        node.right().accept(this);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitNot(@NotNull NotNode node) {
        node.condition().accept(this);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitLt(@NotNull LtNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new IllegalArgumentException(pos(node)
                    + " Operandi di '<' incompatibili: " + left + " e " + right);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitLe(@NotNull LeNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new IllegalArgumentException(pos(node)
                    + " Operandi di '<=' incompatibili: " + left + " e " + right);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitGt(@NotNull GtNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new IllegalArgumentException(pos(node)
                    + " Operandi di '>' incompatibili: " + left + " e " + right);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitGe(@NotNull GeNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new IllegalArgumentException(pos(node)
                    + " Operandi di '>=' incompatibili: " + left + " e " + right);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitTrue(@NotNull TrueNode node) {
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitFalse(@NotNull FalseNode node) {
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitIf(@NotNull IfNode node) {
        node.condition().accept(this);
        for (AstNode child : node.trueBranch())
            child.accept(this);
        if (!node.falseBranch().isEmpty())
            for (AstNode child : node.falseBranch())
                child.accept(this);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitWhile(@NotNull WhileNode node) {
        node.condition().accept(this);
        for (AstNode child : node.instruction())
            child.accept(this);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitDefault(@NotNull AstNode node) {
        for (AstNode child : node.children())
            child.accept(this);
        return zero();
    }

    private DimensionalVector zero() {
        return DimensionalVector.zero(dimIndex.size());
    }
}