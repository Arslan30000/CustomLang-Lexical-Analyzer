import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static class IdentifierInfo {
        String name;
        String type; 
        int firstLine;
        int frequency;

        IdentifierInfo(String name, int firstLine) {
            this.name = name;
            this.type = "N/A"; 
            this.firstLine = firstLine;
            this.frequency = 1;
        }
    }

    private Map<String, IdentifierInfo> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    public void addIdentifier(String name, int line) {
        if (table.containsKey(name)) {
            IdentifierInfo info = table.get(name);
            info.frequency++;
        } else {
            table.put(name, new IdentifierInfo(name, line));
        }
    }
    
    public void printTable() {
        System.out.println("\n--- Symbol Table ---");
        System.out.printf("%-20s %-10s %-15s %-10s%n", "Name", "Type", "First Line", "Frequency");
        for (IdentifierInfo info : table.values()) {
            System.out.printf("%-20s %-10s %-15d %-10d%n", 
                info.name, info.type, info.firstLine, info.frequency);
        }
        System.out.println("--------------------\n");
    }
}