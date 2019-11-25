package wumpusworld;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    private int goal_x = 0;
    private int goal_y = 0;
    private boolean has_goal = false;
    private boolean if_shoot = false;
    private int pits = 0;
    private int wumpus_status = NOT_FOUND;
    private int[] wumpus_pos = {0,0};
    private int[] arrow_goal = {0,0};
    private int[] ban = {0,0};
    private  boolean catch_bug = false;


    /*wumpus status*/
    public static final int NOT_FOUND = 0;
    public static final int FOUND = 1;
    public static final int DEAD = 2;




    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;   
    }
   
            
    /**
     * Asks your solver agent to execute an action.
     */

    public void doAction()
    {
        if(catch_bug){
            System.out.println("????????move function occurs exception??????????");
            return;
        }


        //Location of the player,cx,cy
        int cX = w.getPlayerX();
        int cY = w.getPlayerY();



        //Basic action:
        //Grab Gold if we can.
        if (w.hasGlitter(cX, cY))
        {
            w.doAction(World.A_GRAB);
            return;
        }
        
        //Basic action:
        //We are in a pit. Climb up.
        if (w.isInPit())
        {
            w.doAction(World.A_CLIMB);
            return;
        }

        System.out.println("=========Gather Environmental Information======= ");
        //Test the environment
        if (w.hasBreeze(cX, cY))
        {
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            System.out.println("I am in a Pit");
        }
        if (w.getDirection() == World.DIR_RIGHT)
        {
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT)
        {
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP)
        {
            System.out.println("I am facing Up");
        }
        if (w.getDirection() == World.DIR_DOWN)
        {
            System.out.println("I am facing Down");
        }
        
        //decide next move
        System.out.println("============Decide Next Move=============");
        //reach the goal which  naive_Bayes algorithm finds
        if(cX==goal_x && cY==goal_y){
            has_goal=false;
            System.out.println("Hey! I have arrived the goal("+goal_x+","+goal_y+")!");
            w.setReachable();//set all the coordinates to be reachable
        }

        if(!has_goal){
            EveryQuery everyQuery = new EveryQuery(w);// a new everyquery object.
            Naive_Bayes nb = new Naive_Bayes(everyQuery);// a new Naive_Bayes object
            // check if the WUMPUS is not found
            if(wumpus_status==NOT_FOUND){
                if(nb.findWumpus(wumpus_pos)){
                    wumpus_status = FOUND;
                }
            }
            //check if the WUMPUS is found,set the position of the WUMPUS
            else if (wumpus_status==FOUND){
                nb.set_wumpus_pos(wumpus_pos);
            }
            // used to store the goal
            int[] goal = new int[2];
            //get the goal
            if_shoot = nb.get_goal(goal,wumpus_status);
            has_goal = true;
            goal_x = goal[0];
            goal_y = goal[1];
            // check whether it needs shoot
            if(if_shoot){
                arrow_goal[0]=goal_x;
                arrow_goal[1]=goal_y;
            }
            System.out.println("I have a new goal:  ("+goal_x+","+goal_y+")");
        }

        System.out.println(" ============总的行动模式开始！===========");
        //set a invaild ban position
        ban[0]=0;
        ban[1]=0;
        move_to_goal(cX,cY);
        System.out.println(" ============总的行动模式结束！===========");
                
    }


    public void move_to_goal(int x,int y)//the parameters x,y is the current position of the player
    {
        //check if it is adjacent
        boolean is_adjacent = false;
        if(get_distance(x,y,goal_x,goal_y)==1){
            is_adjacent = true;
        }
        //current direction
        int dir = w.getDirection();
        System.out.println("<<<<ACTION>>>>>");


        /*常规路由*/
            if(x<goal_x){
                //not unknown and not a pit and not be banned or is adjacent
                if(!w.isUnknown(x+1,y)&&!w.hasPit(x+1,y)&&!(x+1==ban[0])||is_adjacent){
                    act(dir,World.DIR_RIGHT);//do this act
                    return;
                }
            }
            //the same as above
            if(x>goal_x){
                if(!w.isUnknown(x-1,y)&&!w.hasPit(x-1,y)&&!(x-1==ban[0])||is_adjacent){
                    act(dir,World.DIR_LEFT);
                    return;
                }
            }

            if(y<goal_y){
                if(!w.isUnknown(x,y+1)&&!w.hasPit(x,y+1)&&!(y+1==ban[1])||is_adjacent){
                    act(dir,World.DIR_UP);
                    return;
                }
            }

            if(y>goal_y){
                if(!w.isUnknown(x,y-1)&&!w.hasPit(x,y-1)&&!(y-1==ban[1])||is_adjacent){
                    act(dir,World.DIR_DOWN);
                    return;
                }
            }

        //check if is an impasse. set it as a ban, next time we don't go to it
        int pitdir = is_impasse();
        if( pitdir >= 0){
            ban[0]=x;
            ban[1]=y;
            act(dir,pitdir); //if it is a impasse, only can it go though the pits
            return;
        }
            /*there is not a way for current position(x,y) to goal. we need to chance the goal
            *first we check all the coordinate bewteen them and if there is any unknown coordinate.
            * has_road will be false
             */
            boolean has_new_goal = false;
            if(x != goal_x){
                for(int yy=1; yy<=w.getSize(); yy++){
                    boolean has_road = true;
                    if(x>goal_x){
                        for(int xx=x; xx>=goal_x; xx--){
                            if(w.isUnknown(xx,yy)){
                                has_road = false;
                                break;
                            }
                        }
                    }

                    else{
                        for(int xx=x; xx<=goal_x; xx++){
                            if(w.isUnknown(xx,yy)){
                                has_road = false;
                                break;
                            }
                        }
                    }

                    if(has_road){
                        goal_y = yy;//set a new goal as xx.
                        System.out.println("I'm standing at ("+w.getPlayerX()+","+w.getPlayerY()+")");
                        System.out.println("heading to new goal:("+goal_x+","+goal_y+") !");
                        has_new_goal = true;
                        break;
                    }
                }

            }

            else if(y != goal_y){
                boolean has_road = true;
                for(int xx=1; xx<=w.getSize(); xx++){
                    if(y>goal_y){
                        for(int yy=y; yy>=goal_y; yy--){
                            if(w.isUnknown(xx,yy)){
                                has_road = false;
                                break;
                            }
                        }
                    }

                    else{
                        for(int yy=y; yy<=goal_y; y++){
                            if(w.isUnknown(xx,yy)){
                                has_road = false;
                                break;
                            }
                        }
                    }
                    //if there is a road,
                    if(has_road){
                        goal_x = xx;//set a new goal as xx.
                        System.out.println("I'm standing at ("+w.getPlayerX()+","+w.getPlayerY()+")");
                        System.out.println("heading to new goal:("+goal_x+","+goal_y+") !");
                        has_new_goal = true;
                        break;
                    }
                }

            }
            // if we can't find any road at last. we need to chance the goal.
            if(!has_new_goal){
                change_goal(goal_x,goal_y);
            }
    }


    private void act(int currentDir,int goalDir)
    {
        // if it is in a PIT
        if(w.isInPit()){
            w.doAction(World.A_CLIMB);
            System.out.println("I fall into the pit!");
            System.out.println("I climbed out!");
        }
        if(currentDir-goalDir==0){// when we reach the goal, if we need to shoot and the distance is 1, shoot and  check the WUMPUS is alive
            if(if_shoot&&get_distance(w.getPlayerX(),w.getPlayerY(),arrow_goal[0],arrow_goal[1])==1){
                w.doAction(World.A_SHOOT);
                System.out.println("I shoot!");
                if_shoot=false;
                if(!w.wumpusAlive()){
                    wumpus_status = DEAD;
                    System.out.println("I killed the fucking wumpus!!!");
                }
            }
            else {
                w.doAction(World.A_MOVE);
                System.out.println("I go ahead!");
            }
        }
        //???
        else if(currentDir-goalDir == 1 || currentDir-goalDir == -3) {
            w.doAction(World.A_TURN_LEFT);
            System.out.println("I turn left!");
        }
        else  {
            w.doAction(World.A_TURN_RIGHT);
            System.out.println("I turn right!");
        }

    }

    private int get_distance(int x1, int y1, int x2, int y2){
        int dis = Math.abs(x1-x2)+Math.abs(y1-y2);
        return  dis;
    }
    // change the goal
    private  void change_goal(int x, int y){
        //set current goal as an unreachable goal
        w.setUnreachable(x,y);
        System.out.println("But it can't reach:  ("+x+","+y+")");
        //find a new goal without this position
        EveryQuery everyQuery = new EveryQuery(w);
        Naive_Bayes nb = new Naive_Bayes(everyQuery);

        if(wumpus_status==NOT_FOUND){
            if(nb.findWumpus(wumpus_pos)){
                wumpus_status = FOUND;
            }
        }

        else if (wumpus_status==FOUND){
            nb.set_wumpus_pos(wumpus_pos);
        }

        int[] goal = new int[2];//存了goal
        if_shoot = nb.get_goal(goal,wumpus_status);
        has_goal = true;
        goal_x = goal[0];
        goal_y = goal[1];
        if(if_shoot){
            arrow_goal[0]=goal_x;
            arrow_goal[1]=goal_y;
        }
        System.out.println("change the goal to:  ("+goal_x+","+goal_y+")");
    }
    /*检查当前位置是否为死胡同（周边只有墙壁或陷阱或未知区域）,若为死胡同返回陷阱方向，否则返回-1*/
    //chech if the current position is only around walls or pits or unknown. if it is, return pits direction else return -1
    private int is_impasse(){
       int dir = -1;
       int x = w.getPlayerX();
       int y = w.getPlayerY();

       if(w.isVisited(x+1,y) && !w.hasPit(x+1,y)){
            return -1;
        }else{
            if(w.hasPit(x+1,y)) dir=w.DIR_RIGHT;
        }

        if(w.isVisited(x-1,y) && !w.hasPit(x-1,y)){
            return -1;
        }else{
            if(w.hasPit(x-1,y)) dir=w.DIR_LEFT;
        }

        if(w.isVisited(x,y+1) && !w.hasPit(x,y+1)){
            return -1;
        }else{
            if(w.hasPit(x,y+1)) dir=w.DIR_UP;
        }

        if(w.isVisited(x,y-1) && !w.hasPit(x,y-1)){
            return -1;
        }else{
            if(w.hasPit(x,y-1)) dir=w.DIR_DOWN;
        }

        System.out.println("I'm stuck in a impasse !");
        return dir;

    }

    
}

