// The player is an Observer

public class Player implements IObserver {
    public static final int LIFE_COUNT = 5;
    public static final int BEGINNING = 0;
    public String name;
    private Counter lifeCounter;
    private Counter scoresCounter;


    Player(String name) {
        this.name = name;

        // The user will start with 5 lives
        this.lifeCounter = new Counter(LIFE_COUNT);

        // The user will start with 0 score
        this.scoresCounter = new Counter(BEGINNING);

        Subject subject = Subject.getInstance();
        subject.Subscribe(this);
    }

    public int getScore() {
        return scoresCounter.getValue();
    }

    public int getLife() {
        return lifeCounter.getValue();
    }

    @Override
    public void handleEvent(Subject.Choice choice) {
        switch (choice) {
            case RIGHT:
                this.scoresCounter.increase(10);
                break;
            case WRONG:
                this.lifeCounter.decrease(1);
                break;
        }
    }
}
