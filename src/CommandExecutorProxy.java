public class CommandExecutorProxy {

    private boolean isAdmin;
    DataBaseExecutor executor;

    public CommandExecutorProxy(String username, DataBaseExecutor dbExecutor){
        super();
        username = username.toLowerCase();
        if (username.equals("admin")) {
            isAdmin = true;
        }
        this.executor = dbExecutor;
    }

    public void runCommand(String command, String[] args) throws Exception{
            if (!isAdmin) {
                switch (command) {
                    case "add":
                    case "remove":
                        throw new Exception("Permission Denied");
                }
            }
            executor.runCommand(command,args);
    }
}


/**
--- gameFlow.java
add_question {

print add a new question:
string [10] args;
print >> question:
args[0] = cin keybaordInput.get
print >> answer:
args[1] = ...

// args = {question, answer, ....}
runCommand(addQuestion, args)


----- DB impl commandexecutor
runCommand(String command, String []args)

switch (command)
case addQuestion
    this.addQuestion(args)

case ...

default ... ivalid
*/
