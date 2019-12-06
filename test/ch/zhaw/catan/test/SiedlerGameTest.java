package test;

import ch.zhaw.catan.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SiedlerGameTest {

    private SiedlerGame testGame;
    private SiedlerBoard testBoard;

    @BeforeEach
    void setUp() {
        testGame = new SiedlerGame(20, 4);
        testBoard = new SiedlerBoard();
    }

    @Test
    void getPlayer() {
        assertEquals("rr", testGame.getPlayers().get(0).getFaction().toString());
        assertEquals("bb", testGame.getPlayers().get(1).getFaction().toString());
        assertEquals("gg", testGame.getPlayers().get(2).getFaction().toString());
        assertEquals("yy", testGame.getPlayers().get(3).getFaction().toString());
    }

    @Test
    void getCurrentPlayer() {
        assertEquals("rr", testGame.getfirstPlayer().getFaction().toString());
    }

    @Test
    void getCurrentPlayerResourceStock() {
        Map<Config.Resource, Integer> testResources = new HashMap<>();
        Bank testBank = new Bank();
        testResources.put(Config.Resource.GRAIN, 1);
        testResources.put(Config.Resource.WOOD, 1);
        testResources.put(Config.Resource.WOOL, 1);
        testResources.put(Config.Resource.STONE, 1);
        testResources.put(Config.Resource.CLAY, 1);

        testGame.getfirstPlayer().addResources(Config.Resource.GRAIN, 1, testBank);
        testGame.getfirstPlayer().addResources(Config.Resource.WOOD, 1, testBank);
        testGame.getfirstPlayer().addResources(Config.Resource.WOOL, 1, testBank);
        testGame.getfirstPlayer().addResources(Config.Resource.STONE, 1, testBank);
        testGame.getfirstPlayer().addResources(Config.Resource.CLAY, 1, testBank);

        assertEquals(testResources, testGame.getfirstPlayer().getResourcesInPossession());
    }

    @Test
    void placeInitialSettlement() {
        Point testPointOne = new Point(5, 7);
        Player testPlayer = new Player(Config.Faction.RED);
        SiedlerGame testGame = new SiedlerGame(7, 2);
        testGame.placeInitialSettlement(testPointOne, testPlayer, testBoard);
        assertEquals(testPlayer.getSettlementsBuilt().get(0), testBoard.getCorner(testPointOne));

        Point testPointTwo = new Point(7, 13);
        testGame.placeInitialSettlement(testPointTwo, testPlayer, testBoard);
        assertEquals(testPlayer.getSettlementsBuilt().get(1), testBoard.getCorner(testPointTwo));
    }

    @Test
    void placeCity() {
        // TODO: Implement
    }

    @Test
    void tradeWithBankFourToOne() {
        Bank testBank = new Bank();

        testGame.getfirstPlayer().addResources(Config.Resource.GRAIN, 5, testBank);
        testGame.getfirstPlayer().addResources(Config.Resource.CLAY, 3, testBank);

        ResourceStock bankTestStock = new ResourceStock();
        bankTestStock.add(Config.Resource.WOOD, 1);
        bankTestStock.add(Config.Resource.GRAIN, 1);
        bankTestStock.add(Config.Resource.CLAY, 3);

        // Positive test
        testGame.tradeWithBankFourToOne(Config.Resource.GRAIN, Config.Resource.WOOD, testGame.getfirstPlayer(), testBank);
        Assertions.assertEquals(bankTestStock.getResources(), testGame.getfirstPlayer().getResourcesInPossession());

        // Negative test
        testGame.tradeWithBankFourToOne(Config.Resource.CLAY, Config.Resource.WOOD, testGame.getfirstPlayer(), testBank);
        Assertions.assertEquals(bankTestStock.getResources(), testGame.getfirstPlayer().getResourcesInPossession());
    }

    @Test
    void getWinner() {
        // TODO: Implement
    }

    @Test
    void createPlayers() {
        testGame = new SiedlerGame(7, 2);
        assertEquals(2, testGame.createPlayers(2));
        testGame = new SiedlerGame(7, 3);
        assertEquals(3, testGame.createPlayers(3));
        testGame = new SiedlerGame(7, 4);
        assertEquals(4, testGame.createPlayers(4));
    }

    @Test
    void isPointACorner() {
        // TODO: Implement
    }

    @Test
    void placeRoad() {
        /** TODO: Doesn't work, i get NullPointerExceptions
         * PlayGame testPlayGame = new PlayGame();
        Point positiveTestPointStart = new Point(6, 6);
        Point positiveTestPointEnd = new Point(5, 7);
        Road testRoad = new Road(testGame.getCurrentPlayer(), positiveTestPointStart, positiveTestPointEnd);
        Bank testBank = new Bank();
        testPlayGame.getSiedlerGame().getCurrentPlayer().addResources(Config.Resource.WOOD, 2, testBank);
        testPlayGame.getSiedlerGame().getCurrentPlayer().addResources(Config.Resource.CLAY, 2, testBank);
        testPlayGame.getSiedlerGame().placeRoad(positiveTestPointStart, positiveTestPointEnd, testPlayGame.getBoard(), testPlayGame.getSiedlerGame().getCurrentPlayer(), testBank);
        assertEquals(testPlayGame.getBoard().getEdge(positiveTestPointStart, positiveTestPointEnd), testRoad);
         **/
    }

    @Test
    void validRoadPlacement() {
        Point positiveTestPointStart = new Point(6, 6);
        Point positiveTestPointEnd = new Point(5, 7);
        Point negativeTestPointStart = new Point(7, 13);
        Point negativeTestPointEnd = new Point(8, 15);
        testGame.placeInitialSettlement(positiveTestPointStart, testGame.getfirstPlayer(), testBoard);

        // Positive test
        assertTrue(testGame.validRoadPlacement(positiveTestPointStart, positiveTestPointEnd, testBoard, testGame.getfirstPlayer()));

        // Negative test
        assertFalse(testGame.validRoadPlacement(negativeTestPointStart, negativeTestPointEnd, testBoard, testGame.getfirstPlayer()));
    }
}