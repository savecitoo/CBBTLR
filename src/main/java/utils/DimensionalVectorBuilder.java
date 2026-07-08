package utils;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This class permits to build a dimensional vector.
 */
public class DimensionalVectorBuilder {

    /**
     * Encodes each dimension to an index of the array.
     */
    private final @NotNull Map< @NotNull String, @NotNull Integer> dim_enc;

    /**
     * The size each vector built has.
     */
    private final int vector_size;

    /**
     * A static constant representing the zero vector.
     */
    private final @NotNull DimensionalVector dimensionless;

    /**
     * The vector is currently being built.
     */
    private int[] vector;

    /**
     * This is a constructor for the builder.
     * @param dim_enc the encoding function of each dimension.
     */
    public DimensionalVectorBuilder(@NotNull Map<@NotNull String, @NotNull Integer> dim_enc) {
        vector_size = dim_enc.size();
        this.dim_enc = Map.copyOf(dim_enc);
        dimensionless = new DimensionalVector(new int[vector_size]);
        vector = new int[vector_size];
    }

    /**
     * Adds the given quantity to the component of the vector under construction
     * corresponding to the specified unit.
     * @param unit the dimension to which the quantity is added.
     * @param quantity the amount to add to the current value of that dimension.
     * @throws IllegalArgumentException if {@code unit} is not among the possible units.
     */
    public void add(@NotNull String unit, int quantity) throws IllegalArgumentException {
        if (!dim_enc.containsKey(unit))
            throw new IllegalArgumentException("The unit given is not among the possible units.");
        int i = dim_enc.get(unit);
        vector[i] = vector[i] + quantity;
    }

    /**
     * Subtracts the given quantity from the component of the vector under construction
     * corresponding to the specified unit.
     * @param unit the dimension from which the quantity is subtracted.
     * @param quantity the amount to subtract from the current value of that dimension.
     * @throws IllegalArgumentException if {@code unit} is not among the possible units.
     */
    public void sub(@NotNull String unit, int quantity) {
        if (!dim_enc.containsKey(unit))
            throw new IllegalArgumentException("The unit given is not among the possible units.");
        int i = dim_enc.get(unit);
        vector[i] = vector[i] - quantity;
    }

    /**
     * Returns the zero vector for this builder's dimension space, i.e. the vector
     * having all components equal to zero.
     * @return the dimensionless (zero) vector.
     */
    public @NotNull DimensionalVector zero() {return dimensionless;}

    /**
     * Builds and returns a {@link DimensionalVector} with the components accumulated
     * so far through calls to {@link #add} and {@link #sub}.
     * @return the built vector.
     */
    public @NotNull DimensionalVector build() {
        return new DimensionalVector(vector);
    }

    /**
     * Resets the vector under construction, bringing every component back to zero.
     */
    public void reset() { vector = new int[vector_size];}
}