package utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class represent a table where each object of type T putted in has an identifier. <br>
 */
public class SymbolTable<T>  {

    /**
     * This field represent a function that goes from objects of type T to the corresponding the identifier.
     */
    private final Map<T, Integer> table_o_id;

    /**
     * This field represent a function that goes from the identifiers to the corresponding object of type T.
     */
    private final Map<Integer, T> table_id_o;

    /**
     * This is a constructor for a SymbolTable.
     */
    public SymbolTable () {
        table_id_o = new HashMap<>();
        table_o_id = new HashMap<>();
    }

    /**
     * This method returns if the given symbol is in the map.
     * @param symbol the symbol that you are searching for.
     * @return true if the symbol is in the map, false otherwise.
     */
    public boolean contains(@NotNull T symbol) {
        return table_o_id.containsKey(symbol);
    }

    /**
     * This method returns the size of the table.
     * @return the size of the table.
     */
    public int size() {
       return table_o_id.size();
    }

    /**
     * This method given a symbol adds it to the SymbolTable and creates a new ID for it. <br>
     * The method returns true if the symbol is new in the table, otherwise the ID won't change and
     * the method returns false.
     * @param symbol the symbol to add in the table.
     * @return true if the symbol is new in the table, false otherwise.
     */
    public boolean add(@NotNull T symbol) {
        if (!table_o_id.containsKey(symbol)) {
            table_o_id.put(symbol, table_id_o.size());
            table_id_o.put(table_id_o.size(), symbol);
            return true;
        }
        return false;
    }

    /**
     * This method given a symbol returns the corresponding ID.
     * @param symbol a symbol in the table.
     * @return the ID of the symbol.
     * @throws NoSuchElementException if the symbol is not in the table.
     */
    public int getId(@NotNull T symbol) throws NoSuchElementException {
        if (!table_o_id.containsKey(symbol))
            throw new NoSuchElementException("The symbol is not in the map");
        return table_o_id.get(symbol);
    }

    /**
     * This method given the ID returns the corresponding object.
     * @param id the ID of a symbol in the table.
     * @return the symbol of the ID.
     * @throws NoSuchElementException if the id is not associated to any object.
     */
    public @NotNull T getSymbol(int id) throws NoSuchElementException{
        if (!table_id_o.containsKey(id))
            throw new NoSuchElementException("The ID is not associated with any symbol");
        return table_id_o.get(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof SymbolTable<?> table)
            return table_id_o.equals(table.table_id_o);
        return false;
    }
}
