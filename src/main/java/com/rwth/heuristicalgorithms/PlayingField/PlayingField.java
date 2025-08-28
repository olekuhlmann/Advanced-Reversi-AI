package com.rwth.heuristicalgorithms.PlayingField;

import static java.util.Map.entry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rwth.heuristicalgorithms.Client.Client;
import com.rwth.heuristicalgorithms.MoveChoosing.MoveHeuristic;


/**
 * This class represents a playing field consisting of tiles (map) and all the nesseccary information about the players.
 */
public class PlayingField {


    //------------------------------------------------------------------------------
    // ATTRIBUTES START
    private int[] tileScores;
    /**
     * Stores the amount of players. Values between 2 and 8.
     */
    private static int playerCount;

    /**
     * Our player number
     */
    private static int ourPlayerNum;


    /**
     * Stores the amount of remaining bombs for each player. Values between 0 and 255.
     */
    private short playerBombCount[];
    
    /**
     * Radius of the bombs.
     */
    private static short bombRadius;

    /**
     * Stores the amount of remaining override stones for each player. Values between 0 and 255.
     */
    private short playerOverrideStoneCount[];

    /**
     * Width (x-axis) of the map. Value between 1 and 50. 
     */
    private static byte mapWidth;

    /**
     * Height (y-axis) of the map. Value between 1 and 50.
     */
    private static byte mapHeight;

    /**
     * Total number of tiles that can be occupied (i.e., all tiles except holes)
     */
    private static int numberOccupiableTiles;

    /**
     * Number of tiles currently occupied by players.
     * We do NOT count expansion stones here.
     */
    private int numberOccupiedTiles;


    /**
     * Stores the curretn Game Phase. Is false when in Expansion phase and True when in elimination phase.
     */
    private boolean isEliminationPhase;

    /**
     * Stores the active, or non-qualified, players. activePlayers[i] is true when Player i+1 is still playing
     */
    private boolean activePlayers[];

    /**
     * Stores the values of each tile. Each byte represents up to 2 adjacent tiles in the same row. Dimensions are [mapHeight][mapWidth/2]. 
     * The 4 most significant bits represent the left tile and the other 4 represent the right tile.
     * Values (as seen in tile_encoding): 
     * 0000 = 0: empty tile.
     * 0001 = 1 - 1000 = 8: tile occupied by player.
     * 1001 = 9: choice tile.
     * 1010 = 10: inversion tile.
     * 1011 = 11: bonus tile.
     * 1100 = 12: expansion tile.
     * 1101 = 13: hole.
     */
    private byte map[][];
    

    /**
     * Yields the Tile for a 4-bit encoding.
     * E.g: TILE_ENCODING[3] will yield OCCUPIED_3.
     */
    public Tile[] TILE_ENCODING = {
        Tile.EMPTY,
        Tile.OCCUPIED_1, 
        Tile.OCCUPIED_2,
        Tile.OCCUPIED_3,
        Tile.OCCUPIED_4,
        Tile.OCCUPIED_5,
        Tile.OCCUPIED_6,
        Tile.OCCUPIED_7,
        Tile.OCCUPIED_8,
        Tile.CHOICE,
        Tile.INVERSION,
        Tile.BONUS,
        Tile.EXPANSION,
        Tile.HOLE
    };

    /**
     * Yields the 4-bit encoding for a Tile.
     * E.g: TILE_DECODING.get(OCCUPIED_3) will yield 3.
     */
    public HashMap<Tile, Byte> TILE_DECODING;


    /**
     * TILE_TRANSLATOR.get(OCCUPIED_X) will yield the stone that player X currently uses.
     * E.g. after players 2 and 3 swap stones, TILE_TRANSLATOR.get(OCCUPIED_2) will yield OCCUPIED_3 and vice versa.
     */
    private HashMap<Tile, Tile> TILE_TRANSLATOR;

    /**
     * TILE_TRANSLATOR.get(OCCUPIED_X) will yield the player's stone that currently uses stone OCC_X.
     * E.g. after players 2 and 3 swap stones, TILE_TRANSLATOR.get(OCCUPIED_2) will yield OCCUPIED_3 since Player 3 uses the stone OCCUPIED_3.
     */
    private HashMap<Tile, Tile> TILE_TRANSLATOR_INVERSE;


    /**
     * map that decodes chars to Tiles.
     */
    public static final Map<Character, Tile> CHAR_TILE_DECODING = Map.ofEntries(
        entry('0', Tile.EMPTY),
        entry('1', Tile.OCCUPIED_1),
        entry('2', Tile.OCCUPIED_2),
        entry('3', Tile.OCCUPIED_3),
        entry('4', Tile.OCCUPIED_4),
        entry('5', Tile.OCCUPIED_5),
        entry('6', Tile.OCCUPIED_6),
        entry('7', Tile.OCCUPIED_7),
        entry('8', Tile.OCCUPIED_8),
        entry('c', Tile.CHOICE),
        entry('i', Tile.INVERSION),
        entry('b', Tile.BONUS),
        entry('x', Tile.EXPANSION),
        entry('-', Tile.HOLE)
    );


    /**
     *  Contains the neighboring tiles for each tile. 
     *  Dimensions are [mapHeight][mapWidth][8][3]. transitions[2][3] contains the 8 [y,x,r] pairs of the tiles neighboring the tile with y = 2, x = 3.
     *  See courseRules document for ordering of the neighbours.
     *  The r bit in the [y,x,r] pair describes the direction since there may be a direction change involved when using a special transition.
     *  For default transitions, transitions[_][_][k] will always = [_,_,k].
     *  Special transitions are already considered here.
     */
    private static byte TRANSITIONS[][][][];

    /**
     * Stores the type of each tile. Dimensions are [mapHeight][mapWidth].
     * Holes are listed as default tiles.
     * Corner tiles have a maximum of 3 neighbours.
     * Edge tiles have a maximum of 5 neighbours.
     * All other tiles are default tiles.
     */
    private static TileType TILE_TYPE[][]; 


