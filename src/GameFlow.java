import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

public class GameFlow {

    private InputHandler m_in;
    private OutputHandler m_out;
    private CommandExecutorProxy proxyExecutor ;
    private DataBaseExecutor dbExecutor;
    private String userName;
    private Player player;

    GameFlow(InputHandler in, OutputHandler out, DataBaseExecutor dbExecutor, String playerName) {
        m_in = in;
        m_out = out;

        this.dbExecutor = dbExecutor;
        this.player = new Player(playerName);
    }

    private boolean handleWelcomeMessage() throws IOException {
        m_out.printString("Welcome to the game " + this.player.name + "! Are you a player or an admin?");
        // handle input
        String res = m_in.get();
        res = res.toLowerCase();
        if (res.toLowerCase().equals("admin")) {
            this.userName = "admin";
        } else if (res.toLowerCase().equals("player")) {
            this.userName = "player";
        }

        boolean r = res.equals("player") || res.equals("admin");
        if (r) {
            this.proxyExecutor = new CommandExecutorProxy(this.userName, dbExecutor);
        }
        return r;
    }

    private boolean handleMainMenu() throws SQLException {
        m_out.printString("1. play\n2. exit");
        String res = m_in.get();
        res = res.toLowerCase();
        if (res.equals("play")) {
            Set<String> subjects = dbExecutor.getSubjectList();
            if (subjects.isEmpty()) {
                m_out.printString("Please choose a new subject:");
            } else {
                m_out.printString("Please choose a subject from the list or enter a new one:");
                for (String s : subjects) {
                    m_out.printString("\t- " + s);
                }
            }

            // Check if subject exists
            String chosenSubject = m_in.get();
            dbExecutor.createNewCacheForSubject(chosenSubject);
            return true;
        }
        return false;
    }
    private boolean handleRemoveQuestion() throws Exception {
        String[] args = new String[1];
        m_out.printString("Add the question you want to remove:");
        m_out.printString("(If the question doesn't exist this will have no effect)");
        args[0] = m_in.get();
        this.proxyExecutor.runCommand("remove", args);
        return true;
    }

    private boolean handleAddQuestion() throws Exception {
        String []args = new String[4];
        m_out.printString("Add the following information for a new question:");
        args[0] = dbExecutor.cur_subject;
        m_out.printString("Question: ");
        args[1] = m_in.get();
        m_out.printString("The number of the correct answer: ");
        args[2] = m_in.get();
        m_out.printString("Possible answers (separated by a comma):");
        String answers = m_in.get();
        String [] answers_list = answers.split(",");
        args[3] = "";
        for (int i = 0; i < answers_list.length; i++) {
            args[3] += (i + 1) + ". " + answers_list[i] + "\n";
        }

        this.proxyExecutor.runCommand("add", args);
        return true;
    }

    private boolean handleGameOptions() throws Exception {
        while (true) {
            m_out.printString("What to do?\n1. add\n2. remove\n3. practice\nn. exit");
            String userInput = m_in.get();
            switch (userInput) {
                case "add":
                    try {
                        handleAddQuestion();
                        m_out.printString("Your question Added successfully");
                    } catch (Exception e) {
                        m_out.printString("Failed to add a new question with error: " + e.toString());
                    }
                    return handleGameOptions();
                case "remove":
                    handleRemoveQuestion();
                    m_out.printString("Your question removed successfully");
                    return handleGameOptions();
                case "practice":
                    String Q = dbExecutor.getQuestion();
                    if (Q.isEmpty()) {
                        m_out.printString("You don't have questions anymore");
                        return false;
                    }
                    m_out.printString(Q);
                    m_out.printString(dbExecutor.getWrong_ans());
                    m_out.printString("Enter your answer:");
                    userInput = m_in.get();
                    if (Integer.parseInt(userInput) == dbExecutor.getAns(Q)) {
                        Subject.getInstance().processEvent(Subject.Choice.RIGHT);
                        m_out.printString("Correct!");
                    } else {
                        Subject.getInstance().processEvent(Subject.Choice.WRONG);
                        m_out.printString("Incorrect!");
                        m_out.printString("Life left: " +  this.player.getLife());
                    }
                    if (userInput.equals("exit")) return false;
                    return handleGameOptions();
                case "exit":
                    return false;
                default:
                    break;
            }
        }
    }

    private boolean handleMenu(int stage) throws Exception {
        switch(stage) {
            case 0:
                return handleWelcomeMessage();
            case 1:
                return handleMainMenu();
            case 2:
                return handleGameOptions();
            default:
                return false;
        }
    }

    private void gameLoop() throws Exception {
        String userInput = "";
        int gameStage = 0;
        while (!userInput.equals("quit") && this.player.getLife() != 0) {
            boolean res = handleMenu(gameStage);

            if (res) {
                gameStage += 1;
            } else {
                if (gameStage > 0) {
                    gameStage -= 1;
                    dbExecutor.flushCache();
                    m_out.printString("Saved current database to disk");
                }
                m_out.printString("Do you want to quit or continue playing?\n 1. quit\n 2. continue");
                userInput = m_in.get();
                if (userInput.equals("continue")) {
                    m_out.printString("Life left: " +  this.player.getLife());
                    m_out.printString("Current score: " +  this.player.getScore());
                }
            }
        }

        if (this.player.getLife() == 0) {
            m_out.printString("Game Over :(");
        }
    }

    public static void main(String []args) throws Exception {
        InputHandler in = new KeyboardInHandler();
        OutputHandler out = new KeyboardOutHandler();
        DataBaseExecutor dbEx = new DataBaseExecutor("Test.db");

        String playerName;
        out.printString("Enter your name: ");
        playerName = in.get();

        GameFlow gf = new GameFlow(in, out, dbEx, playerName);
        gf.gameLoop();

        out.printString("Your final score is " +  gf.player.getScore());
    }
}
