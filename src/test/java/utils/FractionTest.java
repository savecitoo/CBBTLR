package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FractionTest {

    // ---------------------------------------------------------------
    // Costruzione / fromNumDen
    // ---------------------------------------------------------------

    @Test
    void fromNumDen_basicFraction() {
        Fraction f = Fraction.fromNumDen(2, 4);
        assertEquals(Fraction.fromNumDen(1, 2), f);
        assertEquals("1/2", f.toString());
    }

    @Test
    void fromNumDen_alreadyReduced() {
        Fraction f = Fraction.fromNumDen(3, 7);
        assertEquals("3/7", f.toString());
    }

    @Test
    void fromNumDen_zeroNumerator_returnsZeroConstant() {
        Fraction f = Fraction.fromNumDen(0, 5);
        assertEquals(Fraction.ZERO, f);
        assertEquals("0", f.toString());
    }

    @Test
    void fromNumDen_numEqualsDen_returnsOneConstant() {
        Fraction f = Fraction.fromNumDen(5, 5);
        assertEquals(Fraction.ONE, f);
        assertEquals("1", f.toString());
    }

    @Test
    void fromNumDen_negativeNumerator() {
        Fraction f = Fraction.fromNumDen(-2, 4);
        assertEquals(Fraction.fromNumDen(-1, 2), f);
        assertEquals("-1/2", f.toString());
    }

    @Test
    void fromNumDen_negativeDenominator_signMovedToNumerator() {
        Fraction f = Fraction.fromNumDen(2, -4);
        assertEquals(Fraction.fromNumDen(-1, 2), f);
        assertEquals("-1/2", f.toString());
    }

    @Test
    void fromNumDen_bothNegative_resultPositive() {
        Fraction f = Fraction.fromNumDen(-2, -4);
        assertEquals(Fraction.fromNumDen(1, 2), f);
        assertEquals("1/2", f.toString());
    }

    @Test
    void fromNumDen_denominatorZero_throws() {
        assertThrows(IllegalArgumentException.class, () -> Fraction.fromNumDen(1, 0));
    }

    @Test
    void fromNumDen_zeroOverZero_throws() {
        // num == 0 non deve mascherare il controllo su den == 0
        assertThrows(IllegalArgumentException.class, () -> Fraction.fromNumDen(0, 0));
    }

    @Test
    void fromNumDen_integerValue_denIsOne() {
        Fraction f = Fraction.fromNumDen(4, 1);
        assertEquals("4", f.toString());
    }

    @Test
    void fromNumDen_denNegativeOne() {
        Fraction f = Fraction.fromNumDen(3, -1);
        assertEquals("-3", f.toString());
    }

    // ---------------------------------------------------------------
    // add
    // ---------------------------------------------------------------

    @Test
    void add_sameDenominator() {
        Fraction a = Fraction.fromNumDen(1, 4);
        Fraction b = Fraction.fromNumDen(2, 4);
        assertEquals(Fraction.fromNumDen(3, 4), a.add(b));
    }

    @Test
    void add_differentDenominator() {
        Fraction a = Fraction.fromNumDen(1, 2);
        Fraction b = Fraction.fromNumDen(1, 3);
        assertEquals(Fraction.fromNumDen(5, 6), a.add(b));
    }

    @Test
    void add_resultingInZero() {
        Fraction a = Fraction.fromNumDen(1, 3);
        Fraction b = Fraction.fromNumDen(-1, 3);
        assertEquals(Fraction.ZERO, a.add(b));
    }

    @Test
    void add_withNegativeFraction() {
        Fraction a = Fraction.fromNumDen(1, 2);
        Fraction b = Fraction.fromNumDen(-1, 4);
        assertEquals(Fraction.fromNumDen(1, 4), a.add(b));
    }

    @Test
    void add_withZero() {
        Fraction a = Fraction.fromNumDen(3, 5);
        assertEquals(a, a.add(Fraction.ZERO));
    }

    @Test
    void add_isCommutative() {
        Fraction a = Fraction.fromNumDen(2, 3);
        Fraction b = Fraction.fromNumDen(5, 7);
        assertEquals(a.add(b), b.add(a));
    }

    // ---------------------------------------------------------------
    // sub
    // ---------------------------------------------------------------

    @Test
    void sub_basic() {
        Fraction a = Fraction.fromNumDen(3, 4);
        Fraction b = Fraction.fromNumDen(1, 4);
        assertEquals(Fraction.fromNumDen(1, 2), a.sub(b));
    }

    @Test
    void sub_resultingInNegative() {
        Fraction a = Fraction.fromNumDen(1, 4);
        Fraction b = Fraction.fromNumDen(3, 4);
        assertEquals(Fraction.fromNumDen(-1, 2), a.sub(b));
    }

    @Test
    void sub_resultingInZero() {
        Fraction a = Fraction.fromNumDen(2, 5);
        assertEquals(Fraction.ZERO, a.sub(a));
    }

    @Test
    void sub_withZero() {
        Fraction a = Fraction.fromNumDen(3, 5);
        assertEquals(a, a.sub(Fraction.ZERO));
    }

    // ---------------------------------------------------------------
    // mul(Fraction)
    // ---------------------------------------------------------------

    @Test
    void mul_basic() {
        Fraction a = Fraction.fromNumDen(2, 3);
        Fraction b = Fraction.fromNumDen(3, 4);
        assertEquals(Fraction.fromNumDen(1, 2), a.mul(b));
    }

    @Test
    void mul_byZero() {
        Fraction a = Fraction.fromNumDen(7, 9);
        assertEquals(Fraction.ZERO, a.mul(Fraction.ZERO));
    }

    @Test
    void mul_byOne() {
        Fraction a = Fraction.fromNumDen(7, 9);
        assertEquals(a, a.mul(Fraction.ONE));
    }

    @Test
    void mul_negativeByPositive() {
        Fraction a = Fraction.fromNumDen(-2, 3);
        Fraction b = Fraction.fromNumDen(3, 5);
        assertEquals(Fraction.fromNumDen(-2, 5), a.mul(b));
    }

    @Test
    void mul_negativeByNegative() {
        Fraction a = Fraction.fromNumDen(-2, 3);
        Fraction b = Fraction.fromNumDen(-3, 5);
        assertEquals(Fraction.fromNumDen(2, 5), a.mul(b));
    }

    // ---------------------------------------------------------------
    // mul(long)
    // ---------------------------------------------------------------

    @Test
    void mulLong_basic() {
        Fraction a = Fraction.fromNumDen(1, 3);
        assertEquals(Fraction.fromNumDen(2, 3), a.mul(2));
    }

    @Test
    void mulLong_byZero() {
        Fraction a = Fraction.fromNumDen(5, 7);
        assertEquals(Fraction.ZERO, a.mul(0));
    }

    @Test
    void mulLong_reducesToInteger() {
        Fraction a = Fraction.fromNumDen(1, 3);
        assertEquals(Fraction.fromNumDen(1, 1), a.mul(3));
    }

    @Test
    void mulLong_negativeMultiplier() {
        Fraction a = Fraction.fromNumDen(2, 5);
        assertEquals(Fraction.fromNumDen(-4, 5), a.mul(-2));
    }

    // ---------------------------------------------------------------
    // div
    // ---------------------------------------------------------------

    @Test
    void div_basic() {
        Fraction a = Fraction.fromNumDen(1, 2);
        Fraction b = Fraction.fromNumDen(1, 4);
        assertEquals(Fraction.fromNumDen(2, 1), a.div(b));
    }

    @Test
    void div_byOne() {
        Fraction a = Fraction.fromNumDen(3, 7);
        assertEquals(a, a.div(Fraction.ONE));
    }

    @Test
    void div_byItself_givesOne() {
        Fraction a = Fraction.fromNumDen(4, 9);
        assertEquals(Fraction.ONE, a.div(a));
    }

    @Test
    void div_byZero_throws() {
        Fraction a = Fraction.fromNumDen(1, 2);
        assertThrows(IllegalArgumentException.class, () -> a.div(Fraction.ZERO));
    }

    @Test
    void div_negativeResult() {
        Fraction a = Fraction.fromNumDen(1, 2);
        Fraction b = Fraction.fromNumDen(-1, 4);
        assertEquals(Fraction.fromNumDen(-2, 1), a.div(b));
    }

    @Test
    void div_negativeByNegative_positiveResult() {
        Fraction a = Fraction.fromNumDen(-1, 2);
        Fraction b = Fraction.fromNumDen(-1, 4);
        assertEquals(Fraction.fromNumDen(2, 1), a.div(b));
    }

    @Test
    void div_zeroDividedByFraction() {
        Fraction b = Fraction.fromNumDen(3, 5);
        assertEquals(Fraction.ZERO, Fraction.ZERO.div(b));
    }

    // ---------------------------------------------------------------
    // equals / hashCode
    // ---------------------------------------------------------------

    @Test
    void equals_sameInstance() {
        Fraction a = Fraction.fromNumDen(1, 2);
        assertEquals(a, a);
    }

    @Test
    void equals_equivalentFractionsReducedTheSame() {
        Fraction a = Fraction.fromNumDen(2, 4);
        Fraction b = Fraction.fromNumDen(1, 2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_differentValues_notEqual() {
        Fraction a = Fraction.fromNumDen(1, 2);
        Fraction b = Fraction.fromNumDen(1, 3);
        assertNotEquals(a, b);
    }

    @Test
    void equals_null_returnsFalse() {
        Fraction a = Fraction.fromNumDen(1, 2);
        assertNotEquals(null, a);
    }

    @Test
    void equals_differentType_returnsFalse() {
        Fraction a = Fraction.fromNumDen(1, 2);
        assertNotEquals("1/2", a);
    }

    // ---------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------

    @Test
    void toString_properFraction() {
        assertEquals("3/5", Fraction.fromNumDen(3, 5).toString());
    }

    @Test
    void toString_integerFraction() {
        assertEquals("6", Fraction.fromNumDen(6, 1).toString());
    }

    @Test
    void toString_negativeFraction() {
        assertEquals("-3/5", Fraction.fromNumDen(-3, 5).toString());
    }

    @Test
    void toString_zero() {
        assertEquals("0", Fraction.ZERO.toString());
    }

    @Test
    void toString_one() {
        assertEquals("1", Fraction.ONE.toString());
    }

    // ---------------------------------------------------------------
    // Costanti
    // ---------------------------------------------------------------

    @Test
    void constants_zeroAndOneAreDistinct() {
        assertNotEquals(Fraction.ZERO, Fraction.ONE);
    }

    @Test
    void constants_addIdentity() {
        Fraction a = Fraction.fromNumDen(5, 8);
        assertEquals(a, a.add(Fraction.ZERO));
    }

    @Test
    void constants_mulIdentity() {
        Fraction a = Fraction.fromNumDen(5, 8);
        assertEquals(a, a.mul(Fraction.ONE));
    }

    // ---------------------------------------------------------------
    // Casi limite con numeri "grandi" (per verificare l'uso di BigInteger
    // internamente prima della riduzione ai minimi termini)
    // ---------------------------------------------------------------

    @Test
    void largeNumbers_multiplicationDoesNotOverflowIncorrectly() {
        Fraction a = Fraction.fromNumDen(1_000_000_000L, 3);
        Fraction b = Fraction.fromNumDen(3, 1_000_000_000L);
        assertEquals(Fraction.ONE, a.mul(b));
    }

    @Test
    void largeNumbers_additionReducesCorrectly() {
        Fraction a = Fraction.fromNumDen(999_999_999L, 2);
        Fraction b = Fraction.fromNumDen(1L, 2);
        assertEquals(Fraction.fromNumDen(500_000_000L, 1), a.add(b));
    }
}