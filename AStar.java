@@ -0,0 +1,378 @@
package com.company;

import javax.swing.*;
import java.util.*;

import static javax.swing.JOptionPane.showInputDialog;

class State implements Comparable<State> {
    private int level;
    private int sum;
    private int[] state;

    public State(int[] state, int level){
        this.state = state;
        this.level = level;
    }

    public int[] getState() {
        return state;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getSum() {
        return sum;
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
    public int compareTo(State o) {
        return level - o.level;
    }
}

class Utils {

    /* Formula to find moves:
     * ((linear distance from index / sqrt(len) ) +
     * ((linear distance from index % sqrt(len) )
     */
    public static int calculateTiles(State currentState, State endState) {
        int hSum = 0;
        int[] eState = endState.getState();
        int[] state = currentState.getState();

        int sqrt = (int)Math.sqrt(eState.length);
        int arrLen = state.length;

        for(int i = 0; i < arrLen; i++) {

            if(state[i] != 0 && state[i] != eState[i]) {
                int distance = 0;
                for(int j = i+1; j < arrLen; j++ ) {
                    distance++;
                    if(state[i] == eState[j]) {
                        hSum += ((distance / sqrt) + (distance % sqrt));
                        break;
                    }
                }
            }
        }

        return hSum;
    }

    public static List<State> getNextStates(State state, int level) {
        int position = 0;
        int[] matrix = state.getState();
        int sqrt = (int)Math.sqrt(matrix.length);

        List<State> newStates = new ArrayList<>();
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


        if(north >= 0) {
            int[] nArr = matrix.clone();
            swap(nArr, position, north);
            State northState = new State(nArr, level);
            if(!newStates.contains(northState) && !northState.equals(state)) newStates.add(northState);
        }

        if(east < sqrt) {
            int[] nArr = matrix.clone();
            swap(nArr, position, position+1);
            State eastState = new State(nArr, level);
            if(!newStates.contains(eastState) && !eastState.equals(state)) newStates.add(eastState);
        }

        if(west >= 0) {
            int[] nArr = matrix.clone();
            swap(nArr, position, position-1);
            State westState = new State(nArr, level);
            if(!newStates.contains(westState) && !westState.equals(state)) newStates.add(westState);
        }

        if(south < matrix.length) {
            int[] nArr = matrix.clone();
            swap(nArr, position, south);
            State southState = new State(nArr, level);
            if(!newStates.contains(southState) && !southState.equals(state)) newStates.add(southState);
        }

        return newStates;
    }

    public static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
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

            //clean up spaces
            String[] startStateArr = startState.replaceAll("\\s+", " ").trim().split(" ");
            String[] endStateArr = endState.replaceAll("\\s+", " ").trim().split(" ");
            Set<String> sortedStartSet = new TreeSet<>(Arrays.asList(startStateArr));
            Set<String> sortedEndSet = new TreeSet<>(Arrays.asList(endStateArr));
            Set<String> testSet = new TreeSet<>();

            int sLen = startStateArr.length;
            int eLen = endStateArr.length;

            boolean isStartSquare = Math.sqrt(sLen) % 1 == 0.0;
            boolean isEndSquare = Math.sqrt(eLen) % 1 == 0.0;

            if(!(isStartSquare || isEndSquare)) {
                JOptionPane.showMessageDialog(null, "Have to be an N x N matrix\n\nExample: 3 x 3\n\n= 0 1 2 3 4 5 6 7 8");
            } else if(sLen != eLen) {
                JOptionPane.showMessageDialog(null, "Matrix sizes don't match A=" + sLen + " B=" + eLen);
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
                    JOptionPane.showMessageDialog(null, "Matrix is not valid must be like:\n" + testSet +
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

        List<State> open = new ArrayList<>();
        List<State> closed = new ArrayList<>();

        int level = 1;
        boolean noGoal = true;
        System.out.println("Choose next state");

        for(State op : Utils.getNextStates(startNode,level)) {
            if(!open.contains(op)) open.add(op);
        }

        State selectedNode = startNode;
        while (noGoal) {
            closed.add(selectedNode);// we selected this

            if(selectedNode.equals(endNode)) {
                noGoal = false;
                System.out.println("Your win");

                for(State cl : closed) {
                    System.out.println("*****************\n" + cl + "\n");
                }
            } else {
                JOptionPane.showMessageDialog(null,
                        "Your State\n" + selectedNode + "\nEnd State\n" + endNode);

                int index =1;
                for(State s : open) {
                    System.out.println("\n---------------(LEVEL " + level + ")----------------------\n" +
                            index++ + ")\n" +s + "\nh=" + (Utils.calculateTiles(s, endNode) + Utils.calculateTiles(endNode, s)));
                }

                Object[] oStates = open.toArray();
                selectedNode = (State)JOptionPane.showInputDialog(null,
                        "Choose next state",
                        "Title", 0, null, oStates, oStates[0]);

                open.clear(); //delete other states
                for(State op : Utils.getNextStates(selectedNode,level)) {
                    if(!closed.contains(op)) open.add(op); //did we already choose this
                }

                level++;
            }


        }

    }
}

/*
 start: "2 4 3 1 0 6 7 5 8"
 end:   "1 2 3 4 5 6 7 8 0"
Your win
*****************
| 2 | 4 | 3 |
| 1 | 0 | 6 |
| 7 | 5 | 8 |

*****************
| 2 | 4 | 3 |
| 1 | 5 | 6 |
| 7 | 0 | 8 |

*****************
| 2 | 4 | 3 |
| 1 | 5 | 6 |
| 7 | 8 | 0 |

*****************
| 2 | 4 | 3 |
| 1 | 5 | 0 |
| 7 | 8 | 6 |

*****************
| 2 | 4 | 3 |
| 1 | 0 | 5 |
| 7 | 8 | 6 |

*****************
| 2 | 0 | 3 |
| 1 | 4 | 5 |
| 7 | 8 | 6 |

*****************
| 0 | 2 | 3 |
| 1 | 4 | 5 |
| 7 | 8 | 6 |

*****************
| 1 | 2 | 3 |
| 0 | 4 | 5 |
| 7 | 8 | 6 |

*****************
| 1 | 2 | 3 |
| 4 | 0 | 5 |
| 7 | 8 | 6 |

*****************
| 1 | 2 | 3 |
| 4 | 5 | 0 |
| 7 | 8 | 6 |

*****************
| 1 | 2 | 3 |
| 4 | 5 | 6 |
| 7 | 8 | 0 |



15 x 15
start: "0 1 3 4 5 2 6 8 9 10 7 11 13 14 15 12"
  end: "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 0"

Your win
*****************
| 0 | 1 | 3 | 4 |
| 5 | 2 | 6 | 8 |
| 9 | 10 | 7 | 11 |
| 13 | 14 | 15 | 12 |

*****************
| 1 | 0 | 3 | 4 |
| 5 | 2 | 6 | 8 |
| 9 | 10 | 7 | 11 |
| 13 | 14 | 15 | 12 |

*****************
| 1 | 2 | 3 | 4 |
| 5 | 0 | 6 | 8 |
| 9 | 10 | 7 | 11 |
| 13 | 14 | 15 | 12 |

*****************
| 1 | 2 | 3 | 4 |
| 5 | 6 | 0 | 8 |
| 9 | 10 | 7 | 11 |
| 13 | 14 | 15 | 12 |

*****************
| 1 | 2 | 3 | 4 |
| 5 | 6 | 7 | 8 |
| 9 | 10 | 0 | 11 |
| 13 | 14 | 15 | 12 |

*****************
| 1 | 2 | 3 | 4 |
| 5 | 6 | 7 | 8 |
| 9 | 10 | 11 | 0 |
| 13 | 14 | 15 | 12 |

*****************
| 1 | 2 | 3 | 4 |
| 5 | 6 | 7 | 8 |
| 9 | 10 | 11 | 12 |
| 13 | 14 | 15 | 0 |


 */
