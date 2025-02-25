import static java.lang.System.out;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;

import game.board.compact.CTile;
import search.*;

/**
 * The simplest Tree-DFS agent.
 * 
 * @author Jimmy
 */
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;

	@Override
	protected List<EDirection> think(BoardCompact board) {
		this.board = board;
		searchedNodes = 0;
		long searchStartMillis = System.currentTimeMillis();

		SokobanProblem problem = new SokobanProblem();
		List<EDirection> result = null;
		try {
			result = mctsSearch(board, 500);
		} catch (Exception e) {
			if (verbose) {
				out.println("MCTS failed, falling back to DFS: " + e.getMessage());
			}
		}

		// if (result == null || result.isEmpty()) {
		// result = new ArrayList<EDirection>();
		// dfs(20, result);
		// }

		long searchTime = System.currentTimeMillis() - searchStartMillis;

		if (verbose) {
			out.println("Nodes visited: " + searchedNodes);
			out.printf("Performance: %.1f nodes/sec\n",
					((double) searchedNodes / (double) searchTime * 1000));
		}

		return result.isEmpty() ? null : result;
	}

	public  List<EDirection> mctsSearch (BoardCompact state, int iterations) {
		return List.of();
	}
	private class MCTSNode {
		MCTSNode parent;
		CAction action;
		BoardCompact state;
		List<MCTSNode> children;
		int visits;
		double totalValue;
		double alpha;
		double beta;

		MCTSNode(MCTSNode parent, CAction action, BoardCompact state) {
			this.parent = parent;
			this.action = action;
			this.state = state;
			this.children = new ArrayList<>();
			this.visits = 0;
			this.totalValue = 0;
			this.alpha = Double.NEGATIVE_INFINITY;
			this.beta = Double.POSITIVE_INFINITY;
		}

		boolean isLeaf() {
			return children.isEmpty();
		}
	}

	public class SokobanProblem implements HeuristicProblem<BoardCompact, CAction> {

		@Override
		public double estimate(BoardCompact state) {
			double totalEstimate = 0;

			// there is no information about the location of the boxes, their colors or the
			// location of the drop-off points on the BoardCompact class
			// maybe out of speed concerns, but I feel like keeping the state on those in
			// this class and calling that function on init should be faster
			// 4 nested fors for now xDD
			for (int x = 0; x < state.width(); x++) {
				for (int y = 0; y < state.height(); y++) {
					if (CTile.isSomeBox(state.tile(x, y))) {
						double minDistance = Double.MAX_VALUE;
						for (int dx = 0; dx < state.width(); dx++) {
							for (int dy = 0; dy < state.height(); dy++) {
								if (CTile.forSomeBox(state.tile(dx, dy))) {
									double distance = Math.abs(x - dx) + Math.abs(y - dy);
									minDistance = Math.min(minDistance, distance);
								}
							}
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
			return (action instanceof CPush) ? 2.0 : 1.0;
		}
	}

	private boolean dfs(int level, List<EDirection> result) {
		if (level <= 0)
			return false; // DEPTH-LIMITED

		++searchedNodes;

		// COLLECT POSSIBLE ACTIONS

		List<CAction> actions = new ArrayList<CAction>(4);

		for (CMove move : CMove.getActions()) {
			if (move.isPossible(board)) {
				actions.add(move);
			}
		}
		for (CPush push : CPush.getActions()) {
			if (push.isPossible(board)) {
				actions.add(push);
			}
		}

		// TRY ACTIONS
		for (CAction action : actions) {
			// PERFORM THE ACTION
			result.add(action.getDirection());
			action.perform(board);

			// CHECK VICTORY
			if (board.isVictory()) {
				// SOLUTION FOUND!
				return true;
			}

			// CONTINUE THE SEARCH
			if (dfs(level - 1, result)) {
				// SOLUTION FOUND!
				return true;
			}

			// REVERSE ACTION
			result.remove(result.size() - 1);
			action.reverse(board);
		}

		return false;
	}
}
