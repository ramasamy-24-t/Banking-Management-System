public class SavingsAccount extends Account {
    private static final long serialVersionUID = 1L;
    private double interestRate; // e.g., 0.04 for 4%

    public SavingsAccount(String accountNumber, String ownerName, double initialBalance, double interestRate) {
        super(accountNumber, ownerName, initialBalance);
        this.interestRate = interestRate;
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    public void applyInterest() {
        double interest = balance * interestRate;
        balance += interest;
        TransactionLogger.log(String.format("Interest: Applied %.2f interest to %s. New bal=%.2f",
                interest, accountNumber, balance));
    }

    // optional: override withdraw to enforce a minimum balance e.g., 100
    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        double minBalance = 100.0;
        if (balance - amount < minBalance) {
            throw new InsufficientFundsException("Cannot withdraw: savings account requires minimum balance of " + minBalance);
        }
        super.withdraw(amount);
    }
}
