package utils;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents an exact fraction (rational number), always kept in lowest
 * terms, with the sign normalized on the numerator (the denominator is
 * always strictly positive).
 * <p>
 * Instances of this class are immutable: every arithmetic operation returns
 * a new {@code Fraction} without modifying the operands.
 */
public class Fraction {

    /** Numerator, in reduced form (can be negative, zero or positive). */
    private final long num;

    /** Denominator, in reduced form (always strictly positive). */
    private final long den;

    /** Constant representing the fraction {@code 1/1}. */
    public static final @NotNull Fraction ONE = new Fraction(1,1);

    /** Constant representing the fraction {@code 0/1}. */
    public static final @NotNull Fraction ZERO = new Fraction(0,1);

    /**
     * Directly builds a fraction from a numerator and denominator that are
     * already reduced to lowest terms, without any further checks or
     * normalization.
     *
     * @param num numerator, already reduced
     * @param den denominator, already reduced (expected to be strictly positive)
     */
    private Fraction(long num, long den) {
        this.num = num;
        this.den = den;
    }

    /**
     * Reduces the given numerator/denominator pair to lowest terms and
     * builds the corresponding {@code Fraction}, normalizing the sign so
     * that the resulting denominator is always positive.
     *
     * @param num numerator, possibly not reduced
     * @param den denominator, possibly not reduced (non-null and non-zero)
     * @return the fraction equivalent to {@code num/den}, reduced to lowest terms
     */
    private static @NotNull Fraction minimumTerm(BigInteger num, BigInteger den) {
        if (num.equals(BigInteger.ZERO)) return ZERO;
        if (num.equals(den)) return ONE;
        // we need to do the abs because if den <= 0 the function raises a NumberException in the mod operation.
        BigInteger gcd = gcd(num.abs(),den.abs());
        num = num.divide(gcd);
        den = den.divide(gcd);
        if (den.compareTo(BigInteger.ZERO) < 0) {
            return new Fraction(-1 * num.longValue(), -1 * den.longValue());
        }
        return new Fraction(num.longValue(), den.longValue());
    }

    /**
     * Computes the greatest common divisor of two non-negative integers
     * using Euclid's algorithm.
     *
     * @param a first value, non-negative
     * @param b second value, non-negative
     * @return the greatest common divisor of {@code a} and {@code b}
     */
    private static @NotNull BigInteger gcd(@NotNull BigInteger a, @NotNull BigInteger b) {
        BigInteger r;
        while (b.compareTo(BigInteger.ZERO) > 0) {
            r = a.mod(b);
            a = b;
            b = r;
        }
        return a;
    }

    /**
     * Creates a new fraction from a numerator and denominator, automatically
     * reducing it to lowest terms.
     *
     * @param num numerator
     * @param den denominator, must not be zero
     * @return the fraction {@code num/den} reduced to lowest terms
     * @throws IllegalArgumentException if {@code den} is zero
     */
    public static @NotNull Fraction fromNumDen(long num, long den)
            throws IllegalArgumentException {
        if (den == 0)
            throw new IllegalArgumentException("Cannot divide by zero");
        return minimumTerm(BigInteger.valueOf(num), BigInteger.valueOf(den));
    }

    /**
     * Adds another fraction to this fraction.
     *
     * @param f the fraction to add
     * @return a new fraction equal to the sum {@code this + f}, reduced to lowest terms
     */
    public @NotNull Fraction add(@NotNull Fraction f) {
        return minimumTerm(BigInteger.valueOf(num).multiply(BigInteger.valueOf(f.den)).add(
                        BigInteger.valueOf(f.num).multiply(BigInteger.valueOf(den))),
                BigInteger.valueOf(den).multiply(BigInteger.valueOf(f.den)));
    }

    /**
     * Subtracts another fraction from this fraction.
     *
     * @param f the fraction to subtract
     * @return a new fraction equal to the difference {@code this - f}, reduced to lowest terms
     */
    public @NotNull Fraction sub(@NotNull Fraction f) {
        return add(new Fraction(-f.num, f.den));
    }

    /**
     * Multiplies this fraction by another fraction.
     *
     * @param f the fraction to multiply by
     * @return a new fraction equal to the product {@code this * f}, reduced to lowest terms
     */
    public @NotNull Fraction mul(@NotNull Fraction f) {
        return minimumTerm(
                BigInteger.valueOf(num).multiply(BigInteger.valueOf(f.num)),
                BigInteger.valueOf(den).multiply(BigInteger.valueOf(f.den))
        );
    }

    /**
     * Divides this fraction by another fraction.
     *
     * @param f the fraction to divide by, must have a non-zero numerator
     * @return a new fraction equal to the quotient {@code this / f}, reduced to lowest terms
     * @throws IllegalArgumentException if {@code f} is {@link #ZERO} (division by zero)
     */
    public @NotNull Fraction div(@NotNull Fraction f) {
        // call the method to have the minus in the correct point, if I call the constructor we could have
        // the minus in the denominator.
        return mul(fromNumDen(f.den, f.num));
    }

    /**
     * Multiplies this fraction by an integer.
     *
     * @param n the integer factor to multiply by
     * @return a new fraction equal to the product {@code this * n}, reduced to lowest terms
     */
    public @NotNull Fraction mul(long n) {
        return minimumTerm(
                BigInteger.valueOf(num).multiply(BigInteger.valueOf(n)),
                BigInteger.valueOf(den));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ((obj instanceof Fraction f))
            return den == f.den && num == f.num;
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, den);
    }

    @Override
    public String toString() {
        return den != 1 ? String.format("%d/%d", num, den) : String.format("%d", num);
    }

}