package testSintaxTreeCoDiNE;

import ast.AstNode;
import ast.CstToAstVisitor;
import ast.visitor.DimensionalVectorVisitor;
import generated.CoDiNEParser;
import org.junit.jupiter.api.Test;
import tree.ParseTree;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestVisitor {

    private final CoDiNEParser parser = new CoDiNEParser();

    private AstNode buildAst(String source) {
        ParseTree tree = parser.parse(source);
        return CstToAstVisitor.visit(tree);
    }

    private void runVisitor(String source) {
        AstNode ast = buildAst(source);
        DimensionalVectorVisitor dvv = new DimensionalVectorVisitor();
        ast.accept(dvv);
    }

    // -----------------------------------------------------------------
    // Programmi corretti: il visitor non deve lanciare eccezioni
    // -----------------------------------------------------------------

    @Test
    void testArith() {
        assertThatCode(() -> runVisitor("""
                def main(a : Num, b : Num) : Num {
                    return a * b + a / b
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testEnergy() {
        assertThatCode(() -> runVisitor("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s
                dimension Massa     with unit kg

                unit km = 1000 * m
                unit h  = 3600 * s
                unit N  = kg * m / s ^ 2
                unit g  = 981 / 100 * m / s ^ 2

                def forza_peso(m : kg) : N {
                    var f = m * g
                    return f
                }

                def main(massa : kg, altezza : m) : N * m {
                    var f = forza_peso(massa)
                    var e = f * altezza
                    return e
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testFact() {
        assertThatCode(() -> runVisitor("""
                def fact(n : Num) : Num {
                    if n <= 1 { return 1 } else { return n * fact(n - 1) }
                }

                def main(n : Num) : Num {
                    return fact(n)
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testFib() {
        assertThatCode(() -> runVisitor("""
                def fib(n : Num) : Num {
                    if n <= 1 { return n } else { return fib(n - 1) + fib(n - 2) }
                }

                def main(n : Num) : Num {
                    return fib(n)
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testMaxVal() {
        assertThatCode(() -> runVisitor("""
                dimension Lunghezza with unit m

                def max_len(a : m, b : m) : m {
                    if a > b { return a } else { return b }
                }

                def main(a : m, b : m) : m {
                    return max_len(a, b)
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testPythagorean() {
        assertThatCode(() -> runVisitor("""
                dimension Lunghezza with unit m

                def hyp_sq(a : m, b : m) : m ^ 2 {
                    return a * a + b * b
                }

                def main(a : m, b : m) : m ^ 2 {
                    return hyp_sq(a, b)
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testRationals() {
        assertThatCode(() -> runVisitor("""
                def main(a : Num, b : Num) : Num {
                    return 1 / a + 1 / b
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testSumN() {
        assertThatCode(() -> runVisitor("""
                def main(n : Num) : Num {
                    var i = 1
                    var s = 0
                    while i <= n {
                        s = s + i
                        i = i + 1
                    }
                    return s
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testUnitConv() {
        assertThatCode(() -> runVisitor("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                unit km = 1000 * m
                unit h  = 3600 * s

                def main(v : Num) : m / s {
                    return 18 * km / h + v * m / s
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testVelocity() {
        assertThatCode(() -> runVisitor("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : m / s {
                    return d / t
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testTypeWithExponentIntegerLiteralIsAllowed() {
        assertThatCode(() -> runVisitor("""
                dimension Lunghezza with unit m

                def main(a : m ^ 2) : m ^ 2 { return a }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testUnitWithIntegerLiteralIsAllowed() {
        assertThatCode(() -> runVisitor("""
                dimension Lunghezza with unit m
                unit km = 1000 * m

                def main() : m { return 0 }
                """)).doesNotThrowAnyException();
    }

    // -----------------------------------------------------------------
    // Programmi scorretti: il visitor deve lanciare eccezioni
    // I test che lanciano già nell'AST restano tali, qui verifichiamo
    // che il visitor stesso rilevi gli errori sui programmi
    // sintatticamente validi ma semanticamente scorretti.
    // -----------------------------------------------------------------

    @Test
    void testDimensionAfterUnit_throws() {
        assertThatThrownBy(() -> runVisitor("""
                unit km = 1000 * m
                dimension Lunghezza with unit m
                dimension Tempo     with unit s
                dimension Massa     with unit kg
                unit g  = 981 / 100 * m / s ^ 2
                
                def forza_peso(m : kg) : N {
                    var f = m * g
                    return f
                }
                
                def main(massa : kg, altezza : m) : N * m {
                    var f = forza_peso(massa)
                    var e = f * altezza
                    return e
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }
}