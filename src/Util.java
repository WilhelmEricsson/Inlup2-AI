
import processing.core.*;

import java.util.Random;


public class Util {
    private static TankProg tp;
    private static Random rndGen;

    public Util(TankProg tp){
        this.tp = tp;
        rndGen = new Random();
    }

    // call to Team updateLogic()
    public static void updateTeamsLogic() {
        tp.teams[0].updateLogic();
        tp.teams[1].updateLogic();
    }

    // call to Tank updateLogic()
    public static void updateTanksLogic() {

        for (Tank tank : tp.allTanks) {
            if (tank.isReady) {
                tank.updateLogic();
            }
        }

        //for (int i = 0; i < tanks.length; i++) {
        //  this.tanks[i].updateLogic();
        //}
    }

    // call to Tank update()
    public static void updateTanks() {

        for (Tank tank : tp.allTanks) {
            //if (tank.health > 0)
            tank.update();
        }

        //for (int i = 0; i < tanks.length; i++) {
        //  this.tanks[i].updateLogic();
        //}
    }

    public static void updateShots() {
        for (int i = 0; i < tp.allShots.length; i++) {
            if ((tp.allShots[i].passedTime > tp.wait) && (!tp.allTanks[i].hasShot)) {
                tp.allShots[i].resetTimer();
                tp.allTanks[i].loadShot();
            }
            tp.allShots[i].update();
        }
    }

    public static void checkForCollisionsShots() {
        for (int i = 0; i < tp.allShots.length; i++) {
            if (tp.allShots[i].isInMotion) {
                for (int j = 0; j < tp.allTrees.length; j++) {
                    tp.allShots[i].checkCollision(tp.allTrees[j]);
                }

                for (int j = 0; j < tp.allTanks.length; j++) {
                    if (j != tp.allTanks[i].getId()) {
                        tp.allShots[i].checkCollision(tp.allTanks[j]);
                    }
                }
            }
        }
    }

    public static void checkForCollisionsTanks() {
        // Check for collisions with Canvas Boundaries
        for (int i = 0; i < tp.allTanks.length; i++) {
            tp.allTanks[i].checkEnvironment();

            // Check for collisions with "no Smart Objects", Obstacles (trees, etc.)
            for (int j = 0; j < tp.allTrees.length; j++) {
                tp.allTanks[i].checkCollision(tp.allTrees[j]);
            }

            // Check for collisions with "Smart Objects", other Tanks.
            for (int j = 0; j < tp.allTanks.length; j++) {
                //if ((allTanks[i].getId() != j) && (allTanks[i].health > 0)) {
                if (tp.allTanks[i].getId() != j) {
                    tp.allTanks[i].checkCollision(tp.allTanks[j]);
                }
            }
        }
    }

    public static void loadShots() {
        for (int i = 0; i < tp.allTanks.length; i++) {
            tp.allTanks[i].loadShot();
        }
    }

    //void shoot(Tank id, PVector pvec) {
    public static void shoot(int id) {
        System.out.println("App.shoot()");
        // println(id +": "+ pvec);
        //allShots.get(id).setStartPosition(pvec);

        //myAudio.blast();

        tp.allShots[id].isInMotion = true;
        tp.allShots[id].startTimer();
    }

    public static void setTargetPosition(PVector pvec) {
        PVector nodevec = tp.grid.getNearestNodePosition(pvec);
        //allTanks[tankInFocus].moveTo(pvec);
        tp.allTanks[tp.tankInFocus].moveTo(nodevec);
    }

//******************************************************

    /**
     * Find the point of intersection between two lines.
     * This method uses PVector objects to represent the line end points.
     *
     * @param v0 start of line 1
     * @param v1 end of line 1
     * @param v2 start of line 2
     * @param v3 end of line 2
     * @return a PVector object holding the intersection coordinates else null if no intersection
     */
    public static PVector line_line_p(PVector v0, PVector v1, PVector v2, PVector v3) {
        final double ACCY = 1E-30;
        PVector intercept = null;

        double f1 = (v1.x - v0.x);
        double g1 = (v1.y - v0.y);
        double f2 = (v3.x - v2.x);
        double g2 = (v3.y - v2.y);
        double f1g2 = f1 * g2;
        double f2g1 = f2 * g1;
        double det = f2g1 - f1g2;

        if (tp.abs((float) det) > (float) ACCY) {
            double s = (f2 * (v2.y - v0.y) - g2 * (v2.x - v0.x)) / det;
            double t = (f1 * (v2.y - v0.y) - g1 * (v2.x - v0.x)) / det;
            if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
                intercept = new PVector((float) (v0.x + f1 * s), (float) (v0.y + g1 * s));
        }
        return intercept;
    }

//******************************************************
//Används inte.

    /**
     * Put angle in degrees into [0, 360) range
     */
//  public static float fixAngle(float angle) {
    public static float fixAngle(float angle) {
        while (angle < 0f)
            angle += 360f;
        while (angle >= 360f)
            angle -= 360f;
        return angle;
    }

