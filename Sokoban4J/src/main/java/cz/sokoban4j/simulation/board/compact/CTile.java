package cz.sokoban4j.simulation.board.compact;

import cz.sokoban4j.simulation.board.oop.EEntity;
import cz.sokoban4j.simulation.board.oop.EPlace;
import cz.sokoban4j.simulation.board.oop.ESpace;
import cz.sokoban4j.simulation.board.oop.Tile;

/**
 * CTile stands for "Compact Tile". It contains many static methods for querying "tileFlag", obtainable via {@link Tile#computeTileFlag()}.
 * 
 * @author Jimmy
 */
public class CTile {
	
	private static final int spaceFree;
	private static final int spaceWall;
	private static final int entityNone;
	private static final int entityPlayer;
	private static final int entitySomeBox;	
	private static final int[] entitySpecificBox;
	private static final int placeSomeBox;
	private static final int placeAnyBox;
	private static final int[] placeSpecificBox;
	
	static {
		spaceFree = ESpace.FREE.getFlag();
		spaceWall = ESpace.WALL.getFlag();
		
		entityNone = EEntity.NONE.getFlag();
		
		entityPlayer = EEntity.PLAYER.getFlag();
		
		int tempEntitySomeBox = 0;
		for (EEntity entity : EEntity.values()) {
			if (entity.isSomeBox()) tempEntitySomeBox |= entity.getFlag();
		}
		entitySomeBox = tempEntitySomeBox;
		
		entitySpecificBox = new int[]{0, EEntity.BOX_1.getFlag(), EEntity.BOX_2.getFlag(), EEntity.BOX_3.getFlag(), EEntity.BOX_4.getFlag(), EEntity.BOX_5.getFlag(), EEntity.BOX_6.getFlag() }; 
		
		int tempPlaceSomeBox = 0;
		for (EPlace place : EPlace.values()) {
			if (place.forSomeBox()) tempPlaceSomeBox |= place.getFlag();
		}
		placeSomeBox = tempPlaceSomeBox;
		
		placeAnyBox = EPlace.BOX_ANY.getFlag();

		placeSpecificBox = new int[]{0, EPlace.BOX_1.getFlag(), EPlace.BOX_2.getFlag(), EPlace.BOX_2.getFlag(), EPlace.BOX_3.getFlag(), EPlace.BOX_4.getFlag(), EPlace.BOX_5.getFlag() }; 
	}
	
	private static boolean isThis(int whatFlag, int tileFlag) {
		return (whatFlag & tileFlag) != 0;
	}
	
	public static boolean isFree(int tileFlag) {
		return isThis(spaceFree, tileFlag) && isThis(entityNone, tileFlag);
	}
	
	public static boolean isWall(int tileFlag) {
		return isThis(spaceWall, tileFlag);
	}
	
	public static boolean isPlayer(int tileFlag) {
		return isThis(entityPlayer, tileFlag);
	}
	
	public static boolean isSomeBox(int tileFlag) {
		return isThis(entitySomeBox, tileFlag);
	}
	
	public static boolean isBox(int boxNum, int tileFlag) {
		return isThis(entitySpecificBox[boxNum], tileFlag);
	}
	
	public static boolean forSomeBox(int tileFlag) {
		return isThis(placeSomeBox, tileFlag);
	}
	
	public static boolean forAnyBox(int tileFlag) {
		return isThis(placeAnyBox, tileFlag);
	}
	
	public static boolean forBox(int boxNum, int tileFlag) {
		return isThis(placeSpecificBox[boxNum], tileFlag);
	}

	public static int getBoxNum(int tileFlag) {
		if ((tileFlag & EEntity.BOX_1.getFlag()) != 0) return 1;
		if ((tileFlag & EEntity.BOX_2.getFlag()) != 0) return 2;
		if ((tileFlag & EEntity.BOX_3.getFlag()) != 0) return 3;
		if ((tileFlag & EEntity.BOX_4.getFlag()) != 0) return 4;
		if ((tileFlag & EEntity.BOX_5.getFlag()) != 0) return 5;
		if ((tileFlag & EEntity.BOX_6.getFlag()) != 0) return 6;
		return -1;
	}
	
}
