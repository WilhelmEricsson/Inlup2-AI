/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;

import static processing.core.PApplet.radians;
import static processing.core.PApplet.saveStream;

class Tank extends Sprite {
    int id;
    float stepDist = 200;
    //String name; //Sprite
    int team_id;

    PVector acceleration;
    PVector velocity;
    //PVector position; //Sprite

    float rotation;
    float rotation_speed;

    Team team;
    PImage img;
    //float diameter; //Sprite
    //float radius; //Sprite

    float maxrotationspeed;
    float maxspeed;
    float maxforce;

    int health;// 3 är bra, 2 är mindre bra, 1 är immobilized, 0 är destroyed.
    boolean isImmobilized; // Tanken kan snurra på kanonen och skjuta, men inte förflytta sig.
    boolean isDestroyed; // Tanken är död.

    //PVector hitArea;

    PVector startpos;
    PVector positionPrev; //spara temp senaste pos.

    Node startNode; // noden där tanken befinner sig.

    boolean hasTarget; // Används just nu för att kunna köra "manuellt" (ai har normalt target).
    PVector targetPosition; // Används vid förflyttning mot target.
    float targetHeading; // Används vid rotation mot en target.
    PVector sensor_targetPosition;

    PVector[] otherTanks  = new PVector[5];
    PVector distance3_sensor;

    ArrayList listOfActions; // Används ännu inte.

    float heading; // Variable for heading!

    // variabler som anger vad tanken håller på med.
    boolean backward_state;
    boolean forward_state;
    boolean turning_right_state;
    boolean turning_left_state;
    boolean turning_turret_right_state;
    boolean turning_turret_left_state;
    boolean stop_state;
    boolean stop_turning_state;
    boolean stop_turret_turning_state;

    boolean idle_state; // Kan användas när tanken inte har nåt att göra.

    boolean isMoving; // Tanken är i rörelse.
    boolean isRotating; // Tanken håller på att rotera.
    boolean isColliding; // Tanken håller på att krocka.
    boolean isAtHomebase;
    boolean userControlled; // Om användaren har tagit över kontrollen.

    boolean hasShot; // Tanken kan bara skjuta om den har laddat kanonen, hasShot=true.
    CannonBall ball;

    float s = 2.0f;
    float image_scale;

    boolean isSpinning; // Efter träff snurrar tanken runt, ready=false.
    boolean isReady; // Tanken är redo för action efter att tidigare blivit träffad.
    int remaining_turns;
    float heading_saved; // För att kunna återfå sin tidigare heading efter att ha snurrat.

    Turret turret;

    // Tank sensors
    private HashMap<String, Sensor> mappedSensors = new HashMap<String, Sensor>();
    private ArrayList<Sensor> sensors = new ArrayList<Sensor>();

    protected ArrayList<Sensor> mySensors = new ArrayList<Sensor>();


    protected boolean messageReceived = false;
    private TankProg tp;

    //**************************************************
    Tank(int id, Team team, PVector _startpos, float diameter, CannonBall ball, TankProg tp) {
        System.out.println(("*** NEW TANK(): [" + team.getId()+":"+id+"]"));
        this.tp = tp;
        this.id = id;
        this.team = team;
        this.team_id = this.team.getId();

        this.name = "tank";

        this.startpos = new PVector(_startpos.x, _startpos.y);
        this.position = new PVector(this.startpos.x, this.startpos.y);
        this.velocity = new PVector(0, 0);

        this.acceleration = new PVector(0, 0);
        this.positionPrev = new PVector(this.position.x, this.position.y); //spara temp senaste pos.
        this.targetPosition = new PVector(this.position.x, this.position.y); // Tanks har alltid ett target.

        //this.startNode = grid.getNearestNodePosition(this.startpos);


        if (this.team.getId() == 0) this.heading = tp.radians(0); // "0" radians.
        if (this.team.getId() == 1) this.heading = tp.radians(180); // "3.14" radians.

        this.targetHeading = this.heading; // Tanks har alltid en heading mot ett target.
        this.hasTarget = false;

        this.diameter = diameter;
        this.radius = this.diameter/2; // For hit detection.

        this.backward_state = false;
        this.forward_state = false;
        this.turning_right_state = false;
        this.turning_left_state = false;
        this.turning_turret_right_state = false;
        this.turning_turret_left_state = false;
        this.stop_state = true;
        this.stop_turning_state = true;
        this.stop_turret_turning_state = true;
        // Under test
        this.isMoving = false;
        this.isRotating = false;
        this.isAtHomebase = true;
        this.idle_state = true;

        this.ball = ball;
        this.hasShot = false;
        this.maxspeed = 3; //3;
        this.maxforce = 0.1f;
        this.maxrotationspeed = radians(3);
        this.rotation_speed = 0;
        this.image_scale = 0.5f;
        this.isColliding = false;


        //this.img = loadImage("tankBody2.png");
        this.turret = new Turret(this.diameter/2, tp);

        this.radius = diameter/2;

        this.health = 3;// 3 är bra, 2 är mindre bra, 1 är immobilized, 0 är oskadliggjord.
        this.isReady = true; // Tanken är redo för action.
        this.isImmobilized = false; // Tanken kan snurra på kanonen och skjuta, men inte förflytta sig.
        this.isDestroyed = false; // Tanken är död.

        this.isSpinning = false;
        this.remaining_turns = 0;
        this.heading_saved = this.heading;

        this.ball.setColor(this.team.getColor());
        this.ball.setOwner(this);

        initializeSensors();
    }


