import processing.core.PVector;

class Team2 extends Team {

    Team2(TankProg tp, int team_id, int tank_size, int c,
          PVector tank0_startpos, int tank0_id, CannonBall ball0,
          PVector tank1_startpos, int tank1_id, CannonBall ball1,
          PVector tank2_startpos, int tank2_id, CannonBall ball2) {
        super(tp, team_id, tank_size, c, tank0_startpos, tank0_id, ball0, tank1_startpos, tank1_id, ball1, tank2_startpos, tank2_id, ball2);

        tanks[0] = new Tank(tank0_id, this, this.tank0_startpos, this.tank_size, ball0, tp);
        tanks[1] = new Tank(tank1_id, this, this.tank1_startpos, this.tank_size, ball1, tp);
        tanks[2] = new Tank(tank2_id, this, this.tank2_startpos, this.tank_size, ball2, tp);

        //this.homebase_x = width - 151;
        //this.homebase_y = height - 351;
    }

    void updateLogic() {
        //for (int i = 0; i < tanks.length; i++) {
        //  tanks[i].updateLogic();
        //}
    }

    //==================================================
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
            wandertheta += tp.random(-change, change);     // Randomly change wander theta

            // Now we have to calculate the new position to steer towards on the wander circle
            PVector circlepos = new PVector();
            circlepos.set(velocity);    // Start with velocity
            circlepos.normalize();            // Normalize to get heading
            circlepos.mult(wanderD);          // Multiply by distance
            circlepos.add(position);               // Make it relative to boid's position

            float h = velocity.heading();        // We need to know the heading to offset wandertheta
            //float h = this.heading;        // We need to know the heading to offset wandertheta

            PVector circleOffSet = new PVector(wanderR*tp.cos(wandertheta+h), wanderR*tp.sin(wandertheta+h));
            PVector target = PVector.add(circlepos, circleOffSet);

            seek(target);

            // Render wandering circle, etc.
            if (tp.debugOn) drawWanderStuff(position, circlepos, target, wanderR);
        }

        // A method just to draw the circle associated with wandering
        void drawWanderStuff(PVector position, PVector circle, PVector target, float rad) {
            tp.stroke(0);
            tp.noFill();
            tp.ellipseMode(tp.CENTER);
            tp.ellipse(circle.x, circle.y, rad*2, rad*2);

            tp.ellipse(circle.x, circle.y, 10, 10);
            //ellipse(position.x,position.y,10,10);

            tp.ellipse(target.x, target.y, 4, 4);
            tp.line(position.x, position.y, circle.x, circle.y);
            tp.line(circle.x, circle.y, target.x, target.y);
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
}
