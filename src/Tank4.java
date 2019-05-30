import processing.core.PVector;

import java.util.*;

public class Tank4 extends Tank {
    private static final String[] MESSAGE = new String[]{"Enemy Located!"};
    private boolean enemyLocated;
    private boolean started;
    private ArrayList<Tree> obstacles;
    private Tank enemyInfocus; // den tanken som ämnas bekämpas
    private HashMap<Tank, PVector> enemyLocatedAt;


    private PVector nextPosition = new PVector(0,0);

    Tank4(int id, Team team, PVector startpos, float diameter, CannonBall ball, TankProg tp) {
        super(id, team, startpos, diameter, ball, tp);
        obstacles = new ArrayList<>();
        enemyLocatedAt = new HashMap<>();
        enemyLocated = false;
        this.started = false;
    }

    @Override
    public void checkCollision(Tree other){
        super.checkCollision(other);
        readSightSensor(other);
        /*
        if(readSightSensor(other)){
            if(!obstacles.contains(other)){
                obstacles.add(other);
            }
        }*/

    }
    @Override
    public void checkCollision(Tank other){
        super.checkCollision(other);
        if (this.id != other.id) {
            readSightSensor(other);
        }
        /*
        if(readSightSensor(other)){
            if(other.getTeam().id != this.getTeam().id){
                enemyLocated = true;
                enemyInfocus = other;
                enemyLocatedAt.put(other,other.position);
                sendMessageToTeam("Enemy Located!", other.position);
            }
        }*/

    }





    // Tanken meddelas om kollision med trädet.
    public void message_collision(Tree other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tree)");