    //**************************************************
    int getId() {
        return this.id;
    }

    //**************************************************
    //String getName(){
    //  return this.name;
    //}

    //**************************************************
    float getHeadingInDegrees() {
        return tp.degrees(this.heading);
    }

    //**************************************************
    float getHeading() {
        return this.heading;
    }

    //**************************************************
    // Anropas då användaren tar över kontrollen av en tank.
    void takeControl() {
        System.out.println("*** Tank[" + team.getId()+"].takeControl()");
        stopMoving_state();
        this.userControlled = true;
    }

    //**************************************************
    // Anropas då användaren släpper kontrollen av en tank.
    void releaseControl() {
        System.out.println("*** Tank[" + team.getId()+"].releaseControl()");
        stopMoving_state();
        idle_state = true;

        this.userControlled = false;
    }

    //**************************************************
    // Används ännu inte.
    PVector getRealPosition() {
        return this.position;
    }

    //**************************************************
    // Returns the Sensor with the specified ID

    public Sensor getSensor(String ID) {
        return mappedSensors.get(ID);
    }

    //**************************************************
    // Add your Sensor.

    public void addSensor(Sensor s) {
        mySensors.add(s);
    }

    //**************************************************
    //Register a sensor inside this robot, with the given ID

    protected void registerSensor(Sensor sensor, String ID) {
        mappedSensors.put(ID, sensor);
        sensors.add(sensor);
    }

    //**************************************************
    protected void initializeSensors() {

        SensorDistance ultrasonic_front = new SensorDistance(this, 0f, tp);
        registerSensor(ultrasonic_front, "ULTRASONIC_FRONT");

        SightSensor sightSensor = new SightSensor(this);
        registerSensor(sightSensor, "SIGHT_SENSOR");

        //SensorDistance ultrasonic_back = new SensorDistance(this, 180f);
        //registerSensor(ultrasonic_back, "ULTRASONIC_BACK");

    /*
     SensorCompass compass = new SensorCompass(game, this);
     registerSensor(compass, "COMPASS");

     SensorDistance ultrasonic_left = new SensorDistance(game, this, 270f);
     registerSensor(ultrasonic_left, "ULTRASONIC_LEFT");

     SensorDistance ultrasonic_right = new SensorDistance(game, this, 90f);
     registerSensor(ultrasonic_right, "ULTRASONIC_RIGHT");

     SensorDistance ultrasonic_front = new SensorDistance(game, this, 0f);
     registerSensor(ultrasonic_front, "ULTRASONIC_FRONT");

     SensorDistance ultrasonic_back = new SensorDistance(game, this, 180f);
     registerSensor(ultrasonic_back, "ULTRASONIC_BACK");
     */
    }

    //**************************************************

    SensorReading readSensor_distance(Sensor sens) {
        //println("*** Tank.readSensorDistance()");

        Sprite df = sens.readValue().obj();

        return sens.readValue();
    }

    //**************************************************
    void readSensors() {/*
        System.out.println("*** Tank[" + team.getId() + "].readSensors()");
        System.out.println("sensors: " + sensors);

        for (Sensor s : mySensors) {
            if (s.tank == this) {
                PVector sens = (s.readValue().obj().position);

                //println("============");
                //println("("+sens.x + " , "+sens.y+")");
                tp.ellipse(sens.x, sens.y, 10,10);
                if (sens != null) {
                    tp.line(this.position.x, this.position.y, sens.x, sens.y);
                    System.out.println("Tank" + this.team.getId() + ":" + this.id + " ( " + sens.x + ", " + sens.y + " )");

                }
            }

        }
        */
    }

    //**************************************************
    void spin(int antal_varv) {
        System.out.println("*** Tank[" + team.getId()+"].spin(int)");
        if (!this.isSpinning) {
            this.heading_saved = this.heading;
            isSpinning = true;
            this.remaining_turns = antal_varv;
        }
    }

    //**************************************************
    // After calling this method, the tank can shoot.
    void loadShot() {
        System.out.println("*** Tank[" + team.getId()+":"+id+"].loadShot() and ready to shoot.");

        this.hasShot = true;
        this.ball.loaded();
    }

    //**************************************************
    void testCollisionSensor() {
    }

