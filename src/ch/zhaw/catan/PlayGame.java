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
    private SiedlerBoard hexBoard = new SiedlerBoard();
    private int numberOfPlayers;
    private static TextIO textIO = TextIoFactory.getTextIO();
    private static TextTerminal<SwingTextTerminal> textTerminal = (SwingTextTerminal) textIO.getTextTerminal();


    public PlayGame() {
    }

    private void run() {

        UI.setupTerminal();
        UI.setBookmark("BLANK_SCREEN");

        if (!UI.buildStartMenu()) {
            UI.setBookmark("SHOW_MAP");

            numberOfPlayers = UI.askNumberOfPlayers();
            //TODO: Delete for Release
            if (numberOfPlayers == 69) {
                siedlerGame = new SiedlerGame(7, 2);
                siedlerGame.placeInitialSettlement(new Point(5, 15), siedlerGame.getPlayers().get(0), hexBoard);
                siedlerGame.placeInitialSettlement(new Point(8, 12), siedlerGame.getPlayers().get(0), hexBoard);
                siedlerGame.placeInitialSettlement(new Point(6, 6), siedlerGame.getPlayers().get(1), hexBoard);
                siedlerGame.placeInitialSettlement(new Point(9, 7), siedlerGame.getPlayers().get(1), hexBoard);
                siedlerGame.placeInitialRoad(new Point(5, 15), new Point(6, 16), hexBoard, siedlerGame.getPlayers().get(0));
                siedlerGame.placeInitialRoad(new Point(7, 13), new Point(8, 12), hexBoard, siedlerGame.getPlayers().get(0));
                siedlerGame.placeInitialRoad(new Point(6, 6), new Point(5, 7), hexBoard, siedlerGame.getPlayers().get(1));
                siedlerGame.placeInitialRoad(new Point(10, 6), new Point(9, 7), hexBoard, siedlerGame.getPlayers().get(1));
                UI.resetBookmark("BLANK_SCREEN");
                UI.printBoard(hexBoard);
                giveResourcesAfterFirstPhase(hexBoard);
                secondPhase();
            } else {
                UI.print("Ok, there will be " + numberOfPlayers + " players");
                UI.promptEnter();

                //Creating a new game
                siedlerGame = new SiedlerGame(7, numberOfPlayers);
                firstPhase();
                secondPhase();
            }
        }

    }


    public void firstPhase() {

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
        giveResourcesAfterFirstPhase(hexBoard);
    }

    private void setInitialSettlementsAndRoads(String currentPlayerFaction, Player currentPlayer, String turn) {
        //ask for first settlement
        int x = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a x coordinate for your " + turn + " settlement\n");
        UI.resetBookmark("BLANK_SCREEN");
        UI.printBoard(hexBoard);
        int y = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a y coordinate for your " + turn + " settlement\n");
        //set first settlement
        Point point = new Point(x, y);
        Point newPoint = siedlerGame.isPointACorner(point);
        Settlement settlement = siedlerGame.placeInitialSettlement(newPoint, currentPlayer, hexBoard);
        UI.resetBookmark("BLANK_SCREEN");
        UI.printBoard(hexBoard);

        //ask for first road start
        int xRoadStart = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a x coordinate for the start of your " + turn + " road\n");
        UI.resetBookmark("BLANK_SCREEN");
        UI.printBoard(hexBoard);
        int yRoadStart = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a y coordinate for the start of your " + turn + " road\n");
        UI.resetBookmark("BLANK_SCREEN");
        UI.printBoard(hexBoard);
        Point roadStart = new Point(xRoadStart, yRoadStart);
        //ask for first road finish
        int xRoadFinish = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a x coordinate for the finish of your " + turn + " road\n");
        UI.resetBookmark("BLANK_SCREEN");
        UI.printBoard(hexBoard);
        int yRoadFinish = textIO.newIntInputReader().read(currentPlayerFaction + " please pick a y coordinate for the finish of your " + turn + " road\n");

        //set first road
        Point roadEnd = new Point(xRoadFinish, yRoadFinish);
        Road road = siedlerGame.placeInitialRoad(roadStart, roadEnd, hexBoard, currentPlayer);
        UI.newLine();
        UI.resetBookmark("BLANK_SCREEN");
        UI.printBoard(hexBoard);
    }


    // TODO build phase method calls
    //        To build roads:                   siedlerGame.placeRoad(Point_RoadStart, Point_RoadEnd); -- returns boolean
    //        To build settlements:             siedlerGame.placeSettlement(Point_Position); -- returns boolean
    //        To upgrade settlements to cities: siedlerGame.placeCity(Point_Position); -- returns boolean

    // TODO gameplay calls
    //        To check if someone won:          siedlerGame.getWinner(); -- returns boolean
    //        To trade with bank:               siedlerGame.tradeWithBankFourToOne(Resource_offer, Resource_want); -- returns boolean
    //        To roll dice                      siedlerGame.throwDice(dice.roll()); -- returns Map<Faction, List<Resource>

    public boolean isCornerFree(Point corner) {
        return hexBoard.getNeighboursOfCorner(corner).isEmpty();
    }

    public void giveResourcesAfterFirstPhase(SiedlerBoard board) {
        for (Point field : board.getFields()) {
            if (board.getField(field) != Config.Land.DESERT && board.getField(field) != Config.Land.WATER) {
                if (!board.getCornersOfField(field).isEmpty()) {
                    for (Settlement settlement : board.getCornersOfField(field)) {
                        settlement.getPlayer().addResources(board.getField(field).getResource(), 1);
                    }
                }
            }
        }

        printAllGivenRessourcesOfAllPlayers();
        //Ask players to press enter in order to start the second game phase
        UI.promptEnter();
    }

    public void printAllGivenRessourcesOfAllPlayers() {
        for (Player player : siedlerGame.getPlayers()){
            StringBuilder tempOutput = new StringBuilder(player.toString() + " has been given: ");
            for (Config.Resource resource : player.getResourcesInPossession().keySet()){

                tempOutput.append(player.getResourcesInPossession().get(resource)).append(" ").append(resource.toString()).append(", ");
            }
            String finalOutput = tempOutput.substring(0,tempOutput.length() - 2);
            finalOutput += "\n";
            UI.print(finalOutput);
        }
    }

    private void giveResourcesFromDice(int rolledNumber){
        for (Point field : hexBoard.getFields()) {
            if (hexBoard.getField(field) != Config.Land.DESERT && hexBoard.getField(field) != Config.Land.WATER && Config.getStandardDiceNumberPlacement().get(field) == rolledNumber) {
                if (!hexBoard.getCornersOfField(field).isEmpty()) {
                    for (Settlement settlement : hexBoard.getCornersOfField(field)) {
                        settlement.getPlayer().addResources(hexBoard.getField(field).getResource(), 1);
                        textTerminal.print(settlement.getFaction() + " has recieved 1 " + hexBoard.getField(field).getResource() + '\n');
                    }
                }
            }
        }
    }

    private void divideAllRessources(Player currentPlayer) {
        int totalResources = 0;
        for (Integer amountOfRessource : currentPlayer.getResourcesInPossession().values()) {
            totalResources += amountOfRessource;
        }

        //remove random ressources from players with more then seven cards
        if (totalResources > 7) {
            int resourcesToDelete = (totalResources - totalResources % 2) / 2;
            for (int j = 0; j < resourcesToDelete; ++j) {
                //new Arraylist with all resources the player has, to choose a random resource to remove
                ArrayList<Config.Resource> resources = new ArrayList<>(currentPlayer.getResourcesInPossession().keySet());
                //create random number to choose which resource to delete
                int random = new Random().nextInt(resources.size() - 1);
                currentPlayer.removeResources(resources.get(random), 1);
            }
        }
    }

    public void secondPhase() {
        UI.resetBookmark("BLANK_SCREEN");
        UI.printBoard(hexBoard);

        boolean gameIsRunning = true;
        for (int i = 0; gameIsRunning; i = (i + 1) % numberOfPlayers) {
            Player currentPlayer = siedlerGame.getPlayers().get(i);
            int rolledNumber = Dice.roll();
            textTerminal.print(currentPlayer + " rolled a " + rolledNumber + "\n");

            if (rolledNumber == 7) {
                divideAllRessources(currentPlayer);
            } else {
                giveResourcesFromDice(rolledNumber);
            }

            boolean playersTurn = true;
            do {
                UI.resetBookmark("BLANK_SCREEN");
                UI.printBoard(hexBoard);
                UI.print("It's " + currentPlayer + "'s turn\n");

                textTerminal.print("1: Trade with bank\n");
                textTerminal.print("2: Build Settlement\n");
                textTerminal.print("3: Build Road\n");
                textTerminal.print("4: Build City\n");
                textTerminal.print("5: Check my resources\n");
                textTerminal.print("6: End my turn\n");
                textTerminal.print("7: Quit game\n");
                int decision = textIO.newIntInputReader().read("What would you like to do now?\n");

                switch (decision) {
                    case 1:
                        siedlerGame.tradeWithBank(i);
                        break;
                    case 2:
                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int x = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for your settlement\n");

                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int y = textIO.newIntInputReader().read(currentPlayer + " please pick a y coordinate for your settlement\n");

                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        Point position = new Point(x, y);
                        siedlerGame.placeSettlement(position, currentPlayer, hexBoard);
                        break;
                    case 3:
                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int a = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for the start of your road\n");

                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int b = textIO.newIntInputReader().read(currentPlayer + " please pick a y coordinate for the start of your road\n");
                        Point roadStart = new Point(a, b);

                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int c = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for the finish of your road\n");

                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int d = textIO.newIntInputReader().read(currentPlayer + " please pick a y coordinate for the finish of your road\n");

                        Point roadEnd = new Point(c, d);
                        siedlerGame.placeRoad(roadStart, roadEnd, hexBoard, currentPlayer);
                        break;
                    case 4:
                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int e = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for your city\n");

                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        int f = textIO.newIntInputReader().read(currentPlayer + " please pick a x coordinate for your city\n");

                        Point where = new Point(e, f);
                        siedlerGame.placeCity(where, currentPlayer);
                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);
                        break;
                    case 5:
                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);

                        for (HashMap.Entry<Config.Resource, Integer> entry : currentPlayer.getResourcesInPossession().entrySet()) {
                            textTerminal.print(currentPlayer + " has " + entry.getKey() + ": " + entry.getValue() + "\n");
                        }

                        UI.promptEnter();
                        UI.resetBookmark("BLANK_SCREEN");
                        UI.printBoard(hexBoard);


                        break;
                    case 6:
                        String sure = textIO.newStringInputReader().read(currentPlayer + " are you sure you want to end your turn? (Y/N)\n");
                        if (sure.equalsIgnoreCase("Y")) {
                            playersTurn = false;
                        }

                        break;
                    case 7:
                        String ciao = textIO.newStringInputReader().read("Sure?(Y/N)\n");
                        if (ciao.equalsIgnoreCase("Y")) {
                            playersTurn = false;
                            gameIsRunning = false;
                        }
                        break;
                    case 69:
                        for (Config.Resource resource : Config.Resource.values()) {
                            currentPlayer.addResources(resource, 1000);
                        }
                        break;
                    default:
                        textTerminal.print("The number you have selected doesn't exist, please try again\n");
                }
            } while (playersTurn);
            if (siedlerGame.getWinner(currentPlayer)) {
                textTerminal.print(currentPlayer + "has won the game\n");
                break;
            }
        }
    }


    public static void main(String[] Args) {
        new PlayGame().run();

    }

}
