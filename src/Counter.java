public class Counter {
    private int c;
    public Counter(int x) {
        this.c = x;
    }
    public void increase(int number) {
        this.c += number;
    }
    public void decrease(int number) {
        this.c -= number;
    }
    public int getValue() {
        return c;
    }
}
