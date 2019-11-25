package wumpusworld;

import java.util.ArrayList;

import static wumpusworld.Naive_Bayes.PIT;
import static wumpusworld.Naive_Bayes.WUMPUS;

public class EveryQuery {
    private World w,w2;
    private ArrayList<int[]> frontier = new ArrayList<int[]>();//the collection of coordinates(x,y) and a variable(0or1) used in check WUMPUS condition, at first is 0
    private ArrayList<double[]> probability_set = new ArrayList<double[]>();// store the probability of PIT,WUMPUS
    private double PIT_PROB = 0.2;//the initial probability of PIT
    private double WUMPUS_PROB = 0.0667;//the initial probability of WUMPUS
    private int pitNum = 0;// the number of pit
    //constructor function to initialize the frontier that is from the first coordinate(1,1)
    public EveryQuery(World world){

        w = world;
        w2 = w.cloneWorld();
        get_frontier(1,1);//the first coordinate(1,1)
    }
    //get the Frontier
    public ArrayList<int[]> getFrontier() {
        return frontier;
    }

    //get the Probability_set
    public ArrayList<double[]> getProbability_set() {
        return probability_set;
    }
    //get the probability of PIT
    public double getPIT_PROB() {
        return PIT_PROB;

    }
    //get the probability of WUMPUS
    public double getWUMPUS_PROB() {
        return WUMPUS_PROB;
    }
    public ArrayList<int[]> cloneList(ArrayList<int[]> arrayList) {
        ArrayList<int[]> clone = new ArrayList<int[]>(arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            clone.add(arrayList.get(i).clone());

        }
        return clone;
    }
    private void get_frontier(int x, int y) {

        System.out.println("checking the ("+x+","+y+")");
        // if the coordinate can't be valid coordinate, it can't be set as a frontier
        if (!w2.isValidPosition(x, y)) {
            return;
        }
        // if the coordinate can't be reach, it can't be set as a frontier
        if (!w2.hasReach(x,y)){
            return;
        }
        /*every coordinate is UNKNOWN when the world is initialized
        * every time, player visit the coordinate, we remove the UNKNOWN
        * every coordinate is not MARKED at first
        * */
        // if (x,y) is unknown
        if (w2.isUnknown(x, y)) {
            //if (x,y) do not be marked
            if (!(w2.hasMarked(x, y))) {
                //add it to frontier
                frontier.add(new int[]{x, y, 0});
                probability_set.add(new double[]{0,0});   /*Synchronously initialize corresponding probability set*/
                //set it to be MARKED
                w2.setMarked(x, y);
                System.out.println("set ("+x+","+y+") as frontier");
            }
            else         System.out.println("("+x+","+y+") has been set");
            return;
        }
        //if (x,y) is MARKED, don't do any operation
        else if (w2.hasMarked(x,y)) return;
        //if(x,y) is known, check the surroundings.
        else
        {
            w2.setMarked(x,y);
            get_frontier(x + 1, y );
            get_frontier(x - 1, y);
            get_frontier(x, y + 1);
            get_frontier(x, y - 1);
        }
    }
    //update the probability of WUMPUS  1/(16-knowns)
    public double updateWUMPUS_PROB(){
        double knowns = w.getKnowns();
        WUMPUS_PROB = 1/(16-knowns);
        return WUMPUS_PROB;
    }
    //update the probability of PITS  the first for loop count the number of PIT, PIT_PROB = (3-pits)/(16-knowns);
    public double updatePIT_PROB(){
        for (int x=0; x<w.getSize();x++){
            for (int y=0; y<w.getSize();y++){
                if (!w.isUnknown(x,y)&&w.hasPit(x,y)){
                    pitNum++;
                }
            }
        }
        double knowns = w.getKnowns();
        double pits = pitNum;
        PIT_PROB = (3-pits)/(16-knowns);
        return PIT_PROB;
    }
    //check the distance bewteen goalA and goalB if D(A) > D(B) return true
    public boolean is_farther(int[] goalA, int[] goalB){

        int x = w.getPlayerX();
        int y = w.getPlayerY();
        int distanceA = Math.abs(x-goalA[0])+Math.abs(y-goalA[1]);
        int distanceB = Math.abs(x-goalB[0])+Math.abs(y-goalB[1]);

        if(distanceA>distanceB)
            return true;
        else
            return  false;

    }


    //get all the combinations
    public int[] combination(ArrayList<int[]> elements, ArrayList<ArrayList<int[]>> result){
        int n = elements.size();
        int nbit = 1 << n;    /* bit =2^n, indicates the number of different combinations*/
        int[] count = new int[nbit];

        for(int i=0; i<nbit; i++)
        {
            int c = 0;   /*count of positions whose statuses are true*/
            ArrayList<int[]> comb = cloneList(elements);
            for(int j=0; j<n; j++)
            {
                int tmp = 1<<j;
                if((tmp & i)!= 0){
                    comb.get(j)[2]=1; /*set the status in comb[j] as true*/
                    c++;
                }
            }

            result.add(comb);
            count[i] = c;
        }
        return  count;
    }
    //???
    public boolean check_consistent(ArrayList<int[]> arrayList, int condition, int[] query) {

        World w2 = w.cloneWorld();  /*create a world of conjecture*/
        int size = w2.getSize();
        boolean isConsist = true;
        ArrayList<int[]> conject = cloneList(arrayList);
        conject.add(query);
        //this is real condition
        if(condition==PIT)
        {
            for (int x = 1; x <= size; x++) {
                for (int y = 1; y <= size; y++)
                {
                    if(!w2.isUnknown(x,y)&&w2.hasPit(x,y)){
                        w2.markSurrounding(x,y);
                    }
                }
            }
        }

        //all the conject
        for (int i = 0; i < conject.size(); i++) {
            int cx, cy;
            cx = conject.get(i)[0];
            cy = conject.get(i)[1];
            if (conject.get(i)[2] == 1)
            {
                w2.markSurrounding(cx,cy);
            }
        }
        //and compare with both, if conject exist breeze but not marked or marked not breeze is not a real one
        for (int x = 1; x <= size; x++) {
            for (int y = 1; y <= size; y++) {
                if (!(w2.isUnknown(x, y))) {

                    if(condition== PIT) {
                        if (!(w2.hasBreeze(x, y) == w2.hasMarked(x, y))) {
                            isConsist = false;
                        }
                    }

                    else if(condition==WUMPUS) {
                        if (!(w2.hasStench(x, y) == w2.hasMarked(x, y))) {
                            isConsist = false;
                        }
                    }
                }

                if (!isConsist) {
                    return isConsist;   /*once the combination is confirmed as inconsistent, break out of for loop*/
                }
            }

        }

        return isConsist;
    }

    public boolean hasArrow()
    {
        return w.hasArrow();
    }

}
