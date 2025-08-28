package com.rwth.heuristicalgorithms.MoveChoosing;

import java.util.Arrays;
import com.rwth.heuristicalgorithms.PlayingField.PlayingField;
import com.rwth.heuristicalgorithms.PlayingField.Tile;

/**
 * This class rates a given board for any player based on our heuristic.
 */
public class MoveHeuristic {


    //-------------------------------------------------
    // CONSTANTS

    //RULES:
    // 150 = 1.5x
    // 100 = 1x
    // 50 = 0.5x


    /**
     * Rating of an occupied default tile.
     * This value should ALWAYS be 100 since all the other heuristic constants are relative to this one.
     */
    public static final int RATING_OCCUPIED_DEFAULT_TILE = 100;

    /**
     * Rating of an occupied corner tile. A tile is considered a corner if it has a maximum of 3 neighbours.
     */
    public static final int RATING_OCCUPIED_CORNER_TILE = 500;

    /**
     * Rating of an occupied edge tile. A tile is considered an edge tile if it has a maximum of 5 neighbours.
     */
    public static final int RATING_OCCUPIED_EGDE_TILE = 150;


    /**
     * Rating for a frontier stone.
     * This rating should be negative to encourage the AI to obtain stones that are surrounded by other stones and thos hard to flip.
     * The philisophy behind this is that the optimal game setting is: all of our stones in the center, surrounded by opponents' stones
     * NOTE: pay attention that this value is not too great, otherwise the AI will be punished for occupying stones.
     */
    public static final int RATING_FRONTIER_STONE = -25;



    /**
     * Rating for having an override stone available.
     */
    public static final int RATING_OVERRIDE_STONE_AVAILABLE = 1000; 

    /**
     * Rating for having a bomb available.
     * Note that this only plays a role during building phase since we will use one bomb per move in the elimination phase
     */
    public static final int RATING_BOMB_AVAILABLE = (int)(RATING_OVERRIDE_STONE_AVAILABLE*0.9); 

    

    /**
     * Factor for the amount of stones a move flips.
     * 0 -> number of flipped stones is not considered, all valid moves are equally good
     * 10 -> move that flips 1 stone has value of 100+10, move that flips 5 stones has value 100+50.
     * THIS FACTOR MAY INCREASE DURING THE GAME. Early game -> less stones may be better; End game -> More stones = better.
     * This factor should be kept faily low (between 0 and 100). 
     */
    public static int factorNumFlippedStones = 5;

    /**
     * The factor for occupied tiles is computed dynamically during the game based on the percentage of tiles occupied.
     * This constant presents the upper bound for that value (i.e., if 100% of tiles are occupied, the weight for occupied element in the evaluation is this and the other weight is 100-this).
     * The value should always be higher than FACTOR_OCCUPIED_TILES_MIN and lower or equal to 100.
     */
    public static final int FACTOR_OCCUPIED_TILES_MAX = 100;

    /**
     * The factor for occupied tiles is computed dynamically during the game based on the percentage of tiles occupied.
     * This constant presents the lower bound for that value (i.e., if 0% of tiles are occupied, the weight for occupied element in the evaluation is this and the other weight is 100-this).
     * The value should always be lower than FACTOR_OCCUPIED_TILES_MAX and >= 0.
     */
    public static final int FACTOR_OCCUPIED_TILES_MIN = 20;


    /**
     * For testing purposes only
     */
    public static boolean stableTilesEnabled = false;
    /**
     * For testing purposes only
     * @param b new value of stableTilesEnabled
     */
    public static void setStableTilesEnabled(boolean b){
        stableTilesEnabled = b;
    }



