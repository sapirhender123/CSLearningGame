import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandExecutorProxy {

    private boolean isAdmin;
    DataBaseExecutor executor;
    List<String> adminOnly = new ArrayList<String>();

    public CommandExecutorProxy(String username, DataBaseExecutor dbExecutor) throws IOException {
        super();
        username = username.toLowerCase();
        if (username.equals("admin")) {
            isAdmin = true;
        }
        this.executor = dbExecutor;
        ReadAdminActions();
    }

    // read the admin only actions from a file
    public void ReadAdminActions() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("AdminActions"))) {
            String line;
            while ((line = br.readLine()) != null) {
                adminOnly.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runCommand(String command, String[] args) throws Exception{
            if (!isAdmin && adminOnly.contains(command)) {
                    throw new Exception("Permission Denied");
                }
            executor.runCommand(command, args);
    }
}