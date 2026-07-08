package utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

/**
 * This class represents a Dimensional Vector.<br>
 */
public class DimensionalVector implements Iterable<Integer> {

    /**
     * The dimensional vector representation.
     */
    private final int[] dimension;

    /**
     * This is a constuctor for a dimensional vector.
     * @param dimension the array representing the vector.
     */
    public DimensionalVector(int @NotNull[] dimension) {
        this.dimension = Arrays.copyOf(dimension, dimension.length);;
    }

    /**
     * This method return, given a size, the dimensional vector of just zeroes.
     * @param size the size of the wanted vector.
     * @return the zero-dimensional vector.
     */
    public static DimensionalVector zero(int size) {
        return new DimensionalVector(new int[size]);
    }

    /**
     * This method returns the size of the vector.
     * @return the size of the vector.
     */
    public int size() {
        return dimension.length;
    }

    /**
     * This method returns the element in the i-th position of the vector.
     * @param i the index of the wanted element.
     * @return the integer in the i-th position.
     * @throws IllegalArgumentException if i < 0 or i > size() - 1
     */
    public int get(int i) throws IllegalArgumentException {
        if (i < 0 || i > dimension.length)
            throw new IllegalArgumentException("Index out of bound exception, it must be between 0 and dimension.size() - 1");
        return dimension[i];
    }

    /**
     * This method returns a new dimensional vector that corresponds to the subtraction between {@code this} vector
     * and the goven one.
     * @param vector the vector to subtract to {@code this} vector.
     * @return the resulting vector of the subtraction.
     * @throws IllegalArgumentException if the vector has no the same size.
     */
    public @NotNull DimensionalVector sub(@NotNull DimensionalVector vector) throws IllegalArgumentException {
        if (this.size() != vector.size())
            throw new IllegalArgumentException("Vectors must be of the same dimension");
        int[] dim = new int[dimension.length];
        for (int i = 0; i < size(); i++) {
            dim[i] = dimension[i] - vector.dimension[i];
        }
        return new DimensionalVector(dim);
    }

    /**
     * This method returns a new vector that corresponds to the sum of {@code this} vector and the given one.
     * @param vector the vector to add.
     * @return the resulting vector of the sum.
     * @throws IllegalArgumentException if the vector has no the same size.
     */
    public @NotNull DimensionalVector add(@NotNull DimensionalVector vector) throws IllegalArgumentException {
        if (this.size() != vector.size())
            throw new IllegalArgumentException("Vectors must be of the same dimension");
        int[] dim = new int[dimension.length];
        for (int i = 0; i < size(); i++) {
            dim[i] = dimension[i] + vector.dimension[i];
        }
        return new DimensionalVector(dim);
    }

    /**
     * This method scales all the dimension by a factor k.
     * @param k the scaling factor.
     * @return the scaled vector.
     */
    public @NotNull DimensionalVector scale(int k) {
        int[] scaled = new int[dimension.length];
        for (int i = 0; i < dimension.length; i++)
            scaled[i] = dimension[i] * k;
        return new DimensionalVector(scaled);
    }

    @Override
    public @NotNull Iterator<Integer> iterator() {
        return new Iterator<>() {

            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < dimension.length;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                return dimension[i++];
            }
        };
    }

    @Override
    public boolean equals(@NotNull Object o) {
        if (o == this) return true;
        if (o instanceof DimensionalVector dm)
            return Arrays.equals(dimension, dm.dimension);
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dimension);
    }

    @Override
    public String toString() {
        return "DimensionalVector{" +
                "dimension=" + Arrays.toString(dimension) +
                '}';
    }
}
