import search.*;

import java.util.*;

 //A* search
//public interface Problem<S, A> {
//  S initialState();
//  List<A> actions(S state);
//  S result(S state, A action);
//  boolean isGoal(S state);
//  double cost(S state, A action);
//}
class Tuple<S,A>{
  public S state;
  public A action;
  public double cost;

  public Tuple(S a, A b){
    this.state = a;
    this.action = b;
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
public class AStar<S, A> {
  public static <S, A> Solution<S, A> search(HeuristicProblem<S, A> prob) {
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
      return new Solution<>(path,goal_state,visited.get(goal_state));
    }
    return null;
  }
}
