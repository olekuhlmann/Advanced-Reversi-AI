package com.rwth.heuristicalgorithms.MoveChoosing;

import static org.junit.Assume.assumeTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.rwth.heuristicalgorithms.Client.Client;
import com.rwth.heuristicalgorithms.PlayingField.Move;
import com.rwth.heuristicalgorithms.PlayingField.PlayingField;

/**
 * This test class is used to evaluate different move sorting heuristics.
 * For every heuristic, we use a number of different maps (see very bottom) for evaluation.
 * We consider the time usage with no move sorting enabled (base value). 
 * Then we consider the reduction in time (in percent) when using the move sorting heuristic.
 * To check the sanity of the heuristic, we als consider the change in time when using the inverse of the heuristic.  
 */
public class MoveSortingAnalysis {
    private final boolean ACTIVATE_ANALYSIS = false;

    @Test
    public void moveSortAnalysis(){
        assumeTrue(ACTIVATE_ANALYSIS);

        
        String[] maps = {map1, map2, map3, map4, map5, map6, map7, map14, map8, map15, map9, map10, map11};
        int[][] map_depth_num = {{3,2}, {3,100}, {3,1}, {3, 1000}, {3, 1000}, {3, 10}, {4,20}, {5,5}, {6,100}, {4,100}, {6, 30}, {6, 10}, {7,20}};

        
        for(int i = 0; i <= maps.length; i++){
            String map = maps[i];
            int depth = map_depth_num[i][0];
            int num = map_depth_num[i][1];

            PlayingField pf = PlayingField.readMap(map);

            System.out.println("----------------------------------------");
            System.out.println("Now analysing map" + i + " with depth " + depth + " and averaging over " + num + " runs.");



            //With no move sorting

            System.out.print("Time usage with no move sorting activated: ");
            long start = System.nanoTime();
            try{
                for(int run = 0; run < num; run++){
                    ab_no_sorting(pf, (byte)1, depth, 1000000);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            long end = System.nanoTime();

            long timeNoSorting = (long)(((end-start)/1_000_000.0)/num);

            System.out.println(timeNoSorting + "ms");

            System.out.println("---");



            
            for(int metric = 1; metric <= maxMetric; metric++){
                System.out.println("---");
                System.out.println("Now using metric " + metric);
                
                //With move sorting

                System.out.print("Time usage with move sorting activated: ");
                start = System.nanoTime();
                try{
                    for(int run = 0; run < num; run++){
                        ab_with_sorting(pf, (byte)1, depth, 1000000, metric, true);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                end = System.nanoTime();
                long totalTime = (long)(((end-start)/1_000_000.0)/num);
                System.out.print(totalTime + "ms");
                if(totalTime <= timeNoSorting){
                    System.out.println(" (" + (int)(100-((totalTime/(double)timeNoSorting)*100)) + "% faster than no sorting)");
                } else {
                    System.out.println(" (" + (int)(100-((timeNoSorting/(double)totalTime)*100)) + "% slower than no sorting)");
                }

                System.out.println("---");


                //With reverse move sorting

                System.out.print("Time usage with reverse move sorting activated: ");
                start = System.nanoTime();
                try{
                    for(int run = 0; run < num; run++){
                        ab_reverse_sorting(pf, (byte)1, depth, 1000000, metric);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                end = System.nanoTime();
                totalTime = (long)(((end-start)/1_000_000.0)/num);
                System.out.print(totalTime + "ms");
                if(totalTime <= timeNoSorting){
                    System.out.println(" (" + (int)(100-((totalTime/(double)timeNoSorting)*100)) + "% faster than no sorting)");
                } else {
                    System.out.println(" (" + (int)(100-((timeNoSorting/(double)totalTime)*100)) + "% slower than no sorting)");
                }
            }

            System.out.println("----------------------------------------");
        }
        
    }





    private static int maxMetric = 9;

    private static int evaluateMove_MoveSorting(Move m, byte player, int sortingMetric){
        return switch(sortingMetric){
            //case 1 -> evaluateMove_MoveSorting_TileScore(pf, m, player);
            case 2 -> evaluateMove_MoveSorting_OverrideLast(m);
            case 3 -> evaluateMove_MoveSorting_BonusFirst(m);
            case 4 -> evaluateMove_MoveSorting_ChoiceFirst(m);
            case 5 -> evaluateMove_MoveSorting_OverrideLast_BonusLast_choiceFirst(m);
            //case 6 -> evaluateMove_DefaultHeuristic(pf, m, player);
            case 7 -> evaluateMove_MoveSorting__BonusLast_choiceFirst(m);
            case 8 -> evaluateMove_MoveSorting_OverrideLast_BonusLast(m);
            case 9 -> evaluateMove_MoveSorting_OverrideLast_choiceFirst(m);
            default -> throw new IllegalStateException("[ERROR] The parameter sortingMetric was set to the illegal value " + sortingMetric + " in Move sorting analysis test.");
        };
    }

    /**
     * Evaluates a move for move sorting using the tilescore metric saved in the PlayingField.
     * @param pf PlayingField generated when simulating the move
     * @param m The Move
     * @param player to evaluate for
     * @return pf.getTileScore(player)
     */
    /* private static int evaluateMove_MoveSorting_TileScore(PlayingField pf, Move m, byte player){
        return pf.getTileScore(player);
    } */

    /**
     * Evaluates a move for move sorting by putting override moves last.
     * @param m The Move
     * @return 0 if m is an override move, 1 else
     */
    private static int evaluateMove_MoveSorting_OverrideLast(Move m){
        return switch(m.getMoveType()){
            case OVERRIDE -> 0;
            default -> 1;
        };
    }

    private static int evaluateMove_MoveSorting_ChoiceFirst(Move m){
        return switch(m.getMoveType()){
            case CHOICE -> 1;
            default -> 0;
        };
    }

    private static int evaluateMove_MoveSorting_BonusFirst(Move m){
        return switch(m.getMoveType()){
            case BONUS -> 1;
            default -> 0;
        };
    }

    private static int evaluateMove_MoveSorting_OverrideLast_BonusLast_choiceFirst(Move m){
        return switch(m.getMoveType()){
            case CHOICE -> 1;
            case OVERRIDE -> -2;
            case BONUS -> -1;
            default -> 0;
        };
    }

    /* private static int evaluateMove_DefaultHeuristic(PlayingField pf, Move m, byte player){
        return Heuristic.evalBoard_Building(pf, player);
    }
 */
    private static int evaluateMove_MoveSorting__BonusLast_choiceFirst(Move m){
        return switch(m.getMoveType()){
            case CHOICE -> 1;
            case BONUS -> -1;
            default -> 0;
        };
    }

    private static int evaluateMove_MoveSorting_OverrideLast_BonusLast(Move m){
        return switch(m.getMoveType()){
            case OVERRIDE -> -2;
            case BONUS -> -1;
            default -> 0;
        };
    }

    private static int evaluateMove_MoveSorting_OverrideLast_choiceFirst(Move m){
        return switch(m.getMoveType()){
            case CHOICE -> 1;
            case OVERRIDE -> -1;
            default -> 0;
        };
    }






















    // METHODS FOR MOVE SORTING

    private static Move ab_with_sorting(PlayingField pf, byte player, int depth, int timeLimit, int sortingMetric, boolean activateMoveSorting) throws TimeoutException{
        
        
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, player, false);

        
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
                val.put(m,evaluateMove_MoveSorting(m, player, sortingMetric));
            }
            moves.sort((m1,m2)->(val.get(m1).compareTo(val.get(m2))));        
            Collections.reverse(moves); 
        }

        

        Move bestMove = null;
        int bestMoveValue = 0;

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);

        //Test for all possible moves and pick the best one
        for(Move m : moves){
            PlayingField move_pf = doMoveSorting ? fields.get(m) : Move.simulateMove(m,player,pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in minmax: Heuristic.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = ab_value_with_sorting(move_pf, player, next_player, depth-1, timeLimit, alpha, beta, sortingMetric, activateMoveSorting);
            if(move_value>alpha) {
                alpha = move_value;
            }

            if(bestMove == null || move_value > bestMoveValue){
                bestMoveValue = move_value;
                bestMove = m;
            }
        }
        return bestMove;
    } 

    private static int ab_value_with_sorting(PlayingField pf, byte player, byte currentPlayer, int depth, int timeLimit, int pAlpha, int pBeta, int sortingMetric, boolean activateMoveSorting) throws TimeoutException{
        
        if(depth <= 0){
            if(Client.measureTime) Client.prunedEvalCount++;
            //since this is paranoid, we will consider the heuristic rating for player (not currentPlayer)
            return pf.getIsEliminationPhase() ? MoveHeuristic.evalBoard_Elimination(pf, player) : MoveHeuristic.evalBoard_Building(pf, player);
        }
       
        int alpha = pAlpha;
        int beta = pBeta;

        
        boolean isMax = player == currentPlayer;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, currentPlayer, false);
         

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);


        //incase no move is possible, we consider the next player since currentPlayer will be skipped
        if(moves == null){
            return ab_value_with_sorting(pf, player, next_player, depth-1, timeLimit, alpha, beta, sortingMetric, activateMoveSorting);
        }
        
        //only do move sorting for depths >= 3
        if(depth >= 3 && activateMoveSorting) {
            
            Map<Move,Integer> val = new HashMap<Move,Integer>();
            for(Move m : moves){
                val.put(m,evaluateMove_MoveSorting(m, player, sortingMetric));
            }
            moves.sort((m1,m2)->(val.get(m1).compareTo(val.get(m2))));
            if(isMax){
                Collections.reverse(moves);
            }        
        }
             

        int currentMoveValue = 0;
        Move bestMove = null;

        //Test for all possible moves and pick the worst/best one, depending on mix/max
        for(Move m : moves){
            PlayingField move_pf = Move.simulateMove(m, currentPlayer, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in alpha-beta-pruning: Heuristic.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = ab_value_with_sorting(move_pf, player, next_player, depth-1, timeLimit, alpha, beta, sortingMetric, activateMoveSorting);

            if(isMax){
                //we maximize the value
                if(bestMove == null || move_value > currentMoveValue){
                    currentMoveValue = move_value;
                }

                if(move_value>beta){
                    break;
                }else if(move_value>alpha) {
                    alpha = move_value;
                }
            } else {
                //we minimize the value
                if(bestMove == null || move_value < currentMoveValue){
                    currentMoveValue = move_value;
                }
                if(move_value<alpha){
                    break;
                }else if(move_value<beta) {
                    beta = move_value;
                }
            }
        }
        return currentMoveValue;
    }






    //METHODS FOR REVERSE MOVE SORTING




    private static Move ab_reverse_sorting(PlayingField pf, byte player, int depth, int timeLimit, int sortingMetric) throws TimeoutException{
        
        
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, player, false);

        
        if(moves == null) return null;


        //PlayingFields generated when simulating each possible move
        Map<Move,PlayingField> fields = new HashMap<Move,PlayingField>();
        //this map contains the value used for move sorting; high value -> branch this move first
        Map<Move,Integer> val = new HashMap<Move,Integer>();
        for(Move m : moves){
            fields.put(m, Move.simulateMove(m,player,pf));
            val.put(m,evaluateMove_MoveSorting(m, player, sortingMetric));
        }
        moves.sort((m1,m2)->(val.get(m1).compareTo(val.get(m2))));        
        //---REVERSE SORTING---Collections.reverse(moves); 

        Move bestMove = null;
        int bestMoveValue = 0;

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);

        //Test for all possible moves and pick the best one
        for(Move m : moves){
            PlayingField move_pf = fields.get(m);//Move.simulateMove(m, player, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in minmax: Minimax.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = ab_value_reverse_sorting(move_pf, player, next_player, depth-1, timeLimit, alpha, beta, sortingMetric);
            if(move_value>alpha) {
                alpha = move_value;
            }

            if(bestMove == null || move_value > bestMoveValue){
                bestMoveValue = move_value;
                bestMove = m;
            }
        }
        return bestMove;
    } 

