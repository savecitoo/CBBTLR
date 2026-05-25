package lalrparser;

import grammar.ContextFreeGrammar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utils.SymbolIdentifier;

import javax.tools.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestParser {

    private static ParserTables compileAndLoad(Path javaFile, String packageName, String className) throws Exception {

        String qualifiedName = packageName + "." + className + "ParserTables";
        Path sourceRoot = Path.of("src/test/java");
        Path classFile = sourceRoot
                .resolve(packageName.replace('.', '/'))
                .resolve(className + "ParserTables.class");

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

                boolean success = task.call();
                if (!success) {
                    String errors = diagnostics.getDiagnostics().stream()
                            .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                            .map(Object::toString)
                            .collect(Collectors.joining("\n"));
                    throw new IllegalStateException("Compilation failed:\n" + errors);
                }
            }
        }

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{sourceRoot.toUri().toURL()},
                ParserTables.class.getClassLoader()
        )) {
            Class<?> clazz = classLoader.loadClass(qualifiedName);
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

        String className = "Test1";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";

        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);

        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void testParseUnrecognizedThrows() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                "S -> E   \n" +
                        "E -> E - T | T\n" +
                        "T -> n | ( E )"
        );
        String className = "Test2";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
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

        String className = "Test3";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
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


        String className = "Test4";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
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
                        "E -> E + T | T\n" +
                        "T -> T * F | F\n" +
                        "F -> ( E ) | id | num"
        );
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));
        String className = "Test5";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "a b, true",
            "b, true",
            "d, false",
            "a, false"
    })
    void testEpsilonGrammar(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString("S' -> S\n" +
        "S  -> A B\n" +
        "A  -> a | ε\n" +
        "B  -> b");
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));
        String className = "Test6";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "a b c, false",
            "b c, false",
            "c, false",
            "a a c, false",
            "a a a b b c, false",
            "a b b c, false",
            "a b, true",
            "a, false",
            "d, false",
            "a c, false",
            "b a c, false",
            "c a b, false"
    })
    void testEpsilonGrammar2(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString("""
                S' -> S
                S  -> A B
                A  -> a | ε
                B  -> b""");
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));
        String className = "Test7";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "a b c d, true",
            "b c d, true",
            "c d, true",
            "d, true",
            "a c d, true",
            "a b d, true",
            "a d, true",
            "a b d, true",
            "a b c d, true",
            "b c, false",
            "a b c, false",
            "a b d c, false",
            "a, false",
            "a b, false",
            "a c, false",
            "d a b c, false",
            "d c b a, false",
            "d d, false",
            "a b c d d, false"
    })
    void testEpsilonGrammar3(String input, boolean result) {
        ContextFreeGrammar g = ContextFreeGrammar.fromString(
                """
                        S' -> S
                        S -> A B C D
                        A -> a | ε
                        B -> b | ε
                        C -> c | ε
                        D -> d"""
        );
        List<String> tokens = input.isBlank()
                ? List.of()
                : Arrays.asList(input.split(" "));
        String className = "Test8";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(tokens)).isEqualTo(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    @Test
    void testEpsilonGrammarLR1() {
        ContextFreeGrammar g = ContextFreeGrammar.fromString("""
                S' -> S
                S -> A B c
                A -> a
                B -> b | ε""");
        String className = "Test9";
        String packageName = "lalrparser.generated";
        String outputDir = "src/test/java/lalrparser/generated/";
        Parser.generateParser(g, new SymbolIdentifier<>(), packageName, className, outputDir);
        Path javaFile = Path.of(outputDir).resolve(className + "ParserTables.java");
        try {
            ParserTables tables = compileAndLoad(javaFile, packageName, className);
            Parser parser = new Parser(tables);
            assertThat(parser.isRecognizedString(List.of("a", "c"))).isTrue();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
