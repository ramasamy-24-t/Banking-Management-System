import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String name;
    private String password; // simple plain-text for learning; in real apps hash it
    private String role; // "admin", "employee", "customer"
    private List<String> accountNumbers = new ArrayList<>();

    public Customer(String userId, String name, String password, String role) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public boolean checkPassword(String p) { return password.equals(p); }
    public String getRole() { return role; }

    public void addAccount(String accountNumber) {
        if (!accountNumbers.contains(accountNumber)) accountNumbers.add(accountNumber);
    }
    public void removeAccount(String accountNumber) {
        accountNumbers.remove(accountNumber);
    }
    public List<String> getAccountNumbers() { return accountNumbers; }

    @Override
    public String toString() {
        return String.format("User[%s] %s Role=%s Accounts=%s", userId, name, role, accountNumbers);
    }
}
