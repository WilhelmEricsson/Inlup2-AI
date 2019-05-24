import processing.core.PApplet;
import processing.core.PVector;

public class Tank1 extends Tank {
    boolean started;
    Sensor locator;
    Sensor us_front; //ultrasonic_sensor front

    Tank1(int id, Team team, PVector startpos, float diameter, CannonBall ball, TankProg tp) {
        super(id, team, startpos, diameter, ball, tp);

        us_front = getSensor("ULTRASONIC_FRONT");
        addSensor(us_front);

        started = false;
    }

    public void initialize() {
    }

    // Tanken meddelas om kollision med tree.
    public void message_collision(Tree other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tree)");

        chooseAction();
    }

    // Tanken meddelas om kollision med tanken.
    public void message_collision(Tank other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tank)");

        chooseAction();
    }

    public void arrived() {
        super.arrived(); // Tank
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].arrived()");

        chooseAction();
    }

    public void arrivedRotation() {
        super.arrivedRotation();

        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].arrivedRotation()");
        //moveTo(new PVector(int(random(width)),int(random(height))));
        //moveTo(grid.getRandomNodePosition()); // Slumpmässigt mål.
        moveForward_state(); // Tank
    }

    public void chooseAction() {
        //moveTo(grid.getRandomNodePosition());
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].chooseAction()");
        //resetTargetStates(); // Tank
        //resetAllMovingStates(); // Tank

        float r = getTp().random(1, 360);
        rotateTo(getTp().radians(r));
    }

    public void readSensorDistance() {
        SensorReading sr = readSensor_distance(us_front);
        //println("1sr.distance(): "+ sr.distance());
        if ((sr.distance() < this.radius) && this.isMoving) {
            if (!this.stop_state) {
                System.out.println("Team"+this.team_id+".Tank["+ this.getId() + "] Har registrerat ett hinder. (Tank.readSensorDistance())");
                //stopMoving();
                //stopTurning_state()
                //this.stop_state = true;
                stopMoving_state(); //Tank
                //chooseAction();
            }
        }
    }

    public void updateLogic() {
        //super.updateLogic();


        // Avoid contact with other objects and tanks.
        float threshold = .1f;
        //println("========================================");
        //println("Team"+this.team_id+".Tank["+ this.getId() + "] : " + us_front.readValue(0));
        //if (us_front.readValue(0) < threshold) {
        //  println("*** Team"+this.team_id+".Tank["+ this.getId() + "]: (us_front.readValue(0) < threshold)");
        //}

        // println("Team"+this.team_id+".Tank["+ this.getId() + "] : " + us_front.readValue1());



        if (!started) {
            started = true;
            initialize();

            moveForward_state();
            //moveForward();
        }

        if (!this.userControlled) {
            readSensorDistance();

            //moveForward_state();
            if (this.idle_state) {
                //rotateTo()
                chooseAction();
            }
        }
    }
}