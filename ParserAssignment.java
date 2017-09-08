public class ParserAssignment {
    public static void main(String[] args) {
        // Use the assignment example as the default code
        String code = "BEGIN COMPUTE A1 + A2 * ABS ( A3 ) COMPUTE A1 + A1 END EOF";
        
        // Allow code to be given as an argument
        if (args.length > 0) {
            code = args[0];
        }
        System.out.println(code);

        Lexer lexer = new Lexer(code);
        Parser parser = new Parser(lexer);
        parser.parse();
    }
}

class Parser {
    private Lexer lexer;
    private Token curToken;

    public Parser(Lexer l) {
        lexer = l;
        advanceToken();
    }

    // Utility print functions
    private void printEnter(String section) {
        System.out.println("Entering <"+section+">");
    }

    private void printExit(String section) {
        System.out.println("Exiting <"+section+">");
    }

    private void printCurToken() {
        System.out.println("Current Token: "+curToken);
    }

    public void parse() {
        try {
            parseProgram();
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    // Utility parsing functions
    private void advanceToken() {
        curToken = lexer.nextToken();
    }

    private void expectToken(TokenType type) throws ParseException {
        if (curToken.type != type) {
            throw new ParseException(type, curToken.literal);
        }
        printCurToken();
        advanceToken();
    }

    private boolean curTokenIs(TokenType type) {
        return curToken.type == type;
    }
    
    private boolean isFunction() {
        return curTokenIs(TokenType.Abs) || curTokenIs(TokenType.Square) || curTokenIs(TokenType.Sqrt);
    }

    // Grammar rules
    private void parseProgram() throws ParseException {
        printEnter("program");
        expectToken(TokenType.Begin);
        parseBody();
        expectToken(TokenType.End);
        expectToken(TokenType.EOF);
        printExit("program");
    }

    private void parseBody() throws ParseException {
        printEnter("body");
        while(curTokenIs(TokenType.Compute)) {
            parseStmt();
        }
        printExit("body");
    }

    private void parseStmt() throws ParseException {
        printEnter("stmt");
        expectToken(TokenType.Compute);
        parseExpr();
        printExit("stmt");
    }

    private void parseExpr() throws ParseException {
        printEnter("expr");
        parseTerm();
        if (!(curTokenIs(TokenType.Plus) || curTokenIs(TokenType.Minus))) {
            printExit("expr");
            return;
        }
        printCurToken();
        advanceToken(); // Skip operator
        parseTerm();
        printExit("expr");
    }

    private void parseTerm() throws ParseException {
        printEnter("term");
        parseFactor();
        if (!(curTokenIs(TokenType.Asterisk) || curTokenIs(TokenType.Slash))) {
            printExit("term");
            return;
        }
        printCurToken();
        advanceToken(); // Skip operator
        parseFactor();
        printExit("term");
    }

    private void parseFactor() throws ParseException {
        printEnter("factor");
        if (isFunction()) {
            printCurToken();
            advanceToken();
            parseFunction();
            printExit("factor");
            return;
        } else if (curTokenIs(TokenType.Ident)) {
            parseIdent();
            printExit("factor");
            return;
        } else if (curTokenIs(TokenType.Int)) {
            printCurToken();
            advanceToken();
            printExit("factor");
            return;
        }

        if (!curTokenIs(TokenType.LParens)) {
            throw new ParseException(
                new TokenType[]{
                    TokenType.Ident,
                    TokenType.Int,
                    TokenType.LParens,
                    TokenType.Abs,
                    TokenType.Sqrt,
                    TokenType.Square
                }, curToken.literal);
        }
        printCurToken(); // I'm manually checking the token, so checkCurToken doesn't print it
        parseExpr();
        expectToken(TokenType.RParens);
        printExit("factor");
    }

    private void parseIdent() throws ParseException {
        printEnter("id");
        expectToken(TokenType.Ident);
        printExit("id");
    }

    private void parseFunction() throws ParseException {
        printEnter("function");
        expectToken(TokenType.LParens);
        parseExpr();
        expectToken(TokenType.RParens);
        printExit("function");
    }
}

// ParseException is used to tell the user that an unexpected token was encountered
class ParseException extends Exception {
    public ParseException(TokenType expected, String got) {
        super("Unexpected token. Expected "+expected+". Got "+got+".");
    }


    public ParseException(TokenType[] expected, String got) {
        super("Unexpected token. Expected "+expectedListToString(expected)+". Got "+got+".");
    }

    private static String expectedListToString(TokenType[] expected) {
        StringBuilder tokens = new StringBuilder();
        for (int i = 0; i < expected.length; i++) {
            tokens.append(expected[i].toString());
            if (i < expected.length-1) {
                tokens.append(", ");
            }
        }
        return tokens.toString();
    }
}

class Token {
    String literal;
    TokenType type;

    public Token(String lit, TokenType type) {
        this.literal = lit;
        this.type = type;
    }

    public String toString() {
        return "Literal: " + this.literal + " Type: " + this.type;
    }
}

enum TokenType {
    EOF,
    Illegal,

    // Operators
    Plus,
    Minus,
    Asterisk,
    Slash,

    // Data types/identifiers
    Int,
    Ident,

    // Grouping symbols
    LParens,
    RParens,

    // Keywords
    Begin,
    End,
    Compute,

    // Function keywords as defined in the grammar
    Square,
    Sqrt,
    Abs;

    public String toString() {
        switch(this) {
            case Plus:
                return "+";
            case Minus:
                return "-";
            case Asterisk:
                return "*";
            case Slash:
                return "/";
            case LParens:
                return "(";
            case RParens:
                return ")";
            default: // Everything is named as its literal value
                return this.name();
        }
    }

    public static TokenType identOrKeyword(String ident) {
        ident = ident.toUpperCase();

        // Check for keywords
        switch(ident) {
            case "BEGIN":
                return Begin;
            case "END":
                return End;
            case "EOF":
                return EOF;
            case "COMPUTE":
                return Compute;
            case "SQUARE":
                return Square;
            case "SQRT":
                return Sqrt;
            case "ABS":
                return Abs;
            case "A1":
            case "A2":
            case "A3":
                return Ident;
        }

        // Otherwise, IDK what it is
        return Illegal;
    }
}

class Lexer {
    private String input;
    private int inputLoc;

    public Lexer(String input) {
        this.input = input;
        this.inputLoc = 0;
    }

    // Returns the character as the current lex location.
    // Will return null (0x00) if at end of input.
    private char curChar() {
        if (inputLoc == input.length()) {
            return 0;
        }
        return input.charAt(inputLoc);
    }
    
    // Returns the character as the next lex location.
    // Will return null (0x00) if next char would be end of input.
    private char nextChar() {
        if (inputLoc+1 == input.length()) {
            return 0;
        }
        return input.charAt(inputLoc+1);
    }

    private void advanceChar() {
        inputLoc++;
    }

    public Token nextToken() {
        skipWhitespace(); // Whitespace is insignificant and not needed for parsing
        Token token;

        switch(curChar()) {
            // End of file
            case 0:
                token = new Token("EOF", TokenType.EOF);
                break;

            // Operators
            case '+':
                token = new Token("+", TokenType.Plus);
                break;
            case '-':
                token = new Token("-", TokenType.Minus);
                break;
            case '*':
                token = new Token("*", TokenType.Asterisk);
                break;
            case '/':
                token = new Token("/", TokenType.Slash);
                break;

            // Groupings
            case '(':
                token = new Token("(", TokenType.LParens);
                break;
            case ')':
                token = new Token(")", TokenType.RParens);
                break;

            // Complex types/idents
            default:
                if (isDigit()) {
                    token = new Token(lexNumber(), TokenType.Int);
                } else if (isLetter()) {
                    String ident = lexIdent();
                    token = new Token(ident, TokenType.identOrKeyword(ident));
                } else {
                    token = new Token(Character.toString(curChar()), TokenType.Illegal);
                }
        }

        advanceChar();
        return token;
    }

    private String lexNumber() {
        int start = inputLoc;

        while(isDigit(nextChar())) {
            advanceChar();
        }

        return input.substring(start, inputLoc+1);
    }

    private String lexIdent() {
        int start = inputLoc;

        while(isLetter(nextChar()) || isDigit(nextChar())) {
            advanceChar();
        }

        return input.substring(start, inputLoc+1);
    }

    private void skipWhitespace() {
        while(isWhitespace()) {
            advanceChar();
        }
    }

    private boolean isWhitespace() {
        char curChar = curChar();
        return (curChar == '\n' || curChar == '\r' || curChar == '\t' || curChar == ' ');
    }

    private boolean isDigit() {
        char curChar = curChar();
        return (curChar >= '0' && curChar <= '9');
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isLetter() {
        char curChar = curChar();
        return (curChar >= 'A' && curChar <= 'z');
    }

    private boolean isLetter(char c) {
        return (c >= 'A' && c <= 'z');
    }
}