    //-------------
    // CONSTANTS FOR ELIMINATION PHASE
    /*
     * EXAMPLE:
     * LET: 
     * WEIGHT_ELIM_OUR_STONES = 120;
     * WEIGHT_ELIM_PREDECESSOR = -100;
     * CONST_ELIM_PREDECESSOR = 10;
     * CONST_ELIM_SUCCESOR = 20;
     * 
     * 
     * Let us be in Place 5 of 8.
     * 
     * The weights of each player's #tiles is as follows:
     * 
     * Our stones (Place 5): 1.2x
     * 
     * Place 1: -0.7x = WEIGHT_ELIM_PREDECESSOR + 3*CONST_ELIM_PREDECESSOR
     * Place 2: -0.8x = WEIGHT_ELIM_PREDECESSOR + 2*CONST_ELIM_PREDECESSOR
     * Place 3: -0.9x = WEIGHT_ELIM_PREDECESSOR + 1*CONST_ELIM_PREDECESSOR
     * Place 4: -1x = WEIGHT_ELIM_PREDECESSOR
     *
     * Place 6: -0.8x = WEIGHT_ELIM_PREDECESSOR + 1*CONST_ELIM_SUCCESSOR
     * Place 7: -0.6x = WEIGHT_ELIM_PREDECESSOR + 2*CONST_ELIM_SUCCESSOR
     * Place 8: -0.4x = WEIGHT_ELIM_PREDECESSOR + 3*CONST_ELIM_SUCCESSOR
     * 
     * (if WEIGHT_ELIM_PREDECESSOR + x*CONST_ELIM_SUCCESSOR or WEIGHT_ELIM_PREDECESSOR + x*CONST_ELIM_PREDECESSOR is > 0x, the value is set to 0x.)
     */


    /**
     * Base weight for our stones during the elimination phase. This value should remain 120 as all other values are relative to this.
     */
    public final static int WEIGHT_ELIM_OUR_STONES = 120;

    /**
     * Base weight for the player directly before us.
     */
    public final static int WEIGHT_ELIM_PREDECESSOR = -100;

    /**
     * Additive constant for players x places before us.
     */
    public final static int CONST_ELIM_PREDECESSOR = 10;

    /**
     * Additive constant for players x places behind us.
     */
    public final static int CONST_ELIM_SUCCESOR = 20;


    // CONSTANTS
    //-------------------------------------------------

    /**
     * Evaluates a given board in the building phase for the given player.
     * @param pf the board to be evaluated
     * @param player the player to evaluate the board for
     * @return the value
     */
    public static int evalBoard_Building(PlayingField pf, byte player){
        //1: Compute weighted sum of occupied tiles
        int sumOccupiedTiles = calcSumOccupiedTiles(pf, player,false);

        //2: Consider the amount of frontier stones for each player
        int sumFrontierStones = calcSumFrontierStones(pf, player);

        //This is the factor to weigh occupied tiles against frontier tiles
        int factorOccupiedTiles = getFactorOccupiedTiles(pf);

        //Also consider Override stones in order to not waste those 
        //consider bombs to tweak wheter to choose override or bomb
        int sumOverride = pf.getPlayerOverrideStoneCount(player)*RATING_OVERRIDE_STONE_AVAILABLE;
        int sumBomb = pf.getPlayerBombCount(player)*RATING_BOMB_AVAILABLE;

        return (sumOccupiedTiles * factorOccupiedTiles) + (sumFrontierStones * (100-factorOccupiedTiles)) + sumOverride + sumBomb;
    }


    /**
     * Calculates the weighted sum of occupied tiles.
     * Each occupied tile can be a CORNER, EDGE or DEFAULT. 
     * This method adds a constant value for each type of occupied tile.
     * @param pf The PlayingField
     * @param player to count for
     * @param calc whether to calculate the value or read it from pf
     * @return the weighted sum of all occupied tiles
     */
    public static int calcSumOccupiedTiles(PlayingField pf, byte player, boolean calc){
        if(!calc)return pf.getTileScore(player);
        int sumOccupiedTiles = 0;
         //iterate over whole map
         for (byte y = 0; y < PlayingField.getMapHeight(); y++) {
            for (byte x = 0; x < PlayingField.getMapWidth(); x++) {
                if (pf.getMapTile(y, x) == pf.TILE_ENCODING[player]) {
                    sumOccupiedTiles += switch(PlayingField.getTileType(y, x)){
                        case DEFAULT -> RATING_OCCUPIED_DEFAULT_TILE;
                        case EDGE -> RATING_OCCUPIED_EGDE_TILE;
                        case CORNER -> RATING_OCCUPIED_CORNER_TILE;
                    };
                }

            }
        }
        return sumOccupiedTiles;
        
    }


