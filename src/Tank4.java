import processing.core.PVector;

import java.util.Arrays;

public class Tank4 extends Tank {

    boolean started;
    boolean first;
    //boolean moving20_120;

    Tank4(int id, Team team, PVector startpos, float diameter, CannonBall ball, TankProg tp) {
        super(id, team, startpos, diameter, ball, tp);

        this.started = false;
        //this.moving20_120 = true;
    }

    // Tanken meddelas om kollision med trädet.
    public void message_collision(Tree other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tree)");

        //rotateTo(grid.getRandomNodePosition());
    }

    public void arrived() {
        super.arrived();
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].arrived()");

        //moveTo(new PVector(int(random(width)),int(random(height))));
        //moveTo(grid.getRandomNodePosition());
        //this.isMoving = false;
    }

    public void updateLogic() {
        super.updateLogic();
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