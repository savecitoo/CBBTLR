/**
 * LALR(1) parser generator implementation.
 *
 * <p>This package provides the core components for generating and executing
 * LALR(1) parsers from a context-free grammar:
 *
 * <ul>
 *   <li>{@link lalrparser.Parser} — the generated parser, driven by ACTION and GOTO tables</li>
 *   <li>{@link lalrparser.ParseException} — structured parse error with token and position info</li>
 * </ul>
 *
 * <p>Typical usage: construct a {@link lalrparser.Parser} from a grammar,
 * then call {@code parse()} on a token stream.
 */
package lalrparser;