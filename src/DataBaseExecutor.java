import java.sql.SQLException;

public class DataBaseExecutor extends dataBase implements CommandExecutor {
    public DataBaseExecutor(String fileName) throws RuntimeException, SQLException {
        super(fileName);
    }
    public void runCommand(String cmd, String[] args) throws Exception {
    switch (cmd.toLowerCase()) {

        case "add":
            super.addQuestion(args[0], args[1], Integer.parseInt(args[2]), args[3]);
            break;
        case "remove":
            super.deleteQuestion(args[0]);
            break;
        case "play":
            super.getQuestion();
    }
    }
}
