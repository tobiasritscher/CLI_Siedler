package ch.zhaw.catan;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class PlayGame {
    private static SiedlerGame siedlerGame;
    private SiedlerBoard board = new SiedlerBoard();
    private SiedlerBoardTextView view = new SiedlerBoardTextView(board);
    private int numberOfPlayers;
    private static TextIO textIO = TextIoFactory.getTextIO();
    private static TextTerminal<SwingTextTerminal> textTerminal = (SwingTextTerminal) textIO.getTextTerminal();
    private Bank bank = new Bank();

    //starts entire game with both phases
    private void run() {

        UI.setupTerminal();
        UI.setBookmarkBlankScreen();

        if (!UI.buildStartMenu(view)) {

            numberOfPlayers = UI.askNumberOfPlayers();
            //Ccheatcode to make the phase one for you
            int POINTS_TO_WIN = 5;
            if (numberOfPlayers == 420) {
                numberOfPlayers = 2;
                siedlerGame = new SiedlerGame(POINTS_TO_WIN, numberOfPlayers);
                siedlerGame.placeInitialSettlement(new Point(6, 16), siedlerGame.getPlayers().get(0), board, view);
                siedlerGame.placeInitialSettlement(new Point(9, 13), siedlerGame.getPlayers().get(0), board, view);
                siedlerGame.placeInitialSettlement(new Point(6, 6), siedlerGame.getPlayers().get(1), board, view);
                siedlerGame.placeInitialSettlement(new Point(6, 10), siedlerGame.getPlayers().get(1), board, view);
                siedlerGame.placeInitialRoad(new Point(6, 16), new Point(5, 15), board, view, siedlerGame.getPlayers().get(0));
                siedlerGame.placeInitialRoad(new Point(9, 13), new Point(10, 12), board, view, siedlerGame.getPlayers().get(0));
                siedlerGame.placeInitialRoad(new Point(6, 6), new Point(7, 7), board, view, siedlerGame.getPlayers().get(1));
                siedlerGame.placeInitialRoad(new Point(6, 10), new Point(7, 9), board, view, siedlerGame.getPlayers().get(1));

                UI.refresh(view);
                giveResourcesAfterFirstPhase(board);
                secondPhase();
            } else {
                UI.refresh(view);
                UI.print("Ok, there will be " + numberOfPlayers + " players");
                UI.promptEnter();

                //Creating a new game with both phases
                siedlerGame = new SiedlerGame(POINTS_TO_WIN, numberOfPlayers);
                firstPhase();
                secondPhase();
            }
        }
        UI.closeTerminal();
    }

    //distribution of first 2 settlements and their roads
    private void firstPhase() {

        for (int i = 0; i < siedlerGame.getPlayers().size(); i++) {
            String currentPlayerFaction = siedlerGame.getPlayers().get(i).getFaction().name();
            Player currentPlayer = siedlerGame.getPlayers().get(i);
            UI.resetBookmark("SHOW_MAP");

            setInitialSettlementsAndRoads(currentPlayerFaction, currentPlayer, "first");
        }

        for (int i = siedlerGame.getPlayers().size() - 1; i >= 0; i--) {
            String currentPlayerFaction = siedlerGame.getPlayers().get(i).getFaction().name();
            Player currentPlayer = siedlerGame.getPlayers().get(i);

            setInitialSettlementsAndRoads(currentPlayerFaction, currentPlayer, "second");

        }
        giveResourcesAfterFirstPhase(board); // all the players receive resources depending next to which field they are
    }


    private void setInitialSettlementsAndRoads(String currentPlayerFaction, Player currentPlayer, String turn) {
        //ask for first settlement
        UI.refresh(view);
        int x = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a x coordinate for your " + turn + " settlement\n");
        UI.refresh(view);
        int y = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a y coordinate for your " + turn + " settlement\n");
        //set first settlement
        Point point = new Point(x, y);
        Point newPoint = siedlerGame.isPointACorner(point);
        siedlerGame.placeInitialSettlement(newPoint, currentPlayer, board, view);
        UI.refresh(view);

        //ask for first road start
        int xRoadStart = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a x coordinate for the start of your " + turn + " road\n");
        UI.refresh(view);

        int yRoadStart = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a y coordinate for the start of your " + turn + " road\n");
        UI.refresh(view);

        Point roadStart = new Point(xRoadStart, yRoadStart);

        //ask for first road finish
        int xRoadFinish = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a x coordinate for the finish of your " + turn + " road\n");
        UI.refresh(view);

        int yRoadFinish = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a y coordinate for the finish of your " + turn + " road\n");
        UI.refresh(view);

        //set first road
        Point roadEnd = new Point(xRoadFinish, yRoadFinish);
        siedlerGame.placeInitialRoad(roadStart, roadEnd, board, view, currentPlayer);
        UI.refresh(view);
    }

    private void giveResourcesAfterFirstPhase(SiedlerBoard board) {
        for (Point field : board.getFields()) {
            if (board.getField(field) != Config.Land.DESERT && board.getField(field) != Config.Land.WATER) {
                if (!board.getCornersOfField(field).isEmpty()) {
                    for (Settlement settlement : board.getCornersOfField(field)) {
                        Config.Resource currentResource = (board.getField(field).getResource());
                        settlement.getPlayer().addResources(currentResource, 1, bank);
                        bank.removeResources(currentResource, 1);
                    }
                }
            }
        }

        printAllGivenRessourcesOfAllPlayers();
        //Ask players to press enter in order to start the second game phase
        UI.promptEnter();
    }

    private void printAllGivenRessourcesOfAllPlayers() {
        for (Player player : siedlerGame.getPlayers()) {
            StringBuilder tempOutput = new StringBuilder(player.toString() + " has been given: ");
            for (Config.Resource resource : player.getResourcesInPossession().keySet()) {

                tempOutput.append(player.getResourcesInPossession().get(resource)).append(" ").append(resource.toString()).append(", ");
            }
            String finalOutput = tempOutput.substring(0, tempOutput.length() - 2);
            finalOutput += "\n";
            UI.print(finalOutput);
        }
    }

    private void giveResourcesFromDice(int rolledNumber) {
        for (Point field : board.getFields()) {
            if (board.getField(field) != Config.Land.DESERT && board.getField(field) != Config.Land.WATER && Config.getStandardDiceNumberPlacement().get(field) == rolledNumber) {
                if (!board.getCornersOfField(field).isEmpty()) {
                    for (Settlement settlement : board.getCornersOfField(field)) {
                        Config.Resource currentResource = board.getField(field).getResource();
                        Player currentPlayer = settlement.getPlayer();
                        int amount;
                        if (settlement.getIsCity())
                            amount = 2;
                        else
                            amount = 1;
                        currentPlayer.addResources(currentResource, amount, bank);
                        textTerminal.print(currentPlayer.getFaction() + " has recieved " + amount + " " + currentResource + '\n');
                    }
                }
            }
        }
    }

    private void divideAllResources() {
        for (int i = 0; i < numberOfPlayers; ++i) {
            int totalResources = 0;
            for (Integer amountOfRessource : siedlerGame.getPlayers().get(i).getResourcesInPossession().values()) {
                totalResources += amountOfRessource;
            }

            //remove random resources from players with more then seven cards
            if (totalResources > 7) {
                int resourcesToDelete = (totalResources - totalResources % 2) / 2;
                for (int j = 0; j < resourcesToDelete; ++j) {
                    //new Arraylist with all resources the player has, to choose a random resource to remove
                    ArrayList<Config.Resource> resources = new ArrayList<>(siedlerGame.getPlayers().get(i).getResourcesInPossession().keySet());
                    //create random number to choose which resource to delete
                    int random = new Random().nextInt(resources.size());
                    siedlerGame.getPlayers().get(i).removeResources(resources.get(random), 1, bank);
                    bank.addResources(resources.get(random), 1);
                }
            }
        }
    }

    private void secondPhase() {
        UI.refresh(view);

        // each player begins their turn with a dice roll and receive the resources corresponding to the fields
        boolean gameIsRunning = true;
        for (int i = 0; gameIsRunning; i = (i + 1) % numberOfPlayers) {
            Player currentPlayer = siedlerGame.getPlayers().get(i);
            int rolledNumber = Dice.roll();
            UI.print(currentPlayer + " rolled a " + rolledNumber + "\n");

            // If the number rolled is 7 all players with more than 7 resources lose randomly half of their resources
            if (rolledNumber == Config.MAX_CARDS_IN_HAND_NO_DROP) {
                divideAllResources();
            } else {
                giveResourcesFromDice(rolledNumber);
            }

            //gives the different options to the player after the dice roll
            boolean running = true;
            do {
                UI.print("It's " + currentPlayer + "'s turn\n");
                UI.newLine();
                int decision = UI.printSecondPhaseMenu();
                ChosenOption chosenOption = ChosenOption.codeOfOption(decision);
                assert chosenOption != null;

                // switch case to choose the option
                switch (chosenOption) {

                    case TRADE:
                        UI.refresh(view);
                        siedlerGame.askPlayerWhatToTrade(currentPlayer, bank);
                        break;

                    case BUILD_SETTLEMENT:
                        UI.refresh(view);
                        int x = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for your settlement\n");

                        UI.refresh(view);
                        int y = textIO.newIntInputReader().read(currentPlayer + " please pick a y coordinate for your settlement\n");

                        UI.refresh(view);
                        Point position = new Point(x, y);
                        siedlerGame.placeSettlement(position, currentPlayer, board, bank, view);
                        running = siedlerGame.verifyWinner(currentPlayer);
                        gameIsRunning = running;
                        UI.promptEnter();
                        UI.refresh(view);
                        break;

                    case BUILD_ROAD:
                        UI.refresh(view);
                        int a = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for the start of your road\n");

                        UI.refresh(view);
                        int b = textIO.newIntInputReader().read(currentPlayer + " please pick a y coordinate for the start of your road\n");
                        Point roadStart = new Point(a, b);

                        UI.refresh(view);
                        int c = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for the finish of your road\n");

                        UI.refresh(view);
                        int d = textIO.newIntInputReader().read(currentPlayer + " please pick a y coordinate for the finish of your road\n");

                        Point roadEnd = new Point(c, d);
                        siedlerGame.placeRoad(roadStart, roadEnd, board, currentPlayer, bank, view);
                        running = siedlerGame.verifyWinner(currentPlayer);
                        gameIsRunning = running;

                        UI.promptEnter();
                        UI.refresh(view);
                        break;

                    case BUILD_CITY:
                        UI.refresh(view);
                        int e = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for your city\n");

                        UI.refresh(view);
                        int f = textIO.newIntInputReader().read(currentPlayer + " please pick a y coordinate for your city\n");

                        Point where = new Point(e, f);
                        siedlerGame.placeCity(where, currentPlayer, bank, view);

                        running = siedlerGame.verifyWinner(currentPlayer);
                        gameIsRunning = running;
                        UI.promptEnter();
                        UI.refresh(view);
                        break;

                    case CHECK_RESOURCES:
                        UI.refresh(view);

                        for (HashMap.Entry<Config.Resource, Integer> entry : currentPlayer.getResourcesInPossession().entrySet()) {
                            UI.print(currentPlayer + " has " + entry.getKey() + ": " + entry.getValue() + "\n");
                        }

                        UI.promptEnter();
                        UI.refresh(view);

                        break;

                    case END_TURN:
                        UI.refresh(view);
                        String sure = textIO.newStringInputReader().read(currentPlayer + " are you sure you want to end your turn? (Y/N)\n");
                        if (sure.equalsIgnoreCase("Y")) {
                            running = false;
                        }
                        break;

                    case QUIT:
                        UI.refresh(view);
                        String ciao = textIO.newStringInputReader().read("Sure?(Y/N)\n");
                        if (ciao.equalsIgnoreCase("Y")) {
                            gameIsRunning = false;
                            running = false;
                        }
                        break;

                    case CHEATCODE:
                        for (Config.Resource resource : Config.Resource.values()) {
                            currentPlayer.addWithCheat(resource);
                        }
                        break;

                    default:
                        UI.print("The number you have selected doesn't exist, please try again\n");
                }
            } while (running);
        }
    }

    public enum ChosenOption {
        WRONG_INPUT(0, "Wrong Input"),
        TRADE(1, "Trade with bank"),
        BUILD_SETTLEMENT(2, "Build Settlement"),
        BUILD_ROAD(3, "Build Road"),
        BUILD_CITY(4, "Build City"),
        CHECK_RESOURCES(5, "Check my resources"),
        END_TURN(6, "End my turn"),
        QUIT(7, "Quit game"),
        CHEATCODE(420, "");

        private final int chosenOptionCode;
        private final String textForUser;

        /**
         * This function returns the chosen option in the enum ChosenOption
         * @return The number of the enum
         */
        public int getChosenOptionCode() {
            return chosenOptionCode;
        }

        /**
         * This function returns the text for the corresponding text for the user
         * specified in the enum ChosenOption
         * @return the text for the user to print
         */
        public String getTextForUser() {
            return textForUser;
        }

        ChosenOption(int chosenOptionCode, String textForUser) {
            this.chosenOptionCode = chosenOptionCode;
            this.textForUser = textForUser;
        }

        /**
         * This function returns the value of chosen enum
         * @param label chosen option code
         * @return the enum's value
         */
        public static ChosenOption codeOfOption(int label) {
            for (ChosenOption value : values()) {
                if (value.chosenOptionCode == label) {
                    return value;
                }
            }
            return WRONG_INPUT;
        }
    }

    /**
     * This function calls the run-method, which starts the game
     * @param Args arguments from terminal
     */
    public static void main(String[] Args) {
        new PlayGame().run();

    }

}
