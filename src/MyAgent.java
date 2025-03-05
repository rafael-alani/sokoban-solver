import java.lang.System;
import java.util.ArrayList;
import java.util.List;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.slim.*;
import game.board.slim.BoardSlim;
import game.board.slim.STile;
import game.board.compact.BoardCompact;
import game.board.compact.CTile;

/**
 * The simplest Tree-DFS agent.
 * 
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
	protected BoardSlim board;
	protected BoardCompact compactBoard;
	protected int searchedNodes;

	@Override
	protected List<EDirection> think(BoardCompact compactBoard) {
		this.compactBoard = compactBoard;
		this.board = convertToSlim(compactBoard);
		searchedNodes = 0;
		long searchStartMillis = System.currentTimeMillis();

		SokobanProblem problem = new SokobanProblem();
		Solution2<BoardSlim, SAction> solution = AStar.search(problem);

		List<EDirection> result = new ArrayList<>();
		if (solution != null && !solution.actions.isEmpty()) {
			for (SAction action : solution.actions) {
				result.add(action.getDirection());
			}
		}

		long searchTime = System.currentTimeMillis() - searchStartMillis;
		if (verbose) {
			System.out.println("Nodes visited: " + searchedNodes);
			System.out.printf("Performance: %.1f nodes/sec\n",
					((double) searchedNodes / (double) searchTime * 1000));
		}

		return result.isEmpty() ? null : result;
	}

	public class SokobanProblem implements HeuristicProblem<BoardSlim, SAction> {
		boolean[][] deadSquares;

		// public SokobanProblem(BoardCompact board) {
		// this.deadSquares = DeadSquareDetector.detect(board);
		// }

		@Override
		public double estimate(BoardSlim state) {
			double totalEstimate = 0;

			boolean[][] deadSquares = DeadSquareDetector.detect(compactBoard);
			// IGNORE THE COLOR OF THE BOXES, only loses speed
			// tried keeping a map of the closes box and hole in state and update but slower
			List<int[]> goals = new ArrayList<>();
			for (int x = 0; x < state.width(); x++) {
				for (int y = 0; y < state.height(); y++) {
					if (STile.forBox(state.tile(x, y))) {
						goals.add(new int[] { x, y });
					}
				}
			}

			// // maybe keep track of assigned goals?
			for (int x = 0; x < state.width(); x++) {
				for (int y = 0; y < state.height(); y++) {
					if (STile.isBox(state.tile(x, y))) {

						// box is fucked
						if (deadSquares[x][y] && !STile.forBox(state.tile(x, y))) {
							totalEstimate += 300; // play with this, to get best result
							continue;
						}

						// }
						// }
						// }

						// manhatan but walls are hevily penilised
						// honestly can be removed, does nothing
						double minDistance = Double.MAX_VALUE;
						for (int[] goal : goals) {
							double distance = Math.abs(x - goal[0]) + Math.abs(y - goal[1]);

							if (x != goal[0]) { // If we need horizontal movement
								int startX = Math.min(x, goal[0]) + 1;
								int endX = Math.max(x, goal[0]);
								for (int px = startX; px < endX; px++) {
									if (STile.isWall(state.tile(px, y))) {
										distance += 10;
									}
								}
							}

							if (y != goal[1]) {
								int startY = Math.min(y, goal[1]) + 1;
								int endY = Math.max(y, goal[1]);
								for (int py = startY; py < endY; py++) {
									if (STile.isWall(state.tile(x, py))) {
										distance += 10;
									}
								}
							}

							minDistance = Math.min(minDistance, distance);
						}
						totalEstimate += minDistance;
					}
				}
			}
			return totalEstimate;
		}

		@Override
		public BoardSlim initialState() {
			return board;
		}

		@Override
		public List<SAction> actions(BoardSlim state) {
			List<SAction> actions = new ArrayList<SAction>(4);
			for (SMove move : SMove.getActions()) {
				if (move.isPossible(state)) {
					actions.add(move);
				}
			}
			for (SPush push : SPush.getActions()) {
				if (push.isPossible(state)) {
					actions.add(push);
				}
			}

			return actions;
		}

		@Override
		public BoardSlim result(BoardSlim state, SAction action) {
			BoardSlim newState = state.clone();
			action.perform(newState);
			return newState;
		}

		@Override
		public boolean isGoal(BoardSlim state) {
			return state.isVictory();
		}

		@Override
		public double cost(BoardSlim state, SAction action) {
			return (action instanceof SPush) ? 1.1 : 1.0;
		}
	}

	private BoardSlim convertToSlim(BoardCompact compact) {
		BoardSlim slim = new BoardSlim((byte) compact.width(), (byte) compact.height());

		// Copy the board state
		for (int x = 0; x < compact.width(); x++) {
			for (int y = 0; y < compact.height(); y++) {
				byte tile = 0;

				// Convert tiles
				if (CTile.isWall(compact.tile(x, y))) {
					tile |= STile.WALL_FLAG;
				}
				if (CTile.isSomeBox(compact.tile(x, y))) {
					tile |= STile.BOX_FLAG;
				}
				if (CTile.forSomeBox(compact.tile(x, y))) {
					tile |= STile.PLACE_FLAG;
				}
				if (CTile.isPlayer(compact.tile(x, y))) {
					tile |= STile.PLAYER_FLAG;
					slim.playerX = (byte) x;
					slim.playerY = (byte) y;
				}

				slim.tiles[x][y] = tile;
			}
		}

		// Update box counts
		slim.boxCount = 0;
		slim.boxInPlaceCount = 0;
		for (int x = 0; x < slim.width(); x++) {
			for (int y = 0; y < slim.height(); y++) {
				if (STile.isBox(slim.tile(x, y))) {
					slim.boxCount++;
					if (STile.forBox(slim.tile(x, y))) {
						slim.boxInPlaceCount++;
					}
				}
			}
		}

		return slim;
	}
}