    //Används inte.
//public static float relativeAngle(float delta){
    public static float relativeAngle(float delta) {
        while (delta < 180f)
            delta += 360f;
        while (delta >= 180f)
            delta -= 360f;
        return delta;
    }


    // Mouse functions


    // Mousebuttons
    public static void mousePressed() {
        System.out.println("---------------------------------------------------------");
        System.out.println("*** mousePressed() - Musknappen har tryckts ned.");

        tp.mouse_pressed = true;

    }

    public static void checkForInput() {
        if (tp.userControl) {

            if (tp.alt_key) {
                //println("alt_key: " + alt_key);
                if (tp.left) {
                    tp.allTanks[tp.tankInFocus].turnTurretLeft_state();
                } else if (tp.right) {
                    tp.allTanks[tp.tankInFocus].turnTurretRight_state();
                }
            } else if (!tp.alt_key) {

                if (tp.left) {
                    tp.allTanks[tp.tankInFocus].turnLeft_state();
                } else if (tp.right) {
                    tp.allTanks[tp.tankInFocus].turnRight_state();
                }

                if (tp.up) {
                    tp.allTanks[tp.tankInFocus].moveForward_state();
                } else if (tp.down) {
                    tp.allTanks[tp.tankInFocus].moveBackward_state();
                }

                if (!(tp.up || tp.down)) {
                    //allTanks[tankInFocus].deaccelarate();
                    //allTanks[tankInFocus].stopMoving_state();
                }
                if (!(tp.right || tp.left)) {
                    //allTanks[tankInFocus].deaccelarate();
                    //allTanks[tankInFocus].stopTurning_state();
                }
            }

            if (!(tp.alt_key && (tp.left || tp.right))) {
                //allTanks[tankInFocus].stopTurretTurning_state();
            }

            if (tp.mouse_pressed) {
                System.out.println("if (mouse_pressed)");
                //allTanks[tankInFocus].spin(3);
                int mx = tp.mouseX;
                int my = tp.mouseY;

                setTargetPosition(new PVector(mx, my));

                tp.mouse_pressed = false;
            }
        }
    }




    // Initiera användargränssnittet.
// Används inte.
    public static void setGUI() {
        System.out.println("*** setGUI()- Användargränsnittet skapas.");

    }

    //**************************************************
// Gör så att allt i användargränssnittet (GUI) visas.
    public static void showGUI() {
        //println("*** showGUI()");

        tp.textSize(14);
        tp.fill(30);
        tp.text("Team1: " + tp.teams[0].numberOfHits, 10, 20);
        tp.text("Team2: " + tp.teams[1].numberOfHits, tp.width - 100, 20);
        tp.textSize(24);
        tp.text(tp.remainingTime, tp.width / 2, 25);
        tp.textSize(14);


        if (tp.userControl) {
            // Draw an ellipse at the mouse position
            PVector mouse = new PVector(tp.mouseX, tp.mouseY);
            tp.fill(200);
            tp.stroke(0);
            tp.strokeWeight(2);
            tp.ellipse(mouse.x, mouse.y, 48, 48);
            //grid.displayNearestNode(mouseX, mouseY);
        }


        if (tp.debugOn) {
            // Visa framerate.
            tp.fill(30);
            tp.text("FPS:" + tp.floor(tp.frameRate), 10, tp.height - 10);

            // Visa grid.
            tp.fill(205);
            gridDisplay();

            // Visa musposition och den närmaste noden.
            tp.fill(255, 92, 92);
            tp.ellipse(tp.mouseX, tp.mouseY, 5, 5);
            tp.grid.displayNearestNode(tp.mouseX, tp.mouseY);
        }

        if (tp.pause) {
            tp.textSize(36);
            tp.fill(30);
            tp.text("Paused!", tp.width / 2 - 100, tp.height / 2);
        }

        if (tp.gameOver) {
            tp.textSize(36);
            tp.fill(30);
            tp.text("Game Over!", tp.width / 2 - 100, tp.height / 2);
        }
        if(tp.gameWon){
            tp.textSize(36);
            tp.fill(30);
            tp.text("Team " + tp.winner.id +
                    " Won!", tp.width / 2 - 100, tp.height / 2);
        }
    }

    //**************************************************
// Gör så att textfälten visas och uppdateras.
// Används inte.
    public static void showOutput() {
    }

    public static void displayTrees() {
        for (int i = 0; i < tp.allTrees.length; i++) {
            tp.allTrees[i].display();
        }
    }


    public static void gridDisplay() {
        tp.strokeWeight(0.3f);

        tp.grid.display();
    }

    public static void updateTanksDisplay() {
        //for (int i = 0; i < allTanks.length; i++) {
        //  allTanks[i].display();
        //}
        for (Tank tank : tp.allTanks) {
            tank.display();
        }
    }

    public static void updateShotsDisplay() {
        for (int i = 0; i < tp.allShots.length; i++) {
            tp.allShots[i].display();
        }
    }

    public static int getRndDecision(){
        return rndGen.nextInt(8);
    }
    public static int getRndDecision(int upperBound){
        return rndGen.nextInt(upperBound);
    }


}
