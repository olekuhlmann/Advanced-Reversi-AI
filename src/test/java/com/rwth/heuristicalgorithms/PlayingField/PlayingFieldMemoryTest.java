package com.rwth.heuristicalgorithms.PlayingField;


//import org.junit.*;
import org.openjdk.jol.info.ClassLayout;
/*
 * Compares the memory usage of byte vs enum for storing the map.
 * Source: https://www.baeldung.com/jvm-measuring-object-sizes
 * Dependency: org.openjdk.jol, jol-core
 */

 /**
  * Test class for early comparison of memory usage when storing PlayingField
  */
public class PlayingFieldMemoryTest {
    /**
     * Test using enums
     */
    //@Test
    public void playingFieldMemoryTestEnum(){
        System.out.println("-------------------------------");
        System.out.println("Testing ENUM");
        
        
        Tile[][] map = new Tile[50][50];
        for(int i = 0; i < 50; i++){
            for(int j = 0; j < 50; j++){
                map[i][j] = Tile.EMPTY;
            }
        }
        System.out.println("----------------");
        System.out.println("Size of map:");
        System.out.println(ClassLayout.parseInstance(map).toPrintable());
        System.out.println("----------------");
        System.out.println("Size of map[]:");
        System.out.println(ClassLayout.parseInstance(map[1]).toPrintable());
        System.out.println("----------------");
        System.out.println("Size of map[][] (Tile):");
        System.out.println(ClassLayout.parseInstance(map[1][1]).toPrintable());
        System.out.println("-------------------------------");
    }
    /**
     * test using nibbles
     */
    //@Test
    public void playingFieldMemoryTestByte(){
        System.out.println("-------------------------------");
        System.out.println("Testing byte");
        
        System.out.println("----------------");
        byte[][] map = new byte[25][50];
        for(int i = 0; i < 25; i++){
            for(int j = 0; j < 50; j++){
                map[i][j] = 1;
            }
        }
        System.out.println("----------------");
        System.out.println("Size of map:");
        System.out.println(ClassLayout.parseInstance(map).toPrintable());
        System.out.println("----------------");
        System.out.println("Size of map[]:");
        System.out.println(ClassLayout.parseInstance(map[1]).toPrintable());
        System.out.println("----------------");
        System.out.println("Size of map[][] (byte):");
        System.out.println(ClassLayout.parseInstance(map[1][1]).toPrintable());
        System.out.println("-------------------------------");
    }

    
}
