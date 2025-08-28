package com.rwth.heuristicalgorithms.MoveChoosing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.rwth.heuristicalgorithms.PlayingField.Move;
import com.rwth.heuristicalgorithms.PlayingField.MoveType;
import com.rwth.heuristicalgorithms.PlayingField.PlayingField;
import com.rwth.heuristicalgorithms.PlayingField.Tile;
/**
 * tests regarding the heuristics
 */
public class HeuristicTest {

    /**
     * executed before each test
     */
    @Before
    public void setUp(){ 
        //clean up all the static values in pf
        PlayingField.reset();
    }
    /**
     * tests evaluation during Building Phase
     */
    @Test
    public void evalBoard_BuildingTest(){
        //only consideres default potential moves
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "0 c 0 0 0 0 1 2 3 0 i 0 0 0 0\n" +
        "0 0 0 0 0 0 3 1 2 0 0 0 0 0 0\n" +
        "0 0 0 b 0 0 2 3 1 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);

        //calculate weighted occupied tiles sum for p1:
        //3 default tiles
        int p1 = 3*MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
        
        assertEquals(p1, MoveHeuristic.calcSumOccupiedTiles(pf, (byte)1,false));




        //same for p2:
        //3 default tiles on the board
        assertEquals(3*MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE, MoveHeuristic.calcSumOccupiedTiles(pf, (byte)2,false));


    }
    /**
     * tests evaluation during buidling phase
     */
    @Test
    public void evalBoard_BuildingTest_2(){
        //also consideres edge & corner tiles
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 1 - - - - -\n" +
        "- - - - - 1 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "1 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "0 c 0 0 0 0 1 2 3 i 0 0 0 0 0\n" +
        "0 0 0 0 0 0 3 1 2 0 0 0 0 0 0\n" +
        "0 0 0 b 0 0 2 3 1 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 b 0 1\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 1 0 0 0 0 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);
        //calculate weighted occupied tiles sum for p1:
        //3 default tiles
        int p1 = 3*MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
        //3 corner tiles
        p1 += 3*MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
        //2 edge tiles
        p1 += 2*MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
        
        assertEquals(p1, MoveHeuristic.calcSumOccupiedTiles(pf, (byte)1,false));
    }

    /**
     * tests evaluation during building phase
     */
    @Test
    public void evalBoard_BuildingTest_3(){
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\n" +
        "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\n" +
        "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\n" +
        "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\n" +
        "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n";
        PlayingField pf = PlayingField.readMap(mapString);
        //p1 has 8 corner tiles
        int p1 = 8 * MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
        //p1 has 9*4 = 36 edge tiles 
        p1 += 36 * MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
        //p1 has 10*5 + 5*15 = 125 tiles in total -> 125-44 = 81 default tiles
        p1 += 81 * MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;

        assertEquals(p1, MoveHeuristic.calcSumOccupiedTiles(pf, (byte)1,false));
    }



