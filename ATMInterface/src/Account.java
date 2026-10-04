import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {
    private final String accountId;
    private final String holderName;
    private final String pin;
    private double balance;
    private final ArrayList<Transaction> history = new ArrayList<>();

    public Account(String accountId, String holderName, String pin, double openingBalance) {
        this.accountId = accountId;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = openingBalance;
    }

    public String getAccountId() { return accountId; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    public List<Transaction> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public boolean checkPin(String enteredPin) {
        return pin.equals(enteredPin);
    }

    public boolean hasSufficientFunds(double amount) {
        return amount <= balance;
    }

    public void deposit(double amount) {
        balance += amount;
        history.add(new Transaction("DEPOSIT", amount, balance, "Cash deposit", true));
    }

    public boolean withdraw(double amount) {
        if (!hasSufficientFunds(amount)) return false;
        balance -= amount;
        history.add(new Transaction("WITHDRAW", amount, balance, "Cash withdrawal", false));
        return true;
    }

    public boolean transferOut(String toAccountId, double amount) {
        if (!hasSufficientFunds(amount)) return false;
        balance -= amount;
        history.add(new Transaction("TRANSFER OUT", amount, balance, "To " + toAccountId, false));
        return true;
    }

    public void transferIn(String fromAccountId, double amount) {
        balance += amount;
        history.add(new Transaction("TRANSFER IN", amount, balance, "From " + fromAccountId, true));
    }
}