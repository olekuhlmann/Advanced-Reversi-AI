package com.rwth.heuristicalgorithms.MoveChoosing;

import com.rwth.heuristicalgorithms.PlayingField.Move;

/**
 * Class for the move sorting heuristic
 */
public class MoveSortHeuristic {
    /**
     * Evaluates a move for move sorting by putting choice moves first and override moves last.
     * @param m The Move
     * @return value for move sorting. Higher value -> move will be branched first
     */
    static int evaluateMove_MoveSorting(Move m){
        return switch(m.getMoveType()){
            case CHOICE -> 1;
            case OVERRIDE -> -1;
            default -> 0;
        };
    }
}
