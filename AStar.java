package com.company;

import javax.swing.*;
import java.util.*;

import static javax.swing.JOptionPane.showInputDialog;

class SortBySum implements java.util.Comparator<State> {

    @Override
    public int compare(State o1, State o2) {
        return (o1.getLevel() + o1.getSum()) - (o2.getLevel() + o2.getSum());
    }
}

interface Heuristic {
    int calculateSum(State currentState, State goalState);
}

class ManhattanDistance implements Heuristic {

    /*
     * This search works by first finding an un-matching position from
     * current state to goal state, we then count from that position
     * to where it should be in the goal state. We apply this formula
     * to every un-matching value in the array
     *
     *  Formula to find moves:
     * ((linear distance from index / sqrt(len) ) +
     * ((linear distance from index % sqrt(len) )
     *
     * Above formula works out shortest path to its goal position
     */
    public int calculateSum(State currentState, State goalState) {
        return (currentState.getLevel() + calculateSum1(currentState, goalState) + calculateSum1(goalState, currentState));
    }

    private int calculateSum1(State currentState, State goalState) {
        int hSum = 0;
        int[] eState = goalState.getState();
        int[] cState = currentState.getState();

        int sqrt = (int) Math.sqrt(eState.length);
        int arrLen = cState.length;

        for (int i = 0; i < arrLen; i++) {

            if (cState[i] != 0 && cState[i] != eState[i]) {
                int distance = 0;
                for (int j = i + 1; j < arrLen; j++) {
                    distance++;
                    if (cState[i] == eState[j]) {
                        hSum += ((distance / sqrt) + (distance % sqrt));
                        break;
                    }
                }
            }
        }

        return hSum;
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}

class TilePlacement implements Heuristic {

    /* Very basic Heuristic to count how many tiles
     * are out of place
     */
    public int calculateSum(State currentState, State goalState) {
        int hSum = 0;
        int[] eState = currentState.getState();
        int[] gState = goalState.getState();

        for (int i = 0; i < eState.length; i++) {
            if (eState[i] != gState[i]) hSum++;
        }

        return hSum + currentState.getLevel();
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}

class State implements Comparable<State> {
    private int level;
    private int sum;
    private int[] state;

    public State(int[] state, int level) {
        this.state = state;
        this.level = level;
        sum = -1;
    }

    public int[] getState() {
        return state;
    }

    public int getLevel() {
        return level;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    private String stateToString() {
        int sqrt = (int) Math.sqrt(state.length);
        StringBuilder sb = new StringBuilder("| ");

        for (int i = 0; i < state.length; i++) {
            if (i < 10) sb.append(" ");

            sb.append(String.format("%-4s", state[i] + " | "));

            if (i != state.length - 1 && (i % sqrt) == sqrt - 1) sb.append("\n| ");
        }

        return sb.toString();
    }

    public boolean equals(Object that) {
        if (this == that) return true;

        if (that instanceof State) {
            State other = (State) that;
            return Arrays.equals(state, other.state);
        }

        return false;
    }

    public String toString() {
        return stateToString();
    }

    @Override
    public int hashCode() {
        return Arrays.toString(state).hashCode();
    }

    @Override
    public int compareTo(State o) {
        return Arrays.toString(state).compareTo(Arrays.toString(o.state));
    }
}

class Utils {

    public static Set<State> getNextStates(State state, int level) {
        int position = 0;
        int[] matrix = state.getState();
        int sqrt = (int) Math.sqrt(matrix.length);

        Set<State> newStates = new TreeSet<>();
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == 0) {
                position = i; //current position of Zero
                break;
            }
        }

        int north = position - sqrt;//move up
        int south = position + sqrt;//move down
        int east = (position % sqrt) + 1; //move right
        int west = (position % sqrt) - 1; //move left


        if (north >= 0) { //can move north
            newStates.add(new State(swap(matrix.clone(), position, north), level));
        }

        if (east < sqrt) { //can move east
            newStates.add(new State(swap(matrix.clone(), position, position + 1), level));
        }

        if (west >= 0) { //can move west
            newStates.add(new State(swap(matrix.clone(), position, position - 1), level));
        }

        if (south < matrix.length) { //can move south
            newStates.add(new State(swap(matrix.clone(), position, south), level));
        }

        return newStates;
    }

    /**
     * AI method with no human interaction
     *
     * @param heuristics
     * @param currentState
     * @param goalState
     * @return
     */
    public static int calculateBestMove(List<Heuristic> heuristics, State currentState, State goalState) {
        int best = -1;

        for (Heuristic h : heuristics) {
            int sum = h.calculateSum(currentState, goalState);
            if (sum > best) best = sum;
        }

        return best;
    }

    public static int[] swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        return arr;
    }

    public static int[] stringToIntArr(String[] arr) {
        int[] tmp = new int[arr.length];

        for (int i = 0; i < arr.length; i++) tmp[i] = Integer.parseInt(arr[i]);

        return tmp;
    }
}

public class Main {

