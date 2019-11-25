package wumpusworld;


import java.util.ArrayList;

public class Naive_Bayes {
    EveryQuery everyQuery;
    private boolean foundWumpus;
    private int[] wumpus_pos = new int[2];
    private ArrayList<double[]> probability_set = new ArrayList<double[]>();
    private ArrayList<int []> pit_position = new ArrayList<int []>();
    /*condition constants*/
    public static final int PIT = 1;
    public static final int WUMPUS = 2;

    /*reference parameters for decision making*/
    private static final double OFFSET = 0.1;
    private static final double RISK = 0.25;

    public Naive_Bayes(EveryQuery everyQuery) {
        this.everyQuery = everyQuery;
        this.probability_set = cloneList1(everyQuery.getProbability_set());
    }

    private ArrayList<int[]> cloneList(ArrayList<int[]> arrayList) {
        ArrayList<int[]> clone = new ArrayList<int[]>(arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            clone.add(arrayList.get(i).clone());

        }
        return clone;
    }

    private ArrayList<double[]> cloneList1(ArrayList<double[]> arrayList) {
        ArrayList<double[]> clone = new ArrayList<double[]>(arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            clone.add(arrayList.get(i).clone());

        }
        return clone;
    }

    public boolean findWumpus(int[] position) {
        everyQuery.updateWUMPUS_PROB();
        get_probability(WUMPUS);
        if (foundWumpus) {
            position[0] = wumpus_pos[0];
            position[1] = wumpus_pos[1];
        }
        return foundWumpus;
    }

    public void set_wumpus_pos(int[] position) {
        wumpus_pos[0] = position[0];
        wumpus_pos[1] = position[1];
    }

    private boolean checkPits(ArrayList<int[]> pit_position, int x,int y, int condition) {
        boolean b = false;
        for (int i =0; i<pit_position.size();i++){
            if (pit_position.get(i)[0] == x && pit_position.get(i)[1] == y && condition == PIT){
                b = true;
            }
        }
        return b;
    }

    private void get_probability(int condition) {
        double probability;
        ArrayList<int[]> frontier = cloneList(everyQuery.getFrontier());// pass the frontier
        if (condition == PIT) {
            probability = everyQuery.getPIT_PROB();
            System.out.println("calculating P(pit).....");
            System.out.println("now the p of pit is: " + probability);
        } else if (condition == WUMPUS) {
            probability = everyQuery.getWUMPUS_PROB();
            System.out.println("calculating P(Wumpus).....");
            System.out.println("now the p of wumpus is: " + probability);
        } else {
            System.out.println("OUT OF CONDITION RANGE");
            return;
        }
        System.out.println("===============");
        for (int i = 0; i < frontier.size(); i++) {
            if (checkPits(pit_position,frontier.get(i)[0],frontier.get(i)[1],condition)){
                System.out.println("Already calculate");
                continue;
            }
            int[] queryTrue = new int[]{frontier.get(i)[0], frontier.get(i)[1], 1};
            int[] queryFalse = new int[]{frontier.get(i)[0], frontier.get(i)[1], 0};
            double total_pro_true = 0;
            double total_pro_false = 0;
            double f = 0;
            ArrayList<int[]> portion = cloneList(everyQuery.getFrontier());
            portion.remove(i);//the[i] is stored in the queryTure and queryFalse
            ArrayList<ArrayList<int []>> combinations = new ArrayList<ArrayList<int[]>>();
            int[] count = everyQuery.combination(portion,combinations);// get all the combination, the combination is the place is WUMPUS or not
            System.out.println("##### Query ("+frontier.get(i)[0]+","+frontier.get(i)[1]+")##### ");
            /*iteratively calculate P(combination) in list combinations*/
            for(int j = 0; j < combinations.size(); j++)
            {
                int sum = combinations.get(j).size();
                System.out.println("combination "+j+")  ");
                String msg = "Not consistent";
                for(int k=0; k<combinations.get(j).size();k++){
                    System.out.print(combinations.get(j).get(k)[2]);
                }
                System.out.println(" ");

                if((condition==PIT && count[j]<4) || (condition==WUMPUS && count[j]<2)){
                    System.out.print("when P(query) is true: ");
                    if (everyQuery.check_consistent(combinations.get(j),condition,queryTrue)){// check the suppose with real condition
                        double add = Math.pow(probability,count[j]) * Math.pow(1-probability,sum-count[j]);
                        total_pro_true += add;//P(B|A) Indicates the sum of the probabilities of all possible combinations in the case where this grid of the current query is true.
                        System.out.println("P = "+add);
                    }
                    else {
                        System.out.println(msg);
                    }
                    if (everyQuery.check_consistent(combinations.get(j),condition,queryFalse)){
                        double add = Math.pow(probability,count[j]) * Math.pow(1-probability,sum-count[j]);
                        total_pro_false += add;//Indicates the sum of the probabilities of all combinations that satisfy the current environment in the case where the current query grid is false.
                        System.out.println("P = "+add);
                    }
                    else {
                        System.out.println(msg);
                    }
                }
            }
            //
            System.out.println("---------------------");
            total_pro_true = probability*total_pro_true;
            System.out.println("when frontier ("+frontier.get(i)[0]+","+frontier.get(i)[1]+") is true,"+
                    "the total probability is "+total_pro_true);
            total_pro_false = (1-probability)*total_pro_false;
            System.out.println("when frontier ("+frontier.get(i)[0]+","+frontier.get(i)[1]+") is false,"+
                    "the total probability is "+total_pro_false);
            try{
                f= total_pro_true/(total_pro_true+total_pro_false);
            }catch (ArithmeticException e){
                System.out.println("ERROR: You shouldn't divide a number by zero!");
            }catch (Exception e){
                System.out.println("WARNING: Some other exception");
            }
            // put the probability into probability_set
            probability_set.get(i)[condition-1]=f;
            System.out.println("final probability of ("+frontier.get(i)[0]+","+frontier.get(i)[1]+"):"+f);
            System.out.println("---------------------");
            //add pit and wumpus into the array
            if (condition == PIT && f==1){
                pit_position.add(new int[]{frontier.get(i)[0],frontier.get(i)[1]});
            }
            if(condition==WUMPUS && f==1){
                foundWumpus = true;
                wumpus_pos[0] = frontier.get(i)[0];
                wumpus_pos[1] = frontier.get(i)[1];
                System.out.println("Oops! I know where is the wumpus now:)");
                return;
            }
        }
        System.out.println("===============");
    }


