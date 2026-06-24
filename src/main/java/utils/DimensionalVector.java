package utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;

public class DimensionalVector implements Iterable<Integer> {

    private final int[] dimension;

    public DimensionalVector(int @NotNull[] dimension) {
        this.dimension = Arrays.copyOf(dimension, dimension.length);;
    }

    public static DimensionalVector zero(int size) {
        return new DimensionalVector(new int[size]);
    }

    public int size() {
        return dimension.length;
    }

    public @NotNull DimensionalVector sub(@NotNull DimensionalVector vector) {
        if (this.size() != vector.size())
            throw new IllegalArgumentException("Vectors must be of the same dimension");
        int[] dim = new int[dimension.length];
        for (int i = 0; i < size(); i++) {
            dim[i] = dimension[i] - vector.dimension[i];
        }
        return new DimensionalVector(dim);
    }

    public @NotNull DimensionalVector add(@NotNull DimensionalVector vector) {
        if (this.size() != vector.size())
            throw new IllegalArgumentException("Vectors must be of the same dimension");
        int[] dim = new int[dimension.length];
        for (int i = 0; i < size(); i++) {
            dim[i] = dimension[i] + vector.dimension[i];
        }
        return new DimensionalVector(dim);
    }

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

    public int get(int i) throws IllegalArgumentException {
        if (i < 0 || i > dimension.length)
            throw new IllegalArgumentException("Index out of bound exception, it must be between 0 and dimension.size() - 1");
        return dimension[i];
    }

    @Override
    public String toString() {
        return "DimensionalVector{" +
                "dimension=" + Arrays.toString(dimension) +
                '}';
    }
}