    public static void main(String[] args) {
        String startState = showInputDialog(null,
                "Enter initial state", "1 2 3 4 5 10 6 8 9 7 0 11 13 14 15 12");
        String endState = showInputDialog(null,
                "Enter end state", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 0");

        //must not be null and be a valid integer
        if (startState != null && endState != null
                && startState.replaceAll("\\d+", "").trim().equals("")
                && endState.replaceAll("\\d+", "").trim().equals("")) {

            //clean up spaces and spilt elements
            int[] firstState = Utils.stringToIntArr(startState.replaceAll("\\s+", " ").trim().split(" "));
            int[] goalState = Utils.stringToIntArr(endState.replaceAll("\\s+", " ").trim().split(" "));

            int[] sortedStart = firstState.clone();
            int[] sortedEnd = goalState.clone();
            Arrays.sort(sortedStart);
            Arrays.sort(sortedEnd);

            int sLen = sortedStart.length;
            int eLen = sortedEnd.length;

            boolean isStartSquare = Math.sqrt(sLen) % 1 == 0.0;
            boolean isEndSquare = Math.sqrt(eLen) % 1 == 0.0;

            if (!(isStartSquare || isEndSquare)) {
                JOptionPane.showMessageDialog(null,
                        "Have to be an N x N matrix\n\nExample: 3 x 3\n\n= 0 1 2 3 4 5 6 7 8");
            } else if (sLen != eLen) {
                JOptionPane.showMessageDialog(null,
                        "Matrix sizes don't match A=" + sLen + " B=" + eLen);
            } else {

                int[] sortTest = new int[sLen];
                for (int i = 0; i < sLen; i++) {
                    sortTest[i] = i; //make a sorted set of array size
                }

                if (Arrays.equals(sortedStart, sortTest) && Arrays.equals(sortedEnd, sortTest)) {
                    int choice = JOptionPane.showConfirmDialog(
                            null,
                            "Yes for AI mode, No for human Mode",
                            "AI MODE", JOptionPane.YES_NO_OPTION );

                    List<Heuristic> heuristics = new ArrayList<>();
                    heuristics.add(new ManhattanDistance());
                    heuristics.add(new TilePlacement());

                    Object[] oHeuristics = heuristics.toArray();
                    Heuristic heuristic = (Heuristic)JOptionPane.showInputDialog(null,
                            "Choose Heuristic",
                            "Select", 0, null, oHeuristics, oHeuristics[0]);

                    findEndState(firstState, goalState, choice == 0, heuristic);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Matrix is not valid must be like:\n" +
                                    Arrays.toString(sortTest) +
                                    "\n\nYours are:\n"
                                    + Arrays.toString(sortedStart)
                                    + "\n" + Arrays.toString(sortedEnd));
                }
            }

        } else System.out.println("Invalid Matrix");
    }


    public static void findEndState(int[] startState, int[] endState, boolean aiMode, Heuristic heuristic) {
        State startNode = new State(startState, 0);
        State endNode = new State(endState, 0);

        List<State> open = new ArrayList<>();
        Set<State> closed = new LinkedHashSet<>(); //keep order of insertion

        int level = 1;
        boolean noGoal = true;

        //if this size = 1 we need to got back and try another state
        open.addAll(Utils.getNextStates(startNode, level));

        State selectedNode = startNode;

        /*
         * Bart here is an example of just 2 types of heuristics
         * ManhattanDistance is more accurate then TilePlacement
         * and the idea behind creating these is to put all the heuristics
         * int a List so we can loop through the applying each to
         * the current state to work out best route to take.
         *
         * Try come up with more - These are here just as an example
         */
        SortBySum sortBySum = new SortBySum();

        while (noGoal) {
            closed.add(selectedNode);// we selected this

            if (selectedNode.equals(endNode)) {
                noGoal = false;
                System.out.println("Your win");

                for (State cl : closed) {
                    System.out.printf("******* Level(%d) ********\n%s\n", cl.getLevel(), cl);
                }

            } else {
                if (!aiMode) {
                    JOptionPane.showMessageDialog(null,
                            "Your State\n" + selectedNode
                                    + "\nEnd State\n" + endNode);

                    System.out.println("Choose next state");
                }

                for (State s : open) {
                    if (s.getSum() == -1) s.setSum(heuristic.calculateSum(s, endNode));
                }

                /* We could have added this code to the previous loop but we want
                 * to give the user best choice first so we have to  pre-calcuate
                 * the sum, sort then print
                 */
                open.sort(sortBySum);

                if (aiMode) selectedNode = open.get(0); //best move

                if (!aiMode) {
                    int index = 1;
                    for (State s : open) {
                        System.out.printf("---------------(LEVEL %d )" +
                                        "----------------------\n%d)\n%s\nh = %d, g =%d\nh&g=%d\n",
                                level, index++, s, s.getSum(), s.getLevel(), (s.getLevel() + s.getSum()));
                    }

                    Object[] oStates = open.toArray();
                    selectedNode = (State) JOptionPane.showInputDialog(null,
                            "Choose next state",
                            "Title", 0, null, oStates, oStates[0]);
                }

                level++;
                List<State> nextStates = new ArrayList<>(Utils.getNextStates(selectedNode, level));

                open.clear(); //delete other states

                for (State op : nextStates) {
                    if (!closed.contains(op)) open.add(op); //only add states not used
                }

                if(open.size() == 0) {
                    System.out.println("No path found: Level =" + level);
                    break;
                }
            }
        }

    }
}








