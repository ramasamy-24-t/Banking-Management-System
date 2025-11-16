import java.awt.*;
import java.util.List;
import javax.swing.*;

public class BankUI {
    private JFrame frame;
    private JPanel mainPanel; // uses CardLayout
    private CardLayout cardLayout;
    private BankSystem bankSystem;

    // Panels
    private LoginPanel loginPanel;
    private AdminPanel adminPanel;
    private EmployeePanel employeePanel;
    private CustomerPanel customerPanel;

    // Track logged-in user
    private Customer currentUser;

    // ====== ICONS ======
    // wrong password / wrong input
    private Icon[] wrongIcons;
    // success (correct input / successful action)
    private Icon[] successIcons;
    // anything else (info / select something etc.)
    private Icon neutralIcon;

    public BankUI() {
        // 🔹 load all icons (update paths to match your files)
        wrongIcons = new Icon[]{
                loadIcon("icons/wrong1.png"),
                loadIcon("icons/wrong2.png"),
                loadIcon("icons/wrong3.png"),
                loadIcon("icons/wrong4.png"),
                loadIcon("icons/wrong5.png"),
                loadIcon("icons/wrong6.png"),
                loadIcon("icons/wrong7.png"),
                loadIcon("icons/wrong8.png"),
                loadIcon("icons/wrong9.png"),
                loadIcon("icons/wrong10.png")
        };

        successIcons = new Icon[]{
                loadIcon("icons/success1.png"), // angel
                loadIcon("icons/success2.png")  // big smile
        };

        neutralIcon = loadIcon("icons/neutral.png"); // pink hearts

        bankSystem = new BankSystem(); // loads saved state
        initUI();
    }

    // ==== icon helpers ====

    private Icon loadIcon(String path) {
        // simple file-based loader, scales to 64x64
        ImageIcon ii = new ImageIcon(path);
        Image img = ii.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private Icon randomFrom(Icon[] set) {
        if (set == null || set.length == 0) return null;
        int idx = (int) (Math.random() * set.length);
        return set[idx];
    }

    // wrong password / wrong input
    private void showWrongDialog(String msg, String title) {
        JOptionPane.showMessageDialog(
                frame,
                msg,
                title,
                JOptionPane.PLAIN_MESSAGE,
                randomFrom(wrongIcons)
        );
    }

    // success
    private void showSuccessDialog(String msg, String title) {
        JOptionPane.showMessageDialog(
                frame,
                msg,
                title,
                JOptionPane.PLAIN_MESSAGE,
                randomFrom(successIcons)
        );
    }

    // info / neutral
    private void showNeutralDialog(String msg, String title) {
        JOptionPane.showMessageDialog(
                frame,
                msg,
                title,
                JOptionPane.PLAIN_MESSAGE,
                neutralIcon
        );
    }

    private void showNeutralDialog(Object msg, String title) {
        JOptionPane.showMessageDialog(
                frame,
                msg,
                title,
                JOptionPane.PLAIN_MESSAGE,
                neutralIcon
        );
    }

    private int showNeutralConfirm(Object msg, String title, int optionType) {
        return JOptionPane.showConfirmDialog(
                frame,
                msg,
                title,
                optionType,
                JOptionPane.PLAIN_MESSAGE,
                neutralIcon
        );
    }

    private String showNeutralInput(String msg, String title, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                frame,
                msg,
                title,
                JOptionPane.PLAIN_MESSAGE,
                neutralIcon,
                null,
                initialValue
        );
    }

    private String showNeutralOptionInput(String msg, String title, Object[] options, Object initialValue) {
        return (String) JOptionPane.showInputDialog(
                frame,
                msg,
                title,
                JOptionPane.PLAIN_MESSAGE,
                neutralIcon,
                options,
                initialValue
        );
    }

    // ==== UI ====

