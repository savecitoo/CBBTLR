package cli;

import grammar.ContextFreeGrammar;
import lalrparser.Parser;
import lexer.Lexer;
import utils.SymbolTable;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParserGenerator {

    public static void main(String[] args) {
        if (args.length != 4)
            throw new IllegalArgumentException(
                    "Usage: ParserGenerator <grammarFile> <className> <outputDir>"
            );
        generate(args[0], args[1], args[2], args[3]);
    }

    private static void generate(String grammarFile, String packageName,
                                 String className, String outputDir) {
        try {
            String content = Files.readString(Path.of(grammarFile));
            String[] file = content.split("Token:");
            if (file.length != 2)
                throw new IllegalArgumentException(
                        "Grammar file must contain a 'Token:' section");

            ContextFreeGrammar grammar = ContextFreeGrammar.fromString(file[0]);
            SymbolTable<String> encoding = new SymbolTable<>();
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