package com.rwth.heuristicalgorithms.Client;

import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

//import org.junit.Before;
import org.junit.Test;
/**
 * contains tests involving Client-Server interaction. Code designed for windows.
 */
public class ClientTest {




    /**
     * MapTest will be skipped if this is set to false.
    */
    private static final boolean ACTIVATE_MAP_TEST = false;

    /**
     * ManuelTes will be skipped if this is set to false
     */
    private static final boolean ACTIVATE_MANUEL_TEST = false;

    /**
     * PerformanceTest will be skipped if this is set to false
     */
    private static final boolean ACTIVATE_PERFORMANCE_TEST = false;



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
            //assertFalse("None should get disqualified. Problematic Line: " + line, line.contains("disqualified"));
            System.out.println(line);
        }
    }

    // dir /a-d /b | findstr ".map"
    /**
     * test all maps in the map folder by starting processes with the server.exe and ai.exe from bin folder
     */
    @Test
    public void testMaps() {

        //Skip test incase this is false
        assumeTrue(ACTIVATE_MAP_TEST);

        try {
            //recompile our maven code, skip tests
            ProcessBuilder compileBuilder = new ProcessBuilder("cmd.exe", "/c", "mvn clean package -Dmaven.test.skip");
            compileBuilder.redirectErrorStream(true);
            Process compileCmd = compileBuilder.start();
            printResults(compileCmd);

            //get all mapfilenames
            ProcessBuilder mapstringBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\maps & dir /a-d /b | findstr \".map\"");
            mapstringBuilder.redirectErrorStream(true);
            Process mapstringCmd = mapstringBuilder.start();

            BufferedReader mapstringReader = new BufferedReader(new InputStreamReader(mapstringCmd.getInputStream()));
            String line = "";
            //foreach map
            while ((line = mapstringReader.readLine()) != null) {
                for(int j = 1; j < 2; j++){

             
                System.out.println(line);
                //print out the map to get player count
                ProcessBuilder playercountBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\maps & type " + line);
                playercountBuilder.redirectErrorStream(true);
                Process playercountCmd = playercountBuilder.start();

                //start server
                ProcessBuilder serverBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & server ..\\maps\\"+line + " -t 2" );
                serverBuilder.redirectErrorStream(true);
                Process serverCmd = serverBuilder.start(); 

                BufferedReader playercountReader = new BufferedReader(new InputStreamReader(playercountCmd.getInputStream()));
                int playerCount = playercountReader.readLine().charAt(0)-'0';
                System.out.println("Testing map: " + line + " for "+playerCount+ " players");
                //start n-1 ai's
                for(int i = 1; i<playerCount; i++) {
                    System.out.println("Starting Ai " + i + "/" + (playerCount-1));
                    ProcessBuilder aiBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & ai -q" );
                    aiBuilder.redirectErrorStream(true);
                    Process aiCmd = aiBuilder.start();
                    //dummy line to supress variable not used warning:
                    aiCmd.pid();
                }
                //start client
                System.out.println("Starting Client");

                ProcessBuilder clientBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\target & java -jar swp2023_group5.jar -exp "+j+" > ..\\testresults\\clientLog_"+line+"_"+j+".txt" );
                clientBuilder.redirectErrorStream(true);
                 Process clientCmd = clientBuilder.start();
                 //dummy line to supress variable not used warning:
                 clientCmd.pid();
                printResults(serverCmd); 
                   }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * similar to map test, but uses -mt
     */
    @Test
    public void testComparePerformance() {

        //Skip test incase this is false
        assumeTrue(ACTIVATE_PERFORMANCE_TEST);

        try {
            for(int d = 3; d<=3; d++) {
            ProcessBuilder compileBuilder = new ProcessBuilder("cmd.exe", "/c", "mvn clean package -Dmaven.test.skip");
            compileBuilder.redirectErrorStream(true);
            Process compileCmd = compileBuilder.start();
            printResults(compileCmd);


            ProcessBuilder mapstringBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\maps & dir /a-d /b | findstr \".map\"");
            mapstringBuilder.redirectErrorStream(true);
            Process mapstringCmd = mapstringBuilder.start();

            BufferedReader mapstringReader = new BufferedReader(new InputStreamReader(mapstringCmd.getInputStream()));
            String line = "";
            while ((line = mapstringReader.readLine()) != null) {
                 System.out.println(line);
                ProcessBuilder playercountBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\maps & type " + line);
                playercountBuilder.redirectErrorStream(true);
                Process playercountCmd = playercountBuilder.start();

                ProcessBuilder serverBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & server ..\\maps\\"+line + " -d "+d );
                serverBuilder.redirectErrorStream(true);
                 Process serverCmd = serverBuilder.start(); 

                BufferedReader playercountReader = new BufferedReader(new InputStreamReader(playercountCmd.getInputStream()));
                int playerCount = playercountReader.readLine().charAt(0)-'0';
                System.out.println("Testing map: " + line + " for "+playerCount+ " players with depth:" + d);
                for(int i = 1; i<playerCount; i++) {
                    System.out.println("Starting Ai " + i + "/" + (playerCount-1));
                    ProcessBuilder aiBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & ai -q" );
                    aiBuilder.redirectErrorStream(true);
                    Process aiCmd = aiBuilder.start();
                    //dummy line to supress variable not used warning:
                    aiCmd.pid();
                    
                }
                System.out.println("Starting Client");

                ProcessBuilder clientBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\target & java -jar swp2023_group5.jar -mt > ..\\testresults\\TestResult_Depth-"+d+"_"+line+".txt" );
                clientBuilder.redirectErrorStream(true);
                 Process clientCmd = clientBuilder.start();
                //dummy line to supress variable not used warning:
                clientCmd.pid();
                printResults(serverCmd); 
            }

             //printResults(p);

            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


/**
 * easier setup for manuel tests, will not work on its own. Intended far user to start client
 */
@Test
public void manuel_testMaps() {

    //Skip test incase this is false
    assumeTrue(ACTIVATE_MANUEL_TEST);

    try {
        ProcessBuilder compileBuilder = new ProcessBuilder("cmd.exe", "/c", "mvn clean package -Dmaven.test.skip");
        compileBuilder.redirectErrorStream(true);
        Process compileCmd = compileBuilder.start();
        printResults(compileCmd);


        ProcessBuilder mapstringBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\maps & dir /a-d /b | findstr \".map\"");
        mapstringBuilder.redirectErrorStream(true);
        Process mapstringCmd = mapstringBuilder.start();

        BufferedReader mapstringReader = new BufferedReader(new InputStreamReader(mapstringCmd.getInputStream()));
        String line = "";
        while ((line = mapstringReader.readLine()) != null) {
             System.out.println(line);
            ProcessBuilder playercountBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\maps & type " + line);
            playercountBuilder.redirectErrorStream(true);
            Process playercountCmd = playercountBuilder.start();

            ProcessBuilder serverBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & server ..\\maps\\"+line + " -d 1 > ..\\testresults\\serverlog.txt" );
            serverBuilder.redirectErrorStream(true);
             Process serverCmd = serverBuilder.start(); 
            //dummy line to supress variable not used warning:
            serverCmd.pid();

            BufferedReader playercountReader = new BufferedReader(new InputStreamReader(playercountCmd.getInputStream()));
            int playerCount = playercountReader.readLine().charAt(0)-'0';
            System.out.println("Testing map: " + line + " for "+playerCount+ " players");
            for(int i = 1; i<playerCount; i++) {
                System.out.println("Starting Ai " + i + "/" + (playerCount-1));
                ProcessBuilder aiBuilder = new ProcessBuilder("cmd.exe", "/c", "cd .\\bin & ai -q" );
                aiBuilder.redirectErrorStream(true);
                Process aiCmd = aiBuilder.start();
                 //dummy line to supress variable not used warning:
                 aiCmd.pid();
            }
            System.out.println("Starting Client");

             
        }


    } catch (IOException e) {
        e.printStackTrace();
    }
}

}
