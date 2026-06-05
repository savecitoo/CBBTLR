package cli;

import grammar.ContextFreeGrammar;
import lalrparser.Parser;
import lexer.Lexer;
import utils.ObjectIdentifier;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class that generates a lexer/parser pair from a combined grammar file.
 *
 * <p>A grammar file is a plain-text file divided into two sections by a {@code Token:} marker:
 * <ul>
 *   <li>The section <em>before</em> {@code Token:} defines the context-free grammar
 *       ({@link ContextFreeGrammar}).</li>
 *   <li>The section <em>after</em> {@code Token:} defines the lexical rules ({@link Lexer}).</li>
 * </ul>
 *
 * <p>Running this generator produces two artifacts:
 * <ol>
 *   <li>A serialized {@link Lexer} saved to
 *       {@code src/main/resources/<packagePath>/<className>Lexer.ser}.</li>
 *   <li>The {@code .java} parser source files written to the specified output directory.</li>
 * </ol>
 *
 * <p>This class is not instantiable; use the {@link #main(String[])} entry point directly.</p>
 *
 * @see ContextFreeGrammar
 * @see Lexer
 * @see Parser
 */
public class ParserGenerator {

    /**
     * Entry point of the {@code ParserGenerator} application.
     * <p>
     * Reads a grammar file containing a context-free grammar definition and a lexical
     * rules section separated by a {@code Token:} marker, then produces two artifacts:
     * <ul>
     *   <li>A serialized {@link Lexer} saved under
     *       {@code src/main/resources/<packagePath>/<className>Lexer.ser}, where
     *       {@code packagePath} is derived from {@code packageName} by replacing dots
     *       with the OS file separator.</li>
     *   <li>The generated {@code .java} parser sources written to {@code outputDir}.</li>
     * </ul>
     *
     * @param args command-line arguments, in order:
     *             <ol>
     *               <li>{@code grammarFile}  – path to the grammar file</li>
     *               <li>{@code packageName} – target Java package name (e.g. {@code "com.example.generated"})</li>
     *               <li>{@code className}   – base name of the class to generate</li>
     *               <li>{@code outputDir}   – output directory for the generated {@code .java} sources</li>
     *             </ol>
     * @throws IllegalArgumentException if the number of arguments is not exactly four,
     *                                  if the grammar file cannot be read,
     *                                  or if it does not contain exactly one {@code Token:} section
     */
    public static void main(String[] args) {
        if (args.length != 4)
            throw new IllegalArgumentException(
                    "Usage: ParserGenerator <grammarFile> <className> <outputDir>"
            );
        generate(args[0], args[1], args[2], args[3]);
    }

    /**
     * Reads a grammar file, extracts its lexical and syntactic definitions,
     * serializes the resulting {@link Lexer}, and generates the corresponding parser source.
     *
     * <p>The grammar file must contain exactly one {@code Token:} section acting as a
     * delimiter: everything before it is interpreted as a context-free grammar
     * ({@link ContextFreeGrammar}), and everything after it as lexical rules ({@link Lexer}).</p>
     *
     * <p>The serialized {@link Lexer} object is saved to:
     * <pre>
     *   src/main/resources/&lt;packagePath&gt;/&lt;className&gt;Lexer.ser
     * </pre>
     * where {@code packagePath} is the {@code packageName} with dots replaced by the
     * OS file separator.
     *
     * <p>The generated {@code .java} parser sources are written to the directory
     * specified by {@code outputDir}, via
     * {@link Parser#generateParser(ContextFreeGrammar, ObjectIdentifier, String, String, String)}.</p>
     *
     * @param grammarFile  path to the grammar file to read
     * @param packageName  Java package name (e.g. {@code "com.example.generated"}),
     *                     used both for the resources path and the generated sources
     * @param className    base class name; the serialized lexer file will be named
     *                     {@code <className>Lexer.ser}
     * @param outputDir    root directory where the generated {@code .java} files will be written
     * @throws IllegalArgumentException if the grammar file cannot be read,
     *                                  or if it does not contain exactly one {@code Token:} section
     */
    private static void generate(String grammarFile, String packageName,
                                 String className, String outputDir) {
        try {
            if (!grammarFile.endsWith(".cbg")) {
                grammarFile += ".cbg";
            }
            String content = Files.readString(Path.of(grammarFile));
            String[] file = content.split("Token:");
            if (file.length != 2)
                throw new IllegalArgumentException(
                        "Grammar file must contain a 'Token:' section");

            ContextFreeGrammar grammar = ContextFreeGrammar.fromString(file[0].trim());
            ObjectIdentifier<String> encoding = new ObjectIdentifier<>();
            Lexer lexer = Lexer.fromString(file[1], encoding);

            // deriva il path delle resources dal packageName
            // "generated" → "src/main/resources/generated/"
            String packagePath = packageName.replace(".", File.separator);
            Path serFile = Path.of("src/main/resources")
                    .resolve(packagePath)
                    .resolve(className + "Lexer.ser");
            Files.createDirectories(serFile.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    Files.newOutputStream(serFile))) {
                oos.writeObject(lexer);
            }

            // i .java vanno in outputDir come prima
            Parser.generateParser(grammar, encoding, packageName, className, outputDir);

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Cannot read grammar file: " + grammarFile, e);
        }
    }


}