    private void initUI() {
        frame = new JFrame("Simple Bank System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 520);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel();
        adminPanel = new AdminPanel();
        employeePanel = new EmployeePanel();
        customerPanel = new CustomerPanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(adminPanel, "admin");
        mainPanel.add(employeePanel, "employee");
        mainPanel.add(customerPanel, "customer");

        frame.add(mainPanel);
        frame.setVisible(true);

        showCard("login");
    }

    private void showCard(String name) {
        cardLayout.show(mainPanel, name);
    }

    // ------------------ Panels ------------------

    // Login panel
    private class LoginPanel extends JPanel {
        private JTextField txtUser;
        private JPasswordField txtPass;
        private JButton btnLogin, btnExit;

        public LoginPanel() {
            setLayout(null);

            JLabel lblTitle = new JLabel("Bank System Login", SwingConstants.CENTER);
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
            lblTitle.setBounds(200, 30, 400, 30);
            add(lblTitle);

            JLabel lblUser = new JLabel("User ID:");
            lblUser.setBounds(220, 120, 80, 25);
            add(lblUser);

            txtUser = new JTextField();
            txtUser.setBounds(310, 120, 260, 25);
            add(txtUser);

            JLabel lblPass = new JLabel("Password:");
            lblPass.setBounds(220, 160, 80, 25);
            add(lblPass);

            txtPass = new JPasswordField();
            txtPass.setBounds(310, 160, 260, 25);
            add(txtPass);

            btnLogin = new JButton("Login");
            btnLogin.setBounds(310, 210, 120, 30);
            add(btnLogin);

            btnExit = new JButton("Exit");
            btnExit.setBounds(450, 210, 120, 30);
            add(btnExit);

            btnLogin.addActionListener(e -> doLogin());
            btnExit.addActionListener(e -> System.exit(0));
        }

        private void doLogin() {
            String uid = txtUser.getText().trim();
            String pwd = new String(txtPass.getPassword()).trim();
            if (uid.isEmpty() || pwd.isEmpty()) {
                showWrongDialog("Enter both user id and password.", "Login");
                return;
            }
            Customer c = bankSystem.login(uid, pwd);
            if (c == null) {
                showWrongDialog("Invalid user ID or password.", "Login Failed");
                return;
            }
            currentUser = c;
            String role = c.getRole();
            if ("admin".equalsIgnoreCase(role)) {
                adminPanel.refreshUserList();
                showCard("admin");
            } else if ("employee".equalsIgnoreCase(role)) {
                employeePanel.refreshUserList();
                showCard("employee");
            } else { // customer
                customerPanel.loadCustomer(currentUser);
                showCard("customer");
            }
            txtPass.setText("");
            txtUser.setText("");
        }
    }

    // Admin panel (create employee & view all users)
    private class AdminPanel extends JPanel {
        private DefaultListModel<String> userListModel;
        private JList<String> userList;
        private JButton btnCreateEmployee, btnLogout, btnDeleteUser, btnViewLogs, btnApplyInterest;

        public AdminPanel() {
            setLayout(new BorderLayout(8, 8));
            JLabel lbl = new JLabel("Admin Dashboard", SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
            add(lbl, BorderLayout.NORTH);

            userListModel = new DefaultListModel<>();
            userList = new JList<>(userListModel);
            JScrollPane sp = new JScrollPane(userList);
            add(sp, BorderLayout.CENTER);

            JPanel bottom = new JPanel();
            btnCreateEmployee = new JButton("Create Employee");
            btnDeleteUser = new JButton("Delete User");
            btnViewLogs = new JButton("View Logs");
            btnLogout = new JButton("Logout");
            btnApplyInterest = new JButton("Apply Interest");

            bottom.add(btnCreateEmployee);
            bottom.add(btnDeleteUser);
            bottom.add(btnApplyInterest);
            bottom.add(btnViewLogs);
            bottom.add(btnLogout);
            add(bottom, BorderLayout.SOUTH);

            btnCreateEmployee.addActionListener(e -> createEmployee());
            btnDeleteUser.addActionListener(e -> deleteSelectedUser());
            btnLogout.addActionListener(e -> {
                currentUser = null;
                showCard("login");
            });
            btnViewLogs.addActionListener(e -> viewLogs());
            btnApplyInterest.addActionListener(e -> applyInterestToAllSavings());
        }

        private void applyInterestToAllSavings() {
            int count = 0;

            for (Account a : bankSystem.getAccounts().values()) {
                if (a instanceof SavingsAccount) {
                    ((SavingsAccount) a).applyInterest();
                    count++;
                }
            }

            bankSystem.saveState(); // Save updated balances

            showSuccessDialog("Interest applied to " + count + " savings accounts!", "Success");
        }

        private void viewLogs() {
            String logs = TransactionLogger.readLog();
            JTextArea area = new JTextArea(logs);
            area.setEditable(false);

            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(600, 400));

            showNeutralDialog(scroll, "Transaction Logs");
        }

        private void createEmployee() {
            JTextField tfId = new JTextField();
            JTextField tfName = new JTextField();
            JTextField tfPass = new JTextField();

            Object[] fields = {
                    "User ID:", tfId,
                    "Name:", tfName,
                    "Password:", tfPass
            };
            int res = showNeutralConfirm(fields, "Create Employee", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                String id = tfId.getText().trim();
                String name = tfName.getText().trim();
                String pass = tfPass.getText().trim();
                if (id.isEmpty() || name.isEmpty() || pass.isEmpty()) {
                    showWrongDialog("All fields are required.", "Error");
                    return;
                }
                boolean ok = bankSystem.createUser(id, name, pass, "employee");
                if (ok) {
                    showSuccessDialog("Employee created.", "Success");
                    refreshUserList();
                } else {
                    showWrongDialog("User id already exists.", "Error");
                }
            }
        }

        private void deleteSelectedUser() {
            String sel = userList.getSelectedValue();
            if (sel == null) {
                showNeutralDialog("Select a user to delete.", "Info");
                return;
            }
            int confirm = showNeutralConfirm("Delete user: " + sel + " ?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = bankSystem.deleteUser(sel);
                if (ok) {
                    showSuccessDialog("User deleted.", "Success");
                    refreshUserList();
                } else {
                    showWrongDialog("Delete failed.", "Error");
                }
            }
        }

        public void refreshUserList() {
            userListModel.clear();
            for (Customer c : bankSystem.getUsers().values()) {
                userListModel.addElement(c.getUserId());
            }
        }
    }

    // Employee panel (create/delete customers and create accounts)
    private class EmployeePanel extends JPanel {
        private DefaultListModel<String> custListModel;
        private JList<String> custList;
        private JButton btnCreateCustomer, btnCreateAccount, btnDeleteCustomer, btnLogout;

        public EmployeePanel() {
            setLayout(new BorderLayout(8, 8));
            JLabel lbl = new JLabel("Employee Dashboard", SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 18));
            add(lbl, BorderLayout.NORTH);

            custListModel = new DefaultListModel<>();
            custList = new JList<>(custListModel);
            add(new JScrollPane(custList), BorderLayout.CENTER);

            JPanel bottom = new JPanel();
            btnCreateCustomer = new JButton("Create Customer");
            btnCreateAccount = new JButton("Create Account For Selected");
            btnDeleteCustomer = new JButton("Delete Customer");
            btnLogout = new JButton("Logout");
            bottom.add(btnCreateCustomer);
            bottom.add(btnCreateAccount);
            bottom.add(btnDeleteCustomer);
            bottom.add(btnLogout);
            add(bottom, BorderLayout.SOUTH);

            btnCreateCustomer.addActionListener(e -> createCustomer());
            btnCreateAccount.addActionListener(e -> createAccountForSelected());
            btnDeleteCustomer.addActionListener(e -> deleteSelectedCustomer());
            btnLogout.addActionListener(e -> {
                currentUser = null;
                showCard("login");
            });
        }

        private void createCustomer() {
            JTextField tfId = new JTextField();
            JTextField tfName = new JTextField();
            JTextField tfPass = new JTextField();

            Object[] fields = {
                    "User ID:", tfId,
                    "Name:", tfName,
                    "Password:", tfPass
            };
            int res = showNeutralConfirm(fields, "Create Customer", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                String id = tfId.getText().trim();
                String name = tfName.getText().trim();
                String pass = tfPass.getText().trim();
                if (id.isEmpty() || name.isEmpty() || pass.isEmpty()) {
                    showWrongDialog("All fields are required.", "Error");
                    return;
                }
                boolean ok = bankSystem.createUser(id, name, pass, "customer");
                if (ok) {
                    showSuccessDialog("Customer created.", "Success");
                    refreshUserList();
                } else {
                    showWrongDialog("User id already exists.", "Error");
                }
            }
        }

        private void createAccountForSelected() {
            String sel = custList.getSelectedValue();
            if (sel == null) {
                showNeutralDialog("Select a customer first.", "Info");
                return;
            }
            String[] options = {"Savings", "Current"};
            String type = showNeutralOptionInput("Select account type:", "Account Type",
                    options, options[0]);
            if (type == null) return;

            String initStr = showNeutralInput("Initial Balance (number):", "Initial Balance", "0");
            if (initStr == null) return;
            double init;
            try {
                init = Double.parseDouble(initStr);
            } catch (NumberFormatException ex) {
                showWrongDialog("Invalid number entered!", "Error");
                return;
            }
            String acctNo = bankSystem.createAccountForUser(sel, type.toLowerCase(), init);
            if (acctNo != null) {
                showSuccessDialog("Account created: " + acctNo, "Success");
                refreshUserList();
            } else {
                showWrongDialog("Account creation failed.", "Error");
            }
        }

        private void deleteSelectedCustomer() {
            String sel = custList.getSelectedValue();
            if (sel == null) {
                showNeutralDialog("Select a customer to delete.", "Info");
                return;
            }
            int confirm = showNeutralConfirm("Delete customer: " + sel + " ?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = bankSystem.deleteUser(sel);
                if (ok) {
                    showSuccessDialog("Customer deleted.", "Success");
                    refreshUserList();
                } else {
                    showWrongDialog("Delete failed.", "Error");
                }
            }
        }

        public void refreshUserList() {
            custListModel.clear();
            for (Customer c : bankSystem.getUsers().values()) {
                if ("customer".equalsIgnoreCase(c.getRole()))
                    custListModel.addElement(c.getUserId());
            }
        }
    }

