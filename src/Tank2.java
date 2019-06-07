/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import processing.core.PVector;

public class Tank2 extends Tank {

    boolean started;

    //*******************************************************
    Tank2(int id, Team team, PVector startpos, float diameter, CannonBall ball, TankProg tp) {
        super(id, team, startpos, diameter, ball, tp);

        this.started = false;

        //this.isMoving = true;
        //moveTo(grid.getRandomNodePosition());
    }

    //*******************************************************
    // Reterera, fly!
    public void retreat() {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].retreat()");
        moveTo(getTp().getGrid().getRandomNodePosition()); // Slumpmässigt mål.
    }

    //*******************************************************
    // Reterera i motsatt riktning (ej implementerad!)
    public void retreat(Tank other) {
        //println("*** Team"+this.team_id+".Tank["+ this.getId() + "].retreat()");
        //moveTo(grid.getRandomNodePosition());
        retreat();
    }

    //*******************************************************
    // Fortsätt att vandra runt.
    public void wander() {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].wander()");
        //rotateTo(grid.getRandomNodePosition());  // Rotera mot ett slumpmässigt mål.
        moveTo(getTp().getGrid().getRandomNodePosition()); // Slumpmässigt mål.
    }


    //*******************************************************
    // Tanken meddelas om kollision med trädet.
    public void message_collision(Tree other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tree)");
        wander();
    }

    //*******************************************************
    // Tanken meddelas om kollision med tanken.
    public void message_collision(Tank other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tank)");

        //moveTo(new PVector(int(random(width)),int(random(height))));
        //println("this.getName());" + this.getName()+ ", this.team_id: "+ this.team_id);
        //println("other.getName());" + other.getName()+ ", other.team_id: "+ other.team_id);

        if ((other.getName() == "tank") && (other.team_id != this.team_id)) {
            if (this.hasShot && (!other.isDestroyed)) {
                System.out.println("["+this.team_id+":"+ this.getId() + "] SKJUTER PÅ ["+ other.team_id +":"+other.getId()+"]");
                fire();
            } else {
                retreat(other);
            }

            rotateTo(other.position);
            //wander();
        } else {
            wander();
        }
    }

    //*******************************************************
    // Tanken meddelas om den har kommit hem.
    public void message_arrivedAtHomebase() {
        //println("*** Team"+this.team_id+".Tank["+ this.getId() + "].message_isAtHomebase()");
        System.out.println("! Hemma!!! Team"+this.team_id+".Tank["+ this.getId() + "]");
    }

    //*******************************************************
    // används inte.
    public void readyAfterHit() {
        super.readyAfterHit();
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].readyAfterHit()");

        //moveTo(grid.getRandomNodePosition());
        wander();
    }

    //*******************************************************
    public void arrivedRotation() {
        super.arrivedRotation();
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].arrivedRotation()");
        //moveTo(new PVector(int(random(width)),int(random(height))));
        arrived();
    }

    //*******************************************************
    public void arrived() {
        super.arrived();
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].arrived()");

        //moveTo(new PVector(int(random(width)),int(random(height))));
        //moveTo(grid.getRandomNodePosition());
        wander();
    }

    //*******************************************************
    public void updateLogic() {
        super.updateLogic();

        if (!started) {
            started = true;
            moveTo(getTp().getGrid().getRandomNodePosition());
        }
        if (!this.userControlled) {
            //moveForward_state();
            if (this.stop_state) {
                //rotateTo()
                wander();
            }
            if (this.idle_state) {
                wander();
            }


        }
    }
}
