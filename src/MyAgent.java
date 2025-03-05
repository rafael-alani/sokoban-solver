import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;
import game.board.compact.CTile;

/**
 * Optimized Tree-DFS agent using array-based structures.
 */
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;
	protected boolean[][] deadSquares;

	// Pre-allocate arrays for better performance
	private static final int MAX_GOALS = 10;
	private int[][] goalCoords;
	private int goalCount;
	private CAction[] possibleActions;

	public MyAgent() {
		goalCoords = new int[MAX_GOALS][2];
		// Pre-allocate array for all possible actions (moves + pushes)
		possibleActions = new CAction[8]; // 4 moves + 4 pushes
		int i = 0;
		for (CMove move : CMove.getActions()) {
			possibleActions[i++] = move;
		}
		for (CPush push : CPush.getActions()) {
			possibleActions[i++] = push;
		}
	}

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

			// Update goals array
			goalCount = 0;
			for (int x = 0; x < state.width(); x++) {
				for (int y = 0; y < state.height(); y++) {
					if (CTile.forSomeBox(state.tile(x, y))) {
						goalCoords[goalCount][0] = x;
						goalCoords[goalCount][1] = y;
						goalCount++;
					}
				}
			}

			// Calculate distances using pre-allocated arrays
			for (int x = 0; x < state.width(); x++) {
				for (int y = 0; y < state.height(); y++) {
					if (CTile.isSomeBox(state.tile(x, y))) {
						// Check for dead squares
						if (deadSquares[x][y] && !CTile.forSomeBox(state.tile(x, y))) {
							totalEstimate += 300;
							continue;
						}

						// Find minimum Manhattan distance to goals
						double minDistance = Double.MAX_VALUE;
						for (int i = 0; i < goalCount; i++) {
							double distance = Math.abs(x - goalCoords[i][0]) + Math.abs(y - goalCoords[i][1]);
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
			// Use ArrayList with initial capacity for better performance
			List<CAction> actions = new ArrayList<>(8);

			// Check pre-allocated possible actions
			for (CAction action : possibleActions) {
				if (action != null && action.isPossible(state)) {
					actions.add(action);
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
