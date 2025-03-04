import game.board.compact.BoardCompact;
import game.board.compact.CTile;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class DeadSquareDetector {
         // Our cute BFS approach
        // We will spread aliveness so basically anything that the box can be pushed from to the goal state is alive
        // See if our player can pull the box from the cell or not
        public static boolean[][] detect(BoardCompact state) {
            int w = state.width();
            int h = state.height();
            boolean[][] live = new boolean[w][h];
            boolean[][] dead = new boolean[w][h];
            Queue<Point> queue = new LinkedList<>();

            // Initialize: add all goal cells (cells with a goal marker) to the queue.
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (CTile.forSomeBox(state.tile(x, y))) {
                        live[x][y] = true;
                        queue.add(new Point(x, y));
                    }
                }
            }

            // Directions representing up, down, left, right.
            int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

            // Reverse BFS: try to "pull" the stone back from live cells.
            while (!queue.isEmpty()) {
                Point current = queue.poll();
                for (int[] d : directions) {
                    int ax = current.x - d[0];
                    int ay = current.y - d[1];
                    int px = ax - d[0];
                    int py = ay - d[1];

                    // Check bounds for both the candidate stone cell and player cell.
                    if (ax < 0 || ax >= w || ay < 0 || ay >= h) continue;
                    if (px < 0 || px >= w || py < 0 || py >= h) continue;

                    // Only mark as live if both the candidate stone cell and player cell are not walls.
                    if (!CTile.isWall(state.tile(ax, ay)) &&
                            !CTile.isWall(state.tile(px, py)) &&
                            !live[ax][ay]) {
                        live[ax][ay] = true;
                        queue.add(new Point(ax, ay));
                    }
                }
            }

            // All non-wall cells not marked as live are dead positions.
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    if (!CTile.isWall(state.tile(x, y)) && !live[x][y]) {
                        dead[x][y] = true;
                    }
                }
            }
            return dead;
        }
    // public static boolean[][] detect(BoardCompact state) {
    //     // What we do here is to focus on lines between two walls, first in y then in x
    //     // We do this because in the case that there is a box in between two walls but
    //     // there is another space in two walls in the same row/column we don't act as
    //     // there is a reward in the row
    //     // I like to call this space between two walls a closure
    //     // Then after detecting this closure and having all the tiles inside it we
    //     // firstly always mark corners as dead because corners are always dead unless
    //     // there is a box that needs to go there
    //     // In the case there is a reward inside the closure only corners will be marked
    //     // After having the extremes of the closure evaluated we need to check the
    //     // inside tiles
    //     // Inside tiles are only marked if both extremes are corners and if there is a
    //     // continuos wall in between them
    //     // Until now this works. I think that with massive boards it might take a while
    //     // because of the amount of tiles that we quarry so im sure there is room for
    //     // optimization
    //     boolean[][] dead = new boolean[state.width()][state.height()];
    //     for (int x = 1; x < state.width() - 1; x++) {
    //         boolean contains_goal = false;
    //         Set<Integer> closures = new HashSet<Integer>();
    //         for (int y = 0; y < state.height(); y++) {
    //             if (CTile.isWall(state.tile(x, y))) {
    //                 Queue<Integer> secondRound = new LinkedList<>();
    //                 int c = 0;
    //                 int size = closures.size();
    //                 boolean top = false;
    //                 boolean bottom = false;
    //                 boolean continuos = true;
    //                 int side = 0;
    //                 int old_tile = 0;
    //                 for (int tile : closures) {
    //                     if (!contains_goal) {
    //                         if ((CTile.isWall(state.tile(x, tile - 1)) || CTile.isWall(state.tile(x, tile + 1)))) {
    //                             if (c == 0) {
    //                                 dead[x][tile] = true;
    //                                 top = true;
    //                                 if (CTile.isWall(state.tile(x - 1, tile))) {
    //                                     side = -1;
    //                                 } else {
    //                                     side = +1;
    //                                 }
    //                             } else if (c == size - 1) {
    //                                 bottom = true;
    //                                 dead[x][tile] = true;
    //                             }

    //                         } else if (CTile.isWall(state.tile(x + side, tile)) && tile - old_tile == 1) {
    //                             secondRound.add(tile);
    //                         } else {
    //                             continuos = false;
    //                         }
    //                     } else {
    //                         if ((CTile.isWall(state.tile(x, tile - 1)) || CTile.isWall(state.tile(x, tile + 1)))
    //                                 && !CTile.forSomeBox(state.tile(x, tile))) {
    //                             dead[x][tile] = true;
    //                         }
    //                     }
    //                     c++;
    //                     old_tile = tile;
    //                 }
    //                 if (top && bottom && continuos) {
    //                     while (!secondRound.isEmpty()) {
    //                         int tile = secondRound.poll();
    //                         dead[x][tile] = true;

    //                     }
    //                 }
    //                 contains_goal = false;
    //                 closures.clear();
    //             } else {
    //                 if (CTile.isWall(state.tile(x - 1, y)) || CTile.isWall(state.tile(x + 1, y))) {
    //                     closures.add(y);
    //                 }
    //                 if (CTile.forSomeBox(state.tile(x, y))) {
    //                     contains_goal = true;
    //                 }
    //             }
    //         }
    //     }
    //     for (int y = 1; y < state.height() - 1; y++) {
    //         boolean contains_goal = false;
    //         Set<Integer> closures = new HashSet<>();
    //         for (int x = 0; x < state.width(); x++) {
    //             if (CTile.isWall(state.tile(x, y))) {
    //                 Queue<Integer> secondRound = new LinkedList<>();
    //                 int c = 0;
    //                 int size = closures.size();
    //                 boolean top = false;
    //                 boolean bottom = false;
    //                 boolean continuos = true;
    //                 int side = 0;
    //                 int old_tile = 0;
    //                 for (int tile : closures) {
    //                     if (!contains_goal) {
    //                         if ((CTile.isWall(state.tile(tile - 1, y)) || CTile.isWall(state.tile(tile + 1, y)))) {
    //                             if (c == 0) {
    //                                 dead[tile][y] = true;
    //                                 top = true;
    //                                 if (CTile.isWall(state.tile(tile, y - 1))) {
    //                                     side = -1;
    //                                 } else {
    //                                     side = +1;
    //                                 }
    //                             } else if (c == size - 1) {
    //                                 bottom = true;
    //                                 dead[tile][y] = true;
    //                             }

    //                         } else if (CTile.isWall(state.tile(tile, y + side)) && tile - old_tile == 1) {
    //                             secondRound.add(tile);
    //                         } else {
    //                             continuos = false;
    //                         }
    //                     } else {
    //                         if ((CTile.isWall(state.tile(tile - 1, y)) || CTile.isWall(state.tile(tile + 1, y)))
    //                                 && !CTile.forSomeBox(state.tile(tile, y))) {
    //                             dead[tile][y] = true;
    //                         }
    //                     }
    //                     c++;
    //                     old_tile = tile;
    //                 }
    //                 if (top && bottom && continuos) {
    //                     while (!secondRound.isEmpty()) {
    //                         int tile = secondRound.poll();
    //                         dead[tile][y] = true;

    //                     }
    //                 }
    //                 contains_goal = false;
    //                 closures.clear();
    //             } else {
    //                 if (CTile.isWall(state.tile(x, y - 1)) || CTile.isWall(state.tile(x, y + 1))) {
    //                     closures.add(x);
    //                 }
    //                 if (CTile.forSomeBox(state.tile(x, y))) {
    //                     contains_goal = true;
    //                 }
    //             }
    //         }
    //     }
    //     return dead;
    // }
}
