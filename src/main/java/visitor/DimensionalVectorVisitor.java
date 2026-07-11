package visitor;

import ast.AstNode;
import ast.AstVisitor;
import ast.constant.IdentifierNode;
import ast.expression.DivNode;
import ast.expression.MulNode;
import ast.expression.PowNode;
import ast.program.FunctionNode;
import ast.program.ParameterNode;
import ast.program.ProgramNode;
import org.jetbrains.annotations.NotNull;
import utils.DimensionalVector;
import utils.DimensionalVectorBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This visitor builds the dimensional vector unit expressions and type expressions,
 * i.e. the parameter's type and return's type of each function.<br>
 * The return value of the visit method is used just to do a traversal of the tree, to get the result use the getter methods.
 */
public class DimensionalVectorVisitor implements AstVisitor<DimensionalVector> {

    /**
     *  A map that as a key has the unit id and as value has the corresponding unit's dimensional vector.
     */
    private final Map<String, DimensionalVector> unit_vectors = new HashMap<>();

    /**
     *  A map that as a key has the function name and as value has a list of dimensional vectors of the parameters of a function,
     *  the order of the list is the same of the parameters in the definition.
     */
    private final Map<String, List<DimensionalVector>> func_par = new HashMap<>();

    /**
     * A map that as a key has the function name and as value has the dimensional vector
     * of the function's return type.
     */
    private final Map<String, DimensionalVector> func_ret = new HashMap<>();

    /**
     * A map that as a key has the name of a fundamental dimension and as value has the
     * index of that dimension in the dimensional vector's array representation.
     */
    private final Map<String, Integer> dim_enc = new HashMap<>();

    /**
     * A map that as a key has the name of a derived unit and as value has the AST node
     * defining that unit (i.e. its unit expression, e.g. a product/quotient/power of
     * other units or base dimensions).
     */
    private Map<String, AstNode> unit_defs = new HashMap<>();

    /**
     * The stack of builders used to accumulate the dimensional vector of the unit/type
     * expression currently being visited.
     */
    private final Deque<DimensionalVectorBuilder> builder_stack = new ArrayDeque<>();

    /**
     * Tracks whether the visitor is currently traversing the denominator side of a
     * division ({@link #visitDiv}), so that contributions to the current builder are
     * subtracted instead of added.
     */
    private boolean is_den = false;

    /**
     * Returns a map associating each unit id to its dimensional vector.
     * @return a copy of the unit-to-vector map.
     */
    public @NotNull Map<@NotNull String, @NotNull DimensionalVector> getUnitVectors() { return Map.copyOf(unit_vectors); }

    /**
     * Returns a map associating each function name to the list of
     * dimensional vectors of its parameters, in declaration order.
     * @return a copy of the function-to-parameter-vectors map.
     */
    public @NotNull Map< @NotNull String, @NotNull List< @NotNull DimensionalVector>> getFunctionParams() { return Map.copyOf(func_par); }

    /**
     * Returns a map associating each function name to the
     * dimensional vector of its return type.
     * @return a copy of the function-to-return-vector map.
     */
    public @NotNull Map<@NotNull String, @NotNull DimensionalVector> getFunctionReturns() { return Map.copyOf(func_ret); }

    /**
     * Returns a map associating each fundamental dimension's name
     * to its index in the dimensional vector's array representation.
     * @return a copy of the dimension-to-index map.
     */
    public @NotNull Map< @NotNull String, @NotNull Integer> getDimIndex() { return Map.copyOf(dim_enc); }

    /**
     * Returns the builder currently on top of the stack, i.e. the one accumulating
     * contributions for the unit/type expression being visited.
     * @return the current builder.
     */
    private DimensionalVectorBuilder builder() {
        return builder_stack.peek();
    }

    /**
     * Pushes a brand-new builder on top of the stack, becoming the current builder.
     * Used to compute a dimensional vector in isolation, without disturbing any
     * builder already in use further down the call stack.
     */
    private void pushBuilder() {
        builder_stack.push(new DimensionalVectorBuilder(dim_enc));
    }

