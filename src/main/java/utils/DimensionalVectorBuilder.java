package utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DimensionalVectorBuilder {

    private final @NotNull Map< @NotNull String, @NotNull Integer> dim_enc;

    private final int vector_size;

    private final @NotNull DimensionalVector dimensionless;

    private int[] vector;

    public DimensionalVectorBuilder(@NotNull Map<@NotNull String, @NotNull Integer> dim_enc) {
        vector_size = dim_enc.size();
        this.dim_enc = Map.copyOf(dim_enc);
        dimensionless = new DimensionalVector(new int[vector_size]);
        vector = new int[vector_size];
    }

    public void add(@NotNull String unit, int quantity) {
        if (!dim_enc.containsKey(unit))
            throw new IllegalArgumentException("THe unit given is not among the possible units.");
        int i = dim_enc.get(unit);
        vector[i] = vector[i] + quantity;
    }

    public void sub(@NotNull String unit, int quantity) {
        if (!dim_enc.containsKey(unit))
            throw new IllegalArgumentException("THe unit given is not among the possible units.");
        int i = dim_enc.get(unit);
        vector[i] = vector[i] - quantity;
    }

    public @NotNull DimensionalVector zero() {return dimensionless;}

    public @NotNull DimensionalVector build() {
        DimensionalVector vec = new DimensionalVector(vector);
        return vec;
    }

    public void reset() { vector = new int[vector_size];}
}
