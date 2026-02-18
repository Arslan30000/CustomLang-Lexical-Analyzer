package src;
import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private static class ErrorRecord {
        String type;
        int line;
        int column;
        String lexeme;
        String reason;

        ErrorRecord(String type, int line, int column, String lexeme, String reason) {
            this.type = type;
            this.line = line;
            this.column = column;
            this.lexeme = lexeme;
            this.reason = reason;
        }

        @Override
        public String toString() {
            // Format: Error type, line, column, lexeme, reason
            return String.format("ERROR: [%-15s] Line: %-3d Col: %-3d Lexeme: \"%-10s\" -> %s",
                    type, line, column, lexeme, reason);
        }
    }

    private List<ErrorRecord> errors = new ArrayList<>();

    public void addError(String type, int line, int column, String lexeme, String reason) {
        errors.add(new ErrorRecord(type, line, column, lexeme, reason));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors() {
        if (errors.isEmpty()) return;
        System.out.println("\n=== ERROR REPORT ===");
        for (ErrorRecord e : errors) {
            System.out.println(e);
        }
        System.out.println("====================\n");
    }
}