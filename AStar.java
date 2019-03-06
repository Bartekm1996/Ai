package com.company;

import javax.swing.*;
import java.util.*;

import static javax.swing.JOptionPane.showInputDialog;

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

        int sqrt = (int)Math.sqrt(eState.length);
        int arrLen = cState.length;

        for(int i = 0; i < arrLen; i++) {

            if(cState[i] != 0 && cState[i] != eState[i]) {
                int distance = 0;
                for(int j = i+1; j < arrLen; j++ ) {
                    distance++;
                    if(cState[i] == eState[j]) {
                        hSum += ((distance / sqrt) + (distance % sqrt));
                        break;
                    }
                }
            }
        }

        return hSum;
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

        for(int i = 0; i < eState.length; i++) {
            if(eState[i] != gState[i]) hSum++;
        }

        return hSum + currentState.getLevel();
    }
}

class State implements Comparable<State> {
    private int level;
    private int[] state;

    public State(int[] state, int level){
        this.state = state;
        this.level = level;
    }

    public int[] getState() {
        return state;
    }

    public int getLevel() {
        return level;
    }
    private String stateToString() {
        int sqrt = (int)Math.sqrt(state.length);
        StringBuilder sb = new StringBuilder("| ");

        for(int i = 0; i < state.length; i++) {
            if(i < 10) sb.append(" ");

            sb.append(String.format("%-4s", state[i] + " | "));

            if(i != state.length -1 && (i % sqrt) == sqrt-1) sb.append("\n| ");
        }

        return sb.toString();
    }

    public boolean equals(Object that) {
        if(this == that) return true;

        if(that instanceof  State) {
            return Arrays.equals(state, ((State)that).state);
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
        int sqrt = (int)Math.sqrt(matrix.length);

        Set<State> newStates = new TreeSet<>();
        for(int i = 0; i < matrix.length; i++) {
            if(matrix[i] == 0) {
                position = i; //current position of Zero
                break;
            }
        }

        int north = position - sqrt;//move up
        int south = position + sqrt;//move down
        int east = (position % sqrt) + 1; //move right
        int west = (position % sqrt) - 1; //move left


        if(north >= 0) { //can move north
            newStates.add(new State(swap(matrix.clone(), position, north), level));
        }

        if(east < sqrt) { //can move east
            newStates.add(new State(swap(matrix.clone(), position, position+1), level));
        }

        if(west >= 0) { //can move west
            newStates.add(new State(swap(matrix.clone(), position, position-1), level));
        }

        if(south < matrix.length) { //can move south
            newStates.add(new State(swap(matrix.clone(), position, south), level));
        }

        return newStates;
    }

    public static int calculateBestMove(List<Heuristic> heuristics, State currentState, State goalState) {
        int best = -1;

        for(Heuristic h : heuristics) {
            int sum = h.calculateSum(currentState, goalState);
            if(sum > best) best = sum;
        }

        return best;
    }

    public static int[] swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;

        return arr;
    }
}

public class Main {

    public static void main(String[] args) {
        String startState = showInputDialog(null,
                "Enter initial state", "0 1 3 4 5 2 6 8 9 10 7 11 13 14 15 12");
        String endState = showInputDialog(null,
                "Enter end state", "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 0");

        //must not be null and be a valid integer
        if(startState != null && endState != null
                && startState.replaceAll("\\d+", "").trim().equals("")
                && endState.replaceAll("\\d+", "").trim().equals("")) {

            //clean up spaces and spilt elements
            String[] startStateArr = startState.replaceAll("\\s+", " ").trim().split(" ");
            String[] endStateArr = endState.replaceAll("\\s+", " ").trim().split(" ");

            //Make sure user entered a valid order array
            Set<String> sortedStartSet = new TreeSet<>(Arrays.asList(startStateArr));
            Set<String> sortedEndSet = new TreeSet<>(Arrays.asList(endStateArr));
            Set<String> testSet = new TreeSet<>();

            int sLen = startStateArr.length;
            int eLen = endStateArr.length;

            boolean isStartSquare = Math.sqrt(sLen) % 1 == 0.0;
            boolean isEndSquare = Math.sqrt(eLen) % 1 == 0.0;

            if(!(isStartSquare || isEndSquare)) {
                JOptionPane.showMessageDialog(null,
                        "Have to be an N x N matrix\n\nExample: 3 x 3\n\n= 0 1 2 3 4 5 6 7 8");
            } else if(sLen != eLen) {
                JOptionPane.showMessageDialog(null,
                        "Matrix sizes don't match A=" + sLen + " B=" + eLen);
            } else {

                /* Build an ordered set of values from 0 to n
                 * a valid matrix must start at 0 to n, so if a user
                 * inputs 0,1,2,4 the testSet will be 0,1,2,3 therefore
                 * the users matrix is not valid
                 */
                for(int i = 0; i < sLen; i++) {
                    testSet.add("" + i); //make a sorted set of array size
                }

                if(sortedStartSet.equals(testSet) && sortedEndSet.equals(testSet)){
                    findEndState(startStateArr, endStateArr);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Matrix is not valid must be like:\n" + testSet +
                            "\n\nYours are:\n" + sortedStartSet + "\n" + sortedEndSet);
                }
            }

        } else System.out.println("Invalid Matrix");
    }


    public static void findEndState(String[] start, String[] end) {
        int[] startState = new int[start.length];
        int[] endState = new int[start.length];

        //convert to ints
        for(int i = 0; i < startState.length; i++) {
            startState[i] = Integer.parseInt(start[i]);
            endState[i] = Integer.parseInt(end[i]);
        }

        State startNode = new State(startState, 0);
        State endNode = new State(endState, 0);

        Set<State> open = new TreeSet<>();
        Set<State> closed = new LinkedHashSet<>(); //keep order of insertion

        int level = 1;
        boolean noGoal = true;
        open.addAll(Utils.getNextStates(startNode,level));
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
        Heuristic h1 = new ManhattanDistance();
        Heuristic h2 = new TilePlacement();

        while (noGoal) {
            closed.add(selectedNode);// we selected this

            if(selectedNode.equals(endNode)) {
                noGoal = false;
                System.out.println("Your win");

                for(State cl : closed) {
                    System.out.printf("******* Level(%d) ********\n%s\n", cl.getLevel(), cl);
                }

            } else {
                JOptionPane.showMessageDialog(null,
                        "Your State\n" + selectedNode
                                + "\nEnd State\n" + endNode);

                System.out.println("Choose next state");

                int index =1;
                for(State s : open) {
                    System.out.println("---------------(LEVEL " + level + ")----------------------\n" +
                            index++ + ")\n" +s + "\nh + g = " + h2.calculateSum(s, endNode));
                }

                Object[] oStates = open.toArray();
                selectedNode = (State)JOptionPane.showInputDialog(null,
                        "Choose next state",
                        "Title", 0, null, oStates, oStates[0]);

                /* We need to keep all these states even though I delete here because
                 * in Tut for AI if we come to a state with only one move we need to go
                 * back a level and add that to the closed state then pick next best
                 * state from open
                 */
                open.clear(); //delete other states
                for(State op : Utils.getNextStates(selectedNode,level)) {
                    if(!closed.contains(op)) open.add(op); //only add states not used
                }

                level++;
            }
        }

    }
}