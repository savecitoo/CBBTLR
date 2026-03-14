package grammar;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This is an interface where you can specify what are you searching for.
 */
public interface FindMethod <T> {

    // This interface was written to let use emulate the first-order citizen property of the functions
    // in other programming languages so you can deal in an easier way with the



    /**
     *  This method given a set finds a property about an object and returns a set containing strings with the searched property.
     * @param found a set of string that can contains useful information on what you are searching of.
     * @return a set of strings containing all the strings with the sarched property.
     */
    @NotNull Set<@NotNull T> find(@NotNull Set<@NotNull T> found);
}
