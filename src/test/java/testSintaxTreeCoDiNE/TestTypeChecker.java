package testSintaxTreeCoDiNE;

import ast.AstNode;
import ast.CstToAstVisitor;
import ast.visitor.DimensionalVectorVisitor;
import ast.visitor.TypeCheckerVisitor;
import generated.CoDiNEParser;
import org.junit.jupiter.api.Test;
import tree.ParseTree;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestTypeChecker {

    private final CoDiNEParser parser = new CoDiNEParser();

    private AstNode buildAst(String source) {
        ParseTree tree = parser.parse(source);
        return CstToAstVisitor.visit(tree);
    }

    private void runTypeChecker(String source) {
        AstNode ast = buildAst(source);
        DimensionalVectorVisitor dvv = new DimensionalVectorVisitor();
        ast.accept(dvv);
        TypeCheckerVisitor tcv = new TypeCheckerVisitor(dvv);
        ast.accept(tcv);
    }

    // -----------------------------------------------------------------
    // Programmi corretti: il type checker non deve lanciare eccezioni
    // -----------------------------------------------------------------

    @Test
    void testArith() {
        assertThatCode(() -> runTypeChecker("""
                def main(a : Num, b : Num) : Num {
                    return a * b + a / b
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testEnergy() {
        assertThatCode(() -> runTypeChecker("""
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
        assertThatCode(() -> runTypeChecker("""
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
        assertThatCode(() -> runTypeChecker("""
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
        assertThatCode(() -> runTypeChecker("""
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
        assertThatCode(() -> runTypeChecker("""
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
        assertThatCode(() -> runTypeChecker("""
                def main(a : Num, b : Num) : Num {
                    return 1 / a + 1 / b
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testSumN() {
        assertThatCode(() -> runTypeChecker("""
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
        assertThatCode(() -> runTypeChecker("""
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
        assertThatCode(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : m / s {
                    return d / t
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testLocalVariableShadowsUnit() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Massa     with unit kg

                def f(m : kg) : kg {
                    var x = m * m
                    return x
                }

                def main(a : kg) : kg {
                    return f(a)
                }
                """));
    }

    @Test
    void testWhileWithCompatibleCondition() {
        assertThatCode(() -> runTypeChecker("""
                def main(n : Num) : Num {
                    var i = 1
                    while i <= n {
                        i = i + 1
                    }
                    return i
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testIfElseWithCompatibleBranches() {
        assertThatCode(() -> runTypeChecker("""
                dimension Lunghezza with unit m

                def main(a : m, b : m) : m {
                    if a > b { return a } else { return b }
                }
                """)).doesNotThrowAnyException();
    }

    // -----------------------------------------------------------------
    // Programmi scorretti: il type checker deve lanciare eccezioni
    // -----------------------------------------------------------------

    @Test
    void testReturnTypeMismatch_throws() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : m {
                    return d / t
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAddIncompatibleTypes_throws() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : m {
                    return d + t
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSubIncompatibleTypes_throws() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : m {
                    return d - t
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testFunctionCallWrongArgType_throws() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def f(x : m) : m { return x }

                def main(t : s) : m {
                    return f(t)
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testComparisonIncompatibleTypes_throws() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : Num {
                    if d < t { return 1 } else { return 0 }
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testWhileConditionIncompatibleTypes_throws() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : Num {
                    var i = 0
                    while d <= t {
                        i = i + 1
                    }
                    return i
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAssignmentTypeMismatch_throws() {
        assertThatThrownBy(() -> runTypeChecker("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(d : m, t : s) : m {
                    var x = d
                    x = t
                    return x
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }
}