    /**
     * This array has the same size as the map and denotes whether a stone is a frontier stone or not.
     * A frontier stone has at least one neighbouring tile that is not occupied, i.e., this stone could potentially be flipped.
     */
    private boolean is_frontier_stone[][];


    /**
     * This array contains the number of frontier stones per player.
     * Note that player_num_frontier_stones[x] yields the number of stones for player x+1 
     */
    private int player_num_frontier_stones[]; 

    // ATTRIBUTES END
    //------------------------------------------------------------------------------



    //------------------------------------------------------------------------------
    // CONTRSUCTORS START

    /**
     * Constructor for PlayingField class. This is called in the readMap function.
     * @param playerCount
     * @param turn
     * @param playerBombCount
     * @param playerOverrideStoneCount
     * @param mapWidth
     * @param mapHeight
     * @param map
     */
    private PlayingField(int playerCount, short[] playerBombCount, short bombRadius, 
        short[] playerOverrideStoneCount, byte mapWidth, byte mapHeight, byte[][] map) {

        PlayingField.playerCount = playerCount;
        this.activePlayers = new boolean[playerCount];
        for(int i = 0; i < playerCount;i++) activePlayers[i] = true;
        this.isEliminationPhase = false;
        this.player_num_frontier_stones = new int[playerCount];
        this.is_frontier_stone = new boolean[mapHeight][mapWidth];
        
        this.playerBombCount = playerBombCount;
        PlayingField.bombRadius = bombRadius;
        this.playerOverrideStoneCount = playerOverrideStoneCount;
        PlayingField.mapWidth = mapWidth;
        PlayingField.mapHeight = mapHeight;
        this.map = map;

        //create Tile Decoding
        TILE_DECODING = new HashMap<>();
        TILE_DECODING.put(Tile.EMPTY, (byte) 0);
        TILE_DECODING.put(Tile.OCCUPIED_1, (byte) 1);
        TILE_DECODING.put(Tile.OCCUPIED_2, (byte) 2);
        TILE_DECODING.put(Tile.OCCUPIED_3, (byte) 3);
        TILE_DECODING.put(Tile.OCCUPIED_4, (byte) 4);
        TILE_DECODING.put(Tile.OCCUPIED_5, (byte) 5);
        TILE_DECODING.put(Tile.OCCUPIED_6, (byte) 6);
        TILE_DECODING.put(Tile.OCCUPIED_7, (byte) 7);
        TILE_DECODING.put(Tile.OCCUPIED_8, (byte) 8);
        TILE_DECODING.put(Tile.CHOICE, (byte) 9);
        TILE_DECODING.put(Tile.INVERSION, (byte) 10);
        TILE_DECODING.put(Tile.BONUS, (byte) 11);
        TILE_DECODING.put(Tile.EXPANSION, (byte) 12);
        TILE_DECODING.put(Tile.HOLE, (byte) 13);

        //create Tile translators
        TILE_TRANSLATOR = new HashMap<>();
        TILE_TRANSLATOR.put(Tile.EMPTY, Tile.EMPTY);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_1, Tile.OCCUPIED_1);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_2, Tile.OCCUPIED_2);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_3, Tile.OCCUPIED_3);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_4, Tile.OCCUPIED_4);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_5, Tile.OCCUPIED_5);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_6, Tile.OCCUPIED_6);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_7, Tile.OCCUPIED_7);
        TILE_TRANSLATOR.put(Tile.OCCUPIED_8, Tile.OCCUPIED_8);
        TILE_TRANSLATOR.put(Tile.CHOICE, Tile.CHOICE);
        TILE_TRANSLATOR.put(Tile.INVERSION, Tile.INVERSION);
        TILE_TRANSLATOR.put(Tile.BONUS, Tile.BONUS);
        TILE_TRANSLATOR.put(Tile.EXPANSION, Tile.EXPANSION);
        TILE_TRANSLATOR.put(Tile.HOLE, Tile.HOLE);      

        TILE_TRANSLATOR_INVERSE = new HashMap<>();
        TILE_TRANSLATOR_INVERSE.put(Tile.EMPTY, Tile.EMPTY);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_1, Tile.OCCUPIED_1);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_2, Tile.OCCUPIED_2);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_3, Tile.OCCUPIED_3);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_4, Tile.OCCUPIED_4);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_5, Tile.OCCUPIED_5);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_6, Tile.OCCUPIED_6);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_7, Tile.OCCUPIED_7);
        TILE_TRANSLATOR_INVERSE.put(Tile.OCCUPIED_8, Tile.OCCUPIED_8);
        TILE_TRANSLATOR_INVERSE.put(Tile.CHOICE, Tile.CHOICE);
        TILE_TRANSLATOR_INVERSE.put(Tile.INVERSION, Tile.INVERSION);
        TILE_TRANSLATOR_INVERSE.put(Tile.BONUS, Tile.BONUS);
        TILE_TRANSLATOR_INVERSE.put(Tile.EXPANSION, Tile.EXPANSION);
        TILE_TRANSLATOR_INVERSE.put(Tile.HOLE, Tile.HOLE);
        tileScores = new int[playerCount];
        
        
    }

    /**
     * Constructor for copying a PlayingField
     * @param playerBombCount number of bombs available
     * @param playerOverrideStoneCount number of override stones available
     * @param map the map
     * @param tileEncoding static BYTE->TILE translation
     * @param tileDecoding static TILE->BYTE translation
     * @param isElPh true when in elimination phase
     * @param actPl active player(not disqualified)
     * @param TILE_TRANSLATOR used for keeping track of choice/inversion moves
     * @param TILE_TRANSLATOR_INVERSE inverse of TILE_TRANSLATOR
     * @param tileScores values for calcSumOccupiedTiles
     */
    private PlayingField(
        short[] playerBombCount, short[] playerOverrideStoneCount, byte[][] map, 
        Tile[] tileEncoding, Map<Tile, Byte> tileDecoding, boolean isElPh, boolean[] actPl, 
        HashMap<Tile,Tile> TILE_TRANSLATOR, HashMap<Tile,Tile> TILE_TRANSLATOR_INVERSE, 
        int[] tileScores, int[] player_num_frontier_stones, boolean[][]is_frontier_stone, 
        int numberOccupiedTiles){


        this.playerBombCount = Arrays.copyOf(playerBombCount, playerBombCount.length);
        this.playerOverrideStoneCount = Arrays.copyOf(playerOverrideStoneCount, playerOverrideStoneCount.length);
        this.isEliminationPhase = isElPh;
        this.activePlayers = actPl;
        this.map = new byte[map.length][map[0].length];
        //copy map
        for(int i = 0; i < map.length; i++){
            this.map[i] = Arrays.copyOf(map[i], map[i].length);
        }
        //copy tile encoding
        this.TILE_ENCODING = Arrays.copyOf(tileEncoding, tileEncoding.length);
        //copy decoding
        this.TILE_DECODING = new HashMap<>(tileDecoding);
        this.TILE_TRANSLATOR = new HashMap<>(TILE_TRANSLATOR);
        this.TILE_TRANSLATOR_INVERSE = new HashMap<>(TILE_TRANSLATOR_INVERSE);
        this.tileScores = Arrays.copyOf(tileScores,tileScores.length);

        //frontier stones
        this.is_frontier_stone = new boolean[mapHeight][mapWidth];
        for(int i = 0; i < mapHeight; i++){
            this.is_frontier_stone[i] = Arrays.copyOf(is_frontier_stone[i], is_frontier_stone[i].length);
        }
        this.player_num_frontier_stones = Arrays.copyOf(player_num_frontier_stones, player_num_frontier_stones.length);
        this.numberOccupiedTiles = numberOccupiedTiles;    
    }

    // CONTRSUCTORS END
    //------------------------------------------------------------------------------



    //------------------------------------------------------------------------------
    // GETTER/SETTER START


    /**
     * translates this map back to a readbale string. For Debugging purposes
     */
    public String toString(){
        String res = "   ";
        for(byte x = 0; x < mapWidth;x++){
            if(x<10) res += " ";
            res+=" "+ x;
        }
        res+="\n";
        res+="  /";
        for(byte x = 0; x < mapWidth;x++){
            res+="---";
        }
        res+="\n";
        for(byte y = 0; y < mapHeight;y++){
            res += y;
            if(y<10) res += " ";
            res+= "|";
            for(byte x = 0; x < mapWidth;x++){
                res+="  " +
                switch(getMapTile(y, x)){
                    case HOLE -> "-";
                    case EMPTY -> "0";
                    case CHOICE -> "c";
                    case EXPANSION -> "x";
                    case BONUS -> "b";
                    case INVERSION -> "i";
                    case OCCUPIED_1 -> "1";
                    case OCCUPIED_2 -> "2";
                    case OCCUPIED_3 -> "3";
                    case OCCUPIED_4 -> "4";
                    case OCCUPIED_5 -> "5";
                    case OCCUPIED_6 -> "6";
                    case OCCUPIED_7 -> "7";
                    case OCCUPIED_8 -> "8";
                
                    default -> "";
                };

            }
            res+="\n";
        }
        return res;
    }

    /**
     * getter for player count
     * @return the current no. of players. Values between 2 and 8.
     */
    public static int getPlayerCount() {
        return playerCount;
    }

    /**
     * getter for tileScore
     * @param player plyer for which to get the score
     * @return the score
     */
    public int getTileScore(byte player) {
        return tileScores[player-1];
    }


    /**
     * getter for BombCount
     * @param player player for which to get the bomb count
     * @return get bomb count for a player
     */
    public short getPlayerBombCount(byte player) {
        return playerBombCount[player-1];
    }

    /**
     * getter for bombRadius
     * @return the bomb's radius
     */
    public static short getBombRadius() {
        return bombRadius;
    }

    /**
     * getter for OverrideStoneCount
     * @param player player for which to get the OverrideStone count
     * @return get override stone count for a player
     */
    public short getPlayerOverrideStoneCount(byte player) {
        return playerOverrideStoneCount[player-1];
    }

    /**
     * getter for our player number
     * @return our player number
     */
    public static int getOurPlayerNum() {
        return ourPlayerNum;
    }

    /**
     * getter for map width
     * @return map width
     */
    public static byte getMapWidth() {
        return mapWidth;
    }

    /**
     * getter for map height
     * @return map height
     */
    public static byte getMapHeight() {
        return mapHeight;
    }

    /**
     * getter for active players
     * @return Arrays containing disqualification status.
     */
    public boolean[] getActivePlayers() {
        return activePlayers;
    }

    /**
     * getter for game phase
     * @return Game Phase, true if in Elimination phase
     */
    public boolean getIsEliminationPhase() {
        return isEliminationPhase;
    }

    /**
     * getter for transition
     * @param x coordinate of tile
     * @param y coordinate of tile
     * @param direction of neighbour
     * @return array with y and x coordinate and new direction r of neighboring tile in given direction. 
     * {-1, -1, -1} is returned when there is a hole/map boundary.
     */
    public static byte[] getTransition(byte y, byte x, byte direction) {
        return TRANSITIONS[y][x][direction];
    }

    /**
     * getter for Map tiles.
     * Most upper left tile has coordinates (0,0).
     * @param x coordinate
     * @param y coordinate
     * @return map tile at the given coordinates 
     */
    public Tile getMapTile(byte y, byte x){
        if(y == -1 && x == -1){
            return Tile.HOLE;
        }
        if(x < 0 || x >= mapWidth || y < 0 || y >= mapHeight){
            throw new IllegalArgumentException("Invalid coordinates: x:" + x + " y: " + y + " for getMapTile");
        }

        if(x % 2 == 0){
            //even x coordinates are saved in the 4MSB 
            return TILE_TRANSLATOR_INVERSE.get(TILE_ENCODING[(map[y][x/2] >> 4) & 0x0F]); 
        } else {
            //odd x coordinates are saved in the 4LSB (-> delete the 4MSB)
            return TILE_TRANSLATOR_INVERSE.get(TILE_ENCODING[map[y][x/2] & 0b00001111]); 
        }
    }


    /**
     * getter for Tile types
     * @param y coordinate of tile
     * @param x coordinate of tile
     * @return the type of the given tile
     */
    public static TileType getTileType(byte y, byte x){
        return TILE_TYPE[y][x];
    }

    /**
     * checks if a certain player is still part of the game
     * @param player for which to get status
     * @return true if the specified player is disqualified
     */
    public boolean isDisqualified(byte player) {
        return !activePlayers[player-1];
    }

    /**
     * Returns whether a stone is a frontier stone (i.e., this stone is occupied and has at least 1 empty neighbour).
     * @param y coordinate
     * @param x coordinate
     * @return true iff (y,x) is a frontier stone.
     */
    public boolean isFrontierStone(byte y, byte x){
        if(y == -1 || x == -1) return false;
        return is_frontier_stone[y][x];
    }

    /**
     * @param player to return value for
     * @return frontier count for given player
     */
    public int getFrontierCount(byte player){
        return player_num_frontier_stones[player-1];
    }

    /**
     * @return the number of occupiable tiles (i.e., tiles that are not holes)
     */
    public int getNumberOccupiableTiles(){
        return numberOccupiableTiles;
    }

    /**
     * 
     * @return number of currently occupied tiles by any player
     */
    public int getNumberOccupiedTiles(){
        return numberOccupiedTiles;
    }

    /**
     * Returns occupiedTiles/occupiableTiles 
     * @return the percentage of occupied tiles (double between 0 and 1)
     */
    public double getPercentageOccupied(){
        return numberOccupiedTiles/(double)numberOccupiableTiles;
    }









    /**
     * Sets the frontier value for a stone.
     * @param y coordinate
     * @param x coordinate
     * @param value to be set
     */
    public void setFrontierStone(byte y, byte x, boolean value){
        is_frontier_stone[y][x] = value;
    }

    /**
     * Update the player frontier count.
     * @param player to change the value for
     * @param delta to modify the value by. 1 for an increment of 1, -1 for a decrement of 1.
     */
    public void changePlayerFrontierCount(byte player, int delta){
        if(player < 1 || player > playerCount){
            throw new IllegalStateException("[ERROR] frontier count was changed for player " + player + " which does not exist.");
        }
        player_num_frontier_stones[player-1] += delta;
    }

    /**
     * Sets the frontier count for a player. Also see the changePlayerFrontierCount method.
     * @param player to set the value for
     * @param value to be set
     */
    private void setPlayerFrontierCount(byte player, int value){
        player_num_frontier_stones[player-1] = value;
    }

    /**
     * Sets the tile info for a given coordinate.
     * For assigning a tile to player X, ALWAYS use setMapTile(OCCUPIED_X), no matter what stone player X currently uses.
     * NOTE that the computation for the number of occupied tiles DOES NOT WORK ANYMORE when using this method manually 
     * (i.e., outside of executeMove)
     * @param x coordinate
     * @param y coordinate
     * @param tile value to be set
     */
    public void setMapTile(byte y, byte x, Tile tile) {
        if(x < 0 || x >= mapWidth || y < 0 || y >= mapHeight){
            throw new IllegalArgumentException("Invalid coordinates: x:" + x + "y: " + y + " for setMapTile");
        }

        //for non-player tiles, tile_tranlation = tile. For tile = OCCUPIED_X, tile_translation is the tile that player X currently uses. 
        Tile tile_translation = TILE_TRANSLATOR.get(tile);

        if(x % 2 == 0){
            //even x coordinates are saved in the 4MSB 
            //delete 4 MSB and override with the tile
            map[y][x/2] = (byte) ((map[y][x/2] & 0b00001111) | (TILE_DECODING.get(tile_translation) << 4));
        } else {
            //odd x coordinates are saved in the 4LSB 
            //delete the 4 LSB and override with the tile
            map[y][x/2] = (byte) ((map[y][x/2] & 0b11110000) | TILE_DECODING.get(tile_translation));
        }
    }

    /**
     * changes the player bomb count by delta
     * @param player the player for which to adjust the count
     * @param delta the value by which to adjust
     */
    public void changePlayerBombCount(byte player, byte delta){
        if(playerBombCount[player-1] + delta < 0){
            throw new IllegalStateException("Changed a player bomb count to < 0");
        }
        playerBombCount[player-1] += delta;
    }

    /**
     * changes the player override stone count by delta
     * @param player the player for which to adjust the count
     * @param delta the value by which to adjust
     */
    public void changePlayerOverrideStoneCount(byte player, byte delta){
        if(playerOverrideStoneCount[player-1] + delta < 0){
            throw new IllegalStateException("Changed a player override stone count to < 0");
        }
        playerOverrideStoneCount[player-1] += delta;
    }
    /**
     * sets our player number
     * @param ourPlayerNum the new value
     */
    public static void setOurPlayerNum(int ourPlayerNum) {
        PlayingField.ourPlayerNum = ourPlayerNum;
    }


    /**
     * disqualifies a given player
     * @param pl player to disqualify
     */
    public void disqualify(byte pl) {
        activePlayers[pl-1] = false;
    }

    /**
     * starts elimination phase
     * 
     */
    public void startEliminationPhase() {
        isEliminationPhase = true;
    }

    /**
     * Changes the number of currently occupied tiles
     * @param delta value to change by
     */
    public void changeNumberOccupiedTiles(int delta){
        numberOccupiedTiles += delta;
    }




    // GETTER/SETTER END
    //------------------------------------------------------------------------------




    
    //------------------------------------------------------------------------------
    // METHODS START

    /**
     * resets all the class' static attributes
     */
    public static void reset(){
        playerCount = 0;
        mapWidth = 0;
        mapHeight = 0;
        TRANSITIONS = null;
        numberOccupiableTiles = 0;
        ourPlayerNum = 0;
        bombRadius = 0;
        Client.prevTime = new int[Client.maxDepth_TL];
    }

    /**
     * copies this playing field. primary use: simulateMove in Move
     * @return the copied field
     */
    public PlayingField copy(){
        return new PlayingField(playerBombCount, playerOverrideStoneCount, map, TILE_ENCODING, TILE_DECODING, 
        isEliminationPhase, activePlayers, TILE_TRANSLATOR, TILE_TRANSLATOR_INVERSE,tileScores, player_num_frontier_stones, 
        is_frontier_stone, numberOccupiedTiles);
    }

    /**
     * Creates a PlayingField instance from a string describing the map.
     * @param mapString received from server. Line breaks are assumed to be \n or \r\n
     * @return a PlayingField instance
     */
    public static PlayingField readMap(String mapString){
        if(mapString == null || mapString.equals("")) throw new IllegalArgumentException("readMap was provided with an empty String or null");
        //parse the easy inputs
        String[] lines = mapString.split("\r?\n"); //lines may end with \n or \r\n
        int playerCount = Integer.parseInt(lines[0].trim());
        short[] playerOverrideStoneCount = new short[playerCount];
        short[] playerBombCount = new short[playerCount];
        Arrays.fill(playerOverrideStoneCount, Short.parseShort(lines[1].trim()));
        String[] bombInfo = lines[2].trim().split(" ");
        Arrays.fill(playerBombCount, Short.parseShort(bombInfo[0]));
        short bombRadius = Short.parseShort(bombInfo[1]);

        String[] dimensions = lines[3].trim().split(" ");
        byte mapWidth = Byte.parseByte(dimensions[1]);
        byte mapHeight = Byte.parseByte(dimensions[0]);
        byte[][] map = new byte[mapHeight][mapWidth % 2 == 0 ? mapWidth/2 : mapWidth/2+1];


        //this wil be returned
        PlayingField pf = new PlayingField(playerCount, playerBombCount, bombRadius, playerOverrideStoneCount, mapWidth, mapHeight, map);
        
        //map and transitions:

        //parse map
        for (int i = 0; i < mapHeight; i++) {
            String[] row = lines[i + 4].trim().split(" ");
            for (int j = 0; j < mapWidth; j++) {
                char tile = row[j].charAt(0);
                pf.setMapTile((byte)i, (byte)j, CHAR_TILE_DECODING.get(tile));
            }
        }

        TRANSITIONS = new byte[mapHeight][mapWidth][8][3];
        //create default transitions and insert -1,-1 pairs where there is no transition
        generateTransitionMap(map, mapHeight, mapWidth);

        //special transitions
        for (int i = 4 + mapHeight; i < lines.length; i++) {
            String[] transition = lines[i].trim().split(" ");
            byte x1 = Byte.parseByte(transition[0]);
            byte y1 = Byte.parseByte(transition[1]);
            byte r1 = Byte.parseByte(transition[2]);
            //3 is the <->
            byte x2 = Byte.parseByte(transition[4]);
            byte y2 = Byte.parseByte(transition[5]);
            byte r2 = Byte.parseByte(transition[6]);

            TRANSITIONS[y1][x1][r1][0] = y2;
            TRANSITIONS[y1][x1][r1][1] = x2;
            TRANSITIONS[y1][x1][r1][2] = (byte)((r2+4) % 8);
            TRANSITIONS[y2][x2][r2][0] = y1;
            TRANSITIONS[y2][x2][r2][1] = x1;
            TRANSITIONS[y2][x2][r2][2] = (byte)((r1+4) % 8);
        }

        //set all the tile types
        TILE_TYPE = new TileType[mapHeight][mapWidth];
        for(byte i = 0; i < TILE_TYPE.length; i++){
            for(byte j = 0; j < TILE_TYPE[i].length; j++){
                TILE_TYPE[i][j] = computeTileType(i, j);
            }
        }

        for(int i = 0; i< playerCount; i++) {
            pf.tileScores[i] = MoveHeuristic.calcSumOccupiedTiles(pf, (byte) (i+1),true);
        }

        //check the frontier stones and occupied/occupiable stones
        for(byte y = 0; y < mapHeight; y++){ 
            for(byte x = 0; x < mapWidth; x++){
                //a stone is a frontier if it is occupied and has at least one neighbour that is not occupied
                byte player_occ;
                Tile t = pf.getMapTile(y,x);
                if(!t.equals(Tile.HOLE)) numberOccupiableTiles++;
                switch(t){
                    case OCCUPIED_1 -> {pf.numberOccupiedTiles++; player_occ = 1;}
                    case OCCUPIED_2 -> {pf.numberOccupiedTiles++; player_occ = 2;}
                    case OCCUPIED_3 -> {pf.numberOccupiedTiles++; player_occ = 3;}
                    case OCCUPIED_4 -> {pf.numberOccupiedTiles++; player_occ = 4;}
                    case OCCUPIED_5 -> {pf.numberOccupiedTiles++; player_occ = 5;}
                    case OCCUPIED_6 -> {pf.numberOccupiedTiles++; player_occ = 6;}
                    case OCCUPIED_7 -> {pf.numberOccupiedTiles++; player_occ = 7;}
                    case OCCUPIED_8 -> {pf.numberOccupiedTiles++; player_occ = 8;} 
                    default -> player_occ = 0;
                };
                //if the stone is occupied, check for neighbours
                if(player_occ > 0){
                    byte[] neighbour = new byte[3];
                    for(byte k = 0; k < 8; k++){
                        neighbour = getTransition(y, x, k);
                        if(Tile.isTileEmpty(pf.getMapTile(neighbour[0], neighbour[1]))){
                            //now this is a frontier stone, no need to check the other directions
                            pf.setFrontierStone(y, x, true);
                            //increment the frontier counter for the respective player
                            pf.changePlayerFrontierCount(player_occ, 1);
                            break;
                        }
                    }
                }
            }
        }


        



        return pf;
    }


    /**
     * Creates the default transitions for every tile.
     * Default transistions mean all transitions except special transitions.
     * @param map 
     */
    private static void generateTransitionMap(byte[][] map, byte mapHeight, byte mapWidth) {
    
        // Initialize the transition array with -1 values.
        for (int i = 0; i < mapHeight; i++) {
            for (int j = 0; j < mapWidth; j++) {
                for (int k = 0; k < 8; k++) {
                    TRANSITIONS[i][j][k][0] = -1;
                    TRANSITIONS[i][j][k][1] = -1;
                    TRANSITIONS[i][j][k][2] = -1;
                }
            }
        }
    

        int[][] neighborOffsets = {
            {-1, 0}, {-1, 1}, {0, 1}, {1, 1},
            {1, 0}, {1, -1}, {0, -1}, {-1, -1}
        };
        
        // Include default transitions for all neighboring tiles.
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                // Neighbors are indexed as follows:
                // 0: North, 1: Northeast, 2: East, 3: Southeast,
                // 4: South, 5: Southwest, 6: West, 7: Northwest
                
    
                for (int n = 0; n < 8; n++) {
                    int ny = y + neighborOffsets[n][0];
                    int nx = x + neighborOffsets[n][1];
    
                    // Check if the neighbor coordinates are within the map bounds.
                    if (nx >= 0 && nx < mapWidth && ny >= 0 && ny < mapHeight) {
                        // Get the tile value from the map byte array.
                        byte tileValue = (byte) (((nx % 2 == 0) ? (map[ny][nx / 2] >> 4 & 0x0F) : map[ny][nx / 2] & 0x0F));
    
                        // Check if there is a tile (= no hole) at the neighbor position.
                        if (tileValue != 13) {
                            TRANSITIONS[y][x][n][0] = (byte) ny;
                            TRANSITIONS[y][x][n][1] = (byte) nx;
                            TRANSITIONS[y][x][n][2] = (byte) n;
                        }
                    }
                }
            }
        }
    }

    
    /**
     * Computes the type of a tile. 
     * Holes are listed as default tiles.
     * Corner tiles have a maximum of 3 neighbours.
     * Edge tiles have a maximum of 5 neighbours.
     * All other tiles are default tiles.
     * @param y coordinate of tile
     * @param x coordinate of tile
     * @return the type of a tile.
     */
    private static TileType computeTileType(byte y, byte x){
        int numberOfNeighbours = 8;

        //count number of neighbours
        for(int k = 0; k < 8; k++){
            byte[] neighbour = TRANSITIONS[y][x][k];
            if(neighbour[0] == -1 && neighbour[1] == -1){
                //hole 
                numberOfNeighbours--;
            }
        }

        return switch(numberOfNeighbours){
            case 0 -> TileType.DEFAULT;
            case 1,2,3 -> TileType.CORNER;
            case 4,5 -> TileType.EDGE;
            default -> TileType.DEFAULT;
        };
    }

    

    /**
     * Executes the given move
     * @param move the move to execute
     * @param player player executing the moves
     * @return false if the move is illegal. true otherwise
     */ 
    public boolean executeMove(Move move, byte player){
        if(move == null || player < 1 || player > playerCount || move.getY() < 0 || move.getY() >= mapHeight || move.getX() < 0 || move.getX() >= mapWidth){
            throw new IllegalArgumentException("Illegal arguments for executeMove");
        }

        MoveType type = move.getMoveType();
        if(type.equals(MoveType.BOMB)){
            //player has to have at least 1 bomb
            if(getPlayerBombCount(player) <= 0 || !isBombMoveValid(move)){
                return false;
            }
            //execute bomb move
            executeBombMove(move, player);
            return true;
        }
        

        //if it is a special move, the special field should be at the coords
        if(type.equals(MoveType.BONUS) && !getMapTile(move.getY(), move.getX()).equals(Tile.BONUS)){
             return false;
        }
        if(type.equals(MoveType.CHOICE) && !getMapTile(move.getY(), move.getX()).equals(Tile.CHOICE)){
            return false;
        }

        //player has to have at least 1 overriding stone for override move and ov. moves can only be performed on occupied tiles or exapansion
        if(type.equals(MoveType.OVERRIDE) && (getPlayerOverrideStoneCount(player) <= 0 || Tile.isTileEmptyOrHole(getMapTile(move.getY(), move.getX())))){
            return false;
        }
        //compute the enclosed stones
        List<byte[]> enclosed = (move.getEnclosed() == null || move.getEnclosed().size()==0)? getEnclosedStones(move.getY(), move.getX(), player) : move.getEnclosed();
        if(enclosed == null){
            //for override moves, it's okay that no stone is enclosed IF an expansion stone is overriden
            if(!(type.equals(MoveType.OVERRIDE) && getMapTile(move.getY(), move.getX()).equals(Tile.EXPANSION))){
                return false;
            }
        }
        //at this point, at least 1 stone is enclosed (or override with expansion stone).
        //flip the enclosed stones
        if(enclosed != null){

            //before flipping the stones, change frontier stones
            changeFrontierStones(move.getY(), move.getX(), enclosed, player);

            for(byte[] tile : enclosed){
                int ovPl = TILE_DECODING.get(getMapTile(tile[0], tile[1]));
                if(ovPl<9 &&ovPl>0) {

                    tileScores[ovPl-1] -= switch(PlayingField.getTileType(tile[0], tile[1])){
                        case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
                        case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
                        case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
                    };
                }
                tileScores[player-1] += switch(PlayingField.getTileType(tile[0], tile[1])){
                    case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
                    case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
                    case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
                };
                setMapTile(tile[0], tile[1], TILE_ENCODING[player]);
                
            }
        }
        //if it is a special move, execute the special event
        if(type.equals(MoveType.BONUS)){
            if(move.getBonusChooseBomb()){
                changePlayerBombCount(player, (byte)1);
            } else {
                changePlayerOverrideStoneCount(player, (byte)1);
            }
        }
        else if (type.equals(MoveType.CHOICE)){
            //for choice moves, the selected tile has to be placed here so that it will be switched
            int ovPl = TILE_DECODING.get(getMapTile(move.getY(), move.getX()));
            if(ovPl<9 &&ovPl>0) {
                tileScores[ovPl-1] -= switch(PlayingField.getTileType(move.getY(), move.getX())){
                    case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
                    case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
                    case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
                };
            }
                
            tileScores[player-1] += switch(PlayingField.getTileType(move.getY(), move.getX())){
                case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
                case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
                case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
            };
            setMapTile(move.getY(), move.getX(), TILE_ENCODING[player]);
            numberOccupiedTiles++;
            executeChoiceMove(player, move.getChoicePlayer());
            return true;
        } else if (type.equals(MoveType.OVERRIDE)){
            //for overide moves, reduce the override stone count
            changePlayerOverrideStoneCount(player, (byte)-1);
        }
        //at this point, the move is default or inversion or override
        //check for inversion
        else if(getMapTile(move.getY(), move.getX()).equals(Tile.INVERSION)){
            int ovPl = TILE_DECODING.get(getMapTile(move.getY(), move.getX()));
            if(ovPl<9 &&ovPl>0) {
                tileScores[ovPl-1] -= switch(PlayingField.getTileType(move.getY(), move.getX())){
                    case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
                    case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
                    case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
                };
            }
                
            tileScores[player-1] += switch(PlayingField.getTileType(move.getY(), move.getX())){
                case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
                case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
                case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
            };  
            numberOccupiedTiles++;
            setMapTile(move.getY(), move.getX(), TILE_ENCODING[player]);
            executeInversionMove();
            return true;
        }
        //execute the default move (or place the override)
        int ovPl = TILE_DECODING.get(getMapTile(move.getY(), move.getX()));
        if(ovPl<9 &&ovPl>0) {
            tileScores[ovPl-1] -= switch(PlayingField.getTileType(move.getY(), move.getX())){
                case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
                case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
                case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
            };
        }
                
        tileScores[player-1] += switch(PlayingField.getTileType(move.getY(), move.getX())){
            case DEFAULT -> MoveHeuristic.RATING_OCCUPIED_DEFAULT_TILE;
            case EDGE -> MoveHeuristic.RATING_OCCUPIED_EGDE_TILE;
            case CORNER -> MoveHeuristic.RATING_OCCUPIED_CORNER_TILE;
        };
        if(Tile.isTileEmpty(getMapTile(move.getY(), move.getX()))) numberOccupiedTiles++;
        setMapTile(move.getY(), move.getX(), TILE_ENCODING[player]);
        return true;
    }

    /**
     * This method is called when a move is executed and changes the frontier stones accordingly.
     * Note that this method has to be called BEFORE the move has been executed (and thus the board changed)
     * @param y coordinate of tile where stone is placed
     * @param x coordinate of tile where stone is placed
     * @param enclosed array of enclosed stones
     * @param player that executed the move
     */
    private void changeFrontierStones(byte y, byte x, List<byte[]> enclosed, byte player){
        if(isFrontierStone(y, x)){
            //if the tile was a frontier stone before, it still is a frontier. However, the player might have changed
            //remove from the old player count
            changePlayerFrontierCount(TILE_DECODING.get(getMapTile(y, x)), -1);
            //add to new player count
            changePlayerFrontierCount(player, 1);
        } else {
            //if the stone was not a frontier before, then check if it is a frontier now
            byte[] neighbour = new byte[3];
            for(byte k = 0; k < 8; k++){
                neighbour = getTransition(y, x, k);
                if(Tile.isTileEmpty(getMapTile(neighbour[0], neighbour[1]))){
                    //now this is a frontier stone, no need to check the other directions
                    setFrontierStone(y, x, true);
                    //increment the frontier counter for the respective player
                    changePlayerFrontierCount(player, 1);
                    break;
                }
            }
        }
        //now we deal with all 8 neighbours. they might no longer be frontiers since we might have surrounded them now.
        byte[] neighbour = new byte[3];
        for(byte k = 0; k < 8; k++){
            neighbour = getTransition(y, x, k);
            if(isFrontierStone(neighbour[0], neighbour[1])){
                //check if they are still frontiers
                byte[] neighbour2 = new byte[3];
                boolean stillFrontier = false;
                for(byte i = 0; i < 8 && !stillFrontier; i++){
                    neighbour2 = getTransition(neighbour[0], neighbour[1], i);
                    if(Tile.isTileEmpty(getMapTile(neighbour2[0], neighbour2[1]))){
                        //this stone is still a frontier.
                        stillFrontier = true;
                    }
                }
                if(!stillFrontier){
                     //no empty neighbours were found -> no longer a frontier
                    setFrontierStone(neighbour[0], neighbour[1], false);
                    //change the count
                    changePlayerFrontierCount(TILE_DECODING.get(getMapTile(neighbour[0], neighbour[1])), -1);
                }
            }
        }
        //now we handle all the enclosed stones.
        //if an enclosed stone is a frontier, the player counts have to change occordingly.
        for(byte[] enc : enclosed){
            if(isFrontierStone(enc[0], enc[1])){
                //change the counts
                changePlayerFrontierCount(TILE_DECODING.get(getMapTile(enc[0], enc[1])), -1);
                changePlayerFrontierCount(player, 1);
            }
        }
    }


    /**
     * Bomb moves are valid iff the targeted tile is not a hole.
     * @param move
     * @return whether the given bomb move is valid.
     */
    private boolean isBombMoveValid(Move move){
        return !getMapTile(move.getY(), move.getX()).equals(Tile.HOLE);
    }

    /**
     * calculates the tiles enclosed by a move
     * @param y y-coordinate of the move
     * @param x x-coordinate of the move
     * @param player player for which to calculate the moves
     * @return a list of enclosed stones. null if there are none
     */
    public List<byte[]> getEnclosedStones(byte y, byte x, byte player){
        //LinkedList is best since we only need to access the first and last item
        LinkedList<byte[]> enclosed = new LinkedList<>();
        //has entries {y,x,r} for the current observed tile and the direction r
        byte[] cur;
        //check all 8 directions
        for(int k = 0; k < 8; k++){
            //counter of logged enclosed stones for this direction
            int counter = 0;
            //set starting point
            cur = TRANSITIONS[y][x][k];
            //check type of adjacent tile. enclosement only possible if it is occupied by another player or an expansion stone
            if(!isTileOccupiedByAnother(cur[0], cur[1], player)){
                continue;
            }
            enclosed.add(new byte[]{cur[0], cur[1]});
            counter++;
            cur = TRANSITIONS[cur[0]][cur[1]][cur[2]];
            //check type of all other tiles. stop when there is an empty/hole tile or an own tile. 
            // avoid infinite loops by stopping when the original tile was reached
            while(true){
                if(cur[0] == y && cur[1] == x) {
                    for(int i = 0; i < counter; i++){
                        enclosed.removeLast();
                    }
                    break;
                }
                Tile t = getMapTile(cur[0], cur[1]);
                //if it is an own tile, all the enclosed tiles are logged. Continue with the next direction
                if(t.equals(TILE_ENCODING[player])){
                    break;
                }
                //if there is an empty tile or a hole, stop looking in this direction. Also remove all the logged enclosed stones for this direction
                if(!isTileOccupied(cur[0], cur[1])){
                    //remove all the logged stones for this direction
                    for(int i = 0; i < counter; i++){
                        enclosed.removeLast();
                    }
                    break;
                }
                enclosed.add(new byte[]{cur[0], cur[1]});
                counter++;
                cur = TRANSITIONS[cur[0]][cur[1]][cur[2]];
            }
        }
        //if all directions are checked
        return enclosed.isEmpty() ? null : enclosed;
    }

    /**
     * checls if a tiles is occupied(expansion or player other than specified player)
     * @param y coordinate of the tile
     * @param x coordinate of the tile
     * @param player the exception to the occupied status
     * @return true if the (y,x) tile is an expansion stone or occupied by anyone other than player  
     */
    private boolean isTileOccupiedByAnother(byte y, byte x, byte player){
        Tile t = getMapTile(y, x);
        if(t.equals(Tile.EMPTY) || t.equals(Tile.HOLE) || t.equals(TILE_ENCODING[player]) || t.equals(Tile.BONUS) || t.equals(Tile.CHOICE) || t.equals(Tile.INVERSION)){
            return false;
        }
        return true;
    }

    /**
     * checks if a tile is occupied(expansion or any player)
     * @param y coordinate of the tile
     * @param x coordinate of the tile
     * @return true if the tile is occupied by a player or an expansion stone 
     */
    public boolean isTileOccupied(byte y, byte x){
        Tile t = getMapTile(y, x);
        return t != Tile.HOLE && t != Tile.EMPTY && t != Tile.BONUS && t != Tile.CHOICE && t != Tile.INVERSION;
    }


    /*
     * 0 0 0 0 0
     * 0 0 0 - 0
     * 0 0 x 0 0
     * 0 0 0 0 0
     * 0 0 0 0 0
     * 
     */


    /**
     * Executes a bomb move for the given player at the given tile.
     * @param move
     * @param player
     */
    private void executeBombMove(Move move, byte player){
        //decrease bomb count
        changePlayerBombCount(player, (byte)-1);
        short r = getBombRadius();
        LinkedList<byte[]> hit = executeBombMove(move.getX(), move.getY(), (short) (r));
        for(byte[] t:hit) {
            setMapTile(t[1], t[0], Tile.HOLE);
        }
        
    }
    private LinkedList<byte[]> executeBombMove(byte x, byte y, short r){ 
        if((x<0 || y<0 || x>=getMapWidth() || y>=getMapHeight())) {
            return new LinkedList<>();
        }
        LinkedList<byte[]> hit = new LinkedList<>();
        if(r>0) {
            for(int i = 0; i < 8; i++) {
                byte newY = TRANSITIONS[y][x][i][0];
                byte newX = TRANSITIONS[y][x][i][1];
                if(!getMapTile(newY, newX).equals(Tile.HOLE)) {
                    hit.addAll(executeBombMove(newX,newY, (short)(r-1)));
                    
                }
            } 
        }
        hit.add(new byte[]{x,y});
        return hit;
    }
    




    /**
     * Swaps the stones of both players. 
     * This is done by swapping the encoding of the tiles OCCUPIED_player1 and OCCUPIED_player2.
     * @param player1
     * @param player2
     */
    private void executeChoiceMove(byte player1, byte player2){
        //up to this, p1 used this stone:
        Tile p1_old_tile = TILE_TRANSLATOR.get(TILE_ENCODING[player1]);

        //up to this, p2 used this stone:
        Tile p2_old_tile = TILE_TRANSLATOR.get(TILE_ENCODING[player2]);


        //the stone that p2 used before is now used by p1
        TILE_TRANSLATOR_INVERSE.put(p2_old_tile, TILE_ENCODING[player1]);
        //the stone that p1 used before is now used by p2
        TILE_TRANSLATOR_INVERSE.put(p1_old_tile, TILE_ENCODING[player2]);


        
        //P1 now uses the stone that p2 used before
        TILE_TRANSLATOR.put(TILE_ENCODING[player1], p2_old_tile);
        //P2 now uses the stone that p1 used before
        TILE_TRANSLATOR.put(TILE_ENCODING[player2], p1_old_tile);

        int temp = tileScores[player1-1];
        tileScores[player1-1] = tileScores[player2-1];
        tileScores[player2-1] = temp;

        //swap frontier stone counts
        int temp2 = getFrontierCount(player1);
        setPlayerFrontierCount(player1, getFrontierCount(player2));
        setPlayerFrontierCount(player2, temp2);
    }

    /**
     * Swaps the stones of all players according to the course rules. 
     */
        /*
        playerCount-1 swaps are necessary:
        1 2 3 4 5 6 7 8
        2 1 3 4 5 6 7 8
        2 3 1 4 5 6 7 8
        2 3 4 1 5 6 7 8
        2 3 4 5 1 6 7 8
        2 3 4 5 6 1 7 8
        2 3 4 5 6 7 1 8
        2 3 4 5 6 7 8 1
        */
    private void executeInversionMove(){
        //do choice moves for all players
        for(byte i = 1; i < playerCount; i++){
            executeChoiceMove((byte)1, (byte)(i+1));
        } 
    }



    // METHODS END
    //------------------------------------------------------------------------------
    

}