    //**************************************************
    void fire() {
        // Ska bara kunna skjuta när den inte rör sig.
        if (this.stop_state) {
            System.out.println("*** Tank[" + this.team.getId()+":"+this.id+"].fire()");

            if (this.hasShot) {
                System.out.println("! Tank["+ this.getId() + "] – PANG.");
                this.hasShot = false;

                PVector force = PVector.fromAngle(this.heading + this.turret.heading);
                force.mult(10);
                this.ball.applyForce(force);

                Util.shoot(this.id); // global funktion i huvudfilen

                //soundManager.playSound("tank_firing");
                //soundManager.playSound("blast");
            } else {
                System.out.println("! Tank["+ this.getId() + "] – You have NO shot loaded and ready.");
            }
        } else {
            System.out.println("! Tank["+ this.getId() + "] – The tank must stand STILL to shoot.");
        }
    }

    //**************************************************
    // Anropad från den cannonBall som träffat.
    final boolean takeDamage() {
        System.out.println("*** Tank["+ this.getId() + "].takeDamage()");

        if (!this.isDestroyed) {
            this.health -= 1;

            System.out.println("! Tank[" + team.getId()+":"+id+"] has been hit, health is now "+ this.health);

            stopMoving_state();
            resetTargetStates();

            if (!this.isImmobilized) {
                if (this.health == 1) {
                    this.isImmobilized = true;
                }
            }

            if (this.health <= 0) {
                this.health = 0;
                this.isDestroyed = true;
                this.isSpinning  = false;
                this.isReady = false;
                team.addCasualty(this);
                return true;
            }

            spin(3);
            this.isReady = false; // Efter träff kan inte tanken utföra action, så länge den "snurrar".


            return true;
        }

        return false; // ingen successfulHit omtanken redan är destroyed.
    }

    //**************************************************
    // Anropad från sin egen cannonBall efter träff.
    final void successfulHit() {
        this.team.messageSuccessfulHit();
    }

    //**************************************************
    // Det är denna metod som får tankens kanon att svänga vänster.
    void turnTurretLeft_state() {
        if (this.stop_state) {
            if (!this.turning_turret_left_state) {
                //System.out.println("*** Tank[" + getId() + "].turnTurretLeft_state()");
                this.turning_turret_right_state = false;
                this.turning_turret_left_state = true;
                this.stop_turret_turning_state = false;
            }
        } else {
            System.out.println("Tanken måste stå still för att kunna rotera kanonen.");
        }
    }

    //**************************************************
    void turnTurretLeft() {
        this.turret.turnLeft();
    }

    //**************************************************
    // Det är denna metod som får tankens kanon att svänga höger.
    void turnTurretRight_state() {
        if (this.stop_state) {
            if (!this.turning_turret_right_state) {
                //System.out.println("*** Tank[" + getId() + "].turnTurretRight_state()");
                this.turning_turret_left_state = false;
                this.turning_turret_right_state = true;
                this.stop_turret_turning_state = false;
            }
        } else {
            System.out.println("Tanken måste stå still för att kunna rotera kanonen.");
        }
    }

    //**************************************************
    void turnTurretRight() {
        this.turret.turnRight();
    }

    //**************************************************
    // Det är denna metod som får tankens kanon att sluta rotera.
    void stopTurretTurning_state() {
        if (!this.stop_turret_turning_state) {
            //System.out.println("*** Tank[" + getId() + "].stopTurretTurning_state()");
            this.turning_turret_left_state = false;
            this.turning_turret_right_state = false;
            this.stop_turret_turning_state = true;
        }
    }

    //**************************************************
    // Det är denna metod som får tanken att svänga vänster.
    void turnLeft_state() {
        this.stop_turning_state = false;
        this.turning_right_state = false;

        if (!this.turning_left_state) {
            //System.out.println("*** Tank[" + getId() + "].turnLeft_state()");
            this.turning_left_state = true;
        }
    }

    //**************************************************
    void turnLeft() {
        //println("*** Tank[" + getId()+"].turnLeft()");

        if (this.hasTarget && tp.abs(this.targetHeading - this.heading) < this.maxrotationspeed) {
            this.rotation_speed -= this.maxforce;
        } else {
            this.rotation_speed += this.maxforce;
        }
        if (this.rotation_speed > this.maxrotationspeed) {
            this.rotation_speed = this.maxrotationspeed;
        }
        this.heading -= this.rotation_speed;
    }

    //**************************************************
    // Det är denna metod som får tanken att svänga höger.
    void turnRight_state() {
        this.stop_turning_state = false;
        this.turning_left_state = false;

        if (!this.turning_right_state) {
            //System.out.println("*** Tank[" + getId() + "].turnRight_state()");
            this.turning_right_state = true;
        }
    }

    //**************************************************
    void turnRight() {
        //println("*** Tank[" + getId() + "].turnRight()");

        if (this.hasTarget && tp.abs(this.targetHeading - this.heading) < this.maxrotationspeed) {
            this.rotation_speed -= this.maxforce;
        } else {
            this.rotation_speed += this.maxforce;
        }
        if (this.rotation_speed > this.maxrotationspeed) {
            this.rotation_speed = this.maxrotationspeed;
        }
        this.heading += this.rotation_speed;
    }

