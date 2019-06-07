    /*
     * JUST NU:

     */

// för ljud
//import ddf.minim.*;

    import processing.core.*;

    public class TankProg extends PApplet {

        public static void main(String[] args) {
            PApplet.main("TankProg");
        }

    /* Ljud
    Minim minim;
    SoundManager soundManager;*/
        //DETTA GÖRAS SNYGGARE
        Util util = new Util(this);

        boolean debugOn = false;
        boolean pause = false;
        boolean gameOver = false;
        boolean gameWon = false;
       Team winner;
        Grid grid;
        int cols = 15;
        int rows = 15;
        int grid_size = 50;

        // Boolean variables connected to keys
        boolean left, right, up, down;
        boolean fire;
        boolean alt_key; // Turning turret, alt+LEFT/RIGHT

        boolean mouse_pressed;
        boolean userControl;
        int tankInFocus;

        //Setting team colors
        int team0Color = color(204, 50, 50);
        int team1Color = color(0, 150, 200);

        CannonBall[] allShots = new CannonBall[6];

        Tree[] allTrees = new Tree[3];

        Tank[] allTanks = new Tank[6];

        Team[] teams = new Team[2];

        int tank_size = 50;
        // Team0
        PVector team0_tank0_startpos;
        PVector team0_tank1_startpos;
        PVector team0_tank2_startpos;

        // Team1
        PVector team1_tank0_startpos;
        PVector team1_tank1_startpos;
        PVector team1_tank2_startpos;

        // timer
        int savedTime;
        int wait = 3000; //wait 3 sec (reload)
        boolean tick;

        Timer timer;
        int startTime = 3; //minutes
        int remainingTime;

        public Grid getGrid() {
            return grid;
        }

        Util u = new Util(this);

        @Override
        public void settings() {
            size(800, 800);
        }

        @Override
        public void setup() {
        /* LJUD!
        soundManager = new SoundManager(this);
        soundManager.addSound("tank_firing");
        soundManager.addSound("tank_idle");
        soundManager.addSound("blast");*/

            grid = new Grid(cols, rows, grid_size, this);

            //grid = new Node[cols][rows];
            //for (int i = 0; i < cols; i++) {
            //  for (int j = 0; j < rows; j++) {
            //    // Initialize each object
            //    grid[i][j] = new Node(i,j,i*grid_size, j*grid_size);
            //  }
            //}


            // Skapa alla träd
            allTrees[0] = new Tree(230, 600, this);
            allTrees[1] = new Tree(280, 230, this);//280,230(300,300)
            allTrees[2] = new Tree(530, 520, this);//530, 520(500,500);

            // Skapa alla skott
            for (int i = 0; i < allShots.length; i++) {
                allShots[i] = new CannonBall(this);
            }

            // Team0
            team0_tank0_startpos = new PVector(50, 50);
            team0_tank1_startpos = new PVector(50, 150);
            team0_tank2_startpos = new PVector(50, 250);

            // Team1
            team1_tank0_startpos = new PVector(width - 50, height - 250);
            team1_tank1_startpos = new PVector(width - 50, height - 150);
            team1_tank2_startpos = new PVector(width - 50, height - 50);


            // nytt Team: id, color, tank0pos, id, shot
            teams[0] = new Team1(this, 0, tank_size, team0Color,
                    team0_tank0_startpos, 0, allShots[0],
                    team0_tank1_startpos, 1, allShots[1],
                    team0_tank2_startpos, 2, allShots[2]);

            allTanks[0] = teams[0].tanks[0];
            allTanks[1] = teams[0].tanks[1];
            allTanks[2] = teams[0].tanks[2];

            teams[1] = new Team2(this, 1, tank_size, team1Color,
                    team1_tank0_startpos, 3, allShots[3],
                    team1_tank1_startpos, 4, allShots[4],
                    team1_tank2_startpos, 5, allShots[5]);

            allTanks[3] = teams[1].tanks[0];
            allTanks[4] = teams[1].tanks[1];
            allTanks[5] = teams[1].tanks[2];

            Util.loadShots();
            userControl = true;
            tankInFocus = 0;

            savedTime = millis(); //store the current time.

            remainingTime = startTime;
            timer = new Timer(this);
            timer.setDirection("down");
            timer.setTime(startTime);
        }

        @Override
        public void draw() {

            background(200);
            Util.checkForInput(); // Kontrollera inmatning.

            if (!gameOver && !pause && !gameWon) {
                // timer används inte i dagsläget.
                timer.tick(); // Alt.1
                float deltaTime = timer.getDeltaSec();
                remainingTime = (int) timer.getTotalTime();
                if (remainingTime <= 0) {
                    remainingTime = 0;
                    timer.pause();
                    gameOver = true;
                }

                int passedTime = millis() - savedTime; // Alt.2

                //check the difference between now and the previously stored time is greater than the wait interval
                if (passedTime > wait) {
                    //savedTime = millis();//also update the stored time
                }

                // UPDATE LOGIC
                Util.updateTanksLogic();
                Util.updateTeamsLogic();

                // UPDATE TANKS
                Util.updateTanks();
                Util.updateShots();

                // CHECK FOR COLLISIONS
                Util.checkForCollisionsShots();
                Util.checkForCollisionsTanks();
                checkIfThereIsAWinner();
            }

            // UPDATE DISPLAY
            teams[0].displayHomeBase();
            teams[1].displayHomeBase();
            Util.displayTrees();
            Util.updateTanksDisplay();
            Util.updateShotsDisplay();

            if (debugOn) {
                for(Tank t: allTanks) {
                    if (t instanceof Tank4) {
                        ((Tank4) t).drawSensor();
                    }
                }
            }

            Util.showGUI();

        }

        public void mousePressed() {
            System.out.println("---------------------------------------------------------");
            System.out.println("*** mousePressed() - Musknappen har tryckts ned.");

            mouse_pressed = true;

        }

        private void checkIfThereIsAWinner(){
           if(teams[0].isTeamWipedOut()){
               winner = teams[1];
               gameWon = true;
           }else if(teams[1].isTeamWipedOut()){
               winner = teams[0];
               gameWon = true;
           }
        }

        // Används inte
        float getTime() {
            return 0; //Dummy temp
        }
        @Override
        public void keyPressed() {
            if (userControl) {

                if (key == CODED) {
                    switch (keyCode) {
                        case PApplet.LEFT:
                            //myTank1_snd.engineStart();
                            left = true;
                            break;
                        case PApplet.RIGHT:
                            //myTank_snd.engineStart();
                            right = true;
                            break;
                        case PApplet.UP:
                            //myTank_snd.engineStart();
                            up = true;
                            break;
                        case PApplet.DOWN:
                            //myTank_snd.engineStart();
                            down = true;
                            break;
                        case PApplet.ALT:
                            // turret.
                            alt_key = true;
                            break;
                    }
                }
                if (key == ' ') {
                    //myAudio.shot();
                    //myAudio.blast();
                    //myTank1.fire();
                    System.out.println("keyPressed SPACE");
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
        @Override
        public void keyReleased() {
            if (userControl) {

                if (key == CODED) {
                    switch (keyCode) {
                        case PApplet.LEFT:
                            //myTank_snd.engineStop();
                            left = false;
                            allTanks[tankInFocus].stopTurning_state();
                            break;
                        case PApplet.RIGHT:
                            //myTank_snd.engineStop();
                            right = false;
                            allTanks[tankInFocus].stopTurning_state();
                            break;
                        case PApplet.UP:
                            //myTank_snd.engineStop();
                            up = false;
                            allTanks[tankInFocus].stopMoving_state();
                            break;
                        case PApplet.DOWN:
                            //myTank_snd.engineStop();
                            down = false;
                            allTanks[tankInFocus].stopMoving_state();
                            break;
                        case PApplet.ALT:
                            // turret.
                            alt_key = false;
                            allTanks[tankInFocus].stopTurretTurning_state();
                    }
                }
            }
        }
        @Override
        public void keyTyped() {

            if (userControl) {
                switch (key) {
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


    }
