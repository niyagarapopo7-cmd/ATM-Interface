import java.util.HashMap;
import java.util.Map;

public class Bank {
    private final String name;
    private final Map<String, Account> accounts = new HashMap<>();

    public Bank(String name) { this.name = name; }

    public String getName() { return name; }

    public void addAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Account findAccount(String accountId) {
        return accounts.get(accountId);
    }

    /** Returns the account if ID + PIN match, otherwise null. */
    public Account authenticate(String accountId, String pin) {
        Account acc = accounts.get(accountId);
        return (acc != null && acc.checkPin(pin)) ? acc : null;
    }

    /** Debits sender, credits receiver. Both accounts get a transaction log. */
    public boolean transfer(Account from, Account to, double amount) {
        if (from == to || amount <= 0) return false;
        if (!from.transferOut(to.getAccountId(), amount)) return false;
        to.transferIn(from.getAccountId(), amount);
        return true;
    }
}