    //**************************************************
    void turnRight(PVector targetPos) {
        //System.out.println("*** Tank[" + getId() + "].turnRight(PVector)");
        PVector desired = PVector.sub(targetPos, position);  // A vector pointing from the position to the target

        desired.setMag(0);
        PVector steer = PVector.sub(desired, velocity);
        steer.limit(maxforce);  // Limit to maximum steering force
        applyForce(steer);

    }

    //**************************************************
    void stopTurning() {
        //System.out.println("*** Tank[" + getId()+"].stopTurning()");
        this.rotation_speed = 0;
        arrivedRotation();
    }

    //**************************************************
    // Det är denna metod som får tanken att sluta svänga.
    void stopTurning_state() {
        if (!this.stop_turning_state) {
            //System.out.println("*** Tank[" + getId() + "].stopTurning_state()");
            this.turning_left_state = false;
            this.turning_right_state = false;
            this.stop_turning_state = true;

            //System.out.println("! Tank[" + getId() + "].stopTurning_state() – stop_turning_state=true");
        }
    }

    //**************************************************
    void moveTo(float x, float y) {
        //System.out.println("*** Tank["+ this.getId() + "].moveTo(float x, float y)");

        moveTo(new PVector(x, y));
    }

    //**************************************************
    void moveTo(PVector coord) {
        //println("*** Tank["+ this.getId() + "].moveTo(PVector)");
        if (!isImmobilized) {
            //System.out.println("*** Tank["+ this.getId() + "].moveTo(PVector)");

            this.idle_state = false;
            this.isMoving = true;
            this.stop_state = false;

            this.targetPosition.set(coord);
            this.hasTarget = true;
        }
    }

    //**************************************************
    void moveBy(float x, float y) {
        //System.out.println("*** Tank["+ this.getId() + "].moveBy(float x, float y)");

        moveBy(new PVector(x, y));
    }

    //**************************************************
    void moveBy(PVector coord) {
        //System.out.println("*** Tank["+ this.getId() + "].moveBy(PVector)");

        PVector newCoord = PVector.add(this.position, coord);
        PVector nodevec = tp.getGrid().getNearestNodePosition(newCoord);

        moveTo(nodevec);
    }

    //**************************************************
    // Det är denna metod som får tanken att gå framåt.
    void moveForward_state() {
        //System.out.println("*** Tank[" + getId() + "].moveForward_state()");

        if (!this.forward_state) {
            this.acceleration.set(0, 0, 0);
            this.velocity.set(0, 0, 0);

            this.forward_state = true;
            this.backward_state = false;
            this.stop_state = false;
        }
    }

    //**************************************************
    void moveForward() {
        //println("*** Tank[" + getId() + "].moveForward()");

        // Offset the angle since we drew the ship vertically
        float angle = this.heading; // - PI/2;
        // Polar to cartesian for force vector!
        PVector force = new PVector(tp.cos(angle), tp.sin(angle));
        force.mult(0.1f);
        applyForce(force);
    }

    //**************************************************
    void moveForward(int numSteps) {
    }

    //**************************************************
    // Det är denna metod som får tanken att gå bakåt.
    void moveBackward_state() {
        //System.out.println("*** Tank[" + getId() + "].moveBackward_state()");
        this.stop_state = false;
        this.forward_state = false;

        if (!this.backward_state) {
            //System.out.println("! Tank[" + getId() + "].moveBackward_state() – (!this.backward_state)");
            this.acceleration.set(0, 0, 0);
            this.velocity.set(0, 0, 0);
            this.backward_state = true;
        }
    }

    //**************************************************
    void moveBackward() {
        //System.out.println("*** Tank[" + getId() + "].moveBackward()");
        // Offset the angle since we drew the ship vertically
        float angle = this.heading - tp.PI; // - PI/2;
        // Polar to cartesian for force vector!
        PVector force = new PVector(tp.cos(angle), tp.sin(angle));
        force.mult(0.1f);
        applyForce(force);
    }

    //**************************************************
    void stopMoving() {
        //System.out.println("*** Tank[" + getId() + "].stopMoving()");

        this.acceleration.set(0, 0, 0);
        this.velocity.set(0, 0, 0);

        this.isMoving = false;

        resetTargetStates();
    }

    //**************************************************
    // Det är denna metod som får tanken att sluta åka framåt eller bakåt.
    // "this.stop_state" anropas
    void stopMoving_state() {
        //println("stopMoving_state() ");

        if (!this.stop_state) {
            //println("*** Tank[" + getId() + "].stopMoving_state()");

            resetMovingStates();
            stopMoving();
        }
    }

    //**************************************************
    void resetAllMovingStates() {
        //System.out.println("*** Tank[" + getId() + "].resetAllMovingStates()");
        this.stop_state = true;
        this.backward_state = false;
        this.forward_state = false;

        this.turning_right_state = false;
        this.turning_left_state = false;
        this.turning_turret_right_state = false;
        this.turning_turret_left_state = false;
        this.stop_turning_state = true;
        this.stop_turret_turning_state = true;

        this.velocity = new PVector(0, 0);
        this.acceleration = new PVector(0, 0);
    }

