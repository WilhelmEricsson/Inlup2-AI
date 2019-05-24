public class Util {

    // call to Team updateLogic()
    void updateTeamsLogic() {
        teams[0].updateLogic();
        teams[1].updateLogic();
    }

    // call to Tank updateLogic()
    void updateTanksLogic() {

        for (Tank tank : allTanks) {
            if (tank.isReady) {
                tank.updateLogic();
            }
        }

        //for (int i = 0; i < tanks.length; i++) {
        //  this.tanks[i].updateLogic();
        //}
    }

    // call to Tank update()
    void updateTanks() {

        for (Tank tank : allTanks) {
            //if (tank.health > 0)
            tank.update();
        }

        //for (int i = 0; i < tanks.length; i++) {
        //  this.tanks[i].updateLogic();
        //}
    }

    void updateShots() {
        for (int i = 0; i < allShots.length; i++) {
            if ((allShots[i].passedTime > wait) && (!allTanks[i].hasShot)) {
                allShots[i].resetTimer();
                allTanks[i].loadShot();
            }
            allShots[i].update();
        }
    }

    void checkForCollisionsShots() {
        for (int i = 0; i < allShots.length; i++) {
            if (allShots[i].isInMotion) {
                for (int j = 0; j<allTrees.length; j++) {
                    allShots[i].checkCollision(allTrees[j]);
                }

                for (int j = 0; j < allTanks.length; j++) {
                    if (j != allTanks[i].getId()) {
                        allShots[i].checkCollision(allTanks[j]);
                    }
                }
            }
        }
    }

    void checkForCollisionsTanks() {
        // Check for collisions with Canvas Boundaries
        for (int i = 0; i < allTanks.length; i++) {
            allTanks[i].checkEnvironment();

            // Check for collisions with "no Smart Objects", Obstacles (trees, etc.)
            for (int j = 0; j < allTrees.length; j++) {
                allTanks[i].checkCollision(allTrees[j]);
            }

            // Check for collisions with "Smart Objects", other Tanks.
            for (int j = 0; j < allTanks.length; j++) {
                //if ((allTanks[i].getId() != j) && (allTanks[i].health > 0)) {
                if (allTanks[i].getId() != j) {
                    allTanks[i].checkCollision(allTanks[j]);
                }
            }
        }
    }

    void loadShots() {
        for (int i = 0; i < allTanks.length; i++) {
            allTanks[i].loadShot();
        }
    }

    //void shoot(Tank id, PVector pvec) {
    void shoot(int id) {
        println("App.shoot()");
        // println(id +": "+ pvec);
        //allShots.get(id).setStartPosition(pvec);

        //myAudio.blast();

        allShots[id].isInMotion = true;
        allShots[id].startTimer();
    }

    void setTargetPosition(PVector pvec) {
        PVector nodevec = grid.getNearestNodePosition(pvec);
        //allTanks[tankInFocus].moveTo(pvec);
        allTanks[tankInFocus].moveTo(nodevec);
    }

//******************************************************

    /**
     * Find the point of intersection between two lines.
     * This method uses PVector objects to represent the line end points.
     * @param v0 start of line 1
     * @param v1 end of line 1
     * @param v2 start of line 2
     * @param v3 end of line 2
     * @return a PVector object holding the intersection coordinates else null if no intersection
     */
    public PVector line_line_p(PVector v0, PVector v1, PVector v2, PVector v3){
        final double ACCY   = 1E-30;
        PVector intercept = null;

        double f1 = (v1.x - v0.x);
        double g1 = (v1.y - v0.y);
        double f2 = (v3.x - v2.x);
        double g2 = (v3.y - v2.y);
        double f1g2 = f1 * g2;
        double f2g1 = f2 * g1;
        double det = f2g1 - f1g2;

        if(abs((float)det) > (float)ACCY){
            double s = (f2*(v2.y - v0.y) - g2*(v2.x - v0.x))/ det;
            double t = (f1*(v2.y - v0.y) - g1*(v2.x - v0.x))/ det;
            if(s >= 0 && s <= 1 && t >= 0 && t <= 1)
                intercept = new PVector((float)(v0.x + f1 * s), (float)(v0.y + g1 * s));
        }
        return intercept;
    }

//******************************************************
//Används inte.
    /**
     * Put angle in degrees into [0, 360) range
     */
//  public static float fixAngle(float angle) {
    float fixAngle(float angle) {
        while (angle < 0f)
            angle += 360f;
        while (angle >= 360f)
            angle -= 360f;
        return angle;
    }

    //Används inte.
//public static float relativeAngle(float delta){
    float relativeAngle(float delta){
        while (delta < 180f)
            delta += 360f;
        while (delta >= 180f)
            delta -= 360f;
        return delta;
    }



    // Mouse functions



    // Mousebuttons
    void mousePressed() {
        println("---------------------------------------------------------");
        println("*** mousePressed() - Musknappen har tryckts ned.");

        mouse_pressed = true;

    }

    void checkForInput() {
        if (userControl) {

            if (alt_key) {
                //println("alt_key: " + alt_key);
                if (left) {
                    allTanks[tankInFocus].turnTurretLeft_state();
                } else if (right) {
                    allTanks[tankInFocus].turnTurretRight_state();
                }
            } else
            if (!alt_key) {

                if (left) {
                    allTanks[tankInFocus].turnLeft_state();
                } else if (right) {
                    allTanks[tankInFocus].turnRight_state();
                }

                if (up) {
                    allTanks[tankInFocus].moveForward_state();
                } else if (down) {
                    allTanks[tankInFocus].moveBackward_state();
                }

                if (!(up || down)) {
                    //allTanks[tankInFocus].deaccelarate();
                    //allTanks[tankInFocus].stopMoving_state();
                }
                if (!(right || left)) {
                    //allTanks[tankInFocus].deaccelarate();
                    //allTanks[tankInFocus].stopTurning_state();
                }
            }

            if (!(alt_key && (left || right))) {
                //allTanks[tankInFocus].stopTurretTurning_state();
            }

            if (mouse_pressed) {
                println("if (mouse_pressed)");
                //allTanks[tankInFocus].spin(3);
                int mx = mouseX;
                int my = mouseY;

                setTargetPosition(new PVector(mx, my));

                mouse_pressed = false;
            }
        }
    }

    void keyPressed() {
        if (userControl) {

            if (key == CODED) {
                switch(keyCode) {
                    case LEFT:
                        //myTank1_snd.engineStart();
                        left = true;
                        break;
                    case RIGHT:
                        //myTank_snd.engineStart();
                        right = true;
                        break;
                    case UP:
                        //myTank_snd.engineStart();
                        up = true;
                        break;
                    case DOWN:
                        //myTank_snd.engineStart();
                        down = true;
                        break;
                    case ALT:
                        // turret.
                        alt_key = true;
                        break;
                }
            }
            if (key == ' ') {
                //myAudio.shot();
                //myAudio.blast();
                //myTank1.fire();
                println("keyPressed SPACE");
                allTanks[tankInFocus].fire();
            }
        }

        if (key == 'c') {
            userControl = !userControl;

//    allTanks[tankInFocus].stopMoving_state();
//    allTanks[tankInFocus].stopTurning_state();
//    allTanks[tankInFocus].stopTurretTurning_state();

            if (!userControl) {
                allTanks[tankInFocus].releaseControl();

            } else {
                allTanks[tankInFocus].takeControl();
            }
        }

        if (key == 'p') {
            pause = !pause;
            if (pause) {
                timer.pause();
            } else {
                timer.resume();
            }
        }

        if (key == 'd') {
            debugOn = !debugOn;
        }
    }

    void keyReleased() {
        if (userControl) {

            if (key == CODED) {
                switch(keyCode) {
                    case LEFT:
                        //myTank_snd.engineStop();
                        left = false;
                        allTanks[tankInFocus].stopTurning_state();
                        break;
                    case RIGHT:
                        //myTank_snd.engineStop();
                        right = false;
                        allTanks[tankInFocus].stopTurning_state();
                        break;
                    case UP:
                        //myTank_snd.engineStop();
                        up = false;
                        allTanks[tankInFocus].stopMoving_state();
                        break;
                    case DOWN:
                        //myTank_snd.engineStop();
                        down = false;
                        allTanks[tankInFocus].stopMoving_state();
                        break;
                    case ALT:
                        // turret.
                        alt_key = false;
                        allTanks[tankInFocus].stopTurretTurning_state();
                }
            }
        }
    }

    public void keyTyped() {

        if (userControl) {
            switch(key) {
                case '1':
                    allTanks[tankInFocus].releaseControl();
                    tankInFocus = 1;
                    allTanks[tankInFocus].takeControl();
                    break;
                case '2':
                    allTanks[tankInFocus].releaseControl();
                    tankInFocus = 2;
                    allTanks[tankInFocus].takeControl();
                    break;
                case '3':
                    allTanks[tankInFocus].releaseControl();
                    tankInFocus = 3;
                    allTanks[tankInFocus].takeControl();
                    break;
                case '4':
                    allTanks[tankInFocus].releaseControl();
                    tankInFocus = 4;
                    allTanks[tankInFocus].takeControl();
                    break;
                case '5':
                    allTanks[tankInFocus].releaseControl();
                    tankInFocus = 5;
                    allTanks[tankInFocus].takeControl();
                    break;
                case '0':
                    allTanks[tankInFocus].releaseControl();
                    tankInFocus = 0;
                    allTanks[tankInFocus].takeControl();
                    break;
            }
        }
    }





    // Initiera användargränssnittet.
// Används inte.
    void setGUI() {
        println("*** setGUI()- Användargränsnittet skapas.");

    }
    //**************************************************
// Gör så att allt i användargränssnittet (GUI) visas.
    void showGUI() {
        //println("*** showGUI()");

        textSize(14);
        fill(30);
        text("Team1: "+teams[0].numberOfHits, 10, 20);
        text("Team2: "+teams[1].numberOfHits, width-100, 20);
        textSize(24);
        text(remainingTime, width/2, 25);
        textSize(14);


        if (userControl) {
            // Draw an ellipse at the mouse position
            PVector mouse = new PVector(mouseX, mouseY);
            fill(200);
            stroke(0);
            strokeWeight(2);
            ellipse(mouse.x, mouse.y, 48, 48);
            //grid.displayNearestNode(mouseX, mouseY);
        }


        if (debugOn) {
            // Visa framerate.
            fill(30);
            text("FPS:"+ floor(frameRate), 10, height-10);

            // Visa grid.
            fill(205);
            gridDisplay();

            // Visa musposition och den närmaste noden.
            fill(255, 92, 92);
            ellipse(mouseX, mouseY, 5, 5);
            grid.displayNearestNode(mouseX, mouseY);
        }

        if (pause) {
            textSize(36);
            fill(30);
            text("Paused!", width/2-100, height/2);
        }

        if (gameOver) {
            textSize(36);
            fill(30);
            text("Game Over!", width/2-100, height/2);
        }
    }
    //**************************************************
// Gör så att textfälten visas och uppdateras.
// Används inte.
    void showOutput() {
    }

    void displayTrees() {
        for (int i = 0; i<allTrees.length; i++) {
            allTrees[i].display();
        }
    }


    void gridDisplay() {
        strokeWeight(0.3);

        grid.display();
    }

    void updateTanksDisplay() {
        //for (int i = 0; i < allTanks.length; i++) {
        //  allTanks[i].display();
        //}
        for (Tank tank : allTanks) {
            tank.display();
        }
    }

    void updateShotsDisplay() {
        for (int i = 0; i < allShots.length; i++) {
            allShots[i].display();
        }
    }

}
