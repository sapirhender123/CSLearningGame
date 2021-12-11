
import java.awt.*;
import java.util.*;
import java.util.Scanner;

public class KeyboardInHandler implements InputHandler {

    @Override
    public String get() {
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        return myObj.nextLine();
    }
}

