import java.sql.SQLException;
import java.util.Set;

public class GameFlow {

    private InputHandler m_in;
    private OutputHandler m_out;
    private CommandExecutorProxy proxyExecutor ;
    private DataBaseExecutor dbExecutor;
    private String userName;

    GameFlow(InputHandler in, OutputHandler out, DataBaseExecutor dbExecutor) {
        m_in = in;
        m_out = out;

        this.dbExecutor = dbExecutor;
    }

    private boolean handleWelcomeMessage() {
        m_out.printString("Welcome to the game! Are you a player or an admin?");
        // handle input
        String res = m_in.get();
        res = res.toLowerCase();
        if (res.toLowerCase().equals("admin")) {
            this.userName = "admin";
        }
        if (res.toLowerCase().equals("player")) {
            this.userName = "player";
        }
        this.proxyExecutor = new CommandExecutorProxy(this.userName, dbExecutor);
        return res.equals("player") || res.equals("admin");
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
                        m_out.printString("Correct!");
                    } else {
                        m_out.printString("Incorrect!");
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
        while (!userInput.equals("quit")) {
            boolean res = handleMenu(gameStage);

            if (res) {
                gameStage += 1;
            } else {
                gameStage -= 1;

                m_out.printString("Do you want to quit or continue playing?\n 1. quit\n 2. continue");
                userInput = m_in.get();
                dbExecutor.flushCache();
            }

            // ... More logic?
        }
    }

    public static void main(String []args) throws Exception {
        InputHandler in = new KeyboardInHandler();
        OutputHandler out = new KeyboardOutHandler();
        DataBaseExecutor dbEx = new DataBaseExecutor("Test.db");

        GameFlow gf = new GameFlow(in, out, dbEx);
        gf.gameLoop();
    }

//    public static void main(String[] args) throws SQLException {
//        dataBase db = new dataBase("Test.db");
//        db.createNewCacheForSubject("Algo");
//        db.addQuestion("Algo", 4, "How much I love it",
//                2, "1. 0\n2. 60");
//        int ans = db.getAns("How much I love it");
//        System.out.println(ans);
//        db.flushCache();
//        db.createNewCacheForSubject("asas");
//        db.addQuestion("asas", 4, "What is DHCP",
//                1, "1. client\n2. name of a person");
//        db.addQuestion("asas", 5, "What is AAAA",
//                1, "1. client\n2. name of a person");
//        db.addQuestion("asas", 6, "What is BBBB",
//                1, "1. client\n2. name of a person");
//
//        ans = db.getAns("How much I love it");
//        System.out.println(ans);
//
//        ans = db.getAns("What is DHCP");
//        System.out.println(ans);
//
//        //db.deleteQuestion("How much I love it");
//        System.out.println(db.getQuestion());
//        System.out.println(db.getQuestion());
//        db.deleteQuestion("What is DHCP");
//        System.out.println(db.getQuestion());
//
//        if (db.getQuestion().isEmpty()) {
//            System.out.println("No more questions");
//        }
//
//        db.closeAllDB();
//
//    }


}
