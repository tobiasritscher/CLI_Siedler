package ch.zhaw.catan;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;

import java.util.ArrayList;
import java.util.Scanner;

public class PlayGame {
    private Scanner scanner = new Scanner(System.in);
    Config config = new Config();

    public enum Actions{
        NEW_GAME, QUIT
    }

    private void run(){
        TextIO textIO = TextIoFactory.getTextIO();
        TextTerminal<?> textTerminal = textIO.getTextTerminal();

        boolean running = true;
        while (running) {
            switch (getEnumValue(textIO, PlayGame.Actions.class)) {
                case NEW_GAME:
                    //TODO: Hier wird ein neues Spiel instanziert
                    break;
                case QUIT:
                    running = false;
                    break;
                default:
                    throw new IllegalStateException("Internal error found - Command not implemented.");
            }
        }
        textIO.dispose();
    }

    public static <T extends Enum<T>> T getEnumValue(TextIO textIO, Class<T> commands) {
        return textIO.newEnumInputReader(commands).read("What would you like to do?");
    }

    public int numberOfPlayers() {
        System.out.println("How many players will be playing?");
        return scanner.nextInt();
    }

    public static void main(String[] Args) {
        new PlayGame().run();
    }
}
