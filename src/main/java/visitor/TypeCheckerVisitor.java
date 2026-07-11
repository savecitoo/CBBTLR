package visitor;

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

/**
 * This class implements a Type Checker of a CoDiNE AST.<br>
 * In this visitor we check also that all te used variables are declared, that the variables id are not global
 * identifiers and that every function call pays respect to the arity of the called function.<br>
 * The return value of the visit method is used just to do a traversal of the tree, if an error occurs an exception
 * will be raised.
 */
public class TypeCheckerVisitor implements AstVisitor<DimensionalVector> {

    /**
     * A map which key has a unit identifier and as a vector has the corresponding dimensional vector.
     */
    private final @NotNull Map<@NotNull String, @NotNull DimensionalVector> unit_vectors;

    /**
     *  A map that as a key has the function name and as value has a list of dimensional vectors of the parameters of a function,
     *  the order of the list is the same of the parameters in the definition.
     */
    private final @NotNull Map<@NotNull String,@NotNull List<@NotNull DimensionalVector>> func_par;

    /**
     * A map that as a key has the function name and as value has the dimensional vector
     * of the function's return type.
     */
    private final @NotNull Map<@NotNull String, @NotNull DimensionalVector> func_ret;

    /**
     * A map that as a key has the name of a fundamental dimension and as value has the
     * index of that dimension in the dimensional vector's array representation.
     */
    private final @NotNull Map<@NotNull String, @NotNull Integer> dim_enc;

    /**
     * A map that has for each variable declared the id of the variable as a key and as a value its dimensional vector.
     */
    private @NotNull Map<@NotNull String, @NotNull DimensionalVector> local_scope = new HashMap<>();

    /**
     * The dimensional vector expected in the return statement.
     */
    private DimensionalVector exp_return;

    /** A set containing all the global identifiers of the program. */
    private final @NotNull Set<@NotNull String> glob_id = new HashSet<>();

    /**
     * Constructor for a type checker visitor.
     * @param dvv a dimensional vector visitor.
     */
    public TypeCheckerVisitor(@NotNull DimensionalVectorVisitor dvv) {
        this.unit_vectors = dvv.getUnitVectors();
        this.func_par = dvv.getFunctionParams();
        this.func_ret = dvv.getFunctionReturns();
        this.dim_enc = dvv.getDimIndex();
    }

    @Override
    public @NotNull DimensionalVector visitProgram(@NotNull ProgramNode node) {
        glob_id.addAll(node.dimension_name());
        glob_id.addAll(node.unit().keySet());
        glob_id.addAll(node.functions().keySet());
        for (AstNode fn : node.functions().values())
            fn.accept(this);
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitFunction(@NotNull FunctionNode node) {
        local_scope = new HashMap<>();
        exp_return = func_ret.get(node.name().id());

        List<DimensionalVector> params = func_par.get(node.name().id());
        List<ParameterNode> paramNodes = node.parameter();
        for (int i = 0; i < paramNodes.size(); i++)
            local_scope.put(paramNodes.get(i).id().id(), params.get(i));

        node.body().accept(this);
        return exp_return;
    }

    @Override
    public @NotNull DimensionalVector visitDeclaration(@NotNull DeclarationNode node) {
        DimensionalVector vec = node.expression().accept(this);
        if (glob_id.contains(node.var().id()))
            throw new TypeException(
                    String.format("The declared variable %s declared at line %d and column %d is defined as a global identifier.",
                    node.var().id(), node.column(), node.line())
            );
        local_scope.put(node.var().id(), vec);
        return vec;
    }

    @Override
    public @NotNull DimensionalVector visitAssignment(@NotNull AssignmentNode node) {
        if (!local_scope.containsKey(node.var().id()))
            throw new IllegalArgumentException(String.format("Use of a not declared variable %s at line %d and column %d",
                    node.var().id(), node.line(), node.column()));
        DimensionalVector vec = node.expression().accept(this);
        if (!vec.equals(local_scope.get(node.var().id())))
            throw new TypeException(
                    String.format("The expression at line %d and column %d has a different type with respect to the one declared.",
                            node.line(), node.column())
            );
        return vec;
    }

    @Override
    public @NotNull DimensionalVector visitReturn(@NotNull ReturnNode node) {
        DimensionalVector vec = node.ret_stat().accept(this);
        if (!vec.equals(exp_return))
            throw new TypeException("The return expression at line %d and column %d differs from the declared one.");
        return vec;
    }

    @Override
    public @NotNull DimensionalVector visitFuncCall(@NotNull FuncCallNode node) {
        String fnName = node.func().id();
        List<DimensionalVector> expected = func_par.get(fnName);
        List<AstNode> args = node.args();
        if (args.size() != expected.size())
            throw new IllegalArgumentException(
                    String.format("Unexpected number of parameters in the call of function \"%s\" at line %d and column %d, expected %d arguments and found %d.",
                            node.func().id(), node.line(), node.column(), expected.size(), args.size())
            );
        for (int i = 0; i < args.size(); i++) {
            AstNode arg = args.get(i);
            DimensionalVector argVec = arg.accept(this);
            if (!argVec.equals(expected.get(i)))
                throw new TypeException(
                        String.format("The argument number %d of the function %s at line %d and column %d has an incompatible type respect to the declared one.",
                                i, fnName, node.line(), node.column() )
                );
        }
        return func_ret.get(fnName);
    }

    @Override
    public @NotNull DimensionalVector visitAdd(@NotNull AddNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new TypeException(
                    String.format("The sub-expressions of the \"+\" at line %d and column %d have incompatible dimensional vector",
                            node.line(),node.column())
            );
        return left;
    }

    @Override
    public @NotNull DimensionalVector visitSub(@NotNull SubNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new TypeException(String.format("The sub-expressions of the \"-\" at line %d and column %d have incompatible dimensional vector",
                    node.line(),node.column()));
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
        return base.scale(Math.toIntExact(node.exp().num()));
    }

    @Override
    public @NotNull DimensionalVector visitIdentifier(@NotNull IdentifierNode node) {
        String name = node.id();

        if (local_scope.containsKey(name))
            return local_scope.get(name);

        if (unit_vectors.containsKey(name))
            return unit_vectors.get(name);

        if (dim_enc.containsKey(name)) {
            DimensionalVectorBuilder builder = new DimensionalVectorBuilder(dim_enc);
            builder.add(name, 1);
            return builder.build();
        }
        throw new IllegalStateException(String.format("Non declared variable at line %d and column %d", node.line(), node.column()));
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
            throw new TypeException(String.format("The sub-expressions of the \"<\" at line %d and column %d have incompatible dimensional vector",
                    node.line(),node.column()));
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitLe(@NotNull LeNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new TypeException(String.format("The sub-expressions of the \"<=\" at line %d and column %d have incompatible dimensional vector",
                    node.line(),node.column()));
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitGt(@NotNull GtNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new TypeException(String.format("The sub-expressions of the \">\" at line %d and column %d have incompatible dimensional vector",
                    node.line(),node.column()));
        return zero();
    }

    @Override
    public @NotNull DimensionalVector visitGe(@NotNull GeNode node) {
        DimensionalVector left  = node.left().accept(this);
        DimensionalVector right = node.right().accept(this);
        if (!left.equals(right))
            throw new TypeException(String.format("The sub-expressions of the \">=\" at line %d and column %d have incompatible dimensional vector",
                    node.line(),node.column()));
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
        return DimensionalVector.zero(dim_enc.size());
    }
}