public class SampleParser {
    static String inString = "BEGIN END EOF";
    static String remainingString = inString;
    static char BEGIN_CODE = 'B';
    static char END_CODE = 'E';
    static char END_OF_FILE = 'Z';
    static char nextToken;

    public static void main(String args[]) {
        lex();
        parse();
    }

    public static void lex() {
        System.out.print("Enter <lex> - lexeme = ");
        String lexeme = "";

        int start = 0;

        while (start < remainingString.length() && remainingString.charAt(start) == ' ')
            start++;

        int end = start + 1;

        while (end < remainingString.length() && remainingString.charAt(end) != ' ')
            end++;

        if (start >= remainingString.length()) {
            lexeme = "";
            remainingString = "";
            System.out.print("EOF");
        } else {
            lexeme = remainingString.substring(start, end);
            remainingString = remainingString.substring(end, remainingString.length());
        }

        if (lexeme.compareTo("BEGIN") == 0)
            nextToken = BEGIN_CODE;
        else if (lexeme.compareTo("END") == 0)
            nextToken = END_CODE;
        else if (lexeme.compareTo("EOF") == 0)
            nextToken = END_OF_FILE;

        System.out.print(lexeme + "  token = ");
        System.out.println(nextToken);
    }

    public static void parse() {
        System.out.println("Enter <parse>");
        program();
        System.out.println("Exit <parse>");
    }

    public static void program() {
        System.out.println("Enter <program>");
        if (nextToken == BEGIN_CODE) {
            lex();
            body();
        } else
            error();
        if (nextToken == END_CODE)
            lex();
        else
            error();
        System.out.println("Exit <program>");
    }

    public static void body() {
        System.out.println("Enter <body>");
        System.out.println("Exit <body>");
    }

    public static void error() {
        System.out.println("Enter <error>");
        System.out.println("Exit <error>");
    }
}