    public boolean get_goal(int[] position,int wumpus_status) {// get the WUMPUS goal, whether we need to shoot. position store the goal
        everyQuery.updatePIT_PROB();//update the probability of PITS
        ArrayList<int[]> frontier = cloneList(everyQuery.getFrontier());

        int index;
        boolean shoot=false;
        boolean is_safe=false;
        double pit_upper = 0.2;//3/15 the first set of the max pits probability for player to decide how to move

        get_probability(PIT);// get the probability of pit in the positions
        if(wumpus_status==MyAgent.NOT_FOUND){//WUMPUS is not found

            double min_wumpus=1;// suppose the probability of WUMPUS is 100% at the condition
            int n = -1;
            while(n<0)
            {
                for(int i=0; i<probability_set.size(); i++)
                {
                    //*get the probability of a wumpus or a pit in the frontier[i]*//*
                    double pw = probability_set.get(i)[1];
                    double pp = probability_set.get(i)[0];

                    if(pw<=min_wumpus && pp<pit_upper) {
                        if (pw == min_wumpus && n>=0 && everyQuery.is_farther(frontier.get(i),frontier.get(n))) {
                            continue;
                        }
                        min_wumpus=pw;
                        n=i;
                    }
                }

                pit_upper += OFFSET;// not found else the pti_upper will rise
            }

            index = n;
            // if the probability of WUMPUS > RISK, player will shoot though not sure!
            if(probability_set.get(index)[1]>RISK && everyQuery.hasArrow()){
                System.out.println("Anyway, I will shoot though not sure! ");
                shoot = true;
            }
        }



        else{

            double min_pit=1;// suppose the probability of PITS are 100% at the condition
            int n=-1;

            while(!is_safe)
            {
                for(int i=0; i<probability_set.size(); i++){

                    /*get the probability of containing a pit in frontier[i]*/
                    double p = probability_set.get(i)[0];
                    if(p<=min_pit) {
                        if (p == min_pit && n>=0 && everyQuery.is_farther(frontier.get(i),frontier.get(n))) {
                            continue;
                        }
                        min_pit=p;
                        n=i;
                    }
                }

                if(wumpus_status==MyAgent.DEAD)is_safe=true;
                else{

                    /*check if the selected position contains a wumpus*/
                    if(wumpus_pos[0]==frontier.get(n)[0] && wumpus_pos[1]==frontier.get(n)[1])
                    {
                        System.out.println("there is a wumpus in my goal!!");

                        if(everyQuery.hasArrow()){
                            shoot = true;
                            is_safe = true;
                            System.out.println("but I have a arrow :)");
                        }
                        else{
                            if(probability_set.size()>1){
                                probability_set.remove(probability_set.get(n));
                                min_pit=1;
                                System.out.println("so I quit ~");
                            }
                            else is_safe = true;    /*只剩最后一个未知格子，同时存在金块和wumpus，箭已经使用，必输局*/

                        }
                    }
                    else is_safe=true;
                }
            }

            index = n;

        }

        position[0] = frontier.get(index)[0];
        position[1] = frontier.get(index)[1];

        return shoot;

    }


}