        //rotateTo(grid.getRandomNodePosition());
    }

    public void arrived() {
        super.arrived();
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].arrived()");

        if (this.getTeam().getId() == 0) {
            if(((Team1)team).isPosistionSearched(position)){
                ((Team1)getTeam()).addSearchedArea(this.position);
            }
        } else if (this.getTeam().getId() == 1) {
            if(((Team2)team).isPosistionSearched(position)){
                ((Team2)getTeam()).addSearchedArea(this.position);
            }
        }
        
        //isMoving = false;
        //moveTo(new PVector(int(random(width)),int(random(height))));
        //moveTo(grid.getRandomNodePosition());
        //this.isMoving = false;
    }

    @Override
    public void arrivedRotation() {
        super.arrivedRotation();
        moveTo(nextPosition);
        //isMoving = true;
    }

    @Override
    public void updateLogic() {
        SensorReading reading = getLatestSightSensorReading();
        Sprite obj = reading.obj();
        if (obj != null) {
            // Rita ut
            SightSensor sens = (SightSensor)getSensor("SIGHT_SENSOR");
            sens.drawSensor(reading.obj().position);

            if (obj instanceof Tank) {
                Tank tank = (Tank) obj;
                if(tank.getTeam().id != this.getTeam().id){
                    enemyLocated = true;
                    enemyInfocus = tank;
                    if(!enemyLocatedAt.containsKey(tank)|| enemyLocatedAt.get(tank) != tank.position){
                        enemyLocatedAt.put(tank,tank.position);
                        sendMessageToTeam("Enemy Located!", tank.position);
                    }
                }
            } else {
                Tree tree = (Tree) obj;
                if(!obstacles.contains(obj)){
                    obstacles.add(tree);
                }
            }
        }

        if(messageReceived){
            receiveMessage();
        }

        if(enemyLocated){
            battleState();
        }else{
            if(idle_state ) {
                searchState();
            }
        }
        resetSightSensorReading();
    }

    //SEARCH STATE -- Random search, with preference to position located further away from home base/starting position
    public void searchState(){
        nextPosition = getNextPosition();
        rotateTo(nextPosition);
    }

    private void receiveMessage(){
        TankMessage tankMessage = getMessageReceived();

        if(tankMessage.getMessage() == MESSAGE[0]){
            System.out.println("Message from: " + tankMessage.getSender() + " - "  + tankMessage.getMessage()+  " " + tankMessage.getPosition().toString());
            enemyLocated = true;
            enemyLocatedAt.put(null, tankMessage.getPosition());
        }
    }


    private PVector getNextPosition(){
        boolean validPosition = false;
        PVector nextPosition = null;
        ArrayList<PVector> potentialPosition = new ArrayList<>();
        //N
        potentialPosition.add(new PVector(this.position.x, this.position.y-stepDist));
        //NE
        potentialPosition.add(new PVector(this.position.x+stepDist, this.position.y-stepDist));
        //E
        potentialPosition.add(new PVector(this.position.x+stepDist, this.position.y));
        //SE
        potentialPosition.add(new PVector(this.position.x+stepDist, this.position.y+stepDist));
        //S
        potentialPosition.add(new PVector(this.position.x, this.position.y+stepDist));
        //SW
        potentialPosition.add(new PVector(this.position.x-stepDist, this.position.y+stepDist));
        //W
        potentialPosition.add(new PVector(this.position.x-stepDist, this.position.y));
        //NW
        potentialPosition.add(new PVector(this.position.x-stepDist, this.position.y-stepDist));
        potentialPosition = calcPotentialMoveActionsUtil(potentialPosition);

        nextPosition = potentialPosition.get(Util.getRndDecision(potentialPosition.size()));
        System.out.println("Next: " + nextPosition + " enemy found: " + enemyLocated + " Actions: " + potentialPosition.size());

        return nextPosition;
    }
    private ArrayList<PVector> calcPotentialMoveActionsUtil(ArrayList<PVector> potentialPositions){
        int highestUtil = Integer.MIN_VALUE;
        ArrayList<PVector> tempPositions = new ArrayList<>();
        for(PVector position : potentialPositions){
            int positionUtil = calcMoveActionUtil(position);
            System.out.println("Position: " + position.toString()+ " Util: "  + positionUtil);
            if(highestUtil < positionUtil){
                highestUtil = positionUtil;
                tempPositions = new ArrayList<>();
                tempPositions.add(position);
            }else if(highestUtil == positionUtil){
                tempPositions.add(position);
            }
        }
        System.out.println("TEMP POSITIONS: " + tempPositions.size());
        return tempPositions;
    }

    private boolean isObstacle(PVector pos){
        Iterator<Tree> obst = obstacles.iterator();
        boolean isObst = false;
        while(obst.hasNext()){
            Tree temp = obst.next();
            if((temp.position.x-temp.radius-this.radius) <= pos.x && (temp.position.x+temp.radius+this.radius)  >= pos.x){
                if((temp.position.y-temp.radius-this.radius) <= pos.y && (temp.position.y+temp.radius+this.radius)  >= pos.y){
                    isObst = true;
                    break;
                }
            }
        }
        return isObst;
    }





   //BATTLE STATE
    public void battleState(){
        getBestTarget();
        int decision = getMostRewardingAction();

        switch (decision){
            case 0:
                if(idle_state){
                    searchState();
                }
                break;
            case 1:
                if(idle_state && hasClearShot()){
                    fire();
                }

                break;

            default:


        }

    }
    private void getBestTarget(){
        if(enemyInfocus != null) {
            if (enemyInfocus.health <= 0) {
                enemyLocatedAt.remove(enemyInfocus);
                enemyInfocus = null;
            }
            for (Tank enemy : enemyLocatedAt.keySet()) {
                if (enemy != null && (enemyInfocus == null || enemyInfocus.health > enemy.health)) {
                    enemyInfocus = enemy;
                }

            }
        }
    }

    private int getMostRewardingAction(){
        if(hasClearShot() && hasShot){
            System.out.println("TEST 1");
            stopMoving_state();
            return 1;
        }else if (idle_state && !hasClearShot() && enemyInfocus != null){
            System.out.println("TEST 2");
            rotateTo(enemyInfocus.position);
        }

        return 0;
    }
    public boolean hasClearShot(){
        Sprite obj = getLatestSightSensorReading().obj;
         if(enemyInfocus != null){
             if(obj instanceof Tank){
                 return ((Tank) obj).id == enemyInfocus.id;
             }else if(obj == null && aimingInRightDirection()){
                 System.out.println("hasClearShot - aim check");
                 return true; // inget objekt inom synfältet
             }
         }
        return  false;
    }
    private boolean aimingInRightDirection(){
        PVector tempTarget = enemyLocatedAt.get(enemyInfocus);
        PVector target = new PVector(tempTarget.x,tempTarget.y);
        PVector me = new PVector(this.position.x, this.position.y);
        PVector positionToAimAt = PVector.sub(target, me);

        return heading == positionToAimAt.heading();
    }

    public int calcMoveActionUtil(PVector position){
        int util = -1;
        if(isObstacle(position)){
            util = Integer.MIN_VALUE;
            return util;
        }
        if(enemyLocated && hasShot){

            Iterator<PVector> positions = enemyLocatedAt.values().iterator();
            PVector closest = null;
            while (positions.hasNext()) {
                PVector temp = positions.next();
                if (closest == null || position.dist(temp) < position.dist(closest)) {
                    util = -(int) position.dist(temp);
                    closest = temp;
                }

            }

        }else{
            if (this.getTeam().getId() == 0) {
                if(((Team1)team).isPosistionSearched(position)){
                    util -= 1;
                }
            } else if (this.getTeam().getId() == 1) {
                if(((Team2)team).isPosistionSearched(position)){
                    util -= 1;
                }
            }

        }
        return util;
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