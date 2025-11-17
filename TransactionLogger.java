import java.io.*;
import java.time.LocalDateTime;

public class TransactionLogger {
    private static final String LOG_FILE = "transactions.log";

    public static synchronized void log(String message) {
        BufferedWriter bw = null;
        try {
            File f = new File(LOG_FILE);
            bw = new BufferedWriter(new FileWriter(f, true));
            String entry = String.format("%s - %s", LocalDateTime.now(), message);
            bw.write(entry);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.err.println("Logging failed: " + e.getMessage());
        } finally {
            if (bw != null) {
                try { bw.close(); } catch (IOException ignored) {}
            }
        }
    }
    public static String readLog() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }catch (IOException e) {
            return "Unable to read log file: " + e.getMessage();
        }
        return sb.toString();
    }

}
