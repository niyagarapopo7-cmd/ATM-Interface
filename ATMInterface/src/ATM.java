import java.io.Console;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ATM {
    private static final int MAX_ATTEMPTS = 3;
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final Bank bank;
    private final Scanner sc;

    public ATM(Bank bank, Scanner sc) {
        this.bank = bank;
        this.sc = sc;
    }

    // ------------------------------------------------------------ entry point
    public void start() {
        Account acc = login();
        if (acc == null) {
            blocked();
            return;
        }
        menuLoop(acc);
    }

    // ------------------------------------------------------------------ login
    private Account login() {
        String notice = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            ConsoleUI.header(bank.getName(), "SECURE LOGIN");
            if (notice != null) {
                ConsoleUI.error(notice);
                System.out.println();
            }
            System.out.println(ConsoleUI.GRAY
                    + "  Demo accounts:  1001 / 1234   |   1002 / 4321   |   1003 / 1111"
                    + ConsoleUI.RESET + "\n");

            ConsoleUI.prompt("User ID");
            String id = sc.nextLine().trim();
            String pin = readPin();

            Account acc = bank.authenticate(id, pin);
            if (acc != null) {
                ConsoleUI.loading("Verifying credentials");
                return acc;
            }
            notice = "Invalid User ID or PIN. Attempts remaining: " + (MAX_ATTEMPTS - attempt);
        }
        return null;
    }

    private String readPin() {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword("  ➤ PIN : ");
            return chars == null ? "" : new String(chars).trim();
        }
        ConsoleUI.prompt("PIN");
        return sc.nextLine().trim();
    }

    private void blocked() {
        ConsoleUI.header(bank.getName(), "ACCESS DENIED");
        ConsoleUI.error("Too many incorrect attempts. Your card has been blocked.");
        ConsoleUI.info("Please contact your nearest branch.");
        System.out.println();
    }

    // ------------------------------------------------------------------- menu
    private void menuLoop(Account acc) {
        boolean running = true;
        while (running) {
            showMenu(acc);
            int choice = readInt("Select an option (1-5)");
            switch (choice) {
                case 1: showHistory(acc); break;
                case 2: withdraw(acc);    break;
                case 3: deposit(acc);     break;
                case 4: transfer(acc);    break;
                case 5: goodbye(acc); running = false; break;
                default:
                    ConsoleUI.error("Invalid option. Please choose between 1 and 5.");
                    ConsoleUI.pause(sc);
            }
        }
    }

    private void showMenu(Account acc) {
        ConsoleUI.header(bank.getName(), "MAIN MENU");
        ConsoleUI.boxTop();
        ConsoleUI.boxLine("Welcome, " + acc.getHolderName() + "     (A/C: " + acc.getAccountId() + ")",
                ConsoleUI.GREEN);
        ConsoleUI.boxLine("Available Balance : " + ConsoleUI.money(acc.getBalance()), ConsoleUI.YELLOW);
        ConsoleUI.boxDivider();
        ConsoleUI.boxLine("[1]  Transaction History");
        ConsoleUI.boxLine("[2]  Withdraw");
        ConsoleUI.boxLine("[3]  Deposit");
        ConsoleUI.boxLine("[4]  Transfer");
        ConsoleUI.boxLine("[5]  Quit");
        ConsoleUI.boxBottom();
        System.out.println();
    }

    // ---------------------------------------------------- 1. transaction log
    private void showHistory(Account acc) {
        List<Transaction> list = acc.getHistory();
        ConsoleUI.header(bank.getName(), "TRANSACTION HISTORY");
        ConsoleUI.boxTop();
        if (list.isEmpty()) {
            ConsoleUI.boxLine("No transactions in this session yet.", ConsoleUI.GRAY);
        } else {
            ConsoleUI.boxLine(Transaction.header(), ConsoleUI.BOLD + ConsoleUI.CYAN);
            ConsoleUI.boxDivider();
            for (Transaction t : list) {
                ConsoleUI.boxLine(t.toRow(), t.isCredit() ? ConsoleUI.GREEN : ConsoleUI.RED);
            }
            ConsoleUI.boxDivider();
            ConsoleUI.boxLine("Total transactions: " + list.size()
                    + "   |   Current balance: " + ConsoleUI.money(acc.getBalance()),
                    ConsoleUI.YELLOW);
        }
        ConsoleUI.boxBottom();
        ConsoleUI.pause(sc);
    }

    // ------------------------------------------------------------ 2. withdraw
    private void withdraw(Account acc) {
        ConsoleUI.header(bank.getName(), "WITHDRAW CASH");
        ConsoleUI.info("Available balance: " + ConsoleUI.money(acc.getBalance()));
        double amount = readAmount("Amount to withdraw (c = cancel)");
        if (amount < 0) { cancelled(); return; }

        if (!acc.hasSufficientFunds(amount)) {
            ConsoleUI.error("Insufficient Funds! Available balance: " + ConsoleUI.money(acc.getBalance()));
            ConsoleUI.pause(sc);
            return;
        }
        ConsoleUI.loading("Dispensing cash");
        acc.withdraw(amount);
        receipt("WITHDRAW", amount, acc, null);
    }

    // ------------------------------------------------------------- 3. deposit
    private void deposit(Account acc) {
        ConsoleUI.header(bank.getName(), "DEPOSIT CASH");
        ConsoleUI.info("Available balance: " + ConsoleUI.money(acc.getBalance()));
        double amount = readAmount("Amount to deposit (c = cancel)");
        if (amount < 0) { cancelled(); return; }

        ConsoleUI.loading("Counting notes");
        acc.deposit(amount);
        receipt("DEPOSIT", amount, acc, null);
    }

    // ------------------------------------------------------------ 4. transfer
    private void transfer(Account acc) {
        ConsoleUI.header(bank.getName(), "FUND TRANSFER");
        ConsoleUI.info("Available balance: " + ConsoleUI.money(acc.getBalance()));

        ConsoleUI.prompt("Recipient Account ID (c = cancel)");
        String toId = sc.nextLine().trim();
        if (toId.equalsIgnoreCase("c")) { cancelled(); return; }

        Account to = bank.findAccount(toId);
        if (to == null) {
            ConsoleUI.error("Recipient account not found.");
            ConsoleUI.pause(sc);
            return;
        }
        if (to == acc) {
            ConsoleUI.error("You cannot transfer money to your own account.");
            ConsoleUI.pause(sc);
            return;
        }
        ConsoleUI.success("Recipient: " + to.getHolderName());

        double amount = readAmount("Amount to transfer (c = cancel)");
        if (amount < 0) { cancelled(); return; }

        if (!acc.hasSufficientFunds(amount)) {
            ConsoleUI.error("Insufficient Funds! Available balance: " + ConsoleUI.money(acc.getBalance()));
            ConsoleUI.pause(sc);
            return;
        }

        ConsoleUI.prompt("Send " + ConsoleUI.money(amount) + " to " + to.getHolderName() + "? (yes/no)");
        String confirm = sc.nextLine().trim();
        if (!confirm.equalsIgnoreCase("yes") && !confirm.equalsIgnoreCase("y")) {
            cancelled();
            return;
        }

        ConsoleUI.loading("Processing transfer");
        if (bank.transfer(acc, to, amount)) {
            receipt("TRANSFER", amount, acc,
                    "To           : " + to.getHolderName() + " (" + to.getAccountId() + ")");
        } else {
            ConsoleUI.error("Transfer failed. Please try again.");
            ConsoleUI.pause(sc);
        }
    }

    // ---------------------------------------------------------------- 5. quit
    private void goodbye(Account acc) {
        ConsoleUI.header(bank.getName(), "SESSION ENDED");
        ConsoleUI.boxTop();
        ConsoleUI.boxCenter("Thank you, " + acc.getHolderName() + "!", ConsoleUI.GREEN + ConsoleUI.BOLD);
        ConsoleUI.boxCenter("Transactions this session: " + acc.getHistory().size(), ConsoleUI.WHITE);
        ConsoleUI.boxCenter("Final balance: " + ConsoleUI.money(acc.getBalance()), ConsoleUI.YELLOW);
        ConsoleUI.boxDivider();
        ConsoleUI.boxCenter("Goodbye! Have a great day :)", ConsoleUI.CYAN);
        ConsoleUI.boxBottom();
        System.out.println();
    }

    // ---------------------------------------------------------------- helpers
    private void receipt(String type, double amount, Account acc, String extraLine) {
        System.out.println();
        ConsoleUI.boxTop();
        ConsoleUI.boxCenter("✔  TRANSACTION SUCCESSFUL", ConsoleUI.GREEN + ConsoleUI.BOLD);
        ConsoleUI.boxDivider();
        ConsoleUI.boxLine("Type         : " + type);
        ConsoleUI.boxLine("Amount       : " + ConsoleUI.money(amount));
        if (extraLine != null) ConsoleUI.boxLine(extraLine);
        ConsoleUI.boxLine("New Balance  : " + ConsoleUI.money(acc.getBalance()), ConsoleUI.YELLOW);
        ConsoleUI.boxLine("Date & Time  : " + LocalDateTime.now().format(STAMP), ConsoleUI.GRAY);
        ConsoleUI.boxBottom();
        ConsoleUI.pause(sc);
    }

    private void cancelled() {
        ConsoleUI.info("Transaction cancelled.");
        ConsoleUI.pause(sc);
    }

    private int readInt(String label) {
        ConsoleUI.prompt(label);
        String input = sc.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Returns the amount, or -1 if the user typed 'c' to cancel. */
    private double readAmount(String label) {
        while (true) {
            ConsoleUI.prompt(label);
            String input = sc.nextLine().trim().replace(",", "");
            if (input.equalsIgnoreCase("c")) return -1;
            try {
                double value = Double.parseDouble(input);
                if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
                    ConsoleUI.error("Amount must be greater than zero.");
                    continue;
                }
                return Math.round(value * 100.0) / 100.0;
            } catch (NumberFormatException e) {
                ConsoleUI.error("Please enter a valid number.");
            }
        }
    }
}