    //**************************************************
    void resetMovingStates() {
        //System.out.println("*** Tank[" + getId() + "].resetMovingStates()");
        this.stop_state = true;
        this.backward_state = false;
        this.forward_state = false;
    }

    //**************************************************
    void resetTargetStates() {
        //System.out.println("*** Tank[" + getId() + "].resetTargetStates()");
        this.targetPosition = new PVector(this.position.x, this.position.y);

        this.targetHeading = this.heading; // Tanks har alltid en heading mot ett target.
        this.hasTarget = false;
    }

    //**************************************************
    void updatePosition() {

        this.positionPrev.set(this.position); // spara senaste pos.

        this.velocity.add(this.acceleration);
        this.velocity.limit(this.maxspeed);
        this.position.add(this.velocity);
        this.acceleration.mult(0);
    }

    //**************************************************
    // Newton's law: F = M * A
    void applyForce(PVector force) {
        this.acceleration.add(force);
    }

    //**************************************************
    public void destroy() {
        System.out.println("*** Tank.destroy()");
        //dead = true;
        this.isDestroyed = true;
    }

    //**************************************************

    void rotating() {
        //println("*** Tank["+ this.getId() + "].rotating()");
        if (!isImmobilized) {

            if (this.hasTarget) {
                float diff = this.targetHeading - this.heading;

                if ((tp.abs(diff) <= radians(0.5f))) {
                    this.isRotating = false;
                    this.heading = this.targetHeading;
                    this.targetHeading = 0.0f;
                    this.hasTarget = false;
                    stopTurning_state();
                    arrivedRotation();
                } else if ((diff) > radians(0.5f)) {

                    turnRight_state();
                } else if ((diff) < radians(0.5f)) {
                    turnLeft_state();
                }
            }
        }
    }

    //**************************************************

    void rotateTo(float angle) {
        //System.out.println("*** Tank["+ this.getId() + "].rotateTo(float): "+angle);

        if (!isImmobilized) {

            this.heading = angle;

            // Hitta koordinaten(PVector) i tankens riktning
            Sensor sens = getSensor("ULTRASONIC_FRONT");
            PVector sens_pos = (sens.readValue().obj().position);
            PVector grid_pos = tp.getGrid().getNearestNodePosition(sens_pos);
            rotateTo(grid_pos); // call "rotateTo(PVector)"
        }
    }

    //**************************************************

    void rotateTo(PVector coord) {
        //System.out.println("*** Tank["+ this.getId() + "].rotateTo(PVector) – ["+(int)coord.x+","+(int)coord.y+"]");

        if (!isImmobilized) {

            this.idle_state = false;
            this.isMoving = false;
            this.isRotating = true;
            this.stop_state = false;
            this.hasTarget = true;


            PVector target = new PVector(coord.x, coord.y);
            PVector me = new PVector(this.position.x, this.position.y);

            // Bestäm headin till target.
            PVector t = PVector.sub(target, me);
            this.targetHeading = t.heading();
        }
    }

    //**************************************************
    // A method that calculates a steering force towards a target
    // STEER = DESIRED MINUS VELOCITY
    void arrive() {

        // rotera tills heading mot target.
        PVector desired = PVector.sub(this.targetPosition, this.position);  // A vector pointing from the position to the target
        float d = desired.mag();
        // If arrived

        // Scale with arbitrary damping within 100 pixels
        if (d < 100) {
            float m = tp.map(d, 0, 100, 0, maxspeed);
            desired.setMag(m);
        } else {
            desired.setMag(maxspeed);
        }

        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(desired, velocity);
        steer.limit(maxforce);  // Limit to maximum steering force
        applyForce(steer);

        if (d < 1) {
            arrived();
        }
    }

    //**************************************************
    // Tanken meddelas om att tanken är redo efter att blivit träffad.
    void readyAfterHit() {
        System.out.println("*** Tank["+ this.getId() + "].readyAfterHit()");

        if (!this.isDestroyed) {
            this.isReady = true; // Efter träff kan inte tanken utföra action, så länge den "snurrar".
        }
    }

    //**************************************************
    // Tanken meddelas om kollision med trädet.
    void arrivedRotation() {
        //System.out.println("*** Tank["+ this.getId() + "].arrivedRotation()");
        stopTurning_state();
        this.isMoving = false;
    }

    //**************************************************
    void arrived() {
        //System.out.println("*** Tank["+ this.getId() + "].arrived()");
        this.isMoving = false;
        stopMoving_state();
    }

    //**************************************************
    // Är tänkt att överskuggas (override) i subklassen.
    void updateLogic() {
       /*if(!isDestroyed && !isMoving && idle_state){
            chooseAction();
        }*/

        // Use of SightSensor example
        /*
        SensorReading reading = getLatestSightSensorReading();
        if (reading.obj() != null) {
            SightSensor sens = (SightSensor)getSensor("SIGHT_SENSOR");
            sens.drawSensor(reading.obj().position);
        }*/
    }

