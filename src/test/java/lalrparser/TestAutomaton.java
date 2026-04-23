package lalrparser;

import grammar.ContextFreeGrammar;

public class TestAutomaton {

    public static void main(String[] args) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E\n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        Parser.Automaton automaton = Parser.Automaton.fromGrammar(g);
        System.out.println(automaton);
    }

}
