package testSintaxTreeCoDiNE;

import generated.CoDiNEParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCST {

    private final CoDiNEParser parser = new CoDiNEParser();

    @Test
    void testInputArith() {
        assertThat(parser.isRecognized(
                """
                        # Operazioni aritmetiche su Num — verifica precedenza e frazioni esatte
                        # Calcola a * b + a / b
                        def main(a : Num, b : Num) : Num {
                            return a * b + a / b
                        }
                      """
        )).isTrue();
    }

    @Test
    void testInputEnergy() {
        assertThat(parser.isRecognized(
                """
                          # Energia potenziale gravitazionale — esempio del testo
                          # Dimostra: dichiarazioni di dimensioni e unità, funzioni, tipo di ritorno
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
                      """
        )).isTrue();
    }

    @Test
    void testInputFact() {
        assertThat(parser.isRecognized("""
                # Fattoriale — ricorsione su Num
                def fact(n : Num) : Num {
                    if n <= 1 { return 1 } else { return n * fact(n - 1) }
                }
                
                def main(n : Num) : Num {
                    return fact(n)
                }
                """)).isTrue();
    }

    @Test
    void testInputFib() {
        assertThat(parser.isRecognized("""
                # Fibonacci — ricorsione su Num
                def fib(n : Num) : Num {
                    if n <= 1 { return n } else { return fib(n - 1) + fib(n - 2) }
                }
                
                def main(n : Num) : Num {
                    return fib(n)
                }
                """)).isTrue();
    }

    @Test
    void testInputMaxVal() {
        assertThat(parser.isRecognized("""
                # Massimo di due lunghezze — if/else con tipo dimensionale
                 dimension Lunghezza with unit m
                
                 def max_len(a : m, b : m) : m {
                     if a > b { return a } else { return b }
                 }
                
                 def main(a : m, b : m) : m {
                     return max_len(a, b)
                 }
                """)).isTrue();
    }

    @Test
    void testInputPythagorean() {
        assertThat(parser.isRecognized("""
                # Teorema di Pitagora — dimostra l'operatore ^ e il tipo m^2
                # Restituisce il quadrato dell'ipotenusa: a^2 + b^2
                dimension Lunghezza with unit m
                
                def hyp_sq(a : m, b : m) : m ^ 2 {
                    return a * a + b * b
                }
                
                def main(a : m, b : m) : m ^ 2 {
                    return hyp_sq(a, b)
                }
                """)).isTrue();
    }

    @Test
    void testInputRationals() {
        assertThat(parser.isRecognized("""
                # Frazioni esatte — verifica che l'aritmetica sia razionale e non floating-point
                # Calcola 1/a + 1/b = (a+b)/(a*b)
                def main(a : Num, b : Num) : Num {
                    return 1 / a + 1 / b
                }
                """)).isTrue();
    }

    @Test
    void testInputSumN() {
        assertThat(parser.isRecognized("""
                # Somma 1 + 2 + ... + n — ciclo while su Num
                def main(n : Num) : Num {
                    var i = 1
                    var s = 0
                    while i <= n {
                        s = s + i
                        i = i + 1
                    }
                    return s
                }
                """)).isTrue();
    }

    @Test
    void testInputUnitConv() {
        assertThat(parser.isRecognized("""
                # Conversione automatica di unità nella somma
                # 18 km/h + v m/s: le due velocità vengono portate in forma canonica (m/s) prima della somma
                dimension Lunghezza with unit m
                dimension Tempo     with unit s
                
                unit km = 1000 * m
                unit h  = 3600 * s
                
                def main(v : Num) : m / s {
                    return 18 * km / h + v * m / s
                }
                """)).isTrue();
    }

    @Test
    void testInputVelocity() {
        assertThat(parser.isRecognized("""
                # Velocità media — divisione con tipo dimensionale m/s
                # Dimostra il tipo di ritorno frazionario con unità
                dimension Lunghezza with unit m
                dimension Tempo     with unit s
                
                def main(d : m, t : s) : m / s {
                    return d / t
                }
                """)).isTrue();
    }

    @Test
    void testDeclAfterIstr() {
        assertThat(parser.isRecognized(
                """
                # Energia potenziale gravitazionale — esempio del testo
                # Dimostra: dichiarazioni di dimensioni e unità, funzioni, tipo di ritorno
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
                    var d = 10
                }
        """)).isFalse();
}


    @Test
    void emptyProgram() {
        assertThat(parser.isRecognized("")).isFalse();
    }

    @Test
    void testDimensionAfterUnit() {
        assertThat(parser.isRecognized(
                """
                # Energia potenziale gravitazionale — esempio del testo
                # Dimostra: dichiarazioni di dimensioni e unità, funzioni, tipo di ritorno
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
        """)).isTrue();
    }

    @Test
    void testDimensionAfterFunc() {
        assertThat(parser.isRecognized(
                """
                # Energia potenziale gravitazionale — esempio del testo
                # Dimostra: dichiarazioni di dimensioni e unità, funzioni, tipo di ritorno
                unit km = 1000 * m
                dimension Tempo     with unit s
                dimension Massa     with unit kg
                unit g  = 981 / 100 * m / s ^ 2

                def forza_peso(m : kg) : N {
                        var f = m * g
                    return f
                }
                
                dimension Lunghezza     with unit m

                def main(massa : kg, altezza : m) : N * m {
                    var f = forza_peso(massa)
                    var e = f * altezza
                    return e
                }
        """)).isTrue();
    }

    @Test
    void testUnitAfterFunction() {
        assertThat(parser.isRecognized(
                """
                # Energia potenziale gravitazionale — esempio del testo
                # Dimostra: dichiarazioni di dimensioni e unità, funzioni, tipo di ritorno
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
        """)).isTrue();
    }
}