    public void chooseAction(){
        int cmd = calcUtil();
        switch (cmd){
            case 0: //N
                moveTo(new PVector(this.position.x, this.position.y-stepDist));
                isMoving = true;
                break;
            case 1: //NE
                moveTo(new PVector(this.position.x+stepDist, this.position.y-stepDist));
                isMoving = true;
                break;
            case 2://E
                moveTo(new PVector(this.position.x+stepDist, this.position.y));
                isMoving = true;
                break;
            case 3://SE
                moveTo(new PVector(this.position.x+stepDist, this.position.y+stepDist));
                isMoving = true;
                break;
            case 4://S
                moveTo(new PVector(this.position.x, this.position.y+stepDist));
                isMoving = true;
                break;
            case 5://SW
                moveTo(new PVector(this.position.x-stepDist, this.position.y+stepDist));
                isMoving = true;
                break;
            case 6://W
                moveTo(new PVector(this.position.x-stepDist, this.position.y));
                isMoving = true;
                break;
            case 7://NW
                moveTo(new PVector(this.position.x-stepDist, this.position.y-stepDist));
                isMoving = true;
                break;
        }
    }
    public int calcUtil(){
        return Util.getRndDecision();
    }



    //**************************************************
    // Called from game
    final void update() {

        // Om tanken fortfarande lever.
        if (!this.isDestroyed) {
            // Om tanken har blivit träffad och håller på och snurrar runt.
            int spinning_speed = 5;
            if (this.isSpinning) {
                if (this.remaining_turns > 0) {
                    this.heading += rotation_speed * spinning_speed;

                    if (this.heading > (this.heading_saved + (2 * tp.PI))||(this.heading == this.heading_saved)) {

                        this.remaining_turns -= 1;
                        this.heading = this.heading_saved;
                    }
                } else {

                    this.heading = this.heading_saved;
                    this.remaining_turns = 0;
                    this.isSpinning = false;
                    this.isReady = true;

                    this.idle_state = true;
                }
            } else {

                // Om tanken är redo för handling och kan agera.
                if (!this.isImmobilized && this.isReady) {

                    // Om tanken är i rörelse.
                    if (this.isMoving) {

                        this.heading = this.velocity.heading();
                        arrive();
                    }


                    // Om tanken ska stanna, men ännu inte gjort det.
                    if (this.stop_state && !this.idle_state) {

                        resetTargetStates(); // Tank
                        resetAllMovingStates(); // Tank
                        this.idle_state = true;

                        //System.out.println("! Tank[" + getId() + "].update() – idle_state = true");
                    }

                    // Om tanken håller på och rotera.
                    if (this.isRotating) {
                        rotating();
                    }

                    // ----------------
                    // state-kontroller
                    if (this.forward_state) {
                        moveForward();
                    }
                    if (this.backward_state) {
                        moveBackward();
                    }
                    if (this.turning_right_state) {
                        turnRight();
                    }
                    if (this.turning_left_state) {
                        turnLeft();
                    }

                    if (this.stop_state && !this.isMoving && this.hasTarget) {
                        //System.out.println("Tank["+ this.getId() + "], vill stanna!");
                        //this.stop_state = false;
                        stopMoving();
                    }
                    if (this.stop_turning_state && !this.isMoving && this.hasTarget) {
                        //System.out.println("Tank["+ this.getId() + "], vill sluta rotera!");
                        stopTurning();
                    }
                } // end (!this.isImmobilized && this.isReady)



                // Om tanken är immobilized
                // Om tanken har laddat ett skott.
                if (this.hasShot) {
                    this.ball.updateLoadedPosition(this.position);
                }

                //---------------
                // state-kontroller ...
                if (this.turning_turret_left_state) {
                    turnTurretLeft();
                }
                if (this.turning_turret_right_state) {
                    turnTurretRight();
                }


                readSensors();
            }
            updatePosition();
        }
    }

    //**************************************************
    // Anropas från spelet.
    void checkEnvironment() {
        //checkEnvironment_sensor();

        // Check for collisions with Canvas Boundaries
        float r = this.diameter/2;
        if ((this.position.y+r > tp.height) || (this.position.y-r < 0) ||
                (this.position.x+r > tp.width) || (this.position.x-r < 0)) {
            if (!this.stop_state) {
                collided = true;
                this.position.set(this.positionPrev); // Flytta tillbaka.
                //println("***");
                stopMoving_state();
            }
        }

        if (
                position.x > team.homebase_x &&
                        position.x < team.homebase_x+team.homebase_width &&
                        position.y > team.homebase_y &&
                        position.y < team.homebase_y+team.homebase_height) {
            if (!isAtHomebase) {
                isAtHomebase = true;
                message_arrivedAtHomebase();
            }
        } else {
            isAtHomebase = false;
        }
    }

