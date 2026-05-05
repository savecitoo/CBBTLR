package lalrparser;

import grammar.ContextFreeGrammar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.ParseTree;
import utils.SymbolTable;

import javax.tools.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestParser {

    private static ParserTables compileAndLoad(Path javaFile, String className) throws Exception {

        Path classFile = javaFile.getParent().resolve(className + "ParserTables.class");

        boolean needsCompilation = Files.notExists(classFile) ||
                Files.getLastModifiedTime(javaFile).toMillis() > Files.getLastModifiedTime(classFile).toMillis();

        if (needsCompilation) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null)
                throw new IllegalStateException("JavaCompiler not available: use a JDK, not a JRE");

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

            try (StandardJavaFileManager fileManager =
                         compiler.getStandardFileManager(diagnostics, null, null)) {

                Iterable<? extends JavaFileObject> units =
                        fileManager.getJavaFileObjects(javaFile.toFile());

                JavaCompiler.CompilationTask task =
                        compiler.getTask(null, fileManager, diagnostics, null, null, units);
            }
        }

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{javaFile.getParent().toUri().toURL()},
                ParserTables.class.getClassLoader()
        )) {
            Class<?> clazz = classLoader.loadClass(className + "ParserTables");
            return (ParserTables) clazz.getDeclaredConstructor().newInstance();
        }
    }


    @ParameterizedTest
    @CsvSource({
            "n, true",
            "n - n, true",
            "n - n - n, true",
            "( n ), true",
            "( n - n ), true",
            "( ( n ) ), true",
            "( n - ( n ) ), true",
            "( n - ( n ) ) - n, true",
            "( n - n, false",
            "n -, false",
            "- n, false",
            "n n, false",
            "( ), false",
            "( n ) n, false",
            "n - ( ), false",
            "( n - ( n ) ) - n (, false",
            "( n - ( n ) ) ) - n, false",
            "f e, false",
            "n - ( n - ), false",
            "( n - ( n ) ) - ( n ), true",
            "( ( n - n ) - ( n ) ), true"
    })
    void testRecognizer(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E    \n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        List<String> tokens = Arrays.asList(input.split(" "));
        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated", "Test1", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "a a b d, true",
            "a a b c d, true",
            "b d, true",
            "b c c d, true",

            "a b d, true",
            "a a a b c c d, true",

            "a a, false",
            "b, false",
            "a b, false",
            "a a b c, false",
            "c d, false",
            "a d, false",

            "a a b d d, false",
            "b c d d, false"
    })
    void testLR0Parser(String input, boolean result) {
        List<String> tokens = Arrays.asList(input.split(" "));
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> A    \n" +
                        "A -> a A | b B\n" +
                        "B -> c B | d\n"
        );
        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated","Test2", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testParseBase() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E   \n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );

        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated","Test3", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            ParseTree tree = new ParseTree(
                    "E",
                    List.of( new ParseTree("T",
                            List.of(
                                    new ParseTree("(", true),
                                    new ParseTree("E", List.of(
                                            new ParseTree("T", List.of(new ParseTree("n", true)))
                                    )

                                    ),
                                    new ParseTree(")", true)
                            )))
            );
            List<String> tokens = Arrays.asList("( n )".split(" "));
            assertThat(parser.parseStrings(tokens)).isEqualTo(tree);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    void testParseComplex() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E   \n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated","Test4", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            ParseTree tree = new ParseTree("E", List.of(
                    new ParseTree("E", List.of(
                            new ParseTree("T", List.of(
                                    new ParseTree("n", true)
                            ))
                    )),
                    new ParseTree("-", true),
                    new ParseTree("T", List.of(
                            new ParseTree("(", true),
                            new ParseTree("E", List.of(
                                    new ParseTree("E", List.of(
                                            new ParseTree("T", List.of(
                                                    new ParseTree("n", true)
                                            ))
                                    )),
                                    new ParseTree("-", true),
                                    new ParseTree("T", List.of(
                                            new ParseTree("n", true)
                                    ))
                            )),
                            new ParseTree(")", true)
                    ))
            ));

            List<String> tokens = Arrays.asList("n - ( n - n )".split(" "));
            assertThat(parser.parseStrings(tokens)).isEqualTo(tree);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testParseUnrecognizedThrows() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E   \n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated","Test5", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            assertThatThrownBy(() -> parser.parseStrings(Arrays.asList("( n".split(" "))))
                    .isInstanceOf(RuntimeException.class);

            assertThatThrownBy(() -> parser.parseStrings(Arrays.asList("n -".split(" "))))
                    .isInstanceOf(RuntimeException.class);

            assertThatThrownBy(() -> parser.parseStrings(Arrays.asList("n + n".split(" "))))
                    .isInstanceOf(RuntimeException.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }

    @ParameterizedTest
    @CsvSource({
            "n, true",

            "n + n, true",
            "n - n, true",
            "n + n + n, true",
            "n - n - n, true",
            "n + n - n, true",

            "n * n, true",
            "n / n, true",
            "n * n * n, true",
            "n / n / n, true",
            "n * n / n, true",

            "n + n * n, true",
            "n * n + n, true",
            "n + n * n - n, true",
            "n * n + n / n, true",
            "n - n / n * n, true",

            "( n ), true",
            "( n + n ), true",
            "( n * n ), true",
            "( ( n ) ), true",
            "( n + n ) * n, true",
            "n * ( n + n ), true",
            "( n + n ) * ( n - n ), true",
            "( n * ( n + n ) ), true",
            "( ( n + n ) * ( n - n ) ), true",
            "n + ( n * ( n - n ) ) / n, true",
            "( n + n ) * ( n / ( n - n ) ), true",

            "n + n * n - n / n, true",
            "( n + n ) * n - n / ( n - n ), true",
            "( ( n + n ) ) * ( ( n ) ), true",
            "n * n + n * n + n * n, true",

            "n n, false",
            "n + + n, false",
            "n * * n, false",
            "n + - n, false",
            "* n, false",
            "/ n, false",
            "+ n, false",
            "- n, false",
            "n +, false",
            "n -,false",
            "n *,false",
            "n /,false",
            "n + n *, false",

            "( n, false",
            "n ), false",
            "( n + n, false",
            "n + n ), false",
            "( ( n ), false",
            "( n ) ), false",
            "( n + ( n * n ), false",
            "( n + n ) * ( n, false",

            "( ), false",
            "n + ( ), false",
            "( ) * n, false",

            "x, false",
            "n + x, false",
            "f e, false",
    })
    void testArithmetic(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S' -> S \n" +
                        "S -> E\n" +
                        "E -> E + T | E - T | T\n" +
                        "T -> T * F | T / F | F\n" +
                        "F -> ( E ) | n"
        );
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));

        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated","Test6", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "id, true",
            "num, true",

            "- id, true",
            "- - id, true",

            "id ^ id, true",
            "id ^ id ^ id, true",

            "id * id ^ id, true",
            "- id * num ^ num, true",

            "( id ), true",
            "( id + num ) * id, true",

            "id ^, false",
            "^ id, false",
            "id * ^ id, false",
            "-, false",
    })
    void testGrammar1(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S' -> S\n" +
                        "S -> E\n" +
                        "E -> E + T | E - T | T\n" +
                        "T -> T * F | T / F | F\n" +
                        "F -> - F | P ^ F | P\n" +
                        "P -> ( E ) | id | num"
        );

        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));


        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated","Test7", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "id = num, true",
            "id = id + num, true",
            "if id then id = num else id = num, true",
            "if id then if id then id = num else id = num, true",
            "while id do id = num, true",
            "{ id = num }, true",
            "{ id = num ; id = num }, true",
            "if id then id = num, true",
            "if id then if id then id = num, true",
            "if id then { id = num ; id = num } else id = num, true",
            "while id do if id then id = num else id = num, true",
            "if id then, false",
            "if then id = num, false",
            "{ }, false",
            "id =, false",
            "else id = num, false",
    })
    void testIfElseGrammar(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S' -> S\n" +
                        "S -> M | U\n" +
                        "M -> if E then M else M | while E do M | { L } | A\n" +
                        "U -> if E then S | if E then M else U\n" +
                        "L -> L ; S | S\n" +
                        "A -> id = E\n" +
                        // E stratificata: * lega più forte di +, entrambi left-assoc
                        "E -> E + T | T\n" +
                        "T -> T * F | F\n" +
                        "F -> ( E ) | id | num"
        );
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));
        Parser.generateParser(g, new SymbolTable<>(),"lalrparser.generated","Test8", "src/test/java/lalrparser/generated-test/");
        try {
            ParserTables tables = compileAndLoad(Path.of("src/test/java/lalrparser/generated-test/").resolve("MyLangParserTables.java"), "MyLang");
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
