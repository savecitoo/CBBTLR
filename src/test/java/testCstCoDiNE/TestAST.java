package testCstCoDiNE;

import ast.AstNode;
import ast.CstToAstVisitor;
import ast.program.ProgramNode;
import generated.CoDiNEParser;
import org.junit.jupiter.api.Test;
import tree.ParseTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestAST {

    private final CoDiNEParser parser = new CoDiNEParser();

    private AstNode buildAst(String source) {
        ParseTree tree = parser.parse(source);
        return CstToAstVisitor.visit(tree);
    }

    // -----------------------------------------------------------------
    // Programmi semanticamente corretti: la costruzione dell'AST non
    // deve lanciare eccezioni.
    // -----------------------------------------------------------------

    @Test
    void testInputArith() {
        assertThatCode(() -> buildAst("""
                def main(a : Num, b : Num) : Num {
                    return a * b + a / b
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testInputEnergy() {
        AstNode ast = buildAst("""
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
                """);
        assertThat(ast).isInstanceOf(ProgramNode.class);
    }

    @Test
    void testInputFact() {
        assertThatCode(() -> buildAst("""
                def fact(n : Num) : Num {
                    if n <= 1 { return 1 } else { return n * fact(n - 1) }
                }

                def main(n : Num) : Num {
                    return fact(n)
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testInputFib() {
        assertThatCode(() -> buildAst("""
                def fib(n : Num) : Num {
                    if n <= 1 { return n } else { return fib(n - 1) + fib(n - 2) }
                }

                def main(n : Num) : Num {
                    return fib(n)
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testInputMaxVal() {
        assertThatCode(() -> buildAst("""
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
    void testInputPythagorean() {
        assertThatCode(() -> buildAst("""
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
    void testInputRationals() {
        assertThatCode(() -> buildAst("""
                def main(a : Num, b : Num) : Num {
                    return 1 / a + 1 / b
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testInputSumN() {
        assertThatCode(() -> buildAst("""
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
    void testInputUnitConv() {
        assertThatCode(() -> buildAst("""
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
    void testInputVelocity() {
        assertThatCode(() -> buildAst("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : m / s {
                    return d / t
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testDimensionAfterUnit_throws() {
        // 'unit km' usa 'm' prima che 'dimension Lunghezza with unit m' lo dichiari
        assertThatThrownBy(() -> buildAst("""
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
                """))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("'m'");
    }

    @Test
    void testDimensionAfterFunc_throws() {
        assertThatThrownBy(() -> buildAst("""
                unit km = 1000 * m
                dimension Tempo     with unit s
                dimension Massa     with unit kg
                unit g  = 981 / 100 * m / s ^ 2

                def forza_peso(m : kg) : N {
                    var f = m * g
                    return f
                }

                dimension Lunghezza with unit m

                def main(massa : kg, altezza : m) : N * m {
                    var f = forza_peso(massa)
                    var e = f * altezza
                    return e
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUnitAfterFunction_throws() {
        assertThatThrownBy(() -> buildAst("""
                unit km = 1000 * m
                dimension Tempo     with unit s
                dimension Massa     with unit kg
                unit g  = 981 / 100 * m / s ^ 2

                def forza_peso(m : kg) : N {
                    var f = m * g
                    return f
                }

                unit km = 1000 * m

                def main(massa : kg, altezza : m) : N * m {
                    var f = forza_peso(massa)
                    var e = f * altezza
                    return e
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    void testDuplicateDimensionName_throws() {
        assertThatThrownBy(() -> buildAst("""
                dimension Lunghezza with unit m
                dimension Lunghezza with unit cm

                def main() : m { return 0 }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDuplicateUnitSymbol_throws() {
        assertThatThrownBy(() -> buildAst("""
                dimension Lunghezza with unit m
                unit km = 1000 * m
                unit km = 100 * m

                def main() : m { return 0 }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDuplicateFunctionName_throws() {
        assertThatThrownBy(() -> buildAst("""
                def f(a : Num) : Num { return a }
                def f(a : Num) : Num { return a }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUnitUsesUndefinedDimension_throws() {
        assertThatThrownBy(() -> buildAst("""
                unit km = 1000 * xyz

                def main() : Num { return 0 }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParameterTypeUsesUndefinedDimension_throws() {
        assertThatThrownBy(() -> buildAst("""
                def f(a : xyz) : Num { return 0 }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testReturnTypeUsesUndefinedDimension_throws() {
        assertThatThrownBy(() -> buildAst("""
                def f(a : Num) : xyz { return 0 }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testParameterTypeWithBareIntegerLiteral_throws() {
        assertThatThrownBy(() -> buildAst("""
                dimension Lunghezza with unit m

                def f(a : 3 * m) : m { return a }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testReturnTypeWithBareIntegerLiteral_throws() {
        assertThatThrownBy(() -> buildAst("""
                dimension Lunghezza with unit m

                def f(a : m) : 2 * m { return a }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testTypeWithExponentIntegerLiteralIsAllowed() {
        assertThatCode(() -> buildAst("""
                dimension Lunghezza with unit m

                def f(a : m ^ 2) : m ^ 2 { return a }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testUnitWithIntegerLiteralIsAllowed() {
        assertThatCode(() -> buildAst("""
                dimension Lunghezza with unit m
                unit km = 1000 * m

                def main() : m { return 0 }
                """)).doesNotThrowAnyException();
    }
}