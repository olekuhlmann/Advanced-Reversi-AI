package com.rwth.heuristicalgorithms.PlayingField;

/**
 * enum of possible tiles
 */
public enum Tile {
    /**
     * empty tile
     */
    EMPTY,

    /**
     * Tile occupied by player 1
     */
    OCCUPIED_1,
    
    /**
     * Tile occupied by player 2
     */
    OCCUPIED_2,

    /**
     * Tile occupied by player 3
     */
    OCCUPIED_3,

    /**
     * Tile occupied by player 4
     */
    OCCUPIED_4,

    /**
     * Tile occupied by player 5
     */
    OCCUPIED_5,

    /**
     * Tile occupied by player 6
     */
    OCCUPIED_6,

    /**
     * Tile occupied by player 7
     */
    OCCUPIED_7,

    /**
     * Tile occupied by player 8
     */
    OCCUPIED_8,

    /**
     * Choice Tile
     */
    CHOICE,

    /**
     * Inversion Tile
     */
    INVERSION,

    /**
     * Bonus Tile
     */
    BONUS,

    /**
     * Expansion stone
     */
    EXPANSION,

    /**
     * Hole tile
     */
    HOLE;



    /**
     * Checks whether a tile is occupied by a player.
     * @param tile to check
     * @return true if tile is occupied by a player
     */
    public static boolean isTileOccupiedByPlayer(Tile tile){
        return !tile.equals(HOLE) && !tile.equals(EMPTY) && !tile.equals(BONUS) && !tile.equals(CHOICE) && !tile.equals(INVERSION) && !tile.equals(EXPANSION);
    }

    /**
     * Checks whether a tile is empty
     * @param tile to check 
     * @return true if the tile is EMPTY, BONUS, CHOICE or INVERSION
     */
    public static boolean isTileEmpty(Tile tile){
        return tile.equals(EMPTY) || tile.equals(BONUS) || tile.equals(CHOICE) || tile.equals(INVERSION);
    }

    /**
     * Checks whether a tile is empty or a hole (and thus not valid for an override move)
     * @param tile to check
     * @return true if the tile is either empty (or bonus, ...) or a hole
     */
    public static boolean isTileEmptyOrHole(Tile tile){
        return tile.equals(EMPTY) || tile.equals(HOLE) || tile.equals(BONUS) || tile.equals(CHOICE) || tile.equals(INVERSION);
    }
}