    /**
     * tests evaluation during elimination phase
     */
    @Test
    public void evalBoard_EliminationTest(){
        String mapString = "8\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 4 4 4 4 4 - - - - -\n" +
        "- - - - - 0 5 5 5 5 - - - - -\n" +
        "- - - - - 6 6 0 0 1 - - - - -\n" +
        "- - - - - 1 6 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "1 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "0 c 2 0 7 0 1 2 3 i 0 0 0 0 0\n" +
        "0 2 2 0 7 c 3 1 2 0 0 0 0 0 0\n" +
        "0 2 2 b 8 0 2 3 1 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 0 b 0 0 0 0 b 0 1\n" +
        "- - - - - 3 3 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 1 0 0 0 0 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);
        PlayingField pf2 = pf.copy();
        /*
         * Number of stones:
         * P1: 8
         * P2: 8
         * P3: 5
         * P4: 5 <---- us (we will choose player3 and this sould yield place 4)
         * P5: 4
         * P6: 3
         * P7: 2
         * P8: 1
         */

        //the value should be:
        //our base value
        int value = 5*MoveHeuristic.WEIGHT_ELIM_OUR_STONES;
        //p3
        value += 5*MoveHeuristic.WEIGHT_ELIM_PREDECESSOR;
        //p2
        value += 8*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 1*MoveHeuristic.CONST_ELIM_PREDECESSOR);
        //p1
        value += 8*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 2*MoveHeuristic.CONST_ELIM_PREDECESSOR);
        //p5
        value += 4*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 1*MoveHeuristic.CONST_ELIM_SUCCESOR);
        //p6
        value += 3*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 2*MoveHeuristic.CONST_ELIM_SUCCESOR);
        //p7
        value += 2*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 3*MoveHeuristic.CONST_ELIM_SUCCESOR);
        //p8
        value += 1*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 4*MoveHeuristic.CONST_ELIM_SUCCESOR);

        assertEquals(value, MoveHeuristic.evalBoard_Elimination(pf, (byte)3));


        //if we now remove the stone of p8, the value should go up
        pf.setMapTile((byte)8, (byte)4, Tile.EMPTY);
        assertTrue(value < MoveHeuristic.evalBoard_Elimination(pf, (byte)3));


        //if we instead add a stone to p2, the value should go down
        pf.setMapTile((byte)8, (byte)4, Tile.OCCUPIED_8);
        pf.setMapTile((byte)0, (byte)0, Tile.OCCUPIED_2);

        assertTrue(value > MoveHeuristic.evalBoard_Elimination(pf, (byte)3));


        //if we instead add a tile for p4, the value should go up
        pf.setMapTile((byte)0, (byte)0, Tile.OCCUPIED_4);

        assertTrue(value < MoveHeuristic.evalBoard_Elimination(pf, (byte)4));



        //if p8 is disqualified, their value should be omitted from the heuristic
        pf2.disqualify((byte)8);
        //our base value
        value = 5*MoveHeuristic.WEIGHT_ELIM_OUR_STONES;
        //p3
        value += 5*MoveHeuristic.WEIGHT_ELIM_PREDECESSOR;
        //p2
        value += 8*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 1*MoveHeuristic.CONST_ELIM_PREDECESSOR);
        //p1
        value += 8*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 2*MoveHeuristic.CONST_ELIM_PREDECESSOR);
        //p5
        value += 4*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 1*MoveHeuristic.CONST_ELIM_SUCCESOR);
        //p6
        value += 3*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 2*MoveHeuristic.CONST_ELIM_SUCCESOR);
        //p7
        value += 2*(MoveHeuristic.WEIGHT_ELIM_PREDECESSOR + 3*MoveHeuristic.CONST_ELIM_SUCCESOR);

        assertEquals(value, MoveHeuristic.evalBoard_Elimination(pf2, (byte)3));
    }



    






    /**
     * test selectFirstMove during building phase
     */
    @Test
    public void selectFirstMoveTest_Building(){
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "0 c 0 0 0 0 1 2 3 0 i 0 0 0 0\n" +
        "0 0 0 0 0 0 3 1 2 0 0 0 0 0 0\n" +
        "0 0 0 b 0 0 2 3 1 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);

    

        //(5,7)
        Move m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.DEFAULT, m.getMoveType());
        assertEquals(5, m.getY());
        assertEquals(7, m.getX());
        pf.executeMove(m, (byte)1);
        //5,8
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.DEFAULT, m.getMoveType());
        assertEquals(5, m.getY());
        assertEquals(8, m.getX());
        pf.executeMove(m, (byte)1);
        //set (7,8) back to 2, then we should see an override move
        pf.setMapTile((byte)7, (byte)8, Tile.OCCUPIED_2); 
        //6,8
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.OVERRIDE, m.getMoveType());
        assertEquals(6, m.getY());
        assertEquals(8, m.getX());
        pf.executeMove(m, (byte)1);
        //7,5
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.DEFAULT, m.getMoveType());
        assertEquals(7, m.getY());
        assertEquals(5, m.getX());
        pf.executeMove(m, (byte)1);
        //8,5
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.DEFAULT, m.getMoveType());
        assertEquals(8, m.getY());
        assertEquals(5, m.getX());
        pf.executeMove(m, (byte)1);
        //5 expansion stones
        //10,7
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.OVERRIDE, m.getMoveType());
        assertEquals(10, m.getY());
        assertEquals(7, m.getX());
        pf.executeMove(m, (byte)1);
        //11,6
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.OVERRIDE, m.getMoveType());
        assertEquals(11, m.getY());
        assertEquals(6, m.getX());
        pf.executeMove(m, (byte)1);
        //11,7
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.OVERRIDE, m.getMoveType());
        assertEquals(11, m.getY());
        assertEquals(7, m.getX());
        pf.executeMove(m, (byte)1);
        //11,8
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.OVERRIDE, m.getMoveType());
        assertEquals(11, m.getY());
        assertEquals(8, m.getX());
        pf.executeMove(m, (byte)1);
        //12,7
        m = Minimax.selectFirstMove(pf, (byte)1);
        assertEquals(MoveType.OVERRIDE, m.getMoveType());
        assertEquals(12, m.getY());
        assertEquals(7, m.getX());
        pf.executeMove(m, (byte)1);
        //no more moves possible
        assertNull(Minimax.selectFirstMove(pf, (byte)1));
    }


    /**
     * test selectFirstMove during elimination phase
     */
    @Test
    public void selectFirstMoveTest_Elimination(){
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "0 c 0 0 0 0 1 2 3 0 i 0 0 0 0\n" +
        "0 0 0 0 0 0 3 1 2 0 0 0 0 0 0\n" +
        "0 0 0 b 0 0 2 3 1 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);
        pf.startEliminationPhase();

        //p1 should be able to use 4 bombs
        for(int i = 0; i < 4; i++){
            Move m = Minimax.selectFirstMove(pf, (byte)1);
            assertEquals(MoveType.BOMB, m.getMoveType());
            assertNotNull(m);
            pf.executeMove(m, (byte)1);
        }
        //no bombs left
        assertNull(Minimax.selectFirstMove(pf, (byte)1));
    }

















    /**
     * tests getAllPossibleMoves during emilination phase
     */
    @Test
    public void getAllPossibleMoves_ElimTest() throws TimeoutException{
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 1 2 3 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 3 1 2 0 0 0 0 0 0\n" +
        "0 0 0 b 0 0 2 3 1 0 0 0 0 0 0\n" +
        "0 0 0 0 0 0 0 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);
        pf.startEliminationPhase();

        //p3 should be able to bomb every non-hole on the map
        List<Move> moves = Minimax.getAllPossibleMoves(pf, (byte)3, false);
        //log every bomb move

        //bombedHoles[x][y] = true means that getAllPossibleMoves returned this move
        boolean[][] bombedHoles = new boolean[15][15];

        for(Move m : moves){
            bombedHoles[m.getY()][m.getX()] = true;
        }


        
        //assure that (only) every valid tile was bombed
        for(int y = 0; y < 15; y++){
            for(int x = 0; x < 15; x++){
                if((y < 5 && (x < 5 || x > 9))
                || (y > 9 && (x < 5 || x > 9))
                ) {
                    assertFalse("assert true: y=" + y + ", x= " + x, bombedHoles[y][x]);
                } else {
                    assertTrue(bombedHoles[y][x]);
                }
                
            }
        }


        //p1 should not be able to bomb anything if p1 has no bombs
        pf.changePlayerBombCount((byte)1, (byte)-4);
        assertNull(Minimax.getAllPossibleMoves(pf, (byte)1, false));

    }



    /**
     * tests getAllPossibleMoves during emilination phase
     */
    @Test
    public void getAllPossibleMoves_ElimTest2() throws TimeoutException{
        String mapString = "8\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- 8 c 0 i 6 0 x 7 8 - - 2 2 -\n" +
        "- - - - - i 0 6 7 - - - - 6 0\n" +
        "- 2 - - - 6 0 i 5 - - - - - -\n" +
        "- - 8 - - 7 8 0 6 - - - - - 5\n" +
        "- - - - - 2 3 5 x - - - - - -\n" +
        "0 4 0 0 4 4 4 4 4 0 0 0 0 0 0\n" +
        "0 0 0 6 0 0 1 7 6 0 0 0 0 0 0\n" +
        "0 0 0 0 7 0 6 1 5 0 0 8 0 0 0\n" +
        "0 2 0 b 0 0 5 7 2 0 0 0 0 0 0\n" +
        "0 3 0 1 1 1 8 b i c 0 0 b 0 0\n" +
        "- - - - - 3 x 8 0 - - - - - -\n" +
        "- - - - - x 6 4 x - - - - - 4\n" +
        "- - - - - 0 0 x c - - - - - -\n" +
        "- - - - - 0 0 0 0 - - - - - -\n" +
        "- - - - - 0 0 0 0 8 - - - - 2\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);
        pf.startEliminationPhase();
        

        //get possible moves for every player. They should all be equal.
        List<Move> moves = Minimax.getAllPossibleMoves(pf, (byte)1, false);

        for(byte player = 2; player <= 8; player++){
            List<Move> moves2 = Minimax.getAllPossibleMoves(pf, (byte)player, false);
            //assert both-sided inclusion
            for(Move m : moves){
                assertTrue(moves2.contains(m));
            }
            for(Move m : moves2){
                assertTrue(moves.contains(m));
            }
        }

        //exactly every non-hole tile should be present in the list 
        //log every bomb move

        //bombedHoles[x][y] = true means that getAllPossibleMoves returned this move
        boolean[][] bombedHoles = new boolean[15][15];

        for(Move m : moves){
            bombedHoles[m.getY()][m.getX()] = true;
        }

        //assure that exactly every valid tile was bombed
        for(byte y = 0; y < 15; y++){
            for(byte x = 0; x < 15; x++){
                if(!pf.getMapTile(y, x).equals(Tile.HOLE)){
                    assertTrue(bombedHoles[y][x]);
                } else {
                    assertFalse(bombedHoles[y][x]);
                }
                
            }
        }
    }

    /**
     * tests getAllPossibleMoves during building phase
     */
    @Test
    public void getAllPossibleMoves_Building() throws TimeoutException{
        String mapString = "8\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- 8 c 0 i 6 0 x 7 8 - - 2 2 -\n" +
        "- - - - - i 0 6 7 - - - - 6 0\n" +
        "- 2 - - - 6 0 i 5 - - - - - -\n" +
        "- - 8 - - 7 8 0 6 - - - - - 5\n" +
        "- - - - - 2 3 5 x - - - - - -\n" +
        "0 4 0 0 4 4 4 4 4 0 0 0 0 0 0\n" +
        "0 0 0 6 0 0 1 7 6 0 0 0 0 0 0\n" +
        "0 0 0 0 7 0 6 1 5 0 0 8 0 0 0\n" +
        "0 2 0 b 0 0 5 7 2 0 0 0 0 0 0\n" +
        "0 3 0 1 1 1 8 b i c 0 0 b 0 0\n" +
        "- - - - - 3 x 8 0 - - - - - -\n" +
        "- - - - - x 6 4 x - - - - - 4\n" +
        "- - - - - 0 0 x 0 - - - - - -\n" +
        "- - - - - 0 0 0 0 - - - - - -\n" +
        "- - - - - 0 0 0 0 8 - - - - 2\n";
        PlayingField pf = PlayingField.readMap(mapString);
        //these should excatly be the moves p1 can make:
        List<Move> moves = new ArrayList<>();
        //first, all the expansion override moves:
        moves.add(Move.createOverrideMove((byte)0, (byte)7));
        moves.add(Move.createOverrideMove((byte)4, (byte)8));
        moves.add(Move.createOverrideMove((byte)10, (byte)6));
        moves.add(Move.createOverrideMove((byte)11, (byte)5));
        moves.add(Move.createOverrideMove((byte)11, (byte)8));
        moves.add(Move.createOverrideMove((byte)12, (byte)7));
        //all other possible override moves:
        //everything around the (6,6) 1
        moves.add(Move.createOverrideMove((byte)4, (byte)6));
        moves.add(Move.createOverrideMove((byte)3, (byte)6));
        moves.add(Move.createOverrideMove((byte)6, (byte)8));
        moves.add(Move.createOverrideMove((byte)8, (byte)6));
        moves.add(Move.createOverrideMove((byte)9, (byte)6));
        moves.add(Move.createOverrideMove((byte)11, (byte)6)); 
        //everything around the (7,7) 1
        moves.add(Move.createOverrideMove((byte)5, (byte)7));
        moves.add(Move.createOverrideMove((byte)4, (byte)7));
        moves.add(Move.createOverrideMove((byte)9, (byte)5));
        //around the (9,5) 1
        moves.add(Move.createOverrideMove((byte)11, (byte)7));
        moves.add(Move.createOverrideMove((byte)7, (byte)7));

        //all non-override moves:
        //around the (6,6) 1
        moves.add(Move.createDefaultMove((byte)2, (byte)6));
        moves.add(Move.createDefaultMove((byte)6, (byte)9));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)1));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)2));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)3));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)4));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)5));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)6));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)7));
        moves.add(Move.createChoiceMove((byte)9, (byte)9, (byte)8));
        moves.add(Move.createDefaultMove((byte)12, (byte)6));
        //around the (7,7)
        moves.add(Move.createDefaultMove((byte)3, (byte)7));
        moves.add(Move.createDefaultMove((byte)5, (byte)9));
        moves.add(Move.createDefaultMove((byte)7, (byte)9));
        moves.add(Move.createBonusMove((byte)9, (byte)7, true));
        moves.add(Move.createBonusMove((byte)9, (byte)7, false));
        moves.add(Move.createDefaultMove((byte)7, (byte)5));
        //around the (9,5)
        moves.add(Move.createDefaultMove((byte)12, (byte)5));
        moves.add(Move.createDefaultMove((byte)12, (byte)8));
        moves.add(Move.createDefaultMove((byte)13, (byte)8));

        //getAllMoves should yield the exact same results
        List<Move> moves2 = Minimax.getAllPossibleMoves(pf, (byte)1, false);
        for(Move m : moves){
            assertTrue("Y: " + m.getY() + ", X: " + m.getX() + ", Type: " + m.getMoveType(), moves2.contains(m));
        }
        for(Move m : moves2){
            assertTrue("Y: " + m.getY() + ", X: " + m.getX() + ", Type: " + m.getMoveType(), moves.contains(m));
        }
        
    }

    /**
     * tests getNextPlayer function
     */
    @Test
    public void getNextPlayerTest(){
        String mapString = "8\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- 8 c 0 i 6 0 x 7 8 - - 2 2 -\n" +
        "- - - - - i 0 6 7 - - - - 6 0\n" +
        "- 2 - - - 6 0 i 5 - - - - - -\n" +
        "- - 8 - - 7 8 0 6 - - - - - 5\n" +
        "- - - - - 2 3 5 x - - - - - -\n" +
        "0 4 0 0 4 4 4 4 4 0 0 0 0 0 0\n" +
        "0 0 0 6 0 0 1 7 6 0 0 0 0 0 0\n" +
        "0 0 0 0 7 0 6 1 5 0 0 8 0 0 0\n" +
        "0 2 0 b 0 0 5 7 2 0 0 0 0 0 0\n" +
        "0 3 0 1 1 1 8 b i c 0 0 b 0 0\n" +
        "- - - - - 3 x 8 0 - - - - - -\n" +
        "- - - - - x 6 4 x - - - - - 4\n" +
        "- - - - - 0 0 x c - - - - - -\n" +
        "- - - - - 0 0 0 0 - - - - - -\n" +
        "- - - - - 0 0 0 0 8 - - - - 2\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        PlayingField pf = PlayingField.readMap(mapString);
        //no player disqualified
        for(byte i = 1; i <= 8; i++){
            byte next = (byte)(i+1);
            next = next == 9 ? 1 : next;
            assertEquals(next, Minimax.getNextPlayer(pf, i));
        }
        //disqualify players
        pf.disqualify((byte)3);
        assertEquals(4, Minimax.getNextPlayer(pf, (byte)2));
        pf.disqualify((byte)4);
        assertEquals(5, Minimax.getNextPlayer(pf, (byte)2));
        pf.disqualify((byte)5);
        pf.disqualify((byte)6);
        pf.disqualify((byte)7);
        pf.disqualify((byte)8);
        assertEquals(1, Minimax.getNextPlayer(pf, (byte)2));
        pf.disqualify((byte)2);
        assertEquals(1, Minimax.getNextPlayer(pf, (byte)1));
    }




}

