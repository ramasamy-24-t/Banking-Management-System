public class CurrentAccount extends Account {
    private static final long serialVersionUID = 1L;
    private double overdraftLimit; // allowed negative balance up to this amount

    public CurrentAccount(String accountNumber, String ownerName, double initialBalance, double overdraftLimit) {
        super(accountNumber, ownerName, initialBalance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public String getAccountType() {
        return "Current";
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) return;
        if (balance - amount < -overdraftLimit) {
            throw new InsufficientFundsException("Exceeded overdraft limit.");
        }
        balance -= amount;
        TransactionLogger.log(String.format("Withdrawal: %s withdrew %.2f from %s (Current, overdraft allowed). New bal=%.2f",
                ownerName, amount, accountNumber, balance));
    }
}