    // Tanken meddelas om att tanken är i hembasen.
    public void message_arrivedAtHomebase() {
        System.out.println("! Tank["+ this.getId() + "] – har kommit hem.");
    }

    // Tanken meddelas om kollision med trädet.
    public void message_collision(Tree other) {
        //System.out.println("*** Tank["+ this.getId() + "].collision(Tree)");
        //println("Tank.COLLISION");
    }

    // Tanken meddelas om kollision med den andra tanken.
    public void message_collision(Tank other) {
        //System.out.println("*** Tank["+ this.getId() + "].collision(Tank)");
        //println("Tank.COLLISION");
    }

    public void resetSightSensorReading() {
        ((SightSensor) getSensor("SIGHT_SENSOR")).reset();
    }

    protected boolean collided = false;

    //**************************************************
    public void checkCollision(Tree other) {

        //println("*** Tank.checkCollision(Tree)");
        // Check for collisions with "no Smart Objects", Obstacles (trees, etc.)

        // Get distances between the tree component
        PVector distanceVect = PVector.sub(other.position, this.position);

        // Calculate magnitude of the vector separating the tank and the tree
        float distanceVectMag = distanceVect.mag();

        // Minimum distance before they are touching
        float minDistance = this.radius + other.radius;

        if (distanceVectMag <= minDistance && !this.stop_state) {

            //System.out.println("! Tank["+ this.getId() + "] – collided with Tree.");

            if (!this.stop_state) {
                this.position.set(this.positionPrev); // Flytta tillbaka.

                // Kontroll om att tanken inte "fastnat" i en annan tank.
                distanceVect = PVector.sub(other.position, this.position);
                distanceVectMag = distanceVect.mag();

                // Tror att lägga allt i else-satsen fixade så dem inte fuckar ur ifall dem fastnar i varandra
                if (distanceVectMag < minDistance) {
                    System.out.println("! Tank["+ this.getId() + "] – FAST I ETT TRÄD");
                } else {
                    collided = true;
                    stopMoving_state();
                }
            }

            if (this.hasShot) {
                this.ball.updateLoadedPosition(this.positionPrev);
            }


            // Meddela tanken om att kollision med trädet gjorts.
            message_collision( other);//collision(Tree);
        }
    }

    //**************************************************
    // Called from environment
    // Keeps an array with vectors to the other tanks, so the tank object can access the other tanks when called for.
    void checkCollision(Tank other) {

        //println("*** Tank.checkCollision(Tank)");
        // Check for collisions with "Smart Objects", other Tanks.

        // Get distances between the tanks components
        PVector distanceVect = PVector.sub(other.position, this.position);

        // Calculate magnitude of the vector separating the tanks
        float distanceVectMag = distanceVect.mag();

        // Minimum distance before they are touching
        float minDistance = this.radius + other.radius;

        if (distanceVectMag <= minDistance) {
            //System.out.println("! Tank["+ this.getId() + "] – collided with another Tank" + other.team_id + ":"+other.id);

            this.position.set(this.positionPrev); // Flytta tillbaka.
            if (!this.stop_state) {
                //this.position.set(this.positionPrev); // Flytta tillbaka.

                // Kontroll om att tanken inte "fastnat" i en annan tank.
                distanceVect = PVector.sub(other.position, this.position);
                distanceVectMag = distanceVect.mag();

                // Tror att lägga allt i else-satsen fixade så dem inte fuckar ur ifall dem fastnar i varandra
                if (distanceVectMag <= minDistance) {
                    System.out.println("! Tank["+ this.getId() + "] – FAST I EN ANNAN TANK");
                } else {
                    collided = true;
                    this.isMoving = false;
                    stopMoving_state();
                }


            }

            if (this.hasShot) {
                this.ball.updateLoadedPosition(this.positionPrev);
            }


            // Meddela tanken om att kollision med den andra tanken gjorts.
            message_collision(other);
        }
    }

    public PVector checkPotentialCollision(Sprite other, PVector potentialPosition){

        // Get distances between the tanks components
        PVector distanceVect = PVector.sub(other.position, potentialPosition);

        // Calculate magnitude of the vector separating the tanks
        float distanceVectMag = distanceVect.mag();

        // Minimum distance before they are touching
        float minDistance = this.radius + other.radius;

        if (distanceVectMag <= minDistance) {
            return null;
        }


        return potentialPosition;
    }
    void setNode() {
        //setTargetPosition(this.position);
    }

