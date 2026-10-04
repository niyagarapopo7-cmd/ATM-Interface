import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Transaction {
    private static int counter = 1000;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.ENGLISH);

    private final int id;
    private final String type;
    private final double amount;
    private final double balanceAfter;
    private final String details;
    private final boolean credit;
    private final LocalDateTime time;

    public Transaction(String type, double amount, double balanceAfter,
                       String details, boolean credit) {
        this.id = ++counter;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.details = details;
        this.credit = credit;
        this.time = LocalDateTime.now();
    }

    public boolean isCredit() { return credit; }

    public static String header() {
        return String.format("%-6s %-12s %-13s %13s %13s  %s",
                "ID", "DATE/TIME", "TYPE", "AMOUNT", "BALANCE", "DETAILS");
    }

    public String toRow() {
        String signed = (credit ? "+" : "-") + ConsoleUI.money(amount);
        return String.format("#%-5d %-12s %-13s %13s %13s  %s",
                id, time.format(FMT), type, signed,
                ConsoleUI.money(balanceAfter), details);
    }
}