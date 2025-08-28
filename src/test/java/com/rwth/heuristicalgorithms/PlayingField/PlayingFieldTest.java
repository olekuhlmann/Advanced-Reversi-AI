package com.rwth.heuristicalgorithms.PlayingField;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static java.util.Map.entry;

import org.junit.Before;
import org.junit.Test;
/**
 * testclass for everything to do with PlayingFields
 */
public class PlayingFieldTest {

    /**
     * executed before each test
     */
    @Before
    public void setUp(){ 
        //clean up all the static values in pf
        PlayingField.reset();
    }
    
    /**
     * tests the readMap function
     */
    @Test
    public void testReadMap() {
        //create a Playing field from the example map in the courseRules.pdf:
        //uses both \n and \r\n
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
        int playerCount = 3;
        PlayingField pf = PlayingField.readMap(mapString);

        assertEquals(playerCount, PlayingField.getPlayerCount());
        for(int i = 1; i <= playerCount; i++){
            assertEquals(4, pf.getPlayerBombCount((byte) i));
        }
        assertEquals(2, PlayingField.getBombRadius());
        for(int i = 1; i <= playerCount; i++){
            assertEquals(6, pf.getPlayerOverrideStoneCount((byte) i));
        }
        assertEquals(15, PlayingField.getMapWidth());
        assertEquals(15, PlayingField.getMapHeight());

        //assert correct map array
        //these are (y,x) coords
        Map<Tile, byte[][]> tile_occurances = Map.ofEntries(
            entry(Tile.EMPTY, new byte[][]{{0, 5}, {0, 6}, {0, 9}, {1, 6}, {4, 8}, {5, 0}, {5, 14}, {6, 0}, {6, 9}, {14, 5}, {14, 9}}),
            entry(Tile.OCCUPIED_1, new byte[][]{{6,6}, {7,7}, {8,8}}),
            entry(Tile.OCCUPIED_2, new byte[][]{{6,7}, {7,8}, {8,6}}),
            entry(Tile.OCCUPIED_3, new byte[][]{{6,8}, {7,6}, {8,7}}),
            entry(Tile.CHOICE, new byte[][]{{6,1}, {12,8}}),
            entry(Tile.INVERSION, new byte[][]{{3,8}, {6,10}}),
            entry(Tile.BONUS, new byte[][]{{9,12}, {8,3}}),
            entry(Tile.EXPANSION, new byte[][]{{10,7}, {11,6}, {11,7}, {11,8}, {12,7}}),
            entry(Tile.HOLE, new byte[][]{{0, 0}, {0, 4}, {2, 10}, {3, 14}, {13, 3}, {14, 14}})
        );

        for(Tile t : tile_occurances.keySet()){
            byte[][] occurances = tile_occurances.get(t);
            for(byte[] cords : occurances){
                assertEquals(t, pf.getMapTile(cords[0], cords[1]));
            } 
        }

        //assert transitions
        //special transitions:
        //format: from (x,y,r) to (x,y,r) (direction change should be returned)
        Map<byte[], byte[]> special_transitions = Map.ofEntries(
            entry(new byte[]{6,0,0}, new byte[]{9,1,5}),
            entry(new byte[]{9,1,1}, new byte[]{6,0,4}),
            entry(new byte[]{7,14,4}, new byte[]{7,0,4}),
            entry(new byte[]{7,0,0}, new byte[]{7,14,0})
        );

        for(byte[] from : special_transitions.keySet()){
            byte[] to = special_transitions.get(from);
            //convert from (x,y) to (y,x)
            assertArrayEquals(new byte[]{to[1], to[0], to[2]}, PlayingField.getTransition(from[1], from[0], from[2]));
        }

        //asserting that transitions to hole lead to [-1,-1] coords
        //format: from (x,y,r) to hole
        Set<byte[]> no_transitions = new HashSet<>();
        no_transitions.add(new byte[]{5,0,6});
        no_transitions.add(new byte[]{5,0,7});
        no_transitions.add(new byte[]{9,0,7});
        no_transitions.add(new byte[]{9,0,0});
        no_transitions.add(new byte[]{9,0,1});
        no_transitions.add(new byte[]{2,2,1});
        no_transitions.add(new byte[]{2,2,2});
        no_transitions.add(new byte[]{2,2,3});
        no_transitions.add(new byte[]{2,2,5});
        no_transitions.add(new byte[]{2,2,6});


        for(byte[] from : no_transitions){
            //convert from (x,y) to (y,x)
            assertArrayEquals(new byte[]{-1, -1, -1}, PlayingField.getTransition(from[1], from[0], from[2]));
        }


        //assert default transitions
        //format: from (x,y,r) to (x,y, r)
        Map<byte[], byte[]> default_transitions = Map.ofEntries(
            entry(new byte[]{3,5,2}, new byte[]{4,5,2}),
            entry(new byte[]{3,5,5}, new byte[]{2,6,5}),
            entry(new byte[]{7,7,0}, new byte[]{7,6,0}),
            entry(new byte[]{7,7,1}, new byte[]{8,6,1}),
            entry(new byte[]{7,7,2}, new byte[]{8,7,2}),
            entry(new byte[]{7,7,3}, new byte[]{8,8,3}),
            entry(new byte[]{7,7,4}, new byte[]{7,8,4}),
            entry(new byte[]{7,7,5}, new byte[]{6,8,5}),
            entry(new byte[]{7,7,6}, new byte[]{6,7,6}),
            entry(new byte[]{7,7,7}, new byte[]{6,6,7})
        );

        for(byte[] from : default_transitions.keySet()){
            byte[] to = default_transitions.get(from);
            //convert from (x,y) to (y,x)
            //System.out.println("[TEST] default_transitions test: format (y,x,r) to (y,x); from (" + from[1] + "," + from[0] + "," + from[2] + ") to (" + to[1] + "," + to[0] + ")");
            assertArrayEquals(new byte[]{to[1], to[0], to[2]}, PlayingField.getTransition(from[1], from[0], from[2]));
        }

        //test the setter
        Tile[] tiles = Tile.values();
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            //generate random cords
            byte y = (byte)rand.nextInt(15); //generates num from 0-14
            byte x = (byte)rand.nextInt(15);
            Tile t = tiles[rand.nextInt(tiles.length)];
            pf.setMapTile(y, x, t);
            assertEquals(t, pf.getMapTile(y, x));
        }


        
    }
    /**
     * tests the read map function
     */
    @Test
    public void testReadMap2() { //same as testReadMap, but with a non-quadratic map and a few minor changes
        //create a Playing field from the example map in the courseRules.pdf:
        //uses both \n and \r\n
        String mapString = "4\n" +
        "1\n"+
        "4 2\r\n"+
        "10 15\r\n" +
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
        "6 0 0 <-> 9 1 1";
        int playerCount = 4;
        PlayingField pf = PlayingField.readMap(mapString);

        assertEquals(playerCount, PlayingField.getPlayerCount());
        for(int i = 1; i <= playerCount; i++){
            assertEquals(4, pf.getPlayerBombCount((byte) i));
        }
        for(int i = 1; i <= playerCount; i++){
            assertEquals(1, pf.getPlayerOverrideStoneCount((byte) i));
        }
        assertEquals(15, PlayingField.getMapWidth());
        assertEquals(10, PlayingField.getMapHeight());

        //assert correct map array
        //these are (y,x) coords
        Map<Tile, byte[][]> tile_occurances = Map.ofEntries(
            entry(Tile.EMPTY, new byte[][]{{0, 5}, {0, 6}, {0, 9}, {1, 6}, {4, 8}, {5, 0}, {5, 14}, {6, 0}, {6, 9}, {9, 0}, {9, 14}}),
            entry(Tile.OCCUPIED_1, new byte[][]{{6,6}, {7,7}, {8,8}}),
            entry(Tile.OCCUPIED_2, new byte[][]{{6,7}, {7,8}, {8,6}}),
            entry(Tile.OCCUPIED_3, new byte[][]{{6,8}, {7,6}, {8,7}}),
            entry(Tile.CHOICE, new byte[][]{{6,1}}),
            entry(Tile.INVERSION, new byte[][]{{3,8}, {6,10}}),
            entry(Tile.BONUS, new byte[][]{{9,12}, {8,3}}),
            //entry(Tile.EXPANSION, new byte[][]{}),
            entry(Tile.HOLE, new byte[][]{{0, 0}, {0, 4}, {2, 10}, {3, 14}})
        );

        for(Tile t : tile_occurances.keySet()){
            byte[][] occurances = tile_occurances.get(t);
            for(byte[] cords : occurances){
                assertEquals(t, pf.getMapTile(cords[0], cords[1]));
            } 
        }

        //assert transitions
        //special transitions:
        //format: from (x,y,r) to (x,y)
        Map<byte[], byte[]> special_transitions = Map.ofEntries(
            entry(new byte[]{6,0,0}, new byte[]{9,1,5}),
            entry(new byte[]{9,1,1}, new byte[]{6,0,4})
        );

        for(byte[] from : special_transitions.keySet()){
            byte[] to = special_transitions.get(from);
            //convert from (x,y) to (y,x)
            assertArrayEquals(new byte[]{to[1], to[0], to[2]}, PlayingField.getTransition(from[1], from[0], from[2]));
        }

        //asserting that transitions to hole lead to [-1,-1] coords
        //format: from (x,y,r) to hole
        Set<byte[]> no_transitions = new HashSet<>();
        no_transitions.add(new byte[]{5,0,6});
        no_transitions.add(new byte[]{5,0,7});
        no_transitions.add(new byte[]{9,0,7});
        no_transitions.add(new byte[]{9,0,0});
        no_transitions.add(new byte[]{9,0,1});
        no_transitions.add(new byte[]{2,2,1});
        no_transitions.add(new byte[]{2,2,2});
        no_transitions.add(new byte[]{2,2,3});
        no_transitions.add(new byte[]{2,2,5});
        no_transitions.add(new byte[]{2,2,6});
        no_transitions.add(new byte[]{0,9,4});


        for(byte[] from : no_transitions){
            //convert from (x,y) to (y,x)
            assertArrayEquals(new byte[]{-1, -1, -1}, PlayingField.getTransition(from[1], from[0], from[2]));
        }


        //assert default transitions
        //format: from (x,y,r) to (x,y)
        Map<byte[], byte[]> default_transitions = Map.ofEntries(
            entry(new byte[]{3,5,2}, new byte[]{4,5,2}),
            entry(new byte[]{3,5,5}, new byte[]{2,6,5}),
            entry(new byte[]{7,7,0}, new byte[]{7,6,0}),
            entry(new byte[]{7,7,1}, new byte[]{8,6,1}),
            entry(new byte[]{7,7,2}, new byte[]{8,7,2}),
            entry(new byte[]{7,7,3}, new byte[]{8,8,3}),
            entry(new byte[]{7,7,4}, new byte[]{7,8,4}),
            entry(new byte[]{7,7,5}, new byte[]{6,8,5}),
            entry(new byte[]{7,7,6}, new byte[]{6,7,6}),
            entry(new byte[]{7,7,7}, new byte[]{6,6,7})
        );

        for(byte[] from : default_transitions.keySet()){
            byte[] to = default_transitions.get(from);
            //convert from (x,y) to (y,x)
            //System.out.println("[TEST] default_transitions test: format (y,x,r) to (y,x); from (" + from[1] + "," + from[0] + "," + from[2] + ") to (" + to[1] + "," + to[0] + ")");
            assertArrayEquals(new byte[]{to[1], to[0], to[2]}, PlayingField.getTransition(from[1], from[0], from[2]));
        }


        //test the setter
        Tile[] tiles = Tile.values();
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            //generate random cords
            byte y = (byte)rand.nextInt(10); //generates num from 0-19
            byte x = (byte)rand.nextInt(15);
            Tile t = tiles[rand.nextInt(tiles.length)];
            pf.setMapTile(y, x, t);
            assertEquals(t, pf.getMapTile(y, x));
        }

        
    }

    /**
     * checks whether the illegal move detection works properly
     */
    @Test
    public void validMoveCheckerTest(){
        //create a Playing field from the example map in the courseRules.pdf:
        //uses both \n and \r\n
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



        //valid moves:
        //default moves
        
        assertNotNull(Move.simulateMove(Move.createDefaultMove((byte)6, (byte)9), (byte)1, pf));
        assertNotNull(Move.simulateMove(Move.createDefaultMove((byte)9, (byte)6), (byte)1, pf));
        assertNotNull(Move.simulateMove(Move.createDefaultMove((byte)9, (byte)7), (byte)1, pf));
        assertNotNull(Move.simulateMove(Move.createDefaultMove((byte)7, (byte)5), (byte)2, pf));
        assertNotNull(Move.simulateMove(Move.createDefaultMove((byte)9, (byte)8), (byte)3, pf));
        //override moves 
        assertNotNull(Move.simulateMove(Move.createOverrideMove((byte)6, (byte)8), (byte)1, pf));
        assertNotNull(Move.simulateMove(Move.createOverrideMove((byte)11, (byte)7), (byte)2, pf));
        //bomb moves
        assertNotNull(Move.simulateMove(Move.createBombMove((byte)11, (byte)7), (byte)2, pf));
        assertNotNull(Move.simulateMove(Move.createBombMove((byte)0, (byte)7), (byte)1, pf));
        assertNotNull(Move.simulateMove(Move.createBombMove((byte)0, (byte)5), (byte)2, pf));
        assertNotNull(Move.simulateMove(Move.createBombMove((byte)7, (byte)7), (byte)3, pf));

        //invalid moves:
        //default moves
        assertNull(Move.simulateMove(Move.createDefaultMove((byte)6, (byte)5), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createDefaultMove((byte)5, (byte)6), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createDefaultMove((byte)2, (byte)2), (byte)2, pf));
        assertNull(Move.simulateMove(Move.createDefaultMove((byte)6, (byte)6), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createDefaultMove((byte)6, (byte)7), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createDefaultMove((byte)10, (byte)7), (byte)3, pf));
        assertNull(Move.simulateMove(Move.createDefaultMove((byte)11, (byte)7), (byte)2, pf));
        //override moves 
        assertNull(Move.simulateMove(Move.createOverrideMove((byte)5, (byte)5), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createOverrideMove((byte)6, (byte)6), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createOverrideMove((byte)7, (byte)8), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createOverrideMove((byte)7, (byte)5), (byte)1, pf));
        assertNull(Move.simulateMove(Move.createOverrideMove((byte)3, (byte)8), (byte)2, pf));
        assertNull(Move.simulateMove(Move.createOverrideMove((byte)1, (byte)1), (byte)3, pf));
        //bomb moves
        assertNull(Move.simulateMove(Move.createBombMove((byte)2, (byte)2), (byte)3, pf));
        assertNull(Move.simulateMove(Move.createBombMove((byte)13, (byte)12), (byte)2, pf));
        assertNull(Move.simulateMove(Move.createBombMove((byte)13, (byte)2), (byte)1, pf));


    }

    /**
     * tests whether the move simulation works properly
     */
    //@Test
    public void moveSimulationTest_DefaultMove(){
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


        //execute some moves
        //P1 captures upper row 
        PlayingField pf2 = Move.simulateMove(Move.createDefaultMove((byte)6, (byte)9), (byte)1, pf);
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)6, (byte)9));

        //P2 captures 3 in upper row
        PlayingField pf3 = Move.simulateMove(Move.createDefaultMove((byte)6, (byte)9), (byte)2, pf);
        assertEquals(Tile.OCCUPIED_2, pf3.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_2, pf3.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.OCCUPIED_2, pf3.getMapTile((byte)6, (byte)9));
        //now P1 captures inversion
        //P1 now uses the stones of p2
        //P2 now uses the stones of p3
        //p3 now uses the stones of p1
        pf3.executeMove(Move.createDefaultMove((byte)6, (byte)10), (byte)1);
        //P3 should have top row
        assertEquals(Tile.OCCUPIED_3, pf3.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_3, pf3.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_3, pf3.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.OCCUPIED_3, pf3.getMapTile((byte)6, (byte)9));
        //check other fields
        assertEquals(Tile.OCCUPIED_3, pf3.getMapTile((byte)7, (byte)7));
        assertEquals(Tile.OCCUPIED_3, pf3.getMapTile((byte)8, (byte)8));
        assertEquals(Tile.OCCUPIED_1, pf3.getMapTile((byte)8, (byte)6));
        assertEquals(Tile.OCCUPIED_1, pf3.getMapTile((byte)7, (byte)8));
        assertEquals(Tile.OCCUPIED_2, pf3.getMapTile((byte)8, (byte)7));
        assertEquals(Tile.OCCUPIED_2, pf3.getMapTile((byte)7, (byte)6));
        //special fields and empty ones should remain
        assertEquals(Tile.EMPTY, pf3.getMapTile((byte)6, (byte)11));
        assertEquals(Tile.EMPTY, pf3.getMapTile((byte)6, (byte)5));
        assertEquals(Tile.EMPTY, pf3.getMapTile((byte)6, (byte)4));
        assertEquals(Tile.CHOICE, pf3.getMapTile((byte)6, (byte)1));
        assertEquals(Tile.EXPANSION, pf3.getMapTile((byte)11, (byte)7));
        assertEquals(Tile.HOLE, pf3.getMapTile((byte)1, (byte)1));
    }

    
    /**
     * test move simulation regarding special transitions
     */
    @Test
    public void moveSimulationTest_SpecialTransition(){
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 1 0 0 - - - - -\n" +
        "- - - - - 0 0 1 0 0 - - - - -\n" +
        "- - - - - 0 0 2 0 0 - - - - -\n" +
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
        //capture
        pf.executeMove(Move.createDefaultMove((byte)14, (byte)7), (byte)2);
        //assert
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)14, (byte)7));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)0, (byte)7));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)1, (byte)7));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)2, (byte)7));

    }



    /**
     * test move simulation regarding override moves
     */
    @Test
    public void moveSimulationTest_OverrideMove(){
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

        //override tests 
        PlayingField pf2 = Move.simulateMove(Move.createOverrideMove((byte)11, (byte)7), (byte)1, pf);
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)11, (byte)7));
        assertEquals(Tile.EXPANSION, pf2.getMapTile((byte)10, (byte)7));
        assertEquals(Tile.EXPANSION, pf2.getMapTile((byte)12, (byte)7));
        assertEquals(Tile.EXPANSION, pf2.getMapTile((byte)11, (byte)6));
        assertEquals(Tile.EXPANSION, pf2.getMapTile((byte)11, (byte)8));
        assertEquals(5, pf2.getPlayerOverrideStoneCount((byte)1));
        //5 more overrides, so no more stones should be left 
        pf2.executeMove(Move.createOverrideMove((byte)6, (byte)8), (byte)1);
        pf2.executeMove(Move.createOverrideMove((byte)8, (byte)6), (byte)1);
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)7, (byte)6));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)7, (byte)8));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)8, (byte)6));
        assertEquals(Tile.OCCUPIED_1, pf2.getMapTile((byte)8, (byte)7));
        //no more override stones 
        assertNull(Move.simulateMove(Move.createOverrideMove((byte)7, (byte)7), (byte)1, pf2));
    }

    /**
     * test move simulation regarding ChoiceMoves
     */
    //@Test
    public void moveSimulationTestChoiceMove(){
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
        "0 0 0 0 0 c 1 2 3 0 i 0 0 0 0\n" +
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
        
        //choice test
        //choice field from 2 -> swap with 3
        //Now, the player use the follwing stones: P1: 1, P2: 3, P3: 2
        pf.executeMove(Move.createChoiceMove((byte)6, (byte)5, (byte)3), (byte)2);
        //y=6 is like this atm:  0 0 0 0 0 3 3 3 2 0 i 0 0 0 0
        //assert
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)5));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)7, (byte)6));

        //now swap a whole lot
        pf.setMapTile((byte)6, (byte)4, Tile.CHOICE);
        pf.setMapTile((byte)6, (byte)3, Tile.CHOICE);
        pf.setMapTile((byte)6, (byte)2, Tile.CHOICE);
        

        //y=6 is like this atm:  0 0 c c c 3 3 3 2 0 i 0 0 0 0
        //2 will now swap with 3:
        pf.executeMove(Move.createChoiceMove((byte)6, (byte)4, (byte)3), (byte)2);
        //Now, the player use the follwing stones: P1: 1, P2: 2, P3: 3
        //y=6 is like this atm:  0 0 c c 3 3 3 3 3 0 i 0 0 0 0
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)4));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)5));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)8));

        pf.setMapTile((byte)6, (byte)9, Tile.OCCUPIED_1);
        //y=6 is like this atm:  0 0 c c 3 3 3 3 3 1 i 0 0 0 0
        //1 will swap with 2
        pf.executeMove(Move.createChoiceMove((byte)6, (byte)3, (byte)2), (byte)1);
        //Now, the player use the follwing stones: P1: 2, P2: 1, P3: 3
        //y=6 is like this atm:  0 0 c 2 2 2 2 2 2 2 i 0 0 0 0
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)3));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)4));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)5));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)8));

        pf.setMapTile((byte)6, (byte)9, Tile.OCCUPIED_3);
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)9));
        //y=6 is like this atm:  0 0 c 2 2 2 2 2 2 3 i 0 0 0 0
        //3 will swap with 1
        assertTrue(pf.executeMove(Move.createChoiceMove((byte)6, (byte)2, (byte)1), (byte)3));
        //Now, the player use the follwing stones: P1: 3, P2: 1, P3: 2
        //y=6 is like this atm:  0 0 1 1 1 1 1 1 1 1 i 0 0 0 0
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)2));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)3));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)4));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)5));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)8));

        pf.setMapTile((byte)6, (byte)0, Tile.OCCUPIED_1);
        pf.setMapTile((byte)6, (byte)2, Tile.OCCUPIED_2);
        pf.setMapTile((byte)6, (byte)14, Tile.OCCUPIED_3);
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)0));
        //y=6 is like this atm:  1 0 2 1 1 1 1 1 1 1 i 0 0 0 3
        //now 2 occupies inversion
        //3 gets the stones of P1
        //2 gets the stones of p3
        //1 gets the stones from p2
        pf.executeMove(Move.createDefaultMove((byte)6, (byte)10), (byte)2);
        //Now, the player use the follwing stones: P1: 2, P2: 3, P3: 1
        //y=6 is like this atm:  3 0 1 1 1 1 1 1 1 1 1 0 0 0 2
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)0));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)2));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)3));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)4));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)5));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)9));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)10));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)6, (byte)14));

    
    }

    /**
     * test move simulation regarding bomb moves
     */
    @Test
    public void moveSimulationTest_BombMove(){
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
        
        //bomb tests
        pf.executeMove(Move.createBombMove((byte)7, (byte)7), (byte)1);
        assertEquals(3, pf.getPlayerBombCount((byte)1));


        //assert holes
        assertEquals(Tile.HOLE, pf.getMapTile((byte)7, (byte)7));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)6, (byte)7));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)5, (byte)7));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)5, (byte)9));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)7, (byte)8));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)7, (byte)9));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)8, (byte)8));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)9, (byte)9));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)8, (byte)7));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)9, (byte)7));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)8, (byte)6));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)9, (byte)5));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)7, (byte)6));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)7, (byte)5));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)5, (byte)5));
        //assert no changes in other fields
        assertEquals(Tile.EMPTY, pf.getMapTile((byte)5, (byte)4));
        assertEquals(Tile.EMPTY, pf.getMapTile((byte)6, (byte)4));
        assertEquals(Tile.EMPTY, pf.getMapTile((byte)7, (byte)4));
        assertEquals(Tile.EMPTY, pf.getMapTile((byte)4, (byte)7));
        assertEquals(Tile.EXPANSION, pf.getMapTile((byte)10, (byte)7));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)8, (byte)9));

        //special transition bomb
        pf.executeMove(Move.createBombMove((byte)14, (byte)7), (byte)1);
        assertEquals(Tile.HOLE, pf.getMapTile((byte)0, (byte)7));
        assertEquals(Tile.HOLE, pf.getMapTile((byte)1, (byte)7));

        //2 more bombs
        pf.executeMove(Move.createBombMove((byte)7, (byte)0), (byte)1);
        pf.executeMove(Move.createBombMove((byte)7, (byte)14), (byte)1);
        //no bombs left
        assertEquals(0, pf.getPlayerBombCount((byte)1));
        assertNull(Move.simulateMove(Move.createBombMove((byte)11, (byte)7), (byte)1, pf));
    }


    /**
     * test move simulation regarding inversion moves
     */
    //@Test
    public void moveSimulationTest_Inversion(){
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
        "0 c 0 0 0 0 1 2 3 i 0 0 0 0 0\n" +
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
        //test inversion move
        pf.executeMove(Move.createDefaultMove((byte)6, (byte)9), (byte)2);

        //check if inversion was executed correctly
        //p1 now uses the stones of p2
        //p2 now uses the stones of p3
        //p3 now uses the stones of p1
        //y=6 after occupation 0 c 0 0 0 0 1 2 2 2 0 0 0 0 0 
        //y=6 after inversion  0 c 0 0 0 0 3 1 1 1 0 0 0 0 0 
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)9));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)8));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)6, (byte)7));

        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)6, (byte)6));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)7, (byte)6));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)7, (byte)7));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)7, (byte)8));
        assertEquals(Tile.OCCUPIED_1, pf.getMapTile((byte)8, (byte)6));
        assertEquals(Tile.OCCUPIED_2, pf.getMapTile((byte)8, (byte)7));
        assertEquals(Tile.OCCUPIED_3, pf.getMapTile((byte)8, (byte)8));


    }



    /**
     * tests TileType Computation
     */
    @Test
    public void computeTileTypeTest(){
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
        //to remove warning message of unused attribute pf
        pf.changePlayerBombCount((byte)1, (byte)0);       
         
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)0, (byte)0));
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)1, (byte)1));
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)1, (byte)6));
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)1, (byte)8));
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)7, (byte)7));

        //special transition
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)0, (byte)6));
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)1, (byte)9));
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)14, (byte)7));
        assertEquals(TileType.DEFAULT, PlayingField.getTileType((byte)0, (byte)7));

        assertEquals(TileType.EDGE, PlayingField.getTileType((byte)0, (byte)8));
        assertEquals(TileType.EDGE, PlayingField.getTileType((byte)7, (byte)0));
        assertEquals(TileType.EDGE, PlayingField.getTileType((byte)8, (byte)0));
        assertEquals(TileType.EDGE, PlayingField.getTileType((byte)7, (byte)14));
        assertEquals(TileType.EDGE, PlayingField.getTileType((byte)14, (byte)8));

        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)0, (byte)5));
        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)0, (byte)9));
        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)14, (byte)5));
        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)14, (byte)9));
        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)5, (byte)0));
        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)5, (byte)14));
        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)9, (byte)0));
        assertEquals(TileType.CORNER, PlayingField.getTileType((byte)9, (byte)14));
    }


    @Test
    public void frontierCountTest(){
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 1 0 0 0 0 0 0 0 0\n" +
        "0 c 0 0 0 2 2 2 3 b i 0 0 0 0\n" +
        "0 0 0 0 3 3 3 1 2 2 c 0 0 0 0\n" +
        "0 0 0 b 0 2 3 3 1 b b 0 0 0 0\n" +
        "0 0 0 0 2 0 3 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n";
        PlayingField pf = PlayingField.readMap(mapString);
        //frontier counts: 1: 2, 2: 7, 3: 6
        assertEquals(2, pf.getFrontierCount((byte)1));
        assertEquals(7, pf.getFrontierCount((byte)2));
        assertEquals(6, pf.getFrontierCount((byte)3));

        //now place a stone
        pf.executeMove(Move.createDefaultMove((byte)5, (byte)8), (byte)1);
        /**
         * map will look like this:
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 1 0 1 0 0 0 0 0 0\n" +
        "0 c 0 0 0 2 2 2 1 b i 0 0 0 0\n" +
        "0 0 0 0 3 3 3 1 1 2 c 0 0 0 0\n" +
        "0 0 0 b 0 2 3 3 1 b b 0 0 0 0\n" +
        "0 0 0 0 2 0 3 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n";

        -> frontier counts: 1: 5, 2: 6, 3: 5
         */
        assertEquals(5, pf.getFrontierCount((byte)1));
        assertEquals(6, pf.getFrontierCount((byte)2));
        assertEquals(5, pf.getFrontierCount((byte)3));


        //now do an override move on (6, 5)
        pf.executeMove(Move.createDefaultMove((byte)6, (byte)5), (byte)1);
        /**
         * map will look like this:
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 1 0 1 0 0 0 0 0 0\n" +
        "0 c 0 0 0 1 1 1 1 b i 0 0 0 0\n" +
        "0 0 0 0 3 3 3 1 1 2 c 0 0 0 0\n" +
        "0 0 0 b 0 2 3 3 1 b b 0 0 0 0\n" +
        "0 0 0 0 2 0 3 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n";
        -> frontier counts: 1: 8, 2: 3, 3: 5
         */
        assertEquals(8, pf.getFrontierCount((byte)1));
        assertEquals(3, pf.getFrontierCount((byte)2));
        assertEquals(5, pf.getFrontierCount((byte)3));




        //now do swap 1 and 2 with (7, 10)
        pf.executeMove(Move.createChoiceMove((byte)7, (byte)10, (byte)2), (byte)1);
        /**
         * map will look like this:
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 2 0 2 0 0 0 0 0 0\n" +
        "0 c 0 0 0 2 2 2 2 b i 0 0 0 0\n" +
        "0 0 0 0 3 3 3 2 2 2 2 0 0 0 0\n" +
        "0 0 0 b 0 1 3 3 2 b b 0 0 0 0\n" +
        "0 0 0 0 1 0 3 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n";
        -> frontier counts: 1: 2, 2: 10, 3: 5
         */
        assertEquals(2, pf.getFrontierCount((byte)1));
        assertEquals(10, pf.getFrontierCount((byte)2));
        assertEquals(5, pf.getFrontierCount((byte)3));


    }


    @Test
    public void numberOccupiedTest(){
        String mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 i 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "0 0 0 0 0 0 1 0 0 0 0 0 0 0 0\n" +
        "0 c 0 0 0 2 2 2 3 b i 0 0 0 0\n" +
        "0 0 0 0 3 3 3 1 2 2 c 0 0 0 0\n" +
        "0 0 0 b 0 2 3 3 1 b b 0 0 0 0\n" +
        "0 0 0 0 2 0 3 0 0 0 0 0 b 0 0\n" +
        "- - - - - 0 0 x 0 0 - - - - -\n" +
        "- - - - - 0 x x x 0 - - - - -\n" +
        "- - - - - 0 0 x c 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n" +
        "- - - - - 0 0 0 0 0 - - - - -\n";
        PlayingField pf = PlayingField.readMap(mapString);

        //number of occupiable tiles: 125
        //occupied tiles: 17
        assertEquals(125, pf.getNumberOccupiableTiles());
        assertEquals(17, pf.getNumberOccupiedTiles());


        //default move on (5, 7) -> one more tile occupied
        assertTrue(pf.executeMove(Move.createDefaultMove((byte)5, (byte)7), (byte)1));
        assertEquals(18, pf.getNumberOccupiedTiles());
        //bonus move on (6, 9)
        assertTrue(pf.executeMove(Move.createBonusMove((byte)6, (byte)9, false), (byte)1));
        assertEquals(19, pf.getNumberOccupiedTiles());
        //choice move on (7, 10)
        assertTrue(pf.executeMove(Move.createChoiceMove((byte)7, (byte)10, (byte)1), (byte)1));
        assertEquals(20, pf.getNumberOccupiedTiles());
        //override move on (8, 6)
        assertTrue(pf.executeMove(Move.createDefaultMove((byte)8, (byte)6), (byte)1));
        assertEquals(20, pf.getNumberOccupiedTiles());
        //inversion move on (6,10)
        pf.setMapTile((byte)6, (byte)9, Tile.OCCUPIED_2);
        pf.setMapTile((byte)6, (byte)8, Tile.OCCUPIED_1);
        assertTrue(pf.executeMove(Move.createDefaultMove((byte)6, (byte)10), (byte)1));
        assertEquals(21, pf.getNumberOccupiedTiles());



        pf.changeNumberOccupiedTiles(4);
        //occupied now 25 -> 1/5
        assertEquals(20, (int)(pf.getPercentageOccupied()*100));



        //this shouldn't change
        assertEquals(125, pf.getNumberOccupiableTiles());
    }
}
