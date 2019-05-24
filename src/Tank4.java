import processing.core.PVector;

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

        if (!started) {
            started = true;
            //moveTo(grid.getRandomNodePosition());
            //moveForward_state();

            //if (!this.isMoving && moving20_120) {
            //  this.moving20_120 = false;
            moveBy(120, 20);
            //moveTo(grid.getRandomNodePosition());
            //}
        }

        if (!this.userControlled) {
            //moveForward_state();
            if (this.stop_state) {
                //rotateTo()
            }
        }
    }
}