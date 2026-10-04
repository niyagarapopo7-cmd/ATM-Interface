import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Makes box characters and the ₹ symbol display properly
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        Bank bank = new Bank("Nova Bank");
        bank.addAccount(new Account("1001", "Arun Kumar", "1234", 25000.00));
        bank.addAccount(new Account("1002", "Priya S",    "4321", 18000.00));
        bank.addAccount(new Account("1003", "Karthik R",  "1111", 9500.00));

        try (Scanner sc = new Scanner(System.in)) {
            new ATM(bank, sc).start();
        }
    }
}