package src;
import java.io.FileReader;
import java.io.IOException;

public class JFlexTest {
    public static void main(String[] args) {
        String file = "tests/test1.lang";
        
        System.out.println("=== Testing JFlex Scanner ===");
        
        try {
            Yylex scanner = new Yylex(new FileReader(file));
            Token token;
            
            while ((token = scanner.yylex()) != null && token.getType() != TokenType.EOF) {
                System.out.println(token);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Error e) {
             System.err.println("Lexical Error: " + e.getMessage());
        }
    }
}