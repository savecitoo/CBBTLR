/**
 * Parse tree representation produced by the parser generator.
 *
 * <h2>Structure</h2>
 * <p>A parse tree is a rooted, ordered tree in which:</p>
 * <ul>
 *   <li><strong>internal nodes</strong> correspond to non-terminal
 *       symbols of the grammar and record the production rule that
 *       was applied;</li>
 *   <li><strong>leaf nodes</strong> correspond to terminal symbols
 *       (tokens) consumed from the input stream and carry the
 *       matched lexeme and its source position.</li>
 * </ul>
 *
 * <p>This package does not contain visitor implementations,
 * transformation logic, or any other processing beyond the
 * structural representation of the parse tree.</p>
 */
package tree;