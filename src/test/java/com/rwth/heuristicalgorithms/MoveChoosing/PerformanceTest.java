package com.rwth.heuristicalgorithms.MoveChoosing;

import static org.junit.Assume.assumeTrue;

import org.junit.Before;

import com.rwth.heuristicalgorithms.Client.Client;
import com.rwth.heuristicalgorithms.PlayingField.PlayingField;

public class PerformanceTest{

    String map;
    @Before
    public void setUp(){ //executed before each test
        //clean up all the static values in pf
        PlayingField.reset();
         map = Maps.map1;
        
    }

    

    /**
     * The actual PerformanceTest. We werent creative enough for a proper name
     */
    //@Test
    public void test(){

        

        final boolean ACTIVATE_TEST = false; 
        assumeTrue(ACTIVATE_TEST);

        PlayingField pf = PlayingField.readMap(map);
        PlayingField.setOurPlayerNum((byte) 6);

        long start;
        long end;
        long time;
        

      
               
        

        
       
        for(int i = 0; i < 2; i++){
            start = System.nanoTime();
            Client.prevTime = new int[Client.maxDepth_TL];
            Client.calcMove(pf, 8, 2989, 10);

            end = System.nanoTime();
            time = (long)((end-start)/1_000_000.0);
            if(time > 2989){
                System.out.println("[ERROR] Time for computing move with a time limit of 2989ms: " + time + "ms"); 
                return;
            } else {
                System.out.println("Time for computing move with a time limit of 2989ms: " + time + "ms"); 
            }
        }

        System.out.println("\naspiartion windows\n");
        for(int i = 0; i < 1; i++){
            start = System.nanoTime();
            Client.prevTime = new int[Client.maxDepth_TL];
            Client.experimental = 1;
            Client.calcMove(pf, 8, 2989, 10);

            end = System.nanoTime();
            time = (long)((end-start)/1_000_000.0);
            if(time > 2989){
                System.out.println("[ERROR] Time for computing move with a time limit of 2989ms: " + time + "ms"); 
                return;
            } else {
                System.out.println("Time for computing move with a time limit of 2989ms: " + time + "ms"); 
            }
        }

    }


















        




}