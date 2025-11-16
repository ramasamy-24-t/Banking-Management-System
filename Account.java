import java.io.Serializable;
import java.text.DecimalFormat;

public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String accountNumber;
    protected String ownerName;
    protected double balance;

    public Account(String accountNumber, String ownerName, double initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.balance = initialBalance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        if (amount <= 0) return;
        balance += amount;
        TransactionLogger.log(String.format("Deposit: %s deposited %.2f to %s. New bal=%.2f",
                ownerName, amount, accountNumber, balance));
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) return;
        if (balance < amount) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal.");
        }
        balance -= amount;
        TransactionLogger.log(String.format("Withdrawal: %s withdrew %.2f from %s. New bal=%.2f",
                ownerName, amount, accountNumber, balance));
    }

    public abstract String getAccountType();

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.00");
        return String.format("%s Account [%s] Owner: %s Balance: %s",
                getAccountType(), accountNumber, ownerName, df.format(balance));
    }
}