    /**
     * Calculates the weighted sum of frontier stones.
     * @param pf The PlayingField
     * @param player to sum for
     * @return the weighted sum
     */
    public static int calcSumFrontierStones(PlayingField pf, byte player){
        return pf.getFrontierCount(player) * RATING_FRONTIER_STONE;
    }

    /**
     * Yields the factor for occupied tiles by considering the percantage of occupied tiles on the map. 
     * @param pf the PlayingField
     * @return a value between FACTOR_OCCUPIED_TILES_MIN and FACTOR_OCCUPIED_TILES_MAX proportial to the percentage 
     */
    private static int getFactorOccupiedTiles(PlayingField pf){
        double percentage_occupied = pf.getPercentageOccupied();

        //Now map the percentage between FACTOR_OCCUPIED_TILES_MIN and FACTOR_OCCUPIED_TILES_MAX:
        return (int)(FACTOR_OCCUPIED_TILES_MIN + percentage_occupied * (FACTOR_OCCUPIED_TILES_MAX - FACTOR_OCCUPIED_TILES_MIN));
    }

    

    /**
     * Evaluates a given board in the elimination phase for the given player.
     * Counts the stones of each player. Stones of players "close" to the specified player 
     * (i.e., similar #stones) are weighted more  
     * 
     * @param pf the board to be evaluated
     * @param player the player to evaluate the board for
     * @return the value
     */
    public static int evalBoard_Elimination(PlayingField pf, byte player){
        //step 1: count stones for each player
        //the specified player (us) is not saved in the array but in a variable
        short ourPlayerStonesCount = 0;
        //this array will contain one 0 entry (count for our stones)
        short[] stones_player = new short[PlayingField.getPlayerCount()];
        //iterate over map
        for(byte y = 0; y < PlayingField.getMapHeight(); y++){
            for(byte x = 0; x < PlayingField.getMapWidth(); x++){
                Tile t = pf.getMapTile(y, x);
                if(Tile.isTileOccupiedByPlayer(t)){
                    if(t.equals(pf.TILE_ENCODING[player])){
                        ourPlayerStonesCount++;
                    }
                    else {
                        stones_player[pf.TILE_DECODING.get(t)-1]++;
                    }
                }
            }
        }

        //step 2: determine our position
        int pos = stones_player.length;

        //add our count to the array and then sort
        stones_player[player-1] = ourPlayerStonesCount;

        //remove diaqualified players
        for(byte p = 1; p <= PlayingField.getPlayerCount(); p++){
            if(pf.isDisqualified(p)) stones_player[p-1] = 0;
        }

        //sort the stones_player array (ascending order; place 1 is in stones_player[stones_player.length-1])
        Arrays.sort(stones_player);
        //find our value in the array. The index will tell us our position 
        //(if there is another player with the same #stones as us, we choose the lower position)
        while(pos > 0 && ourPlayerStonesCount <= stones_player[pos-1]){
            pos--;
        }

        //weigh the number of tiles (see constant definitions at the top for an example)
        int weightedSum = ourPlayerStonesCount * WEIGHT_ELIM_OUR_STONES;

        //players with more stones than us
        for(int i = 1; i+pos < stones_player.length; i++){
            int factor = (WEIGHT_ELIM_PREDECESSOR + (i-1) * CONST_ELIM_PREDECESSOR);
            if(factor > 0) factor = 0;
            weightedSum += stones_player[pos+i] * factor;
        }
        //players with fewer stones than us
        for(int i = 1; pos-i >= 0; i++){
            int factor = (WEIGHT_ELIM_PREDECESSOR + i * CONST_ELIM_SUCCESOR);
            if (factor > 0) factor = 0;
            weightedSum += stones_player[pos-i] * factor;
        }


        return weightedSum;
    }
    


}