    void displayInfo() {
        String[] blockedPaths = new String[8];
        if (this instanceof Tank4) {
            Tank4 test = (Tank4) this;
            blockedPaths = test.blockedPaths.toArray(new String[8]);
        }
        tp.fill(230);
        tp.rect(tp.width - 151, 0, 150, 420);
        tp.strokeWeight(1);
        tp.fill(255, 0, 0);
        tp.stroke(255, 0, 0);
        tp.textSize(10);
        tp.text("id: "+this.id+"\n"+
                        "health: "+this.health+"\n"+
                        "position: ("+(int)this.position.x +","+(int)this.position.y+")"+"\n"+
                        "isMoving: "+this.isMoving+"\n"+
                        "isSpinning : "+this.isSpinning +"\n"+
                        "remaining_turns: "+this.remaining_turns +"\n"+
                        "isReady : "+this.isReady +"\n"+
                        "hasTarget : "+this.hasTarget +"\n"+
                        "stop_state : "+this.stop_state +"\n"+
                        "stop_turning_state : "+this.stop_turning_state +"\n"+
                        "idle_state : "+this.idle_state +"\n"+
                        "isDestroyed : "+this.isDestroyed +"\n"+
                        "isImmobilized : "+this.isImmobilized +"\n"+
                        "targetHeading : "+this.targetHeading +"\n"+
                        "heading : "+this.heading +"\n"+
                        "heading_saved: "+this.heading_saved +"\n"+
                        "blockedPaths: "+blockedPaths[0] +"\n"+
                        "blockedPaths: "+blockedPaths[1] +"\n"+
                        "blockedPaths: "+blockedPaths[2] +"\n"+
                        "blockedPaths: "+blockedPaths[3] +"\n"+
                        "blockedPaths: "+blockedPaths[4] +"\n"+
                        "blockedPaths: "+blockedPaths[5] +"\n"+
                        "blockedPaths: "+blockedPaths[6] +"\n"+
                        "blockedPaths: "+blockedPaths[7] +"\n"
                , tp.width - 145, 35 );
    }

    //**************************************************
    void drawTank(float x, float y) {
        tp.fill(this.team.getColor());
        //tp.rect(x-100, y-100, 200, 200);
        if (this.team.getId() == 0) tp.fill((((255/6) * this.health) *40 ), 50* this.health, 50* this.health, 255 - this.health*60);
        if (this.team.getId() == 1) tp.fill(10*this.health, (255/6) * this.health, (((255/6) * this.health) * 3), 255 - this.health*60);

        if (this.userControlled) {
            tp.strokeWeight(3);
        } else tp.strokeWeight(1);

        tp.ellipse(x, y, 50, 50);
        tp.strokeWeight(1);
        tp.line(x, y, x+25, y);

        tp.fill(this.team.getColor(), 255);
        this.turret.display();
    }

    //**************************************************
    final void display() {
        tp.imageMode(tp.CENTER);
        tp.pushMatrix();
        tp.translate(this.position.x, this.position.y);

        tp.rotate(this.heading);

        //image(img, 20, 0);
        drawTank(0, 0);

        if (tp.debugOn) {
            tp.noFill();
            tp.strokeWeight(2);
            tp.stroke(255, 0, 0);
            tp.ellipse(0, 0, this.radius * 2, this.radius * 2);

            //for (Sensor s : mySensors) {
            //  if (s.tank == this) {
            //     strokeWeight(2);
            //     stroke(0,0,255);
            //     PVector sens = s.readValue1();
            //     println("============");
            //     println("("+sens.x + " , "+sens.y+")");
            //     //ellipse(sens.x, sens.y, 10,10);
            //  }
            //}
        }

        tp.popMatrix();

        if (tp.pause) {
            PVector mvec = new PVector(tp.mouseX, tp.mouseY);
            PVector distanceVect = PVector.sub(mvec, this.position);
            float distanceVectMag = distanceVect.mag();
            if (distanceVectMag < getRadius()) {
                displayInfo();
            }
        }

        if (tp.debugOn) {

            for (Sensor s : mySensors) {
                if (s.tank == this) {
                    //tp.pushMatrix();
                    // Rita ut vad sensorn ser (target och linje dit.)
                    tp.strokeWeight(1);
                    tp.stroke(0, 0, 255);
                    PVector sens = (s.readValue().obj().position);
                    /*
                    sens.sub(position);
                    sens.normalize();
                    sens.mult(200);
                    */

                    //println("============");
                    //println("("+sens.x + " , "+sens.y+")");
                    //ellipse(sens.x, sens.y, 10,10);
                    //tp.translate(position.x, position.y);
                    if ((sens != null && !this.isSpinning && !isImmobilized)) {
                        tp.line(position.x, position.y, sens.x, sens.y);
                        tp.ellipse(sens.x, sens.y, 10, 10);
                        //println("Tank" + this.team.getId() + ":"+this.id + " ( " + sens.x + ", "+ sens.y + " )");
                    }
                    //tp.popMatrix();
                }
            }

            // Rita ut en linje mot target, och tank-id och tank-hälsa.
            tp.strokeWeight(2);
            tp.fill(255, 0, 0);
            tp.stroke(255, 0, 0);
            tp.textSize(14);
            tp.text(this.id+":"+this.health, this.position.x + this.radius, this.position.y + this.radius);

            if (this.hasTarget) {
                tp.strokeWeight(1);
                tp.line(this.position.x, this.position.y, this.targetPosition.x, targetPosition.y);
            }
        }
    }

    public TankProg getTp(){
        return tp;
    }
    public Team getTeam(){
        return team;
    }

}
