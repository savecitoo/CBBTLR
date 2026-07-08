package ast.visitor;

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
 * i.e. the parameter's type and return's type of each function.
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
    private boolean isDenominator = false;

    /**
     * Returns a map associating each unit id to its dimensional vector.
     * @return a copy of the unit-to-vector map.
     */
    public Map<String, DimensionalVector> getUnitVectors() { return Map.copyOf(unit_vectors); }

    /**
     * Returns a map associating each function name to the list of
     * dimensional vectors of its parameters, in declaration order.
     * @return a copy of the function-to-parameter-vectors map.
     */
    public Map<String, List<DimensionalVector>> getFunctionParams() { return Map.copyOf(func_par); }

    /**
     * Returns a map associating each function name to the
     * dimensional vector of its return type.
     * @return a copy of the function-to-return-vector map.
     */
    public Map<String, DimensionalVector> getFunctionReturns() { return Map.copyOf(func_ret); }

    /**
     * Returns a map associating each fundamental dimension's name
     * to its index in the dimensional vector's array representation.
     * @return a copy of the dimension-to-index map.
     */
    public Map<String, Integer> getDimIndex() { return Map.copyOf(dim_enc); }

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

    /**
     * Visits the whole program: first indexes the fundamental dimensions, then computes
     * the dimensional vector of every derived unit, and finally computes the signature
     * (parameter and return types) of every function.
     * @param node the program's root node.
     * @return the zero vector, as this node has no dimensional value of its own.
     */
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

        // unità di base: contribuisce al builder
        if (dim_enc.containsKey(name)) {
            if (isDenominator)
                builder().sub(name, 1);
            else
                builder().add(name, 1);
            return builder().zero();
        }

        // unità derivata: ricalcola il suo contributo sul builder corrente
        if (unit_defs.containsKey(name)) {
            DimensionalVector derived;
            if (unit_vectors.containsKey(name)) {
                derived = unit_vectors.get(name);
            } else {
                // non ancora calcolata: calcola ricorsivamente su un builder isolato
                derived = computeUnit(name);
            }
            // applica il vettore dell'unità derivata al builder corrente
            applyVector(derived);
            return builder().zero();
        }

        throw new IllegalStateException(String.format("Unknown identifier %s in line %d and column %d",
                name, node.line(), node.column()));
    }


    @Override
    public @NotNull DimensionalVector visitMul(@NotNull MulNode node) {
        // entrambi i figli contribuiscono al builder con lo stesso segno
        node.left().accept(this);
        node.right().accept(this);
        return builder().zero();
    }

    @Override
    public @NotNull DimensionalVector visitDiv(@NotNull DivNode node) {
        node.left().accept(this);
        isDenominator = !isDenominator;
        node.right().accept(this);
        isDenominator = !isDenominator;
        return builder().zero();
    }

    @Override
    public @NotNull DimensionalVector visitPow(@NotNull PowNode node) {
        // u ^ k: applica il contributo dell'unità base k volte
        int exp = Math.toIntExact(node.exp().num());
        if (isDenominator) exp = -exp;

        String baseName = node.base().id();
        if (dim_enc.containsKey(baseName)) {
            builder().add(baseName, exp);
        } else if (unit_defs.containsKey(baseName)) {
            DimensionalVector baseVec = unit_vectors.containsKey(baseName)
                    ? unit_vectors.get(baseName)
                    : computeUnit(baseName);
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
            if (isDenominator)
                builder().sub(entry.getKey(), contribution);  // nota: sub già nega
            else
                builder().add(entry.getKey(), contribution);
        }
    }

    private DimensionalVector computeUnit(String name) {
        pushBuilder();
        // we have to cache the den because we could encounter a not dimensioned unit in the unit,
        // since the represantation with a hashmap doesn't preserve the order specified in the file.
        boolean savedDenom = isDenominator;
        isDenominator = false;
        unit_defs.get(name).accept(this);
        DimensionalVector result = builder().build();
        unit_vectors.put(name, result);
        isDenominator = savedDenom;
        popBuilder();
        return result;
    }
}