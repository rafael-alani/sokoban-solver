//import search.*;

import java.util.*;
interface HeuristicProblem<S, A> extends Problem<S, A> {
    double estimate(S state);  // optimistic estimate of cost from state to goal
}

// S = state type, A = action type
interface Problem<S, A> {
    S initialState();

    List<A> actions(S state);

    S result(S state, A action);

    boolean isGoal(S state);

    double cost(S state, A action);
}

class Tuple<S,A>{
    public S state;
    public A action;
    public double cost;

    public Tuple(S a, A b){
        this.state = a;
        this.action = b;
    }
}
class Solution2<S, A> {
    public List<A> actions;  // series of actions from start state to goal state
    public S goalState;      // goal state that was reached
    public double pathCost;  // total cost from start state to goal

    public Solution2(List<A> actions, S goalState, double pathCost) {
        this.actions = actions; this.goalState = goalState; this.pathCost = pathCost;
    }

    // Return true if this is a valid solution to the given problem.
    public boolean isValid(Problem<S, A> prob) {
        S state = prob.initialState();
        double cost = 0.0;

        // Check that the actions actually lead from the problem's initial state to the goal.
        for (A action : actions) {
            cost += prob.cost(state, action);
            state = prob.result(state, action);
        }

        return state.equals(goalState) && prob.isGoal(goalState) && pathCost == cost;
    }

    // Describe a solution.
    public static <S, A> boolean report(Solution2<S, A> solution, Problem<S, A> prob) {
        if (solution == null) {
            System.out.println("no solution found");
            return false;
        } else if (!solution.isValid(prob)) {
            System.out.println("solution is invalid!");
            return false;
        } else {
            System.out.println("solution is valid");
            System.out.format("total cost is %.1f\n", solution.pathCost);
            return true;
        }
    }
}

class Node<S>{
    public S state;
    public double cost;
    public double estimatedCost;

    public Node(S s, double d, double e){
        this.state = s;
        this.cost = d;
        this.estimatedCost = e;
    }
}

class AStar<S, A> {
    public static <S, A> Solution2<S, A> search(HeuristicProblem<S, A> prob) {
        HashMap<S,Double> visited = new HashMap<>();
        HashMap<S,Tuple<S,A>> parents = new HashMap<>();
        PriorityQueue<Node<S>> pq = new PriorityQueue<>(Comparator.comparingDouble(a ->  a.estimatedCost));
        pq.add(new Node<>(prob.initialState(),0.0,prob.estimate(prob.initialState())));
        visited.put(prob.initialState(), 0.0);
        S goal_state = null;
        boolean flag = false;
        while(!pq.isEmpty() && !flag) {
            Node<S> curr = pq.poll();
            S s = curr.state;
            double currentScore = visited.getOrDefault(s, 0.0);

            if (curr.cost > visited.get(s))
                continue;

            for (A action : prob.actions(s)){
                S nextState = prob.result(s, action);
                double cost = prob.cost(s, action) + currentScore;
                if (!visited.containsKey(nextState)|| cost < visited.get(nextState)) {
                    visited.put(nextState, cost);
                    parents.put(nextState, new Tuple<>(s, action));
                    pq.add(new Node<>(nextState, cost, cost + prob.estimate(nextState)));
                    if (prob.isGoal(nextState)) {
                        flag = true;
                        goal_state = nextState;
                        break;
                    }
                }
            }
        }
        if (goal_state != null) {
            S temp = goal_state;
            List<A> path = new ArrayList<>();
            while(!temp.equals(prob.initialState())) {
                path.add(parents.get(temp).action);
                temp = parents.get(temp).state;
            }
            Collections.reverse(path);
            return new Solution2<>(path,goal_state,visited.get(goal_state));
        }
        return null;
    }
}