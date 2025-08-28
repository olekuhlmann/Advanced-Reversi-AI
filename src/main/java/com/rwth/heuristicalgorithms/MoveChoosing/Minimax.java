package com.rwth.heuristicalgorithms.MoveChoosing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rwth.heuristicalgorithms.Client.Client;
import com.rwth.heuristicalgorithms.PlayingField.Move;
import com.rwth.heuristicalgorithms.PlayingField.PlayingField;
import com.rwth.heuristicalgorithms.PlayingField.Tile;

/**
 * Contains all minimax related functionality for choosing the next move.
 */
public class Minimax {
    /**
     * Selects the best move according to our heuristic for a given playing field
     * @param pf The PlayingField
     * @param player to select the move for
     * @param depth maximal depth that minimax will use
     * @param pAlpha starting value for alpha bound
     * @param pBeta starting value for beta bound
     * @param timeLimit will not be exceeded 
     * @param prune whether to use alpha-beta pruning or minimax
     * @param activateMoveSorting true if moves will be sorted
     * @return a Move object, null if no moves are possible
     * @throws TimeoutException signals imminient timeout. 
     */
    public static Move selectBestMove(PlayingField pf, byte player, int depth, int pAlpha, int pBeta, int timeLimit, boolean prune, boolean activateMoveSorting) throws TimeoutException{
        return prune ? alphaBeta_getBestMove(pf, player, depth,pAlpha,pBeta, activateMoveSorting):minimax_getBestMove(pf, player, depth);
    }

    /**
     * Returns the first valid move the method finds.
     * The heuristic can fall back to this method if the time limit is almost exceeded to avoid disqualification.
     * @param pf The PlayingField
     * @param player to execute the move for.
     * @return a Move object, null if no moves are possbile
     */
    public static Move selectFirstMove(PlayingField pf, byte player){ 
        if(!pf.getIsEliminationPhase()){
            return selectFirstMove_BuildingPhase(pf, player);
        } else {
            return selectFirstMove_EliminationPhase(pf, player);
        }
    }

    /**
     * Returns the first valid move the method finds for the building phase.
     * @param pf The PlayingField
     * @param player to execute the move for.
     * @return a Move object, null if no moves are possbile
     */
    private static Move selectFirstMove_BuildingPhase(PlayingField pf, byte player){
        List<Move> moves = null;
        try{
            moves = getAllPossibleMoves_Building(pf, player, false);
        } catch (TimeoutException e){
            System.out.println("[ERROR] TimeoutException caught in selectFirstMove_EliminationPhase. This should NOT happen");
            e.printStackTrace();
            System.exit(0);
        }

        return moves == null ? null : moves.get(0);
    }


    /**
     * Returns the first valid move the method finds for the elimination phase.
     * @param pf The PlayingField
     * @param player to execute the move for.
     * @return a Move object, null if no moves are possbile
     */
    private static Move selectFirstMove_EliminationPhase(PlayingField pf, byte player){
        List<Move> moves = null;
        try{
            moves = getAllPossibleMoves_Elimination(pf, player, false);
        } catch (TimeoutException e){
            System.out.println("[ERROR] TimeoutException caught in selectFirstMove_EliminationPhase. This should NOT happen");
            e.printStackTrace();
            System.exit(0);
        }
        return moves == null ? null : moves.get(0);
    }

