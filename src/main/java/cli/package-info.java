/**
 * Command-line interface for the parser generator tool.
 *
 * <p>This package contains a single entry-point class,
 * {@link cli.ParserGenerator}, which drives the end-to-end generation
 * of a lexer/parser pair from a combined grammar file.</p>
 *
 * <p>The grammar file is a plain-text file divided into two sections
 * by a {@code Token:} marker:</p>
 * <ul>
 *   <li>the section <em>before</em> {@code Token:} defines the
 *       context-free grammar consumed by the parser generator;</li>
 *   <li>the section <em>after</em> {@code Token:} defines the lexical
 *       rules used to build the lexer.</li>
 * </ul>
 *
 * <p>Invoking {@link cli.ParserGenerator#main(String[])} with the
 * required arguments produces two artifacts:</p>
 * <ol>
 *   <li>a serialized {@code .ser} lexer saved under
 *       {@code src/main/resources/<packagePath>/<className>Lexer.ser};</li>
 *   <li>the generated {@code .java} parser source files written to
 *       the specified output directory.</li>
 * </ol>
 *
 */package cli;