# CBBTLR — LALR(1) Parser Generator

![Java](https://img.shields.io/badge/Java-21-orange)
![Gradle](https://img.shields.io/badge/build-gradle-blue)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

CBBTLR is a LALR(1) parser generator for Java.
Given a context-free grammar and lexical specifications, it generates:

- a serialized lexer
- a LALR(1) parsing table
- a Java parser implementation

C.B.B.T.L.R. stands for "Could Be Better Tool for Language Recognizing".

## Prerequisites

To build and run this project, you need:

- **Java 21**

Check your Java version with:
```bash
java --version
```

## Installation

Clone the repository:
```bash
git clone https://github.com/savecitoo/CBBTLR.git
cd CBBTLR
```
If you don't have git you can download the zip of the file and extract it.

## Build

### macOS/Linux
```bash
./gradlew build
```
### Windows
```bash
gradlew build
```

## Run

### 1. Write your grammar file

Place your `.cbg` file anywhere in the project (the default location is
`src/main/grammars/MyGrammar.cbg`).

The file must contain two sections separated by the `Token:` marker.

---

**Section 1 — Context-free grammar** (before `Token:`)

```
A -> alt_1 [#tag]? | alt_2 [#tag]? | ...
```

- The left-hand side must be a single non-terminal symbol.
- The right-hand side must contain at least one symbol; symbols are separated by spaces.
- Each alternative may optionally carry a `#tag` label (similar to ANTLR's alternative labels),
  which can be used to identify the matched alternative during semantic processing.

---

**Section 2 — Lexical rules** (after `Token:`)

```
Name : pattern [skip]?
```

- `Name` is the terminal symbol as it appears in the grammar rules.
- `pattern` is a regular expression that matches the token's lexeme.
- The optional `skip` keyword marks tokens that should be discarded by the lexer
  (e.g. whitespace, comments).

---

**Example** (`src/main/grammars/ITEGrammar.cbg`):

```
S' -> S
S  -> M | U
M  -> If E Then M Else M #ite | While E Do M #while | Lgraph L Rgraph | A
U  -> If E Then S #it | If E Then M Else U
L  -> L Next S | S
A  -> Id Equal E
E  -> E Plus T | T
T  -> T Times F #mul | F
F  -> LParen E RParen | Id | Number

Token:
Plus   : \+
Times  : \*
LParen : \(
RParen : \)
If     : if
Then   : then
Else   : else
While  : while
Do     : do
Lgraph : \{
Rgraph : \}
Next   : ;
Equal  : =
Number : [0-9][0-9]*
Id     : [a-zA-Z][a-zA-Z]*
WS     : [ \t\n]+ skip
```

---

### 2. Run the generator

#### macOS / Linux
```bash
./gradlew generateParser --args="<grammarFile> <packageName> <className> <outputDir>"
```

#### Windows
```bash
gradlew generateParser --args="<grammarFile> <packageName> <className> <outputDir>"
```

| Argument      | Description                                                         |
|---------------|---------------------------------------------------------------------|
| `grammarFile` | Path to your `.cbg` file                                            |
| `packageName` | Target Java package for the generated sources (e.g. `com.example`) |
| `className`   | Base name of the generated parser class (e.g. `MyParser`)          |
| `outputDir`   | Directory where the generated `.java` files will be written         |

**Example:**
```bash
./gradlew generateParser --args="src/main/grammars/ITEGrammar.cbg generated ITEGrammar src/main/java/generated"
```

If you are happy with the defaults, you can omit `--args` entirely:

```bash
./gradlew generateParser        # macOS / Linux
gradlew generateParser          # Windows
```
---

### 3. Output

Three artifacts are produced under the target package path
(`<packagePath>` is derived from `packageName` by replacing dots with the OS file separator):

| Artifact | Location | Description |
|----------|----------|-------------|
| `<className>Lexer.ser` | `src/main/resources/<packagePath>/` | Serialized lexer |
| `<className>Parser.java` | `<outputDir>/<packagePath>/` | Generated parser class |
| `<className>ParserTable.java` | `<outputDir>/<packagePath>/` | Generated LALR(1) parsing table |

**Example** — with the default parameters:

```
src/main/resources/generated/MyGrammarLexer.ser
src/main/java/generated/MyGrammarParser.java
src/main/java/generated/MyGrammarParserTable.java
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.