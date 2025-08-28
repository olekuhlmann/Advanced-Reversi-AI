package com.rwth.heuristicalgorithms.Client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

/**
 * Simulates 1v1 games against the Reversi++ AI on the standard map.
 */
public class OnevsOneTests {

    public static final boolean ACTIVATE_TEST = false;

    /**
    * Simulates 1v1 games against the Reversi++ AI on the standard map.
    */
    @Test
    public void RevresiPPTest(){

        assumeTrue(ACTIVATE_TEST);
        int num = 1;
        System.out.println("Now simulating " + num + " 1v1 games on standard map vs reversi++.");


        try{
            //recompile our maven code, skip tests
            ProcessBuilder compileBuilder = new ProcessBuilder("cmd.exe", "/c", "mvn clean package -Dmaven.test.skip");
            compileBuilder.redirectErrorStream(true);
            compileBuilder.start();
            

            //start server with standard map and 2s time limit
            ProcessBuilder serverBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & server -s -t 2" );
            serverBuilder.redirectErrorStream(true);
            Process serverCmd = serverBuilder.start(); 

            //start AI
            ProcessBuilder aiBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & ai -q" );
            aiBuilder.redirectErrorStream(true);
            aiBuilder.start();


            //start client
            ProcessBuilder clientBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\target & java -jar swp2023_group5.jar  > nil" );
            clientBuilder.redirectErrorStream(true);
            clientBuilder.start();


            printResults(serverCmd); 


        } catch (IOException e){
            e.printStackTrace();
        }
        

    }

    /**
     * prints the ouptut of the given process to the console(as this is a test in VScode usually DEBUG CONSOLE)
     * WARNING: may casue issues with blocking/flow
     * @param process the process of which the output is taken
     * @throws IOException if something goes wrong with the reader
     */
    public static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            assertFalse("None should get disqualified. Problematic Line: " + line, line.contains("disqualified"));
            System.out.println(line);
        }
    }

}