    // Customer panel (view accounts, deposit, withdraw, details)
    private class CustomerPanel extends JPanel {
        private JLabel lblWelcome;
        private DefaultListModel<String> acctListModel;
        private JList<String> acctList;
        private JButton btnDeposit, btnWithdraw, btnRefresh, btnLogout, btnDetails;

        public CustomerPanel() {
            setLayout(new BorderLayout(6, 6));
            lblWelcome = new JLabel("Customer Dashboard", SwingConstants.CENTER);
            lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 18));
            add(lblWelcome, BorderLayout.NORTH);

            acctListModel = new DefaultListModel<>();
            acctList = new JList<>(acctListModel);
            add(new JScrollPane(acctList), BorderLayout.CENTER);

            JPanel bottom = new JPanel();
            btnDeposit = new JButton("Deposit");
            btnWithdraw = new JButton("Withdraw");
            btnDetails = new JButton("Account Details");
            btnRefresh = new JButton("Refresh");
            btnLogout = new JButton("Logout");
            bottom.add(btnDeposit);
            bottom.add(btnWithdraw);
            bottom.add(btnDetails);
            bottom.add(btnRefresh);
            bottom.add(btnLogout);
            add(bottom, BorderLayout.SOUTH);

            btnDeposit.addActionListener(e -> doDeposit());
            btnWithdraw.addActionListener(e -> doWithdraw());
            btnRefresh.addActionListener(e -> reloadAccounts());
            btnLogout.addActionListener(e -> {
                currentUser = null;
                showCard("login");
            });
            btnDetails.addActionListener(e -> showDetails());
        }

        public void loadCustomer(Customer c) {
            currentUser = c;
            lblWelcome.setText("Welcome, " + c.getName());
            reloadAccounts();
        }

        private void reloadAccounts() {
            acctListModel.clear();
            if (currentUser == null) return;
            List<String> accts = currentUser.getAccountNumbers();
            for (String a : accts) acctListModel.addElement(a);
        }

        private void doDeposit() {
            String sel = acctList.getSelectedValue();
            if (sel == null) {
                showNeutralDialog("Select an account first.", "Info");
                return;
            }
            String amtStr = showNeutralInput("Amount to deposit:", "Deposit", "0");
            if (amtStr == null) return;
            double amt;
            try {
                amt = Double.parseDouble(amtStr);
            } catch (NumberFormatException ex) {
                showWrongDialog("That is not a valid amount.", "Wrong Input");
                return;
            }
            boolean ok = bankSystem.deposit(sel, amt);
            if (ok) {
                showSuccessDialog("Amount deposited successfully.", "Success");
            } else {
                showWrongDialog("Deposit failed.", "Error");
            }
            reloadAccounts();
        }

        private void doWithdraw() {
            String sel = acctList.getSelectedValue();
            if (sel == null) {
                showNeutralDialog("Select an account first.", "Info");
                return;
            }
            String amtStr = showNeutralInput("Amount to withdraw:", "Withdraw", "0");
            if (amtStr == null) return;
            double amt;
            try {
                amt = Double.parseDouble(amtStr);
            } catch (NumberFormatException ex) {
                showWrongDialog("That is not a valid amount.", "Wrong Input");
                return;
            }
            boolean ok = bankSystem.withdraw(sel, amt);
            if (ok) {
                showSuccessDialog("Amount withdrawn successfully.", "Success");
            } else {
                showWrongDialog("Withdrawal failed (maybe insufficient funds).", "Error");
            }
            reloadAccounts();
        }

        private void showDetails() {
            String sel = acctList.getSelectedValue();
            if (sel == null) {
                showNeutralDialog("Select an account first.", "Info");
                return;
            }
            Account a = bankSystem.getAccount(sel);
            if (a == null) {
                showWrongDialog("Account not found.", "Error");
                return;
            }
            String msg = a.toString();
            showNeutralDialog(msg, "Account Details");
        }
    }

    // ------------------ Entry Point ------------------
    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread-safety
        SwingUtilities.invokeLater(BankUI::new);
    }
}