    /**
     * Pops the current builder off the stack, restoring the previous one (if any)
     * as the current builder.
     */
    private void popBuilder() {
        builder_stack.pop();
    }

    
    @Override
    public @NotNull DimensionalVector visitProgram(@NotNull ProgramNode node) {
        // building the encoding map
        int i = 0;
        for (String dim : node.dimension())
            dim_enc.put(dim, i++);

        // building the unit's dimensional vectors.
        unit_defs = node.unit();
        for (Map.Entry<String, AstNode> entry : unit_defs.entrySet()) {
            pushBuilder();
            entry.getValue().accept(this);
            unit_vectors.put(entry.getKey(), builder().build());
            popBuilder();
        }

       // building the paramenters dimensional vectors.
        for (AstNode fn : node.functions().values())
            fn.accept(this);

        pushBuilder();
        return builder().zero();
    }

    @Override
    public @NotNull DimensionalVector visitFunction(@NotNull FunctionNode node) {
        // parameters
        List<DimensionalVector> params = new ArrayList<>();
        for (AstNode param : node.parameter()) {
            pushBuilder();
            param.accept(this);
            params.add(builder().build());
            popBuilder();
        }
        func_par.put(node.name().id(), params);

        // return_type
        pushBuilder();
        node.ret_type().accept(this);
        DimensionalVector ret_type = builder().build();
        popBuilder();
        func_ret.put(node.name().id(), ret_type);

        return ret_type;
    }

    @Override
    public @NotNull DimensionalVector visitParameter(@NotNull ParameterNode node) {
        node.type().accept(this);
        return builder().zero();
    }

    @Override
    public @NotNull DimensionalVector visitIdentifier(@NotNull IdentifierNode node) {
        String name = node.id();

        if (dim_enc.containsKey(name)) {
            if (is_den)
                builder().sub(name, 1);
            else
                builder().add(name, 1);
            return builder().zero();
        }

        if (unit_defs.containsKey(name)) {
            DimensionalVector derived;
            if (unit_vectors.containsKey(name)) {
                derived = unit_vectors.get(name);
            } else {
                derived = computeUnit(name);
            }
            applyVector(derived);
            return builder().zero();
        }

        throw new IllegalStateException(String.format("Unknown identifier %s in line %d and column %d",
                name, node.line(), node.column()));
    }


    @Override
    public @NotNull DimensionalVector visitMul(@NotNull MulNode node) {
        node.left().accept(this);
        node.right().accept(this);
        return builder().zero();
    }

    @Override
    public @NotNull DimensionalVector visitDiv(@NotNull DivNode node) {
        node.left().accept(this);
        is_den = !is_den;
        node.right().accept(this);
        is_den = !is_den;
        return builder().zero();
    }

    @Override
    public @NotNull DimensionalVector visitPow(@NotNull PowNode node) {
        int exp = Math.toIntExact(node.exp().num());
        if (is_den) exp = -exp;

        String base_id = node.base().id();
        if (dim_enc.containsKey(base_id)) {
            builder().add(base_id, exp);
        } else if (unit_defs.containsKey(base_id)) {
            DimensionalVector baseVec = unit_vectors.containsKey(base_id)
                    ? unit_vectors.get(base_id)
                    : computeUnit(base_id);
            applyVectorScaled(baseVec, exp);
        }
        return builder().zero();
    }

    @Override
    public @NotNull DimensionalVector visitDefault(@NotNull AstNode node) {
        return builder().zero();
    }

    private void applyVector(DimensionalVector v) {
        applyVectorScaled(v, 1);
    }

    private void applyVectorScaled(DimensionalVector v, int scale) {
        for (Map.Entry<String, Integer> entry : dim_enc.entrySet()) {
            int contribution = v.get(entry.getValue()) * scale;
            if (is_den)
                builder().sub(entry.getKey(), contribution);
            else
                builder().add(entry.getKey(), contribution);
        }
    }

    private DimensionalVector computeUnit(String name) {
        pushBuilder();
        // we have to cache the den because we could start a new computation derived from the current one,
        // so it has to be saved.
        boolean old_den = is_den;
        is_den = false;
        unit_defs.get(name).accept(this);
        DimensionalVector result = builder().build();
        unit_vectors.put(name, result);
        is_den = old_den;
        popBuilder();
        return result;
    }
}