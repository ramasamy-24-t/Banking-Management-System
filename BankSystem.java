import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankSystem {
    private static final String DATA_FILE = "bankdata.ser";

    // maps userId -> Customer
    private Map<String, Customer> users = new HashMap<>();
    // maps accountNumber -> Account
    private Map<String, Account> accounts = new HashMap<>();

    public BankSystem() {
        loadState();
        ensureDefaultAdmin();
    }

    private void ensureDefaultAdmin() {
        if (!users.containsKey("admin")) {
            Customer admin = new Customer("admin", "Administrator", "admin123", "admin");
            users.put("admin", admin);
            saveState();
        }
    }

    // create user (employee or customer)
    public boolean createUser(String userId, String name, String password, String role) {
        if (users.containsKey(userId)) return false;
        Customer c = new Customer(userId, name, password, role);
        users.put(userId, c);
        TransactionLogger.log("User created: " + c);
        saveState();
        return true;
    }

    public boolean deleteUser(String userId) {
        if (!users.containsKey(userId)) return false;
        Customer c = users.remove(userId);
        // optionally remove accounts
        for (String acctNo : c.getAccountNumbers()) {
            accounts.remove(acctNo);
            TransactionLogger.log("Account removed with user deletion: " + acctNo);
        }
        TransactionLogger.log("User deleted: " + userId);
        saveState();
        return true;
    }

    public Customer login(String userId, String password) {
        Customer c = users.get(userId);
        if (c != null && c.checkPassword(password)) return c;
        return null;
    }

    public String createAccountForUser(String userId, String type, double initialBalance) {
        if (!users.containsKey(userId)) return null;
        String acctNo = UUID.randomUUID().toString().substring(0, 8);
        Account a;
        if ("savings".equalsIgnoreCase(type)) {
            a = new SavingsAccount(acctNo, users.get(userId).getName(), initialBalance, 0.04);
        } else {
            a = new CurrentAccount(acctNo, users.get(userId).getName(), initialBalance, 500.0);
        }
        accounts.put(acctNo, a);
        users.get(userId).addAccount(acctNo);
        TransactionLogger.log("Account created: " + a);
        saveState();
        return acctNo;
    }

    public boolean deleteAccount(String accountNumber) {
        if (!accounts.containsKey(accountNumber)) return false;
        accounts.remove(accountNumber);
        // remove from any user
        for (Customer c : users.values()) {
            c.removeAccount(accountNumber);
        }
        TransactionLogger.log("Account deleted: " + accountNumber);
        saveState();
        return true;
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public boolean deposit(String accountNumber, double amount) {
        Account a = accounts.get(accountNumber);
        if (a == null) return false;
        a.deposit(amount);
        saveState();
        return true;
    }

    public boolean withdraw(String accountNumber, double amount) {
        Account a = accounts.get(accountNumber);
        if (a == null) return false;
        try {
            a.withdraw(amount);
            saveState();
            return true;
        } catch (InsufficientFundsException e) {
            TransactionLogger.log("Failed withdrawal on " + accountNumber + ": " + e.getMessage());
            return false;
        }
    }

    // Serialization: save users + accounts maps
    public void saveState() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE));
            oos.writeObject(users);
            oos.writeObject(accounts);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        } finally {
            if (oos != null) {
                try { oos.close(); } catch (IOException ignored) {}
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadState() {
        ObjectInputStream ois = null;
        try {
            File f = new File(DATA_FILE);
            if (!f.exists()) return;
            ois = new ObjectInputStream(new FileInputStream(f));
            Object u = ois.readObject();
            Object a = ois.readObject();
            if (u instanceof HashMap) users = (HashMap<String, Customer>) u;
            if (a instanceof HashMap) accounts = (HashMap<String, Account>) a;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Load failed or no previous data: " + e.getMessage());
        } finally {
            if (ois != null) {
                try { ois.close(); } catch (IOException ignored) {}
            }
        }
    }

    // simple for debugging/testing
    public void printAll() {
        System.out.println("=== USERS ===");
        users.values().forEach(System.out::println);
        System.out.println("=== ACCOUNTS ===");
        accounts.values().forEach(System.out::println);
    }

    // Getter methods to safely access internal data
    public Map<String, Customer> getUsers() {
        return users;
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }


    // main for quick manual test (will be replaced by Swing UI later)
    public static void main(String[] args) {
        BankSystem bs = new BankSystem();
        bs.createUser("emp1", "Employee One", "emp123", "employee");
        String acct = bs.createAccountForUser("emp1", "savings", 1000.0);
        bs.deposit(acct, 200.0);
        try {
            bs.getAccount(acct).withdraw(50.0);
        } catch (Exception e) { /* ignore for quick demo */ }
        bs.printAll();
    }
}
