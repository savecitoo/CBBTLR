package testSintaxTreeCoDiNE;

import ast.CstToAstVisitor;
import ast.program.ProgramNode;
import generated.CoDiNEParser;
import lalrparser.ParseException;
import org.junit.jupiter.api.Test;
import tree.ParseTree;
import utils.Fraction;
import visitor.*;

import java.util.List;

public class TestVolatile {

    @Test
    void testProgram() {

        List<Fraction> arg = List.of(
                Fraction.fromNumDen(5,1)
        );
        CoDiNEParser parser = new CoDiNEParser();
        try {
            ParseTree cst = parser.parse("""
                    dimension Lunghezza with unit m
                    
                    unit cm = 1 / 100 * m
                    
                    def acc(n : Num) : cm {
                        if n <= 0 {
                            return 0 * cm
                        }
                        return acc(n - 1) + 1 * cm
                    }
                    
                    def main(n : Num) : cm {
                        return acc(n)
                    }
                    """);
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
            ScaleFracVisitor psfv = new ScaleFracVisitor(ufv.getUnitFract());
            ast.accept(psfv);
            System.out.println(new RecursiveInterpreter(ufv.getUnitFract(), psfv, ast.functions()).run(arg));
        } catch (NumberFormatException e) {
            System.out.println("RUNTIME ERROR: " + e.getMessage());
        } catch (ParseException | IllegalArgumentException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage());
        } catch (TypeException e) {
            System.err.println("TYPE EXCEPTION: " + e.getMessage());
        }
    }

}
