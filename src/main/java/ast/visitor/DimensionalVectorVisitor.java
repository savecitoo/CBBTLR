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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionalVectorVisitor implements AstVisitor<DimensionalVector> {

    private final Map<String, DimensionalVector> unitVectors = new HashMap<>();
    private final Map<String, List<DimensionalVector>> functionParams = new HashMap<>();
    private final Map<String, DimensionalVector> functionReturns = new HashMap<>();
    private final Map<String, Integer> dimIndex = new HashMap<>();
    private Map<String, AstNode> unitDefs = new HashMap<>();

    private DimensionalVectorBuilder builder;
    private boolean isDenominator = false;

    // getter per il TypeCheckerVisitor
    public Map<String, DimensionalVector> getUnitVectors() { return unitVectors; }
    public Map<String, List<DimensionalVector>> getFunctionParams() { return functionParams; }
    public Map<String, DimensionalVector> getFunctionReturns() { return functionReturns; }
    public Map<String, Integer> getDimIndex() { return dimIndex; }

    @Override
    public DimensionalVector visitProgram(@NotNull ProgramNode node) {
        // 1. dimensioni fondamentali
        int i = 0;
        for (String dim : node.dimension())
            dimIndex.put(dim, i++);

        builder = new DimensionalVectorBuilder(dimIndex);

        // 2. unità derivate
        unitDefs = node.unit();
        for (Map.Entry<String, AstNode> entry : unitDefs.entrySet()) {
            builder.reset();
            entry.getValue().accept(this);
            unitVectors.put(entry.getKey(), builder.build());
        }

        // 3. firme delle funzioni
        for (AstNode fn : node.functions().values())
            fn.accept(this);

        return builder.zero();
    }

    @Override
    public DimensionalVector visitFunction(@NotNull FunctionNode node) {
        // parametri
        List<DimensionalVector> params = new ArrayList<>();
        for (AstNode param : node.parameter()) {
            builder.reset();
            param.accept(this);
            params.add(builder.build());
        }
        //System.out.println(getFunctionParams());
        functionParams.put(node.name().id(), params);

        // tipo di ritorno
        builder.reset();
        node.ret_expr().accept(this);
        DimensionalVector retVec = builder.build();
        functionReturns.put(node.name().id(), retVec);

        // NON visita il corpo
        return retVec;
    }

    @Override
    public @NotNull DimensionalVector visitParameter(ParameterNode node) {
        node.expression().accept(this);
        return builder.zero();
    }

    @Override
    public DimensionalVector visitIdentifier(@NotNull IdentifierNode node) {
        String name = node.id();

        // unità di base: contribuisce al builder
        if (dimIndex.containsKey(name)) {
            if (isDenominator)
                builder.sub(name, 1);
            else
                builder.add(name, 1);
            return builder.zero();
        }

        // unità derivata: ricalcola il suo contributo sul builder corrente
        if (unitDefs.containsKey(name)) {
            // salva lo stato corrente, calcola il vettore dell'unità derivata,
            // poi applica il suo contributo al builder corrente
            DimensionalVector derived;
            if (unitVectors.containsKey(name)) {
                derived = unitVectors.get(name);
            } else {
                DimensionalVectorBuilder old = builder;
                builder = new DimensionalVectorBuilder(dimIndex);
                // non ancora calcolata: calcola ricorsivamente
                // builder.reset();
                unitDefs.get(name).accept(this);
                derived = builder.build();
                unitVectors.put(name, derived);
                builder = old;
            }
            // applica il vettore dell'unità derivata al builder corrente
            applyVector(derived);
            return builder.zero();
        }

        throw new IllegalStateException("Identificatore sconosciuto nel contesto di tipo: " + name);
    }

    @Override
    public DimensionalVector visitMul(@NotNull MulNode node) {
        // entrambi i figli contribuiscono al builder con lo stesso segno
        node.left().accept(this);
        node.right().accept(this);
        return builder.zero();
    }

    @Override
    public DimensionalVector visitDiv(@NotNull DivNode node) {
        node.left().accept(this);
        isDenominator = !isDenominator;   // entra al denominatore
        node.right().accept(this);
        isDenominator = !isDenominator;   // ripristina
        return builder.zero();
    }

    @Override
    public DimensionalVector visitPow(@NotNull PowNode node) {
        // u ^ k: applica il contributo dell'unità base k volte
        // più semplice: calcola il vettore della base e scala
        int exp = node.exp().num();
        if (isDenominator) exp = -exp;

        // l'id della base contribuisce direttamente
        String baseName = node.base().id();
        if (dimIndex.containsKey(baseName)) {
            builder.add(baseName, exp);
        } else if (unitDefs.containsKey(baseName)) {
            DimensionalVector baseVec = unitVectors.getOrDefault(
                    baseName,
                    computeUnit(baseName)
            );
            applyVectorScaled(baseVec, exp);
        }
        return builder.zero();
    }

    @Override
    public DimensionalVector visitDefault(AstNode node) {
        return builder.zero();
    }

    // -----------------------------------------------------------------------
    // metodi ausiliari
    // -----------------------------------------------------------------------

    // applica il vettore v al builder corrente (rispettando isDenominator)
    private void applyVector(DimensionalVector v) {
        applyVectorScaled(v, 1);
    }

    private void applyVectorScaled(DimensionalVector v, int scale) {
        for (Map.Entry<String, Integer> entry : dimIndex.entrySet()) {
            int contribution = v.get(entry.getValue()) * scale;
            if (isDenominator)
                builder.sub(entry.getKey(), contribution);  // nota: sub già nega
            else
                builder.add(entry.getKey(), contribution);
        }
    }

    private DimensionalVector computeUnit(String name) {
        DimensionalVectorBuilder temp = new DimensionalVectorBuilder(dimIndex);
        // usa un builder temporaneo per non sporcare quello corrente
        DimensionalVectorBuilder saved = builder;
        boolean savedDenom = isDenominator;
        builder = temp;
        isDenominator = false;
        unitDefs.get(name).accept(this);
        DimensionalVector result = builder.build();
        builder = saved;
        isDenominator = savedDenom;
        unitVectors.put(name, result);
        return result;
    }
}