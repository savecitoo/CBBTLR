package testSintaxTreeCoDiNE;

import ast.AstNode;
import ast.CstToAstVisitor;
import ast.visitor.ValueExpressionVisitor;
import generated.CoDiNEParser;
import org.junit.jupiter.api.Test;
import tree.ParseTree;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TestValueExpressionVisitor {

    private final CoDiNEParser parser = new CoDiNEParser();

    private void runValueExpressionCheck(String source) {
        ParseTree tree = parser.parse(source);
        AstNode ast = CstToAstVisitor.visit(tree);
        ast.accept(new ValueExpressionVisitor());
    }

    // -----------------------------------------------------------------
    // Programmi corretti: il visitor non deve lanciare eccezioni
    // -----------------------------------------------------------------

    @Test
    void testIntLiteralAlone() {
        assertThatCode(() -> runValueExpressionCheck("""
                def main(n : Num) : Num {
                    return 42
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testParameterAlone() {
        assertThatCode(() -> runValueExpressionCheck("""
                def main(n : Num) : Num {
                    return n
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testFunctionCallAlone() {
        assertThatCode(() -> runValueExpressionCheck("""
                def f(x : Num) : Num { return x }

                def main(n : Num) : Num {
                    return f(n)
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testIntTimesUnit_isValueExpression() {
        // 3 * m appartiene a V: basta l'intero, l'unità a fianco è lecita
        assertThatCode(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m {
                    return 3 * m
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testUnitTimesInt_orderDoesNotMatter() {
        assertThatCode(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m {
                    return m * 3
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testVariableTimesUnits() {
        // v * m / s: l'identificatore di valore v qualifica tutta l'espressione
        assertThatCode(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(v : Num) : m / s {
                    return v * m / s
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testPowWithValueBase() {
        // x ^ 2 è di valore perché la base è una variabile
        assertThatCode(() -> runValueExpressionCheck("""
                def main(x : Num) : Num {
                    return x ^ 2
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testPowWithIntBase() {
        // 2 ^ 3: l'intero in base (a sinistra di ^) conta
        assertThatCode(() -> runValueExpressionCheck("""
                def main(n : Num) : Num {
                    return n ^ 3
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testDeclarationThenUse() {
        // la variabile dichiarata entra nello scope ed è usabile dopo
        assertThatCode(() -> runValueExpressionCheck("""
                def main(n : Num) : Num {
                    var a = n + 1
                    var b = a * 2
                    return b
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testConditionsWithValueOperands() {

        assertThatCode(() -> runValueExpressionCheck("""
                def main(n : Num) : Num {
                    var i = 0
                    while i < n and not i > 100 {
                        i = i + 1
                    }
                    if i <= n or true { return i } else { return 0 }
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testTrueFalseLiteralConditions() {
        assertThatCode(() -> runValueExpressionCheck("""
                def main(n : Num) : Num {
                    if true { return 1 } else { return 0 }
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testParameterShadowsUnit() {
        // il parametro m è un identificatore di valore anche se esiste l'unità m
        assertThatCode(() -> runValueExpressionCheck("""
                dimension Massa with unit m

                def main(m : m) : m {
                    return m
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testUnitDeclarationsAreNotChecked() {
        // le espressioni di unità (destra di `unit =`) NON devono essere
        // soggette a questo controllo: m / s ^ 2 lì è lecito
        assertThatCode(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                unit acc = m / s ^ 2

                def main(a : Num) : m / s ^ 2 {
                    return a * acc
                }
                """)).doesNotThrowAnyException();
    }

    @Test
    void testScopeResetBetweenFunctions_validSecondFunction() {
        // ogni funzione ha il proprio scope: il parametro di f non deve
        // "sporcare" il controllo in main e viceversa
        assertThatCode(() -> runValueExpressionCheck("""
                def f(x : Num) : Num { return x }

                def main(y : Num) : Num {
                    return f(y)
                }
                """)).doesNotThrowAnyException();
    }

    // -----------------------------------------------------------------
    // Programmi scorretti: il visitor deve lanciare IllegalArgumentException
    // -----------------------------------------------------------------

    @Test
    void testReturnPureUnit_throws() {
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m {
                    return m
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testReturnDimensionalExpression_throws() {
        // m / s è in U ma non in V: nessun intero, variabile o chiamata
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(v : m / s) : m / s {
                    return m / s
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testDeclarationWithPureUnit_throws() {
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m {
                    var x = m
                    return a
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAssignmentWithPureUnit_throws() {
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m {
                    var x = a
                    x = m
                    return x
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testIntOnlyRightOfPow_throws() {
        // l'intero a destra di ^ non conta: m ^ 2 resta dimensionale
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m ^ 2 {
                    return m ^ 2
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testComparisonWithPureUnitOperand_throws() {
        // in un confronto ENTRAMBI i lati devono essere espressioni di valore
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : Num {
                    if a > m { return 1 } else { return 0 }
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testComparisonBothPureUnits_throws() {
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m
                dimension Tempo     with unit s

                def main(a : Num) : Num {
                    if m < s { return 1 } else { return 0 }
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testWhileConditionWithPureUnit_throws() {
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : Num {
                    var i = 0
                    while m >= a {
                        i = i + 1
                    }
                    return i
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInvalidExpressionInsideIfBranch_throws() {
        // il controllo deve entrare anche nei rami dell'if
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m {
                    var x = a
                    if a > 0 * m { x = m } else { x = a }
                    return x
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testInvalidExpressionInsideWhileBody_throws() {
        assertThatThrownBy(() -> runValueExpressionCheck("""
                dimension Lunghezza with unit m

                def main(a : m) : m {
                    var x = a
                    var i = 0
                    while i < 3 {
                        x = m
                        i = i + 1
                    }
                    return x
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testVariableUsedBeforeDeclaration_throws() {
        // b non è ancora nello scope quando viene dichiarata a
        assertThatThrownBy(() -> runValueExpressionCheck("""
                def main(n : Num) : Num {
                    var a = b
                    var b = 1
                    return a
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testScopeResetBetweenFunctions_throws() {
        // x è parametro di f, non deve essere visibile in main
        assertThatThrownBy(() -> runValueExpressionCheck("""
                def f(x : Num) : Num { return x }

                def main(n : Num) : Num {
                    return x
                }
                """)).isInstanceOf(IllegalArgumentException.class);
    }
}