    /**
     * Computes all the possible moves <i>player</i> can make.
     * Works for both phases of the game.
     * @param pf The PlayingField
     * @param player to compute all possible moves for.
     * @param checkForTime is this is set to true, the method will pay attention to and not exceed the time limit set in Client
     * @return a List of Moves. Null if no moves are possible
     * @throws TimeoutException incase the time limit is reached
     */ 
    public static List<Move> getAllPossibleMoves(PlayingField pf, byte player, boolean checkForTime) throws TimeoutException{
        return pf.getIsEliminationPhase() ? getAllPossibleMoves_Elimination(pf, player, checkForTime) : getAllPossibleMoves_Building(pf, player, checkForTime);
    }

    
    /**
     * Computes all the possible moves <i>player</i> can make in the building phase.
     * Works for both phases of the game.
     * @param pf The PlayingField
     * @param player to compute all possible moves for.
     * @param checkForTime is this is set to true, the method will pay attention to and not exceed the time limit set in Client
     * @return a List of Moves. Null if no moves are possible
     */
    private static List<Move> getAllPossibleMoves_Building(PlayingField pf, byte player, boolean checkForTime) throws TimeoutException{
        //This method is a variant of the calcWeightedSumMoves. However, this is NOT redundant code. 
        //For calcWeightedSumMoves, bonus and choice moves only have to be counted once.

        List<Move> moves = new ArrayList<>();

        //check ov. stone count
        boolean overrideAvailable = pf.getPlayerOverrideStoneCount(player) > 0;

        //traverse the whole map:
        for(byte y = 0; y < PlayingField.getMapHeight(); y++){

            //check for time
            if(checkForTime && System.nanoTime()/1_000_000 >= Client.returnTime){
                throw new TimeoutException();
            }

            for(byte x = 0; x < PlayingField.getMapWidth(); x++){
                Tile tile = pf.getMapTile(y, x);
                //first, check if the tile is empty. If yes, a non-override move can be performed.
                if(!tile.equals(Tile.HOLE) && (tile.equals(Tile.EMPTY) || tile.equals(Tile.BONUS) || tile.equals(Tile.CHOICE) || tile.equals(Tile.INVERSION))){ //hole check first for more efficiency
                    //check if a foreign stone would be enclosed in any direction
                    List<byte[]> enclosed = pf.getEnclosedStones(y, x, player);
                    if(enclosed != null){
                        //add possible move
                        switch(tile){
                            case EMPTY -> moves.add(Move.createDefaultMove(y, x));
                            case BONUS -> {moves.add(Move.createBonusMove(y, x, false)); moves.add(Move.createBonusMove(y, x, true));}
                            case INVERSION -> moves.add(Move.createDefaultMove(y, x));
                            case CHOICE -> {
                                for(byte i = 1; i <= PlayingField.getPlayerCount(); i++){
                                    //we can only swap with non-disqualified players
                                    if(!pf.isDisqualified(i)) moves.add(Move.createChoiceMove(y, x, i));
                                }
                            }
                            default -> throw new IllegalStateException();
                        }
                    }
                    
                } else if(!tile.equals(Tile.HOLE) && overrideAvailable){//in any other case that is not a hole, the tile is occupied and an override move can potentially be performed.
                    List<byte[]> enclosed = pf.getEnclosedStones(y, x, player);
                    if(enclosed != null || tile.equals(Tile.EXPANSION)){//expansion moves do not have to enclose a stone
                        moves.add(Move.createOverrideMove(y, x));
                    }
                }
            }
        }

        return moves.size() == 0 ? null : moves;
    }

    /**
     * Computes all the possible moves <i>player</i> can make in the elimination phase.
     * @param pf The PlayingField
     * @param player to compute all possible moves for.
     * @param checkForTime is this is set to true, the method will pay attention to and not exceed the time limit set in Client.
     * @return a List of Moves. Null if no moves are possible
     */
    private static List<Move> getAllPossibleMoves_Elimination(PlayingField pf, byte player, boolean checkForTime) throws TimeoutException{
        List<Move> moves = new ArrayList<>();

        //player has to have bombs left
        if(pf.getPlayerBombCount((byte)(player)) > 0){
            //all holes can be bombed
            for(byte y = 0; y < PlayingField.getMapHeight(); y++){

                //check for time
                if(checkForTime && System.nanoTime()/1_000_000 >= Client.returnTime){
                    throw new TimeoutException();
                }

                for(byte x = 0; x < PlayingField.getMapWidth(); x++){
                    if(!pf.getMapTile(y, x).equals(Tile.HOLE)){
                        moves.add(Move.createBombMove(y, x));
                    }
                }
            }
        }   
        
        //return null if no moves are possible
        return moves.size() == 0 ? null : moves;
    }

