import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;

import game.board.compact.CTile;

/**
 * The simplest Tree-DFS agent.
 * 
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;
	protected boolean[][] deadSquares;

	@Override
	protected List<EDirection> think(BoardCompact board) {
		this.board = board;
		searchedNodes = 0;
		long searchStartMillis = System.currentTimeMillis();
		deadSquares = DeadSquareDetector.detect(board);

		SokobanProblem problem = new SokobanProblem();
		Solution2<BoardCompact, CAction> solution = AStar.search(problem);

		List<EDirection> result = new ArrayList<>();
		if (solution != null && !solution.actions.isEmpty()) {
			for (CAction action : solution.actions) {
				result.add(action.getDirection());
			}
		}

		long searchTime = System.currentTimeMillis() - searchStartMillis;
		if (verbose) {
			out.println("Nodes visited: " + searchedNodes);
			out.printf("Performance: %.1f nodes/sec\n",
					((double) searchedNodes / (double) searchTime * 1000));
		}

		return result.isEmpty() ? null : result;
	}

	public class SokobanProblem implements HeuristicProblem<BoardCompact, CAction> {
		@Override
		public double estimate(BoardCompact state) {
			double totalEstimate = 0;
			boolean[][] deadSquares = MyAgent.this.deadSquares;
			// IGNORE THE COLOR OF THE BOXES, only loses speed
			// tried keeping a map of the closes box and hole in state and update but slower
			List<int[]> goals = new ArrayList<>();
			for (int x = 0; x < state.width(); x++) {
				for (int y = 0; y < state.height(); y++) {
					if (CTile.forSomeBox(state.tile(x, y))) {
						goals.add(new int[] { x, y });
					}
				}
			}

			// // maybe keep track of assigned goals?
			for (int x = 0; x < state.width(); x++) {
				for (int y = 0; y < state.height(); y++) {
					if (CTile.isSomeBox(state.tile(x, y))) {
						// box is fucked
						if (deadSquares[x][y] && !CTile.forSomeBox(state.tile(x, y))) {
							totalEstimate += 300; // play with this, to get best result
							continue;
						}

						// manhatan but walls are hevily penilised
						// honestly can be removed, does nothing
						double minDistance = Double.MAX_VALUE;
						for (int[] goal : goals) {
							double distance = Math.abs(x - goal[0]) + Math.abs(y - goal[1]);
							minDistance = Math.min(minDistance, distance);
						}
						totalEstimate += minDistance;
					}
				}
			}
			return totalEstimate;
		}

		@Override
		public BoardCompact initialState() {
			return board;
		}

		@Override
		public List<CAction> actions(BoardCompact state) {
			List<CAction> actions = new ArrayList<CAction>(4);
			for (CMove move : CMove.getActions()) {
				if (move.isPossible(state)) {
					actions.add(move);
				}
			}
			for (CPush push : CPush.getActions()) {
				if (push.isPossible(state)) {
					actions.add(push);
				}
			}

			return actions;
		}

		@Override
		public BoardCompact result(BoardCompact state, CAction action) {
			BoardCompact newState = state.clone();
			action.perform(newState);
			return newState;
		}

		@Override
		public boolean isGoal(BoardCompact state) {
			return state.isVictory();
		}

		@Override
		public double cost(BoardCompact state, CAction action) {
			return (action instanceof CPush) ? 1.1 : 1.0;
		}
	}
}
