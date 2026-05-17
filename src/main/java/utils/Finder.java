package utils;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents a strategy to search for or extract a property from a set of objects. <br>
 * Implementations define the criteria for finding elements.
 *
 * @param <T> The type of the objects the find method has to produce.
 */
public interface Finder <T> {

    // This interface was written to let use emulate the first-order citizen property of the functions
    // in other programming languages so you can deal in an easier way with the closure of a function.



    /**
     * Finds elements in the given set that match a certain property or condition.
     *
     * @param input a non-null set of elements to search through, it can be empty.
     * @return a non-null set containing all elements that match the search criteria.
     */
    @NotNull Set<@NotNull T> find(@NotNull Set<@NotNull T> input);
}
