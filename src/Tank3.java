/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import processing.core.PVector;

public class Tank3 extends Tank {

    PVector cr = new PVector();
    float wandertheta;
    float maxforce;

    Tank3(int id, Team team, PVector startpos, float diameter, CannonBall ball, TankProg tp) {
        super(id, team, startpos, diameter, ball, tp);

        this.wandertheta = 0;
        this.maxforce = 0.05f;
        this.stop_state = false;
    }

    //--------------------
    // A method that calculates and applies a steering force towards a target
    // STEER = DESIRED MINUS VELOCITY
    void seek(PVector target) {
        PVector desired = PVector.sub(target, position);  // A vector pointing from the position to the target

        // Normalize desired and scale to maximum speed
        desired.normalize();
        desired.mult(maxspeed);


        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(desired, velocity);

        steer.limit(maxforce);  // Limit to maximum steering force
        //println("steer: " + steer.getHeading());

        if (steer.heading() < 0) {
            this.turning_right_state = false;
            this.turning_left_state = true;
        } else
        if (steer.heading() > 0) {
            this.turning_right_state = true;
            this.turning_left_state = false;
        }

        //applyForce(steer);
    }

    void wander() {
        float wanderR = 25;         // Radius for our "wander circle"
        float wanderD = 200;//80         // Distance for our "wander circle"
        float change = 0.3f;
        wandertheta += getTp().random(-change, change);     // Randomly change wander theta

        // Now we have to calculate the new position to steer towards on the wander circle
        PVector circlepos = new PVector();
        circlepos.set(velocity);    // Start with velocity
        circlepos.normalize();            // Normalize to get heading
        circlepos.mult(wanderD);          // Multiply by distance
        circlepos.add(position);               // Make it relative to boid's position

        float h = velocity.heading();        // We need to know the heading to offset wandertheta
        //float h = this.heading;        // We need to know the heading to offset wandertheta

        PVector circleOffSet = new PVector(wanderR*getTp().cos(wandertheta+h), wanderR*getTp().sin(wandertheta+h));
        PVector target = PVector.add(circlepos, circleOffSet);

        seek(target);

        // Render wandering circle, etc.
        if (getTp().debugOn) drawWanderStuff(position, circlepos, target, wanderR);
    }

    // A method just to draw the circle associated with wandering
    void drawWanderStuff(PVector position, PVector circle, PVector target, float rad) {
        getTp().stroke(0);
        getTp().noFill();
        getTp().ellipseMode(getTp().CENTER);
        getTp().ellipse(circle.x, circle.y, rad*2, rad*2);

        getTp().ellipse(circle.x, circle.y, 10, 10);
        //ellipse(position.x,position.y,10,10);

        getTp().ellipse(target.x, target.y, 4, 4);
        getTp().line(position.x, position.y, circle.x, circle.y);
        getTp().line(circle.x, circle.y, target.x, target.y);
    }
    //--------------------

    void checkFront_sensor() {
    }

    void checkEnvironment_sensor() {
        float tempx = 0;
        PVector w = new PVector(0, 0);
    }

    public void updateLogic() {
        super.updateLogic();

        if (!this.userControlled) {
            checkEnvironment_sensor();


            if (!this.stop_state) {
                moveForward_state();
                wander();

                //println("heading1: " + this.heading);
                //println("velocity1: " + this.velocity);
                //println("velocityheading1: " + this.velocity.getHeading());
                //this.heading = this.velocity.getHeading();
            }
        }
    }
}