    private static int ab_value_reverse_sorting(PlayingField pf, byte player, byte currentPlayer, int depth, int timeLimit, int pAlpha, int pBeta, int sortingMetric) throws TimeoutException{
        
        if(depth <= 0){
            if(Client.measureTime) Client.prunedEvalCount++;
            //since this is paranoid, we will consider the heuristic rating for player (not currentPlayer)
            return pf.getIsEliminationPhase() ? MoveHeuristic.evalBoard_Elimination(pf, player) : MoveHeuristic.evalBoard_Building(pf, player);
        }
       
        int alpha = pAlpha;
        int beta = pBeta;

        
        boolean isMax = player == currentPlayer;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, currentPlayer, false);
         

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);


        //incase no move is possible, we consider the next player since currentPlayer will be skipped
        if(moves == null){
            return ab_value_reverse_sorting(pf, player, next_player, depth-1, timeLimit, alpha, beta, sortingMetric);
        }
        Map<Move,PlayingField> fields = new HashMap<Move,PlayingField>();
        
        if(depth >=3) {
            
            Map<Move,Integer> val = new HashMap<Move,Integer>();
            for(Move m : moves){
                fields.put(m, Move.simulateMove(m,currentPlayer,pf));
                val.put(m,evaluateMove_MoveSorting(m, player, sortingMetric));

            //i++;
            }
            moves.sort((m1,m2)->(val.get(m1).compareTo(val.get(m2))));        
            //---REVERSE SORTING---: isMax -> !isMax
            if(!isMax){
                Collections.reverse(moves);
            }
        } else{
        }
             

        int currentMoveValue = 0;
        Move bestMove = null;

        //Test for all possible moves and pick the worst/best one, depending on mix/max
        for(Move m : moves){
            PlayingField move_pf = depth>=3?fields.get(m):Move.simulateMove(m, currentPlayer, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in alpha-beta-pruning: Minimax.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = ab_value_reverse_sorting(move_pf, player, next_player, depth-1, timeLimit, alpha, beta, sortingMetric);

            if(isMax){
                //we maximize the value
                if(bestMove == null || move_value > currentMoveValue){
                    currentMoveValue = move_value;
                }

                if(move_value>beta){
                    break;
                }else if(move_value>alpha) {
                    alpha = move_value;
                }
            } else {
                //we minimize the value
                if(bestMove == null || move_value < currentMoveValue){
                    currentMoveValue = move_value;
                }
                if(move_value<alpha){
                    break;
                }else if(move_value<beta) {
                    beta = move_value;
                }
            }
        }
        return currentMoveValue;
    }








    //METHODS FOR NO MOVE SORTING




    private static Move ab_no_sorting(PlayingField pf, byte player, int depth, int timeLimit) throws TimeoutException{
        
        
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, player, false);

        
        if(moves == null) return null;


        //PlayingFields generated when simulating each possible move
        Map<Move,PlayingField> fields = new HashMap<Move,PlayingField>();
        //this map contains the value used for move sorting; high value -> branch this move first
        
        for(Move m : moves){
            fields.put(m, Move.simulateMove(m,player,pf));
            
        }

        Move bestMove = null;
        int bestMoveValue = 0;

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);

        //Test for all possible moves and pick the best one
        for(Move m : moves){
            PlayingField move_pf = fields.get(m);//Move.simulateMove(m, player, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in minmax: Minimax.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = ab_value_no_sorting(move_pf, player, next_player, depth-1, timeLimit, alpha, beta);
            if(move_value>alpha) {
                alpha = move_value;
            }

            if(bestMove == null || move_value > bestMoveValue){
                bestMoveValue = move_value;
                bestMove = m;
            }
        }
        return bestMove;
    } 

    private static int ab_value_no_sorting(PlayingField pf, byte player, byte currentPlayer, int depth, int timeLimit, int pAlpha, int pBeta) throws TimeoutException{
        
        if(depth <= 0){
            if(Client.measureTime) Client.prunedEvalCount++;
            //since this is paranoid, we will consider the heuristic rating for player (not currentPlayer)
            return pf.getIsEliminationPhase() ? MoveHeuristic.evalBoard_Elimination(pf, player) : MoveHeuristic.evalBoard_Building(pf, player);
        }
       
        int alpha = pAlpha;
        int beta = pBeta;

        
        boolean isMax = player == currentPlayer;

        //Get all possible moves
        List<Move> moves = Minimax.getAllPossibleMoves(pf, currentPlayer, false);
         

        //Calculate turn of next player
        byte next_player = Minimax.getNextPlayer(pf, player);


        //incase no move is possible, we consider the next player since currentPlayer will be skipped
        if(moves == null){
            return ab_value_no_sorting(pf, player, next_player, depth-1, timeLimit, alpha, beta);
        }
        Map<Move,PlayingField> fields = new HashMap<Move,PlayingField>();
        if(depth >=3) {
            
            for(Move m : moves){
                fields.put(m, Move.simulateMove(m,currentPlayer,pf));

            //i++;
            }
        } 
             

        int currentMoveValue = 0;
        Move bestMove = null;

        //Test for all possible moves and pick the worst/best one, depending on mix/max
        for(Move m : moves){
            PlayingField move_pf = depth>=3?fields.get(m):Move.simulateMove(m, currentPlayer, pf);
            if(move_pf == null){
                throw new IllegalStateException("[ERROR] in alpha-beta-pruning: Heuristic.getAllPossibleMoves yielded a move that is illegal according to Move.simulateMove");
            }
            int move_value = ab_value_no_sorting(move_pf, player, next_player, depth-1, timeLimit, alpha, beta);

            if(isMax){
                //we maximize the value
                if(bestMove == null || move_value > currentMoveValue){
                    currentMoveValue = move_value;
                }

                if(move_value>beta){
                    break;
                }else if(move_value>alpha) {
                    alpha = move_value;
                }
            } else {
                //we minimize the value
                if(bestMove == null || move_value < currentMoveValue){
                    currentMoveValue = move_value;
                }
                if(move_value<alpha){
                    break;
                }else if(move_value<beta) {
                    beta = move_value;
                }
            }
        }
        return currentMoveValue;
    }






    //Maps used in evaluation:
    /*We tried to create a representative set of maps. 
    * All special tiles are considered. 
    * Various game stages are incorporated.
    * Special Transitions are not considered since we abstract them anyways.
    * Various number of players are considered.
    * Different sizes of maps are considerd.
    * Different depth limits are used. Time limits are not used since we want to measure the time used for a fixed depth.
    */
    /**
     * Random 50x50 map with 8 players and many special tiles. Few consecutive lines of stones.
     */
    private static String map1 = 
        """
        8
        42
        39 0
        50 50
        4 7 0 0 c 0 3 0 5 1 1 7 7 - b 1 c 2 c 8 6 c x i 4 i 6 c - x 0 7 1 i i 0 1 5 i 2 0 i 4 4 5 i 0 5 0 i 
        0 8 8 4 c 0 b 8 4 0 0 i 8 0 5 0 1 7 b x 0 c 5 0 i 0 2 0 0 0 8 x 5 0 x 0 x x 7 8 6 3 0 3 0 x 0 3 3 0 
        i c i c i 3 4 x 6 - i 0 5 3 0 0 2 2 5 c 0 5 0 5 c c 6 i c 7 0 4 c c 8 4 0 0 0 0 4 4 x 0 5 c - 5 0 b 
        i 3 - 0 - - 4 c 6 0 0 0 i 0 b 0 0 x b c - 3 - 6 0 0 - b 0 3 0 - - 0 0 0 7 0 0 6 3 0 2 0 2 0 0 0 0 8 
        0 0 - c 0 1 2 0 2 i 0 0 6 0 2 0 0 0 c 7 0 3 c 5 7 0 3 0 1 6 2 2 6 0 0 5 4 7 b 5 2 0 0 6 0 0 3 6 0 0 
        5 7 0 0 b 5 4 0 0 0 6 0 5 b 2 2 5 0 1 0 7 0 i 0 4 c - 5 7 - i 5 x 7 0 0 0 8 1 3 - 0 7 4 1 0 8 x c 3 
        0 c 0 5 7 7 4 1 0 8 0 i 0 i 2 8 0 4 3 - 2 7 0 0 7 0 3 4 b 6 7 6 x 1 0 1 8 c - 2 c 2 0 4 4 - - 0 4 5 
        0 1 0 0 1 0 4 1 5 b 7 2 1 0 7 1 8 8 0 7 8 5 b 3 - 0 0 5 - x i 1 b c 8 3 c 1 0 0 c 2 0 - 0 0 0 0 b 5 
        5 3 0 c 0 0 i b 5 0 i 0 0 b 3 3 3 1 0 - 5 i 0 2 0 - 6 0 3 0 2 0 i x 3 5 8 x 3 8 8 x x 4 1 7 0 b 4 0 
        c 0 8 3 i 4 0 5 0 - 0 0 b b - 0 - 5 7 i 0 i 0 4 c 5 i 3 4 - 0 0 6 b - 1 4 0 0 - i 3 1 3 5 i b 0 - 8 
        5 4 x 0 2 - 0 - 2 7 i i 5 1 7 x b b 3 0 8 0 5 6 - 0 6 0 3 - c 0 c 0 0 3 1 i 5 3 i b 0 0 0 0 0 5 0 0 
        3 6 6 4 0 0 x 4 i 8 0 0 0 7 8 6 - 0 0 2 3 0 i 4 0 2 0 7 0 c 8 3 i 0 0 0 i 6 i 1 6 x 0 6 5 0 5 0 3 2 
        0 6 b 0 0 0 2 5 2 0 6 0 x x 0 5 0 2 0 8 b 4 b 5 0 - 0 i 0 x 5 0 6 x 0 0 2 0 0 0 0 8 - i 5 0 0 0 3 4 
        5 6 x c 1 2 0 8 0 c x c 4 6 1 x 8 6 0 b 0 0 7 0 x 0 0 0 6 1 0 0 1 3 0 2 i c 1 0 6 1 - 0 8 0 7 5 2 7 
        0 c 6 3 0 0 0 1 i 0 7 0 0 5 3 3 7 0 5 0 0 1 - 3 5 7 2 0 0 0 8 0 4 0 5 0 - 0 0 0 b 0 c 0 c b b 7 b c 
        6 b 1 0 8 0 3 5 3 0 2 3 5 0 0 0 i c i 0 x 4 - c 0 0 3 0 7 8 0 7 3 0 0 0 1 0 2 0 0 x 4 0 3 3 0 c 0 0 
        c 6 3 0 0 i 3 0 0 8 1 3 0 7 2 0 c 0 - 8 4 5 4 i 3 - b x 0 8 5 4 - x 0 0 7 3 8 x 8 0 - 0 8 1 5 5 0 x 
        6 0 i 0 0 8 0 0 5 c 0 0 4 c 0 b 8 5 - 7 i 5 3 4 2 0 3 c i 0 7 1 4 7 6 0 1 0 0 0 3 0 i 4 c 1 2 0 0 0 
        3 - 3 2 1 b 0 8 i 3 - 6 - 6 8 0 i b 4 6 x 8 7 1 1 i 1 1 c 7 b 0 8 x 1 7 0 0 x 2 0 - i 0 7 8 0 i 8 0 
        4 i - 0 2 2 - 7 c 6 2 2 1 2 8 2 1 0 c 0 5 0 8 1 b 8 7 0 4 - c 6 0 0 0 1 0 0 3 2 3 x 3 b x 0 - x 7 0 
        c 0 0 x 7 0 x 0 - 0 x i 1 b 0 - i 8 i 0 - 0 7 1 4 0 2 x 2 4 b 2 x 0 - 4 4 - b 0 c i - 4 0 6 2 i - 0 
        x i 1 3 c 6 0 1 6 4 5 6 5 1 2 - 2 7 3 5 3 7 0 0 0 0 x 3 i 4 b 0 7 8 3 6 1 8 7 1 0 c 0 0 4 5 4 c 0 0 
        1 3 2 0 5 0 5 0 0 3 0 x c 1 7 3 1 - 0 0 x b 6 0 0 2 0 7 2 0 3 0 1 - 2 4 4 5 0 1 0 5 3 0 0 0 0 7 5 6 
        7 - 8 3 2 b 5 - c 6 3 i i 3 8 0 4 0 6 6 1 c 2 2 2 4 1 1 1 0 8 0 0 5 0 x 0 1 - 0 0 1 1 1 4 i 5 x 4 0 
        0 x 0 1 2 3 6 0 0 x 7 0 0 8 0 6 2 0 c 0 0 1 5 2 5 0 7 0 5 8 i 0 6 0 0 i 5 0 0 x 4 x 5 0 4 0 - 6 - i 
        0 0 6 b 0 0 0 2 x 0 x b 0 0 - b 5 1 x - 6 0 b 3 6 b - 7 - 8 2 4 7 2 - b 0 i 1 2 c 1 b 0 4 b 1 - 0 0 
        c - 0 5 0 7 4 3 i 0 4 5 5 i c c 6 - 0 0 8 2 0 0 - 2 0 x 2 4 c c - 0 b 0 0 6 0 x 8 8 x 2 1 5 5 4 0 7 
        x 0 2 0 0 0 1 6 c 2 0 1 b 6 4 5 0 i x 0 6 5 8 - 0 - 2 6 0 0 c c 2 i 0 b 1 0 0 0 3 8 2 b 3 0 5 6 7 - 
        4 c 7 3 x c - 7 6 8 7 7 i 3 - i c 1 7 8 2 i b 2 b x 8 c - - 3 - 0 7 b - 2 7 7 0 i 0 3 0 5 0 0 4 0 0 
        3 0 - 1 0 c b x c 0 x 0 0 b 6 3 c 1 8 2 0 4 5 0 5 0 1 0 0 0 8 b 1 x 0 1 0 1 - 4 1 x 5 0 0 - b - b 5 
        0 0 0 3 0 4 6 1 0 7 0 1 4 6 1 8 2 0 - 5 0 c 3 0 4 3 3 i 3 5 c 0 0 0 i 7 x 0 i 0 c 0 3 0 2 8 i c 5 7 
        7 6 7 6 - 2 - 2 0 0 8 1 0 0 0 3 0 2 8 i - i 0 8 0 0 - b 0 6 7 0 x 2 3 0 2 1 0 0 6 0 0 6 0 0 0 8 0 1 
        c i 0 5 - 3 x 7 3 2 0 0 1 1 0 0 1 b 0 8 4 i 4 0 0 i 5 0 6 2 3 0 1 i 0 0 - 5 0 - 4 x 6 8 3 0 i 7 - 0 
        b 1 x 1 i 3 - 1 - 8 6 0 8 0 3 0 1 2 1 0 - 0 0 i 0 - 4 0 b 5 0 0 4 0 0 2 0 5 4 2 8 6 i 2 0 5 6 x 5 5 
        b 3 0 4 8 7 8 5 5 i c 8 4 0 6 6 0 7 0 8 5 x - 3 x 6 3 4 8 0 8 5 5 0 b 0 0 3 c 0 3 8 0 b 6 6 4 5 c - 
        c 0 x 7 0 4 0 6 1 7 0 0 0 8 b i 0 0 c 0 0 7 0 0 i 4 0 i 5 4 x 8 0 6 3 0 i 0 3 - 1 - 0 x i 0 2 6 i 5 
        6 0 i 0 0 3 5 i c 3 c 0 0 0 0 0 3 4 7 c x 0 c 0 0 8 0 0 1 0 8 0 0 4 b b 0 8 0 2 0 c 6 - 4 4 - 0 x 2 
        1 8 - x 0 x 4 1 0 x 2 1 2 4 3 i x 5 i 0 5 6 x c 1 i 0 5 4 5 x 0 0 0 6 8 8 0 c - 4 8 c 0 6 c 4 6 7 0 
        0 0 3 3 0 8 x 1 0 x 8 8 1 0 0 4 2 0 x 1 - 0 0 2 7 0 c 0 0 0 x b 4 x b 5 1 - 1 1 c b 6 4 5 0 4 7 1 0 
        0 5 2 6 i 2 0 0 2 5 0 2 0 c - 0 2 0 0 2 0 x x b 5 x 0 3 6 b c b 8 x 0 i b 0 5 0 0 8 2 x b 1 0 - 7 1 
        - 3 8 6 8 0 3 0 2 0 2 x 0 x 8 0 0 2 3 0 8 0 2 5 4 0 6 0 0 0 0 0 - 0 0 8 c 4 0 3 1 3 i x 0 0 b x 8 - 
        b 6 0 0 2 0 b 5 8 2 6 2 - 8 7 0 8 2 2 6 0 8 2 8 0 3 i 5 6 i 0 0 6 0 0 5 2 0 i 0 c b x 1 0 2 0 i 0 0 
        0 7 0 3 3 b x 0 3 - 2 4 0 0 2 0 4 0 - 2 4 i x 1 2 1 0 1 c 8 6 0 - 3 b 0 - 0 0 c 8 0 - 0 8 b 6 6 0 0 
        2 0 0 2 5 3 7 0 1 8 1 - 0 - - 0 3 0 0 b 0 2 c 0 i 2 - 6 0 b 0 7 1 i 0 - 0 6 3 0 c 0 x c 5 6 0 0 c x 
        3 0 3 - 0 6 6 1 b 0 1 2 - i 1 c 0 i 0 0 0 0 x 0 7 - 8 i 3 2 i 0 1 2 3 5 1 3 4 - 0 x 0 7 0 6 0 8 8 6 
        0 1 6 1 0 0 5 b i 2 b - 1 0 6 1 b 0 4 5 - 0 0 7 0 0 3 3 b 3 3 5 0 6 b x 3 c - 0 8 8 c 0 6 2 1 c 2 - 
        1 1 c 0 x 0 1 4 i 5 0 7 x c 0 - 1 0 - 2 4 6 0 0 - 0 b 1 1 6 1 7 i 1 0 4 5 1 8 0 c 1 3 c 8 0 - 6 0 0 
        b - 8 0 0 2 0 c 1 1 b c 0 0 b 0 0 b 3 x x 1 7 i 5 c 0 5 4 0 0 6 5 4 8 i c 0 7 0 b 5 - 0 4 0 c - 2 2 
        c x 6 0 0 x 4 0 7 i i 0 0 1 0 b 8 4 1 0 c 0 - 0 5 5 i i 2 0 x - 2 b 3 8 0 0 0 c 8 3 c 0 0 1 0 0 0 0 
        i 6 5 x 8 4 5 7 x c b c i b c 6 6 7 b 5 5 7 0 0 4 0 4 1 x i 0 5 0 0 1 c 2 3 i - c 6 2 x 2 x 1 2 0 0 
        """;

    /**
     * Giant map in early game with very few stones.
     */
    private static String map2 = 
    """
        5
        5
        2 2
        50 50
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 b - 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 x 0 0
        0 0 0 0 0 0 0 0 0 0 0 5 2 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 - 0 0 0 c - 0 - 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 x 0 - 0 0 0 0 2 5 0 0 x 0 0 0 - 0 0 0 0 0 0 b 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 i 0 0 - 0 0 0 0
        0 - 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 - 0 0 0 0 0 0 0 0 0 0 0 0 c 0
        0 0 0 0 - 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 c 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 - 0 0 0 0 - 0 0 0 0 0 x 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0
        0 0 0 - 0 0 0 0 x 0 0 0 3 2 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 x 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 2 3 0 0 0 b - 0 0 0 0 0 c 0 0 0 0 - 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 x 0 0 0 - 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 x 0 0 0 0 0 0 - 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 x - 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 - 0 0 0 b 0 0 0 0 0 0
        0 0 0 i 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 3 5 0 0 0 0 0 x 0 0 0 0 0 x - 0 0 0 0 0 0 0 b 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 - 0 0 - x - 0 - 0 0 0 0 0 5 3 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 b 0 0 0 0 0 0 0 0 0 0 0 0 0 - 1 5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 - 0 0 0 0 0 0 x b 0 0 0 0 0 0 0 0 0 0 0 0 0 0 5 1 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 - 0 0 0 0
        0 0 0 b 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 - 0 0 0 0 0 - 0 0 0 0 - 0 0 - 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0
        0 x 0 0 0 - 0 0 0 0 0 - 0 - 0 0 0 0 0 0 0 0 - 0 b b 0 0 0 0 0 0 0 - 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0
        x 0 0 0 0 0 0 x 0 x 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - x 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 b 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 b 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 2 0 0 0 0 0 0 0 0
        0 4 3 0 0 - 0 0 0 1 4 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 2 1 0 0 0 0 0 0 0 0
        0 3 4 0 0 0 0 0 0 4 1 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 b 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 x 0 0 0 x 0 x 0 0
        0 0 x 0 0 - 0 - 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 b 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 - 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 - b 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 x 0 0 0 b 0 - 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 2 4 0 0
        x 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 - 0 0 0 0 x 0 0 0 4 2 0 0
        0 0 0 0 0 - x 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 - 0 0 0 0 0 0 0 b - 0 0 0 0 0 0 0
        0 0 0 0 b 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 b 0 0 0 x 0 0 0 0 0 0 - 0
        0 0 0 0 0 c 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 0 0 x 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 - 0 0 0
        x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 0 5 4 0 0 0 0 0
        0 0 - 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 4 5 0 0 b 0 0
        0 0 0 0 0 0 0 i - 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 b
        - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 b 0 0 x - 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0
        0 0 x 0 0 0 c 0 0 0 0 0 0 - 0 0 0 0 c 0 0 0 0 0 0 x b 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 - 0
        0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 - 0 - 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 b 0 0 0 0 0 0 0 0 0 x x 0 c 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 1 0 b
        0 x x 0 0 0 0 0 0 0 0 0 - 0 - 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 0 0 0 0 0 0 1 3 0 0
        0 0 0 0 0 0 0 0 0 0 0 b 0 0 0 0 0 0 0 0 0 0 0 0 x 0 - 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 - 0 0 0 0
        0 0 0 0 - 0 0 0 0 - 0 x 0 0 0 x 0 0 0 i 0 0 0 0 0 0 0 0 0 0 0 0 0 0 - 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0 0 0 b 0 0 0 0 0 0 0 0 0 x 0 0 0 x 0 0 0 b x - 0 0 0 - 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 x 0 0 0 - b 0 0 0 0 x 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 x 0 0 0 0
        0 0 0 0 b 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 -
            """;
    
    /**
     * Chaos map with few empty tiles
     */
    private static String map3 = 
    """
        8
        255
        5 3
        50 49
        x 6 8 6 x x x 4 - 0 4 1 i 4 b 6 0 b 2 7 4 2 2 1 7 - 4 2 i 1 2 x 2 i - 7 i 0 1 c i 7 0 - 5 7 7 3 0
        b 3 x 8 4 i x - i b 3 7 5 5 6 7 c 2 4 4 5 1 3 0 6 b - c 3 4 4 3 c i 5 6 6 0 c 0 3 i 3 4 i 7 8 8 0
        1 5 c 0 b b 0 x i 6 1 3 1 6 0 c 7 5 6 2 - x 4 0 5 6 2 0 6 0 i 1 4 0 c - 1 5 0 3 c 4 6 i 4 4 0 8 -
        0 7 0 2 1 7 2 8 c x 3 2 b - i b c 1 i 2 x b - - - 6 8 c 6 x c 0 5 5 5 c b 5 0 2 b 0 c b 3 c 4 7 3
        4 2 3 1 3 c - b 8 5 1 7 i 8 5 5 2 x 8 - 5 1 3 i i 3 6 7 4 7 0 2 - 5 3 1 2 5 3 4 3 6 7 b - 4 b 3 x
        b 0 7 0 c 4 6 b 4 6 4 x b b b 2 1 i 5 5 - b 8 0 x 0 3 2 b 4 x 7 7 4 c i 0 1 0 b 6 - x 1 - 1 0 2 -
        - 0 2 7 4 1 4 4 4 8 x - 4 4 3 1 c - 5 4 - 7 x i 6 0 0 i 5 i 3 0 c - - 3 b 1 7 2 2 0 6 6 1 1 4 x 6
        x 4 2 - 7 x c x 4 8 c 3 i - 6 2 1 5 b 7 1 0 c 3 8 2 x b b 4 i c x 6 6 3 b 0 c i 2 2 b 0 b 7 8 - -
        i 5 6 8 7 b 1 6 c - 4 - 6 5 2 2 7 i 0 1 4 7 b 2 x 5 - 6 x 5 2 4 3 c 4 3 c 7 2 6 x c x b i 7 x 7 c
        0 2 1 c 8 5 0 i c 1 2 c 6 7 0 0 b b c c 0 - 5 c - 2 i 1 c 0 2 i 1 8 x - 6 8 3 6 3 3 1 6 - b 8 6 c
        5 4 4 0 b - 3 8 7 i 7 2 2 3 x b 2 b c 0 - c 3 3 8 6 4 4 i 6 8 b 0 c 6 4 x 5 7 3 - 2 6 x 4 3 x i 6
        8 5 4 0 7 2 4 4 8 6 3 - 8 0 7 8 7 c 2 2 1 5 x 7 3 i 2 5 - c 1 1 - 5 6 2 - 0 4 i 8 4 5 3 2 x 5 0 6
        3 i 2 5 3 3 x x i - 7 8 2 i 6 b - x 4 i x 0 i 1 1 4 2 2 8 1 1 4 i 2 8 c x 0 8 3 8 x i 3 4 6 - 0 6
        c - 2 0 5 4 x c 3 7 3 5 8 0 7 - 4 - 4 i 5 3 5 7 - b 4 c 6 c 3 c 8 7 b 3 x 8 3 0 8 i 2 4 8 7 1 3 b
        0 8 1 7 0 7 3 c 4 3 1 7 7 6 7 i 0 4 c 7 4 x 6 6 c 0 0 3 1 7 i b c 1 i 4 4 4 i - x i 1 0 - 4 i 7 7
        x b b i 5 4 - 8 2 0 i 2 x b b 2 8 8 6 x 2 c b 0 - x 2 5 3 i b i 8 0 3 c 8 2 1 2 i 4 b 2 6 x 5 x 7
        x 1 5 - b - b 6 3 6 7 3 c 5 1 - x 6 3 2 c - c - 2 5 8 1 3 2 1 6 6 6 c b - 6 3 c 5 c - 3 b 2 4 1 -
        3 - 1 0 b - 8 2 7 4 i 4 3 - 2 6 6 6 b 0 c x - c 2 - 5 1 7 7 4 c 5 b b 7 b 6 7 c - 5 2 6 i 7 0 2 x
        2 2 3 0 8 - - 2 x 0 b 8 3 8 0 x 2 1 8 b 3 5 0 x 0 7 3 - 1 0 c 0 b 6 6 8 8 7 2 7 i 1 i x b x - - 5
        c 3 8 0 1 0 7 c x c 1 8 c 2 0 5 7 8 7 i 6 i x 3 - - x x 1 6 6 i b 4 x 8 1 2 3 c x i 1 c 5 i 6 5 3
        2 4 5 b 2 b 8 8 3 4 i i 6 1 8 5 1 1 b 2 c 7 c 2 7 b x 5 1 - 4 6 7 7 x 6 x 8 x 1 4 c 0 - 3 c 7 7 6
        x b b 5 1 - 2 2 i 8 3 7 8 8 7 4 5 2 6 b - b i x 3 - 6 0 8 x c - 2 2 i 0 b 2 8 6 c 1 i b - 5 7 7 6
        5 0 0 8 7 1 3 8 6 3 5 - 0 7 0 7 c i i x 8 4 3 i b 3 8 2 6 5 i c 4 4 c 3 8 x 4 3 b x 3 3 6 8 8 1 0
        8 1 b 0 0 x 4 8 0 i 2 1 2 0 2 7 7 1 - 5 6 4 7 7 7 1 5 7 0 6 3 0 1 2 c 4 0 1 1 c 6 8 8 5 c 3 0 3 4
        8 0 5 c 1 8 6 b x 4 2 4 - - 2 - 1 x - 5 0 0 i 5 3 4 c b 3 8 8 5 c c 6 4 7 - 3 7 6 b i 7 c 1 1 3 8
        6 6 - b 5 1 4 2 b x i c 6 0 7 - 0 7 c 2 b 7 c 7 x c 2 4 4 - 1 2 6 8 4 1 3 3 i - 2 0 2 2 i 3 i 2 2
        7 8 c i 4 7 3 3 7 - 4 8 3 1 5 b 5 b i 7 x 5 b c b 4 2 3 c 0 6 i 1 c 8 0 6 7 4 i x 7 1 1 - 7 3 7 5
        1 2 8 4 b c 4 3 x 8 3 1 1 x i b 4 0 8 6 3 3 i 3 3 0 i 4 2 1 6 0 2 6 6 i 1 1 2 - - 1 3 5 2 2 - 1 6
        0 i 3 4 0 4 b - 8 5 1 - 7 x 8 5 5 2 0 3 - 7 5 5 c i 8 8 5 5 b x i c i c 0 1 3 4 0 i c 6 4 4 8 - x
        i b 4 4 5 7 x 7 2 0 0 x x 0 i 7 b 6 5 8 5 - 1 c b 4 3 6 0 i 8 4 - - b i 7 x i 4 x c b i 0 1 i 5 -
        1 5 2 7 1 x 5 b 8 8 i 1 7 3 1 2 7 7 i x i 6 - b 2 i 8 x 4 b 6 4 8 - 8 1 b - i 6 6 3 c 7 b 4 2 c 5
        x 7 6 c 7 - 8 c 5 b 6 8 - i 2 5 b x 6 5 b - c 1 2 - 5 8 1 - 7 6 6 5 7 5 5 2 3 x 8 i - 1 4 1 0 c i
        5 3 5 c 3 8 i 5 6 i i 4 c 7 3 0 0 3 1 c 0 7 4 1 6 7 1 - i 7 8 1 1 c 2 3 7 5 5 - 3 b 1 b 6 2 7 3 b
        2 5 3 b x i 0 4 1 1 2 i 6 4 x 3 x 5 4 2 4 4 5 8 i x - x 3 6 i 0 4 c 5 7 - c 5 1 0 - - 8 4 3 4 - 6
        8 7 x 2 - 5 b 3 8 7 7 2 i x 0 0 1 c 3 1 6 8 4 x 8 4 x 7 5 c 6 - 2 8 c 2 b i 3 4 5 8 c 6 0 - c 4 5
        4 3 3 i 4 c 5 x 3 2 3 4 3 c b x c 6 0 6 i - 0 i 4 x 4 4 0 4 5 1 i 6 i c 3 4 0 0 c 0 b b - x x 7 8
        8 8 5 3 b 2 6 c 5 6 5 7 x x 7 2 8 7 2 2 - 8 b 8 i 1 b 0 2 5 6 6 1 5 b i 3 1 0 3 2 0 5 1 4 0 1 b 5
        7 - 3 6 i 8 4 7 7 7 6 c 8 1 3 4 6 x 4 4 2 3 0 6 3 b b i i b 1 c 6 x x c - c b 2 6 3 x b 6 6 2 3 -
        6 8 1 0 x 0 7 2 1 0 7 i 5 - 1 5 6 3 x x b - 2 8 b c 8 5 3 4 c 1 x b 7 4 - i 5 0 7 2 5 0 b 4 x - x
        4 6 - c 7 x b - 8 3 i c 6 2 6 0 x 8 6 6 4 i 5 x b 6 x c b x - 1 4 c b 2 i i 6 2 8 x 5 b - 3 6 c 3
        c 0 2 - 7 4 3 1 6 1 b 8 8 x 6 0 0 - x 3 3 1 5 b i 4 c 2 3 x 4 b 7 - 5 1 i 1 1 7 8 4 0 c 2 8 3 b i
        b 2 2 3 b 5 3 c 7 b 6 x 3 b 7 4 8 2 7 0 c 6 i 4 x i 0 2 0 3 8 8 6 c 3 5 5 8 b - - 4 4 x 8 2 c 1 6
        x b b 6 4 1 4 3 c i 6 5 8 i 6 8 2 x 5 - 8 5 - i i 6 c 5 b 3 0 - 4 3 1 4 c - 0 c 0 c 7 - b 3 3 0 0
        8 i - c - 4 4 c 1 7 b i 3 3 c 5 3 5 8 x - 1 5 c b 6 8 5 5 1 7 - x 7 4 x 3 0 3 7 4 i c 3 4 4 b 6 i
        - 8 b b 8 1 8 3 5 6 x 8 2 8 - x 2 5 c 2 3 5 0 i b - 3 4 7 2 0 7 5 c 2 x c 2 - 4 4 5 4 3 i 5 6 - -
        i 2 4 0 - i - 0 5 6 i x - 6 2 6 5 6 x b 7 2 x 3 5 0 c 3 i 3 0 0 7 2 8 c x i 6 5 x 1 6 5 2 x 1 0 0
        6 x - 1 5 i 5 1 5 3 8 - 7 5 - 1 5 1 7 c - 6 c b 2 1 - 3 0 x 8 - 2 x 2 6 5 c 4 2 c 1 2 x x 3 8 c 4
        0 5 - 4 6 0 c 5 6 b 8 - 0 x 8 6 x x - 4 2 0 x 3 6 7 0 0 5 c 6 x 7 2 c b 6 i 4 7 6 3 3 6 b c 4 7 -
        3 3 i - 1 5 5 b x i b 2 i 0 8 4 5 2 2 2 7 2 x x 0 2 2 x x i x i 7 b x 3 8 c 0 7 7 5 6 2 6 5 4 4 4
        - 6 7 4 3 4 6 6 3 - 2 3 c 5 1 5 7 1 3 5 x 4 5 x 7 i - - i - 4 6 8 - x 3 5 8 1 2 b 4 4 8 7 1 i 2 5
            """;

            /**
             * Our standard 4v4 map in the early game
             */
    private static String map4 = 
    """
        4 
        0
        0 2
        9 9 
        - - - b c b - - -
        - 0 0 x x x 0 0 -
        - 0 1 0 0 0 2 0 -
        b x 0 1 x 2 0 x b
        c x 0 x - x 0 x c
        b x 0 4 x 3 0 x b
        - 0 4 0 0 0 3 0 -
        - 0 0 x x x 0 0 -
        - - - b c b - - -        
            """;


            /**
             * Our standard 4v4 map in the mid game
             */
    private static String map5 = 
    """
        4 
        1
        0 2
        9 9 
        - - - b c b - - -
        - 0 0 x x 1 0 0 -
        - 0 1 0 0 1 2 0 -
        b x 0 1 1 1 2 x b
        c x 0 1 - 3 2 x c
        b x 2 1 2 2 2 x b
        - 0 4 1 4 0 2 0 -
        - 0 0 x x x 2 0 -
        - - - b c b - - -        
            """;


    /**
     * Random 2v2 map with lots of occupied stones
     */
    private static String map6 = 
    """
        2
        2
        2 1
        25 25
        c 0 1 0 1 0 1 2 2 0 0 2 2 1 c - 0 i 2 2 1 1 2 - 0 
        1 0 c 0 0 b 1 0 1 1 2 - 1 c 2 0 i 2 1 0 0 2 2 1 1 
        c 0 b b 0 2 2 1 0 0 2 2 2 0 x b 1 1 0 0 0 0 1 x 1 
        0 - 0 0 1 0 2 1 0 c - 0 b x b 2 0 0 1 2 0 0 0 2 1 
        c 0 1 0 0 - 1 1 1 2 2 0 0 b 0 b 0 0 1 0 0 2 0 1 0 
        c c 0 0 1 0 1 2 x x i 0 1 2 i 2 1 2 1 1 b 0 c 1 2 
        2 1 1 x 0 - i 0 x 1 x 2 1 2 2 i 0 2 1 1 2 0 1 - 1 
        0 i 1 0 x 0 - 2 b 2 1 1 1 c 0 0 0 0 2 c 2 x 1 c 0 
        2 1 0 2 0 0 0 1 1 0 0 2 1 1 c x 2 c c - i c i b x 
        2 0 0 1 0 i 2 i - 0 2 1 0 0 2 b 0 1 0 b 2 0 1 - 2 
        c 0 0 2 0 1 2 0 0 1 0 1 - 0 i 0 1 1 2 2 c - 1 1 1 
        1 0 2 1 0 2 2 i c 0 b 1 - - 2 i x - 2 2 0 2 1 2 0 
        1 1 b 2 0 2 2 0 1 0 0 0 i 0 0 2 1 2 0 2 1 0 0 1 c 
        c 0 0 1 c i 2 - 2 2 2 i 1 0 0 1 0 i 0 2 2 - 2 c 1 
        b 2 1 i 0 - 2 0 0 0 i x 0 x 0 2 b 1 1 c 0 2 b 2 0 
        0 0 0 2 1 0 2 1 1 0 1 c c 1 x 1 0 - 0 c b i 0 1 2 
        0 i 2 b 1 - - x 1 2 0 0 2 1 0 2 i 2 b 2 - 0 1 2 1 
        2 0 0 c c i 0 0 0 1 1 0 1 1 0 - 1 - 1 b 1 0 2 i c 
        0 0 - 2 1 0 1 0 2 2 i 0 1 x 1 2 1 2 0 0 0 b c 0 x 
        0 c b 2 x b i 0 x 2 0 0 i 1 0 1 0 x i 0 c i b - c 
        2 0 0 1 0 2 0 0 - x 2 2 0 0 i 2 1 x x 2 x 0 0 0 0 
        1 0 0 2 2 0 1 1 c 2 0 - c 0 i 2 0 0 i 0 i 2 0 1 i 
        2 1 2 1 2 2 1 0 1 2 0 0 x 0 0 0 0 2 0 0 1 2 2 0 x 
        i 1 0 2 c 0 2 1 0 c 1 - 0 0 1 b i 0 2 c x 2 0 2 - 
        2 1 i i 0 x 0 0 0 - 2 0 0 1 - i 1 1 1 x 0 2 0 2 c 
            """;


            private static String map7=map5;
            private static String map14=map5;


        private static String map8 = 
        """
            2
            1
            0 0
            8 8
            b 0 0 0 0 0 0 0
            0 0 0 0 0 0 0 0
            0 0 1 0 1 0 c 0
            0 2 2 2 2 2 0 0
            0 0 2 2 1 0 0 0
            0 0 0 2 0 0 0 0
            0 0 0 0 0 0 0 0
            0 0 0 0 0 0 0 0
                """;

        private static String map15 =
        """
            2
            3
            3 5
            11 11
            0 2 0 0 0 0 0 0 1 1 0
            1 2 1 0 0 - 0 2 2 2 2
            2 2 1 0 - 0 - 0 1 1 0
            0 0 0 - 0 c 0 - 0 0 0
            0 0 - 0 0 1 0 0 - 0 0
            0 - 0 0 1 2 2 0 0 - 0
            0 0 - 1 0 2 0 0 - 0 0
            0 b 0 - 2 0 0 - 2 0 0
            0 2 0 0 - 0 - 0 0 2 0
            1 1 1 0 0 - 0 0 1 0 2
            0 2 0 0 0 0 0 1 0 1 0
                """;

        private static String map9 = 
        """
            3
            3
            3 3
            15 15
            i 0 0 0 - - - - - - - 0 c 0 0 
            0 0 b 0 - - - 0 - - - 0 0 b 0 
            0 c 0 0 - - 0 0 0 - - b 0 0 0 
            - - - - - 0 0 b 0 0 - - - - - 
            - - - - 0 0 0 x 0 0 0 - - - - 
            - - - 0 0 2 0 3 0 0 0 0 - - - 
            - - 0 0 0 0 2 3 1 0 0 0 0 - - 
            - 0 0 b x 0 1 3 1 0 x b 0 0 - 
            - - 0 0 0 0 1 3 1 0 0 0 0 - - 
            - - - 0 0 0 1 3 1 0 0 0 - - - 
            - - - - 0 0 0 x 0 0 0 - - - - 
            - - - - - 0 0 b 0 0 - - - - - 
            0 0 0 0 - - 0 0 0 - - 0 0 0 c 
            b b 0 0 - - - 0 - - - 0 i 0 0 
            0 0 0 b - - - - - - - 0 0 0 0 
            0 0 7 <-> 14 14 3
            0 14 5 <-> 14 0 1
            0 2 3 <-> 3 5 7
            1 2 3 <-> 4 5 7
            2 2 3 <-> 4 4 7
            3 2 3 <-> 5 4 7
            3 1 3 <-> 5 3 7
            3 0 3 <-> 6 3 7
            11 0 5 <-> 8 3 1
            11 1 5 <-> 9 3 1
            11 2 5 <-> 9 4 1
            12 2 5 <-> 10 4 1
            13 2 5 <-> 10 5 1
            14 2 5 <-> 11 5 1
            0 12 1 <-> 3 9 5
            1 12 1 <-> 4 9 5
            2 12 1 <-> 4 10 5
            3 12 1 <-> 5 10 5
            3 13 1 <-> 5 11 5
            3 14 1 <-> 6 11 5
            11 14 7 <-> 8 11 3
            11 13 7 <-> 9 11 3
            11 12 7 <-> 9 10 3
            12 12 7 <-> 10 10 3
            13 12 7 <-> 10 9 3
            14 12 7 <-> 11 9 3            
                """;

        private static String map10 =
        """
            3
            1
            1 2
            16 12
            - - - - 0 0 0 0 - - - -
            - - - - 0 2 2 2 - - - -
            - - - - 0 3 3 0 - - - -
            - - - - 0 0 3 0 - - - -
            0 0 2 0 0 0 0 0 3 0 0 0
            0 1 2 1 0 0 0 0 3 1 1 0
            0 2 2 2 0 0 0 0 1 3 3 0
            0 0 0 0 0 0 0 1 0 0 0 0
            - - - - 0 0 0 0 - - - -
            - - - - 0 0 0 0 - - - -
            - - - - 0 0 0 0 - - - -
            - - - - 0 0 0 0 - - - -
            - - - - 0 0 0 0 - - - -
            - - - - 0 0 0 0 - - - -
            - - - - 0 0 0 0 - - - -
            - - - - 0 0 0 0 - - - -
            4 0 0 <-> 4 15 4
            5 0 0 <-> 5 15 4
            6 0 0 <-> 6 15 4
            7 0 0 <-> 7 15 4
            0 4 0 <-> 4 0 6
            1 4 0 <-> 4 1 6
            2 4 0 <-> 4 2 6
            3 4 0 <-> 4 3 6
            8 4 0 <-> 7 3 2
            9 4 0 <-> 7 2 2
            10 4 0 <-> 7 1 2
            11 4 0 <-> 7 0 2
            0 7 4 <-> 4 11 6
            1 7 4 <-> 4 10 6
            2 7 4 <-> 4 9 6
            3 7 4 <-> 4 8 6
            8 7 4 <-> 7 8 2
            9 7 4 <-> 7 9 2
            10 7 4 <-> 7 10 2
            11 7 4 <-> 7 11 2
            0 4 6 <-> 4 15 6
            0 5 6 <-> 4 14 6
            0 6 6 <-> 4 13 6
            0 7 6 <-> 4 12 6
            11 4 2 <-> 7 15 2
            11 5 2 <-> 7 14 2
            11 6 2 <-> 7 13 2
            11 7 2 <-> 7 12 2            
                """;

        private static String map11 =
        """
            2
            1
            0 0
            10 10
            - - - - 0 - - - - -
            - - - 0 0 0 - - - -
            - - 0 0 c 0 0 - - -
            - b 0 1 2 0 0 0 - -
            0 1 1 1 1 2 0 0 0 -
            - 0 0 b 2 2 0 0 0 0
            - - 0 0 0 2 0 0 0 -
            - - - 0 0 0 0 0 - -
            - - - - 0 0 0 - - -
            - - - - - 0 - - - -
                """;
}
