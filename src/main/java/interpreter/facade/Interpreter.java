package interpreter.facade;

import ast.CstToAstVisitor;
import ast.program.ProgramNode;
import ast.visitor.*;
import generated.CoDiNEParser;
import tree.ParseTree;
import utils.Fraction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Interpreter {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java interpreter.facade.Interpreter <source.codine>");
            System.exit(2);
        }

        String program;
        try {
            program = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Cannot read source file '" + args[0] + "': " + e.getMessage());
            System.exit(2);
            return;
        }

        List<Fraction> arg = read();
        CoDiNEParser parser = new CoDiNEParser();
        try {
            ParseTree cst = parser.parse(program);
            ProgramNode ast = CstToAstVisitor.visitStart(cst);
            ast.accept(new UnitDeclarationVisitor()); // checks unit are ok
            ast.accept(new TypeDeclarationVisitor()); // checks types expr ok
            ast.accept(new ValueExpressionVisitor()); // checks value expr ok
            ast.accept(new ReturnVisitor());          // checks define return ok
            // checks type-checking e glob_id not redeclared.
            DimensionalVectorVisitor dvv = new DimensionalVectorVisitor();
            ast.accept(dvv);
            ast.accept(new TypeCheckerVisitor(dvv));
            UnitFractionVisitor ufv = new UnitFractionVisitor();
            ast.accept(ufv);

            System.out.println(new RecursiveInterpreter(ufv.getUnitFract(), ast.functions()).run(arg));

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static List<Fraction> read() {
        Scanner s = new Scanner(System.in);
        List<Fraction> args = new LinkedList<>();
        try {
            while (s.hasNextLine()) {
                String line = s.nextLine().strip();
                if (line.isEmpty())
                    continue;
                String[] values = line.split("/");
                if (values.length == 1) {
                    args.add(Fraction.fromNumDen(
                            Long.parseLong(values[0].strip()), 1));
                } else if (values.length == 2) {
                    args.add(Fraction.fromNumDen(
                            Long.parseLong(values[0].strip()), Long.parseLong(values[1].strip())));
                } else {
                    throw new NumberFormatException(line);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Input Exception, it must be integer or integer/integer.");
            System.exit(2);
        }
        return args;
    }
}