    /**
     * Uses the miximax algorithm to compute the best move for the specified player. 
     * This method assumes that it is currently <i>player</i>'s move.
     * Minimax will NOT compute moves into the next phase. If the current phase is building, only moves in this phase will be considered.
     * @param pf The PlayingField
     * @param player to compute the best move for
     * @param depth maximal depth the algorithm will use. Depth = 0 means that only the next possible move of <i>player</i> is considered. For depth = 1, the move of the player after that will also be considered, ... 
     * @return a Move object. Null if no move is possible
     */
    private static Move minimax_getBestMove(PlayingField pf, byte player, int depth){
        //Get all possible moves
        List<Move> moves = null;
        try{
            moves = getAllPossibleMoves(pf, player, false);
        } catch(TimeoutException e){
            System.out.println("[ERROR] TimeoutException caught in selectFirstMove_EliminationPhase. This should NOT happen");
            e.printStackTrace();
            System.exit(0);
        }
        
        if(moves == null) return null;


        Move bestMove = null;
        int bestMoveValue = 0;

        //Calculate turn of next player
        byte next_player = getNextPlayer(pf, player);

        //Test for all possible moves and pick the best one
        for(Move move : moves){
            PlayingField move_pf = Move.simulateMove(move, player, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in minmax: Heuristic.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = minimaxValue(move_pf, player, next_player, depth-1);

            if(bestMove == null || move_value > bestMoveValue){
                bestMoveValue = move_value;
                bestMove = move;
            }
        }
        return bestMove;
    }

    /**
     * Uses the miximax algorithm to determine the heuristic value of a given playingfield by simulating <i>depth</i> moves into the future.
     * This version uses the PARANOID assumption (i.e., every player tries to minimize the heuristic value of <i>player</i>)
     * @param pf The PlayingField.
     * @param player to compute the best move for.
     * @param currentPlayer player whose turn it is in the state of PlayingField.
     * @param depth maximal depht the method will use.
     * @return the heuristic rating of the PlayingField by simulating <i>depth</i> moves into the future.
     */
    public static int minimaxValue(PlayingField pf, byte player, byte currentPlayer, int depth){
        if(depth <= 0){
            if(Client.measureTime) Client.unprunedEvalCount++;
            //since this is paranoid, we will consider the heuristic rating for player (not currentPlayer)
            return pf.getIsEliminationPhase() ? MoveHeuristic.evalBoard_Elimination(pf, player) : MoveHeuristic.evalBoard_Building(pf, player);
        }
        //determine whether currentPlayer is min or max
        //it is a max move if the specified player is the player whose turn it is. Min move otherwise
        boolean isMax = player == currentPlayer;

        //Get all possible moves
        List<Move> moves = null;
        try{
            moves = getAllPossibleMoves(pf, player, false);
        } catch(TimeoutException e){
            System.out.println("[ERROR] TimeoutException caught in selectFirstMove_EliminationPhase. This should NOT happen");
            e.printStackTrace();
            System.exit(0);
        }


        //Calculate turn of next player
        byte next_player = getNextPlayer(pf, player);


        //incase no move is possible, we consider the next player since currentPlayer will be skipped
        if(moves == null){
            return minimaxValue(pf, player, next_player, depth-1);
        }

        int currentMoveValue = 0;
        Move bestMove = null;

        //Test for all possible moves and pick the worst/best one, depending on mix/max
        for(Move move : moves){
            PlayingField move_pf = Move.simulateMove(move, currentPlayer, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in minmax: Heuristic.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = minimaxValue(move_pf, player, next_player, depth-1);

            if(isMax){
                //we maximize the value
                if(bestMove == null || move_value > currentMoveValue){
                    currentMoveValue = move_value;
                }
            } else {
                //we minimize the value
                if(bestMove == null || move_value < currentMoveValue){
                    currentMoveValue = move_value;
                }
            }
        }
        return currentMoveValue;
    }

    /**
     * Uses alpha-beta pruning to compute the best move for the specified player. 
     * works analogously to minimax
     * @param pf The PlayingField
     * @param player to compute the best move for
     * @param depth maximal depth the algorithm will use. Depth = 0 means that only the next possible move of <i>player</i> is considered. For depth = 1, the move of the player after that will also be considered, ...
     * @param pAlpha current value for alpha
     * @param pBeta current value for beta 
     * @param activateMoveSorting true if moves will be sorted
     * @return a Move object. Null if no move is possible
     * @throws TimeoutException when our time expires. Is catched in calMove. Simply a means to exit the recursive stack
     */
    public static Move alphaBeta_getBestMove(PlayingField pf, byte player, int depth, int pAlpha, int pBeta,boolean activateMoveSorting) throws TimeoutException{
        
        if(System.nanoTime()/1_000_000 >= Client.returnTime){
            throw new TimeoutException();
        }
        //initialize tiht +infinity and -infinity
        int alpha = pAlpha;
        int beta = pBeta;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, player, true);

        if(moves == null) return null;


        //PlayingFields generated when simulating each possible move
        Map<Move,PlayingField> fields = new HashMap<Move,PlayingField>();

        //only do move sorting when depth >= 3
        boolean doMoveSorting = depth >= 3 && activateMoveSorting;
        if(doMoveSorting){
            //this map contains the value used for move sorting; high value -> branch this move first
            Map<Move,Integer> val = new HashMap<Move,Integer>();
            for(Move m : moves){
                fields.put(m, Move.simulateMove(m,player,pf));
                val.put(m,MoveSortHeuristic.evaluateMove_MoveSorting(m));
            }
            moves.sort((m1,m2)->(val.get(m1).compareTo(val.get(m2))));        
            Collections.reverse(moves); 
            
        }

        

        Move bestMove = null;
        int bestMoveValue = 0;

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);
        Move[] dummy = {};
        Move[] moveArray = moves.toArray(dummy);
        moves = null;

        //Test for all possible moves and pick the best one
        for(int i = 0; i< moveArray.length; i++){
            PlayingField move_pf = doMoveSorting ? fields.get(moveArray[i]) : Move.simulateMove(moveArray[i],player,pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in minmax: Heuristic.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = alphaBeta_value(move_pf, player, next_player, depth-1, alpha, beta, activateMoveSorting);
            if(move_value>alpha) {
                alpha = move_value;
            }

            if(bestMove == null || move_value > bestMoveValue){
                bestMoveValue = move_value;
                bestMove = moveArray[i];
                bestMove.value = bestMoveValue;
            }
            moveArray[i] = null;
        }
        moveArray = null;
        return bestMove;
    } 
    
    /**
     * Uses alpha-beta pruning to determine the heuristic value of a given playingfield by simulating <i>depth</i> moves into the future.
     * This version uses the PARANOID assumption (i.e., every player tries to minimize the heuristic value of <i>player</i>)
     * Works anlogously to minimax
     * @param pf The PlayingField.
     * @param player to compute the best move for.
     * @param currentPlayer player whose turn it is in the state of PlayingField.
     * @param depth maximal depht the method will use.
     * @param pAlpha current value for alpha
     * @param pBeta current value for beta
     * @param activateMoveSorting true if moves will be sorted
     * @return the heuristic rating of the PlayingField by simulating <i>depth</i> moves into the future.
     * @throws TimeoutException when our time expires. Is catched in calMove. Simply a means to exit the recursive stack
     */
    public static int alphaBeta_value(PlayingField pf, byte player, byte currentPlayer, int depth, int pAlpha, int pBeta,boolean activateMoveSorting) throws TimeoutException{
        
        if(System.nanoTime()/1_000_000 >= Client.returnTime){
            throw new TimeoutException();
        }

        if(depth <= 0){
            Client.prunedEvalCount++;
            //since this is paranoid, we will consider the heuristic rating for player (not currentPlayer)
            return pf.getIsEliminationPhase() ? MoveHeuristic.evalBoard_Elimination(pf, player) : MoveHeuristic.evalBoard_Building(pf, player);
        }
       
        int alpha = pAlpha;
        int beta = pBeta;

        boolean isMax = player == currentPlayer;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, currentPlayer, true);
         

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);

