package com.rwth.heuristicalgorithms.MoveChoosing;


import org.junit.Before;
//import org.junit.Test;

import com.rwth.heuristicalgorithms.PlayingField.PlayingField;

/**
 * Tests the performance of the implemented heuristic. 
 * Focuses on the performance of the computation of stable tiles.
 */
public class HeristicPerformanceTest {
    /**
     * executed before each test
     */
    @Before
    public void setUp(){ 
        //clean up all the static values in pf
        PlayingField.reset();
    }
    
    /**
     * Tests the evalBoard_Building method numerous times and measures the time.
     * Times are measured for the version with and without couting stable tiles. 
     */
    //@Test 
    public void evalBoard_BuildingPerformanceTest(){
        long withStable = 0;
        long withoutStable = 0;

        int runs = 100;
        for(int i = 0; i < runs; i++){
            withStable += evalBoard_BuildingPerformanceTest_withStableTiles();
            withoutStable += evalBoard_BuildingPerformanceTest_withoutStableTiles();
        }

        withStable /= runs;
        withoutStable /= runs;

        
        System.out.println("[TESTS] Performance Test of eval_Board_Building. Time consumed with stable tiles: " + withStable + " milliseconds. Time consumed without stable tiles: " + withoutStable + " milliseconds. Values are averaged over " + runs + " runs.");
    }

    /**
     * Tests the evalBoard_Building method with stable tile couting enabled
     * @return milliseconds measured
     */
    private long evalBoard_BuildingPerformanceTest_withStableTiles(){
        MoveHeuristic.setStableTilesEnabled(true);
        return evalBoard_BuildingPerformanceTestGeneric();
    }

    /**
     * Tests the evalBoard_Building method with stable tile couting disabled
     * @return milliseconds measured
     */
    private long evalBoard_BuildingPerformanceTest_withoutStableTiles(){
        MoveHeuristic.setStableTilesEnabled(false);
        return evalBoard_BuildingPerformanceTestGeneric();
    }

    /**
     * This method measures the time for a few maps
     * @return
     */
    private long evalBoard_BuildingPerformanceTestGeneric(){
        long total = 0;
        //create map
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
        //call evalBoard and stop time
        long startTime = System.nanoTime();
        for(int i = 0; i < 10_000; i++){
            MoveHeuristic.evalBoard_Building(pf, (byte)1);
            MoveHeuristic.evalBoard_Building(pf, (byte)2);
            MoveHeuristic.evalBoard_Building(pf, (byte)3);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        long durationInMilliseconds = (long)(duration / 1_000_000.0);

        total += durationInMilliseconds;

        //clean up and next map
        setUp();
        mapString = "3\n" +
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
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        pf = PlayingField.readMap(mapString);

        //call evalBoard and stop time
        startTime = System.nanoTime();
        for(int i = 0; i < 10_000; i++){
            MoveHeuristic.evalBoard_Building(pf, (byte)1);
            MoveHeuristic.evalBoard_Building(pf, (byte)2);
            MoveHeuristic.evalBoard_Building(pf, (byte)3);
        }
        endTime = System.nanoTime();
        duration = endTime - startTime;
        durationInMilliseconds = (long)(duration / 1_000_000.0);

        total += durationInMilliseconds;



        //clean up and next map

        setUp();
        mapString = "3\n" +
        "6\n"+
        "4 2\r\n"+
        "15 15\r\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 0 1 1 1 1 - - - - -\n" +
        "- - - - - 1 2 0 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 3 1 1 1 2 - - - - -\n" +
        "1 1 0 1 1 3 1 1 1 1 2 1 0 1 1\n" +
        "1 1 1 1 1 2 1 1 1 1 1 1 1 0 1\n" +
        "1 1 1 1 1 1 1 1 1 1 1 1 1 2 1\n" +
        "1 1 1 1 1 1 1 1 1 3 0 1 1 1 1\n" +
        "1 1 1 1 1 1 1 1 1 1 2 1 1 1 1\n" +
        "- - - - - 1 1 1 0 1 - - - - -\n" +
        "- - - - - 3 1 1 1 1 - - - - -\n" +
        "- - - - - 1 1 1 1 1 - - - - -\n" +
        "- - - - - 2 1 1 1 1 - - - - -\n" +
        "- - - - - 1 0 1 1 1 - - - - -\n" +
        "6 0 0 <-> 9 1 1\n" +
        "7 14 4 <-> 7 0 0";
        pf = PlayingField.readMap(mapString);

        //call evalBoard and stop time
        startTime = System.nanoTime();
        for(int i = 0; i < 10_000; i++){
            MoveHeuristic.evalBoard_Building(pf, (byte)1);
            MoveHeuristic.evalBoard_Building(pf, (byte)2);
            MoveHeuristic.evalBoard_Building(pf, (byte)3);
        }
        endTime = System.nanoTime();
        duration = endTime - startTime;
        durationInMilliseconds = (long)(duration / 1_000_000.0);

        total += durationInMilliseconds;



        return total;
    }
}
