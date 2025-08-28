package com.rwth.heuristicalgorithms.PlayingField;

import java.util.ArrayList;
import java.util.List;

/**
 * class containing all the relevant information for our move implementation
 */
public class Move {

    //------------------------------------------------------------------------------
    // ATTRIBUTES START


    /**
     * y coordinate of tile associated with this move
     */
    private final byte y;

    /**
     * x coordinate of tile associated with this move
     */
    private final byte x;

    /**
     * The chosen player when occupying a choice tile.
     * This is not -1 iff this move is a choice move
     */
    private final byte choicePlayer;

    /**
     * Wheter a bomb or override stone is chosen.
     */
    private final boolean bonusChooseBomb;

    /**
     * this moves type
     */
    private final MoveType type;

    /**
     * the tiles this move encloses
     */
    private List<byte[]> enclosed;

    /**
     * stores this moves value
     */
    public int value = 0;
    
    // ATTRIBUTES END
    //------------------------------------------------------------------------------



    //------------------------------------------------------------------------------
    // CONTRSUCTORS START

    /**
     * Constructor for Move. See attribute descriptions for more info.
     * @param y y-coordinate
     * @param x x-coordinate
     * @param choicePlayer player for choice move
     * @param bonusChooseBomb true if choice for bonus tile is bomb
     * @param type the move type
     * @param enclosed the tiles enclosed by this move
     */
    private Move(byte y, byte x, byte choicePlayer, boolean bonusChooseBomb, MoveType type, List<byte[]> enclosed){
        this.y = y;
        this.x = x;
        this.choicePlayer = choicePlayer;
        this.bonusChooseBomb = bonusChooseBomb;
        this.type = type;
        this.enclosed = (enclosed == null || enclosed.size() == 0) ? new ArrayList<>() : enclosed;
    }

    /**
     * Creates a Move that is neither a bomb move, choice move or bonus move.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @param enclosed the tiles enclosed by this move
     * @return Move instance
     */
    public static Move createDefaultMove(byte y, byte x,List<byte[]> enclosed){
        return new Move(y, x, (byte)-1, false, MoveType.DEFAULT,enclosed);
    }

    /**
     * Creates a Move that occupies a choice tile.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @param choicePlayer Player to switch stones with
     * @param enclosed the tiles enclosed by this move
     * @return Move instance
     */
    public static Move createChoiceMove(byte y, byte x, byte choicePlayer,List<byte[]> enclosed){
        return new Move(y, x, choicePlayer, false,MoveType.CHOICE,enclosed);
    }

    /**
     * Creates a Move that occupies a choice tile.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @param chooseBomb true iff the player chooses the bomb as reward.
     * @param enclosed the tiles enclosed by this move
     * @return Move instance
     */
    public static Move createBonusMove(byte y, byte x, boolean chooseBomb,List<byte[]> enclosed){
        return new Move(y, x, (byte)-1, chooseBomb, MoveType.BONUS,enclosed);
    }

    /**
     * Creates a Move that uses an override stone.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @param enclosed the tiles enclosed by this move
     * @return Move instance
     */
    public static Move createOverrideMove(byte y, byte x,List<byte[]> enclosed){
        return new Move(y, x, (byte)-1, false, MoveType.OVERRIDE,enclosed);
    }

    /**
     * Creates a Move that is neither a bomb move, choice move or bonus move.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @return Move instance
     */
    public static Move createDefaultMove(byte y, byte x){
        return new Move(y, x, (byte)-1, false, MoveType.DEFAULT,null);
    }

    /**
     * Creates a Move that occupies a choice tile.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @param choicePlayer Player to switch stones with
     * @return Move instance
     */
    public static Move createChoiceMove(byte y, byte x, byte choicePlayer){
        return new Move(y, x, choicePlayer, false,MoveType.CHOICE,null);
    }

    /**
     * Creates a Move that occupies a choice tile.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @param chooseBomb true iff the player chooses the bomb as reward.
     * @return Move instance
     */
    public static Move createBonusMove(byte y, byte x, boolean chooseBomb){
        return new Move(y, x, (byte)-1, chooseBomb, MoveType.BONUS,null);
    }

    /**
     * Creates a Move that uses an override stone.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @return Move instance
     */
    public static Move createOverrideMove(byte y, byte x){
        return new Move(y, x, (byte)-1, false, MoveType.OVERRIDE,null);
    }

    /**
     * Creates a Move that uses a bomb.
     * @param y coordinate of affected tile
     * @param x coordinate of affected tile
     * @return Move instance
     */
    public static Move createBombMove(byte y, byte x){
        return new Move(y, x, (byte)-1, false, MoveType.BOMB,null);
    }


    // CONSTRUCTORS END
    //------------------------------------------------------------------------------



    
    //------------------------------------------------------------------------------
    // GETTER START


    /**
     * getter for y coordinate
     * @return y coordinate of affected tile
     */
    public byte getY() {
        return y;
    }

    /**
     * getter for x coordinate
     * @return x coordinate of affected tile
     */
    public byte getX() {
        return x;
    }
    
    /**
     * getter for choice player
     * @return player chosen to swap tiles with
     */
    public byte getChoicePlayer() {
        return choicePlayer;
    }

    /**
     * geter for bonusChooseBomb
     * @return true if a bomb was chosen over an override stone
     */
    public boolean getBonusChooseBomb(){
        return bonusChooseBomb;
    }

    /**
     * getter for MoveType
     * @return the type of move executed.
     */
    public MoveType getMoveType(){
        return type;
    }

    /**
     * getter for enclosed tiles
     * @return the enclosed tiles
     */
    public List<byte[]>  getEnclosed(){
        return this.enclosed;
    }

    // GETTER END
    //------------------------------------------------------------------------------





    
    //------------------------------------------------------------------------------
    // METHODS START

    /**
     * Copies the given PlayingField, executes the move and returns the new PlayingField. 
     * @param move to be executed
     * @param playingfield will remain untouched
     * @param player player executing the move
     * @return the new playingfield with the move executed
     */
    public static PlayingField simulateMove(Move move, byte player, PlayingField playingfield){
        PlayingField pf = playingfield.copy();
        boolean success = pf.executeMove(move, player);
        return success ? pf : null;
    } 


    /**
     * checks the the specified move for equivalnece with the executing move
     * @param o the object to compare to, should always be a move
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof Move m){
            if(m.getY() == y && m.getX() == x && m.getMoveType() == type){
                if(type.equals(MoveType.BONUS)){
                    return m.getBonusChooseBomb() == getBonusChooseBomb();
                } else if (type.equals(MoveType.CHOICE)){
                    return m.getChoicePlayer() == getChoicePlayer();
                } 
                return true;
            }
        }
        return false; 
    }

    // METHODS END
    //------------------------------------------------------------------------------
}
