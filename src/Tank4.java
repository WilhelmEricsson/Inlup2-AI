import processing.core.PVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Tank4 extends Tank {
    private boolean enemyLocated;
    private boolean started;
    private HashSet<Tree> obstacles;
    private Tank enemyInfocus; // den tanken som ämnas bekämpas
    private HashMap<Tank, PVector> enemyLocatedAt;

    Tank4(int id, Team team, PVector startpos, float diameter, CannonBall ball, TankProg tp) {
        super(id, team, startpos, diameter, ball, tp);
        obstacles = new HashSet<>();
        enemyLocatedAt = new HashMap<>();
        enemyLocated = false;
        this.started = false;
    }

    // Tanken meddelas om kollision med trädet.
    public void message_collision(Tree other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tree)");

        //rotateTo(grid.getRandomNodePosition());
    }

    public void arrived() {
        super.arrived();
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].arrived()");
        ((Team1)getTeam()).addSearchedArea(this.position);
        isMoving = false;
        //moveTo(new PVector(int(random(width)),int(random(height))));
        //moveTo(grid.getRandomNodePosition());
        //this.isMoving = false;
    }

    public void updateLogic() {
        if(enemyLocated){
            battleState();
        }else{
            if(idle_state) {
                searchState();
            }
        }
    }

    //Random search, with preference to position located further away from home base/starting position
    public void searchState(){
        PVector nextPosition = getNextPosition();
        moveTo(nextPosition);
        isMoving = true;


    }

    private PVector getNextPosition(){
        boolean validPosition = false;
        PVector nextPosition = null;
        ArrayList<PVector> potentialPosition = new ArrayList<>();
        //N
        potentialPosition.add(new PVector(this.position.x, this.position.y-stepDist));
        //NE
        potentialPosition.add(new PVector(this.position.x+stepDist, this.position.y-stepDist));
        //E
        potentialPosition.add(new PVector(this.position.x+stepDist, this.position.y));
        //SE
        potentialPosition.add(new PVector(this.position.x+stepDist, this.position.y+stepDist));
        //S
        potentialPosition.add(new PVector(this.position.x, this.position.y+stepDist));
        //SW
        potentialPosition.add(new PVector(this.position.x-stepDist, this.position.y+stepDist));
        //W
        potentialPosition.add(new PVector(this.position.x-stepDist, this.position.y));
        //NW
        potentialPosition.add(new PVector(this.position.x-stepDist, this.position.y-stepDist));
        while(!validPosition){
            nextPosition = potentialPosition.get(Util.getRndDecision(potentialPosition.size()));
            //Detta måste fixas så att casten är säker.
            if(!((Team1)getTeam()).isPosistionSearched(nextPosition) && !obstacles.contains(nextPosition)){
                break;
            }
        }

        return nextPosition;
    }
    public void battleState(){

    }


    @Override
    public int calcUtil(){
        return 0;
    }

    private int argMax(float[] utility){
        int max = 0;
        for(int i = 1; i < utility.length; i++ ){
            if(utility[max] < utility[i]){
                max = i;
            }
        }
        System.err.println("TANK_ID: " + id + " util: " + utility[max] + " arg: " + max);
        return max;
    }
}