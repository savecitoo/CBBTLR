package testSintaxTreeCoDiNE;

import ast.CstToAstVisitor;
import ast.program.ParameterNode;
import ast.program.ProgramNode;
import generated.CoDiNEParser;
import lalrparser.ParseException;
import org.junit.jupiter.api.Test;
import tree.ParseTree;
import utils.Fraction;
import visitor.*;

import java.util.ArrayList;
import java.util.List;

public class TestVolatile {

    @Test
    void testProgram() {

        List<Fraction> arg = List.of(
                Fraction.fromNumDen(3,1)
        );
        CoDiNEParser parser = new CoDiNEParser();
        try {
            ParseTree cst = parser.parse("""
                    dimension Lunghezza with unit m
                    
                    def main(n : Num) : Num {
                        var i = 0
                        var tot = 0
                        var j = 0
                        while i < n {
                    
                    
                            while j < n {
                                tot = tot + 1
                                j = j + 1
                            }
                            i = i + 1
                        }
                        return tot
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
            List<ParameterNode> main_arg = ast.functions().get("main").parameter();
            if (arg.size() != main_arg.size())
                throw new IllegalStateException("The number of input parameters differs from the number of parameters in the main");
            List<Fraction> can_arg = new ArrayList<>();
            for (int i = 0; i < arg.size(); i++) {
                CanonicalRepresentationTransformer crt = new CanonicalRepresentationTransformer(
                        ufv.getUnitFract(),
                        arg.get(i),
                        main_arg.get(i).type()
                );
                can_arg.add(crt.getCanonical());
            }
            RecursiveInterpreter interpreter = new RecursiveInterpreter(ufv.getUnitFract(), can_arg);
            System.out.println(ast.accept(interpreter));
        } catch (NumberFormatException e) {
            System.out.println("RUNTIME ERROR: " + e.getMessage());
        } catch (ParseException | IllegalArgumentException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage());
        } catch (TypeException e) {
            System.err.println("TYPE ERROR: " + e.getMessage());
        }
    }

}
