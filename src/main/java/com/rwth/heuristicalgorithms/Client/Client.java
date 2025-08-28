package com.rwth.heuristicalgorithms.Client;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.io.*;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.rwth.heuristicalgorithms.MoveChoosing.Minimax;
import com.rwth.heuristicalgorithms.MoveChoosing.TimeoutException;
import com.rwth.heuristicalgorithms.PlayingField.Move;
import com.rwth.heuristicalgorithms.PlayingField.PlayingField;

/**
 * Class for handling the Client-Side of the network protocol
 */
public class Client {

    // -------------------------------------------------
    // CONSTANTS START

    /**
     * Message for sending our Group number at the beginning of the protocol, right
     * after establishing connection.
     */
    private static final byte[] groupNumberMsg = { (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 5 };

    // CONSTANTS END
    // -------------------------------------------------

    // -------------------------------------------------
    // ATTRIBUTES START
    
    /**
     * The host address to be connected to. Default: localhost. Set by -s or --server flag 
     */
    @Parameter(names = { "--server", "-s" }, description = "server Ip-address or Hostname.")
    public String host = "127.0.0.1";

    /**
     * The host side port adrress to connect to. Default: 7777. Set by -p or --port flag 
     */
    @Parameter(names = { "--port", "-p" }, description = "server port number.")
    public int port = 7777;

    /**
     * Is true when help is to be displayed instead of code-execution. Set by -h or --help flag
     */
    @Parameter(names = { "--help", "-h" }, description = "Displays help.", help = true)
    public boolean showHelp = false;

    /**
     * Is true when minimax is to be used instead of alpha-beta pruning, set by -np or --nopruning flag
     */
    @Parameter(names = { "--nopruning", "-np" }, description = "Disables alpha-beta pruning")
    public static boolean chooseNoPruning = false;

    /**
     * is true when moves ar to be sorted
     */
    @Parameter(names = { "--nomovesorting", "-nm" }, description = "Disables alpha-beta move sorting")
    public static boolean chooseNoMoveSorting = false;

    /**
     * Is true when minimax and alpha-betapruning are to be compared. May be used differently in the future, 
     * set by -d or --debug flag
     */
    @Parameter(names = { "--debug", "-d" }, description = "Toggles debug mode")
    public static boolean debug = false;

    /**
     * Is true when the time is to be measured. Also counts number of states evaluated. Set by -mt or --measureTime flag
     */
    @Parameter(names = { "--measureTime",
            "-mt" }, description = "When enabled measures time differnce between minimax and alpha-beta pruning")
    public static boolean measureTime = false;

    /**
     * IF true extends the ouptut for manual debugging. Set by -v or --verbose flag
     */
    @Parameter(names = { "--verbose", "-v" }, description = "When enabled expands output.")
    public static boolean verbose = false;

    /**
     * Is true for experimental feature. 
     */
    @Parameter(names = { "--experimental", "-exp" }, description = "When enabled uses experimental feature.")
    public static int experimental = 0;

    /**
     * stores the active Playing field
     */
    private static PlayingField pf;

    /**
     * Socket used for the connection to the server, the game is running on.
     * Used for receiving and sending messages.
     */
    private static Socket connection;

    /**
     * Internet Address of the corresponding serverside socket.
     */
    private static InetSocketAddress server;

    /**
     * This is InputStream is used for actually receiving the messages.
     * It will be "provided" by the socket.
     * Note: Unbuffered
     */
    private static InputStream input;

    /**
     * This OutputStream is used for sending messages.
     * it will be "provided" by the sockete.
     * Note: Unbuffered
     */
    private static OutputStream outputU;

    /**
     * Extends the OutputStream with a buffer.
     */
    private static BufferedOutputStream output;

    /**
     * private static BufferedInputStream input; would also add a buffer to the
     * Input Stream.
     * Unfortunately this also messes with the blocking behaviour of the Input
     * stream.
     */

    /**
     * Used for storing the received Message's Payload (excluding header)
     */
    private static byte[] msg;

    /**
     * Used for storing The Answer to a move request and then handing it over to the
     * output stream.
     * Always consists of:
     * HEADER
     * 1 Byte conatining the type
     * 4 Bytes conatining the payload length
     * PAYLOAD
     * 2 Bytes X coordinate
     * 2 Bytes Y coordinate
     * 1 Byte addition information for choice and bonus tiles
     */
    private static byte[] ans = new byte[10];

    /**
     * Used for Storing the 5 Byte header of incoming messages
     */
    private static byte[] header = new byte[5];

    /**
     * Used for stopping the program once the game is over and the connection is
     * closed
     */
    private static boolean run = true;

    /**
     * used to keep track of the number of moves
     */
    public static int movenr = 0;

    /**
     * Used to counting the number of states evaluated when pruning. Primarly for -mt
     */
    public static int prunedEvalCount;

    /**
     * Used to counting the number of states evaluated when not pruning. Primarly for -mt
     */
    public static int unprunedEvalCount;

    /**
     * used to keep track of the time when we want to throw a TimeoutException
     */
    public static long returnTime = 0;

    /**
     * When only receiving a time limit, this depth limit will apply on top of the time limit.
     */
    public static final int maxDepth_TL = 100;

    /**
     * Keeps track of our player number to detect disqualifications
     */
    public static byte ourPlayerNumber = -1;

    
    /**
     * Logs the number of timeout exceptions. Value is printed at the end of the game.
     */
    public static int numTimeoutExceptions = 0;

    /**
     * Logs the number of cases where our estimation stopped us from going into the next depth. 
     * Value is printed at the end of the game.
     */
    public static int numEstimationAborted = 0;

    /**
     * Logs the time limit of the last move. Value is printed at the end of the game.
     */
    public static int lastTimeLimit = 0;
    

    /**
     * used to keep track previous (potentially insufficient) times
     */
    public static int[] prevTime = new int[maxDepth_TL];

    private static int pAlpha = Integer.MIN_VALUE;

    private static int pBeta = Integer.MAX_VALUE;

    private static int prevValue =0;

    // ATTRIBUTES END
    // -------------------------------------------------

    // -------------------------------------------------
    // METHODS START

    /**
     * calculates the next move
     * 
     * @param pf           the map the move is to be made on
     * @param playerNumber the player making the move
     * @param tl           timelimit in miliseconds
     * @param dl           depthlimit
     * @return the calculated move
     */
    public static Move calcMove(PlayingField pf, int playerNumber, int tl, int dl) {
        //buffer = 1% + 1000ms
        returnTime = System.nanoTime() / 1_000_000 + (long) (0.99*tl) - 1000;
        //best move so far
        Move res = null;
        //default case

        if(experimental == 2) {
            pAlpha = Integer.MIN_VALUE;
            pBeta = Integer.MAX_VALUE;
        }

        if (!debug && !measureTime) {
            System.out.println("\nOur Move: " + movenr);
            for (int i = 1; i <= dl; i++) {
                System.out.println("Entering Depth: " + i);
                prunedEvalCount = 0;
                long start = System.nanoTime()/1_000_000;
                try {                       
                    if(i>1 && prevTime[i-1]>(returnTime-start)) {
                        numEstimationAborted++;
                        return res;
                    }
                    if(experimental == 0 || i == 1){
                        res = Minimax.selectBestMove(pf, (byte) playerNumber, i,Integer.MIN_VALUE,Integer.MAX_VALUE, tl, 
                                        !chooseNoPruning, !chooseNoMoveSorting);
                        prevValue = res.value;
                    } else {
                        int newA = pAlpha;
                        int newB = pBeta;
                        switch(experimental){
                            case 1:     newA = (int) (prevValue - Math.abs(prevValue)*0.5 - Math.abs(pAlpha)/2);//(prevValue/2)+(pAlpha/2);
                                        newB = (int) (prevValue + Math.abs(prevValue)*0.5 + Math.abs(pBeta)/2);//(int)(prevValue*1.5)+(pBeta/2);
                                        break;
                            case 2:     newA = (int) (prevValue - Math.abs(prevValue)*0.5 - Math.abs(pAlpha)/2);//(prevValue/2)+(pAlpha/2);
                                        newB = (int) (prevValue + Math.abs(prevValue)*0.5 + Math.abs(pBeta)/2);//(int)(prevValue*1.5)+(pBeta/2);
                                        break;
                            case 3:     newA = (int)(prevValue - (Math.abs(prevValue)*(0.75)));
                                        newB = (int) (prevValue + (Math.abs(prevValue)*(0.75)));
                                        break;
                            default:    break;            
                        }
                        System.out.println("Aspiration Window: "+newA+" <-> " + newB);
                        res = Minimax.selectBestMove(pf, (byte) playerNumber, i,newA,newB, tl, !chooseNoPruning, !chooseNoMoveSorting);
                        if(res == null){
                            res = Minimax.selectFirstMove(pf, (byte) playerNumber);
                            System.out.println("[ERROR] couldnt find move due to aspiration windows, using first move instead");
                        }else{
                            prevValue = res.value;
                            pAlpha = newA;
                            pBeta = newB;
                        }
                    }
                    long end = System.nanoTime()/1_000_000;
                    prevTime[i-1] = (int)(end-start);
                    System.out.println("Move Value: " + prevValue + " States Evaluated: " + prunedEvalCount);
                } catch (TimeoutException e) {
                    numTimeoutExceptions++;
                    prevTime[i-1] = (int)(System.nanoTime()/1_000_000-start);
                    if(res == null) return Minimax.selectFirstMove(pf, (byte) playerNumber);
                    return res;
                }
            }
            if(res == null) return Minimax.selectFirstMove(pf, (byte) playerNumber);
            return res;
        }
        // for measuring time and comparing between minimax and alpha beta pruning.
        if (measureTime) {
            int rep = 1;
            long prunedTotal = 0;
            long unprunedTotal = 0;
            //dummy line to supress variable not used warning:
            rep += unprunedTotal;

            Move pruned = null;
            unprunedEvalCount = 0;
            prunedEvalCount = 0;
            for (int i = 0; i < rep; i++) {
                long prunedStart = System.nanoTime();
                try {
                    pruned = Minimax.selectBestMove(pf, (byte) playerNumber, dl,Integer.MIN_VALUE,Integer.MAX_VALUE, tl, !chooseNoPruning, !chooseNoMoveSorting);
                } catch (TimeoutException e) {
                    return res;
                }
                long prunedEnd = System.nanoTime();

                long prunedDuration = prunedEnd - prunedStart;
                long prunedDurationInMilliseconds = (long) (prunedDuration / 1_000_000.0);


                prunedTotal += prunedDurationInMilliseconds;
            }
            prunedTotal /= rep;
            unprunedTotal /= rep;
            prunedEvalCount /= rep;
            unprunedEvalCount /= rep;
            System.out.println(
                    "[TESTS] Performance Comparison Between Minimax and alpha-beta pruning. Time consumed with minimax: "
                            + "N/A" + " Time consumed with alpha-beta pruning: " + prunedTotal);
            System.out.println(
                    "[TESTS] Performance Comparison Between Minimax and alpha-beta pruning. States Evaluated with minimax: "
                            + "N/A" + " States Evaluated with alpha-beta pruning: " + prunedEvalCount);
            System.out.println("");

            return pruned;

        // for comparing alpha beta and minimax
        } else if (debug) {
            try {
                Move pruned = Minimax.selectBestMove(pf, (byte) playerNumber, dl,Integer.MIN_VALUE,Integer.MAX_VALUE, tl, true, !chooseNoMoveSorting);
                Move unpruned = Minimax.selectBestMove(pf, (byte) playerNumber, dl,Integer.MIN_VALUE,Integer.MAX_VALUE, tl, false, false);

                PlayingField prunedPF = Move.simulateMove(pruned, (byte) playerNumber, pf);
                PlayingField unprunedPF = Move.simulateMove(unpruned, (byte) playerNumber, pf);

                int prunedValue = Minimax.minimaxValue(prunedPF, (byte) playerNumber,
                        Minimax.getNextPlayer(prunedPF, (byte) playerNumber), dl - 1);
                int unprunedValue = Minimax.minimaxValue(unprunedPF, (byte) playerNumber,
                        Minimax.getNextPlayer(unprunedPF, (byte) playerNumber), dl - 1);
                if (prunedValue != unprunedValue) {
                    System.out.println("alpha-beta pruning and minimax returned non-equivalent moves");
                }
                return unpruned;
            } catch (TimeoutException e) {
                return res;
            }

        }

        return null;
    };

    /**
     * Called When receiving a map
     * Decodes the map and creates a Playing field
     * 
     * @param data encoded Map
     */
    private static void rcvMap(byte[] data) {
        String s = new String(data, StandardCharsets.UTF_8);
        pf = PlayingField.readMap(s);
        if (!measureTime)
            System.out.println("Map received: \n" + s);
    }

    /**
     * Called When provide with our PlayerNumber
     * 
     * @param data received Playernumber
     */
    private static void rcvPlayerNumber(byte[] data) {
        PlayingField.setOurPlayerNum(data[0]);
        ourPlayerNumber = data[0];
        if (!measureTime)
            System.out.println("Our player number:" + PlayingField.getOurPlayerNum());
    }

    /**
     * Called when receiving a move Request
     * Calculates our next moves, encode it and sends it back
     * 
     * @param data 32 Bit Time Limit and 8 Bit maximum search depth
     * @throws IOException when an issue with the socket conenction occurs
     */
    private static void rcvMoveRequest(byte[] data) throws IOException {
        int timelimit = ByteBuffer.wrap(data, 0, 4).getInt();
        int depthlimit = data[4];


        //log the received time limit
        lastTimeLimit = timelimit;

        long start = System.nanoTime();

        System.out.println("Move request received with depth limit " + depthlimit + " and time limit " + timelimit + "ms.");

        // incase we get no time or depth limit
        if (timelimit == 0) {
            timelimit = 10000;//Implicit timelimit 10s;
        }
        if (depthlimit == 0) {
            depthlimit = maxDepth_TL;
        }

        Move m = calcMove(pf, PlayingField.getOurPlayerNum(), timelimit, depthlimit);

        if (m == null) {
            throw new IllegalStateException("[ERROR] in rcvMoveRequest: calcMove yielded null.");
        }

        //encode move coordinates
        ans[5] = (byte) 0;
        ans[6] = m.getX();
        ans[7] = (byte) 0;
        ans[8] = m.getY();

        //encode addtional information
        switch (m.getMoveType()) {
            case BONUS:
                ans[9] = m.getBonusChooseBomb() ? (byte) 20 : (byte) 21;
                break;
            case CHOICE:
                ans[9] = m.getChoicePlayer();
                break;
            default:
                ans[9] = (byte) 0;
                break;
        }

        output.write(ans, 0, 10);
        output.flush();

        long end = System.nanoTime();

        long time = (long)((end-start)/1_000_000.0);

        if(time > timelimit){
            System.out.print("[ERROR] ");
        }
        System.out.println("Move was sent. Time limit was " + timelimit + "ms and we used up " + time + "ms.");
    }

    /**
     * Called when receiving the announcement of a move made by a playe
     * 
     * @param data containing the move specifications(see -> ans) and the player who
     *             made the move
     */
    private static void rcvMoveAnnouncement(byte[] data) {
        Move m;
        //convert the received move into our move format
        if (pf.getIsEliminationPhase()) {
            m = Move.createBombMove(data[3], data[1]);
        } else {
            if (data[4] < (byte) 10 && data[4] > 0) {
                m = Move.createChoiceMove(data[3], data[1], data[4]);
            } else if (data[4] == (byte) 21) {
                m = Move.createBonusMove(data[3], data[1], false);
            } else if (data[4] == (byte) 20) {
                m = Move.createBonusMove(data[3], data[1], true);
            } else {
                if (pf.isTileOccupied(data[3], data[1])) {
                    m = Move.createOverrideMove(data[3], data[1]);
                } else {
                    m = Move.createDefaultMove(data[3], data[1]);
                }
            }
        }

        //update map
        pf.executeMove(m, data[5]);

        //print some information
        movenr++;
        if (!measureTime && experimental == 0)
            System.out.println("Move " + movenr + " by Player " + data[5] + ": X:" + m.getX() + " Y:" + m.getY());
        if (verbose) {
            System.out.println(pf.toString());
        }
    }

    /**
     * called when the server announces the disqualification of a player
     * 
     * @param data the player who got Disqualified
     */
    private static void rcvDisqualification(byte[] data) {
        pf.disqualify(data[0]);
        if(data[0] == ourPlayerNumber){
            System.out.println("[ERROR] We were disqualified!");
            System.out.println("""
                _       
                (_)      
            _ __ _ _ __  
           | '__| | '_ \\ 
           | |  | | |_) |
           |_|  |_| .__/ 
                  | |    
                  |_|
                    """);
            System.out.println("Terminating now.");
            run = false;
        } else {
            System.out.println("Player " + data[0] + " was disqualified.");
        }
    }

    /**
     * Called when the server announces the end of the First phase
     */
    private static void rcvFirstEnd() {
        if (!measureTime)
            System.out.println("Starting Elimination Phase");
        pf.startEliminationPhase();

        //Reset estimated times
        prevTime = new int[maxDepth_TL];
    }

    /**
     * Called when the server announces the end of the Second phase and therefore
     * the end of the game.
     */
    private static void rcvSecondEnd() {
        if (!measureTime)
            System.out.println("Game has ended.");
        run = false;

        //Print some metrics to analyse our estimation
        System.out.println("------------------------------");
        System.out.println("Estimation Test results: #Exceptions: " + numTimeoutExceptions 
            + ", #EstimationAborts: " + numEstimationAborted + ". Fraction (timeout/sum): " 
            + (((double)numTimeoutExceptions) / (numTimeoutExceptions + numEstimationAborted)) 
            + ". Last time limit " + lastTimeLimit + "ms.");
        System.out.println("------------------------------");

    }

    /**
     * This Method handles the logic behind incoming messages.
     * It decodes the Message type and calls the corresponding Subroutine
     * 
     * @throws IOException when an issue occurs with the socket connection
     */
    private static void messageHandler() throws IOException {
        // the read method blocks until data is available, and returns -1 when the
        // connection is closed

        // read message type
        int readLen = input.read(header, 0, 1);
        if (readLen == -1) {
            System.out.println("closed");
            run = false;
            return;
        }

        // read message length
        readLen = input.read(header, 1, 4);
        if (readLen == -1) {
            System.out.println("closed");
            run = false;
            return;
        }

        int msgLength = ByteBuffer.wrap(header, 1, 4).getInt();
        // read meesage payload
        if (msgLength > 0) {
            msg = new byte[msgLength];
            readLen = input.read(msg, 0, msgLength);
            if (readLen == -1) {
                System.out.println("closed");
                run = false;
                return;
            }
        }

        // call subroutine
        switch (header[0]) {
            case (byte) 2:
                rcvMap(msg);
                break;
            case (byte) 3:
                rcvPlayerNumber(msg);
                break;
            case (byte) 4:
                rcvMoveRequest(msg);
                break;
            case (byte) 6:
                rcvMoveAnnouncement(msg);
                break;
            case (byte) 7:
                rcvDisqualification(msg);
                break;
            case (byte) 8:
                rcvFirstEnd();
                break;
            case (byte) 9:
                rcvSecondEnd();
                break;
            default:
                break;
        }
    }

    // METHODS END
    // -------------------------------------------------

    // -------------------------------------------------
    // MAIN START
    /**
     * Can be considered the main method.
     */
    public void run() {

        try {
            server = new InetSocketAddress(host, port);
            connection = new Socket(server.getAddress(), server.getPort());

            input = connection.getInputStream();
            outputU = connection.getOutputStream();
            output = new BufferedOutputStream(outputU);

            // send GroupNumber
            output.write(groupNumberMsg, 0, 6);
            output.flush();

            // for every move we sedn to the server, the header is identical
            ans[0] = (byte) 5;
            ans[1] = (byte) 0;
            ans[2] = (byte) 0;
            ans[3] = (byte) 0;
            ans[4] = (byte) 5;

            while (run) {
                messageHandler();
            }

            input.close();
            output.close();
            outputU.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * deals with JCOmmander logic and calls the run method if our code is to be executed
     * @param args commandline arguments
     */
    public static void main(String args[]) {
        Client client = new Client();
        JCommander jct = JCommander.newBuilder().addObject(client).build();
        jct.parse(args);
        if (client.showHelp) {
            jct.usage();
        } else {
            client.run();
        }

    }
}

// MAIN END
// -------------------------------------------------
