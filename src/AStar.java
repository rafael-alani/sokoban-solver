import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

interface HeuristicProblem<S, A> extends Problem<S, A> {
    double estimate(S state); // optimistic estimate of cost from state to goal
}

// S = state type, A = action type
interface Problem<S, A> {
    S initialState();

    List<A> actions(S state);

    S result(S state, A action);

    boolean isGoal(S state);

    double cost(S state, A action);
}

class Tuple<S, A> {
    public S state;
    public A action;
    public double cost;

    public Tuple(S a, A b) {
        this.state = a;
        this.action = b;
    }
}

class Solution2<S, A> {
    public List<A> actions; // series of actions from start state to goal state
    public S goalState; // goal state that was reached
    public double pathCost; // total cost from start state to goal

    public Solution2(List<A> actions, S goalState, double pathCost) {
        this.actions = actions;
        this.goalState = goalState;
        this.pathCost = pathCost;
    }

    // Return true if this is a valid solution to the given problem.
    public boolean isValid(Problem<S, A> prob) {
        S state = prob.initialState();
        double cost = 0.0;

        // Check that the actions actually lead from the problem's initial state to the
        // goal.
        for (A action : actions) {
            cost += prob.cost(state, action);
            state = prob.result(state, action);
        }

        return state.equals(goalState) && prob.isGoal(goalState) && pathCost == cost;
    }
}

class Node<S> {
    public S state;
    public double cost;
    public double estimatedCost;

    public Node(S s, double d, double e) {
        this.state = s;
        this.cost = d;
        this.estimatedCost = e;
    }
}

class AStar<S, A> {
    public static <S, A> Solution2<S, A> search(HeuristicProblem<S, A> prob) {
        PriorityQueue<Tuple<S, A>> pq = new PriorityQueue<>();
        Map<S, Double> visited = new HashMap<>();

        pq.add(new Tuple<S, A>(prob.initialState(), null, 0.0, 0.0, null));
        visited.put(prob.initialState(), 0.0);

        while (!pq.isEmpty()) {
            Tuple<S, A> curr = pq.poll();

            if (visited.containsKey(curr.state) && visited.get(curr.state) < curr.pathCost)
                continue;

            if (prob.isGoal(curr.state))
                return makePath(prob, curr);

            for (A action : prob.actions(curr.state)) {
                S nextS = prob.result(curr.state, action);
                double pathCost = curr.pathCost + prob.cost(curr.state, action);
                double totalCost = pathCost + prob.estimate(nextS);

                if (!visited.containsKey(nextS) || pathCost < visited.get(nextS)) {
                    visited.put(nextS, pathCost);
                    pq.add(new Tuple<S, A>(nextS, action, pathCost, totalCost, curr));
                }
            }
        }

        return null;
    }

    public static <S, A> Solution2<S, A> makePath(HeuristicProblem<S, A> prob, Tuple<S, A> curr) {

        Tuple<S, A> goal = curr;

        LinkedList<A> actions = new LinkedList<>();

        while (curr.action != null) {
            actions.addFirst(curr.action);
            curr = curr.parent;
        }

        return new Solution2<S, A>(actions, goal.state, goal.pathCost);
    }

    public static class Tuple<S, A> implements Comparable<Tuple<S, A>> {
        public S state;
        public A action;
        public double pathCost;
        public double totalCost;
        public Tuple<S, A> parent;

        public Tuple(
                S state, A action,
                double pathCost,
                double totalCost, Tuple<S, A> parent) {
            this.state = state;
            this.pathCost = pathCost;
            this.action = action;
            this.totalCost = totalCost;
            this.parent = parent;
        }

        @Override
        public int compareTo(Tuple<S, A> other) {
            return Double.compare(this.totalCost, other.totalCost);
        }
    }
}