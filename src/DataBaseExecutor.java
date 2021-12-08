import java.sql.SQLException;
import java.util.Map;

public class DataBaseExecutor extends dataBase implements CommandExecutor {
    Map<String, Command> cmds;

    public DataBaseExecutor(String fileName) throws RuntimeException, SQLException {
        super(fileName);
        cmds = Map.ofEntries(
                Map.entry("add", new AddCmd(this)),
                Map.entry("remove", new RemCmd(this)),
                Map.entry("practice", new PracticeCmd(this))
        );
    }
    public void runCommand(String cmd, String[] args) {
        Command command = cmds.get(cmd.toLowerCase());
        if (command != null) {
            cmds.get(cmd.toLowerCase()).run(args);
        }
    }
}