        //incase no move is possible, we consider the next player since currentPlayer will be skipped
        if(moves == null){
            return alphaBeta_value(pf, player, next_player, depth-1, alpha, beta, activateMoveSorting);
        }
        
        //only do move sorting for depths >= 3
        if(depth >= 3 && activateMoveSorting) {
            
            Map<Move,Integer> val = new HashMap<Move,Integer>();
            for(Move m : moves){
                val.put(m,MoveSortHeuristic.evaluateMove_MoveSorting(m));
            }
            moves.sort((m1,m2)->(val.get(m1).compareTo(val.get(m2))));
            if(isMax){
                Collections.reverse(moves);
            }        
        }
             

        int currentMoveValue = 0;
        Move bestMove = null;

        Move[] dummy = {};
        Move[] moveArray = moves.toArray(dummy);
        moves = null;

        //Test for all possible moves and pick the worst/best one, depending on mix/max
        for(int i = 0; i< moveArray.length; i++){
            PlayingField move_pf = Move.simulateMove(moveArray[i], currentPlayer, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in alpha-beta-pruning: Heuristic.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = alphaBeta_value(move_pf, player, next_player, depth-1, alpha, beta, activateMoveSorting);

            if(isMax){
                //we maximize the value
                if(bestMove == null || move_value > currentMoveValue){
                    currentMoveValue = move_value;
                }

                if(move_value>beta || (Client.experimental != 0 && move_value < alpha)){
                    break;
                }else if(move_value>alpha) {
                    alpha = move_value;
                }
            } else {
                //we minimize the value
                if(bestMove == null || move_value < currentMoveValue){
                    currentMoveValue = move_value;
                }
                if(move_value<alpha|| (Client.experimental != 0 && move_value > beta)){
                    break;
                }else if(move_value<beta) {
                    beta = move_value;
                }
            }
            moveArray[i] = null;
        }
        moveArray = null;
        return currentMoveValue;
    }

    /**
     * Computes the player whose turn it is after the specified player
     * @param pf The PlayingField
     * @param player the player we want to know the successor of
     * @return the first non-disqualified player after <i>player</i>
     */
    public static byte getNextPlayer(PlayingField pf, byte player){
        byte next_player = player;
        do{
            next_player = (next_player+1) == PlayingField.getPlayerCount()+1 ? (byte)1 : (byte)(next_player+1);
        } while (pf.isDisqualified(next_player));

        return next_player;
    }
}
