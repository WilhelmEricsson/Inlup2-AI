/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */

import processing.core.PVector;

import java.util.*;

public class Tank4 extends Tank {
    private static final String[] MESSAGE = new String[]{"Enemy Located!"};
    private boolean enemyLocated;
    private boolean started;
    private ArrayList<Sprite> obstacles;
    private Tank enemyInfocus; // den tanken som ämnas bekämpas
    private HashMap<Tank, PVector> enemyLocatedAt;
    protected HashSet<String> blockedPaths;
    private SensorReading latestSightSensorReadning;
    private PVector nextPosition = new PVector(0,0);

    private PVector lastPosition = new PVector(0,0);
    private String lastDirection = "";

    Tank4(int id, Team team, PVector startpos, float diameter, CannonBall ball, TankProg tp) {
        super(id, team, startpos, diameter, ball, tp);
        obstacles = new ArrayList<>();
        enemyLocatedAt = new HashMap<>();
        enemyLocated = false;
        this.started = false;
        blockedPaths = new HashSet<>();
        latestSightSensorReadning = new SensorReading();
    }


    // Skickar ett meddelande till alla andra tanks i laget
    private void sendMessageToTeam(String message, PVector position) {
        team.addMessage(new TankMessage(id, message, position));
    }

    // Kallad av team när någon skickar ett meddelande
    // Tanks kollar varje varv ifall message recieved är true
    // när den är detta hanterar de meddelandet och sätter den till false
    public void receiveMessageFromTeam(TankMessage message) {
        messageReceived = true;
    }

    // Returnerar det senaste meddelandet från team:ets lista messages
    private TankMessage getMessageReceived() {
        messageReceived = false;
        return team.getLatestMessage();
    }

    // Använder SightSensor för att kolla ifall sprite är inom tankens synfält
    // Ifall inte samt att inget annan objekt är närmre så läggs den till i SightSensor objektet latestReading
    // Denna metod anropas i checkCollision() (båda) och efter alla träd och tankar har gåtts igenom
    // kommer latestReading vara det närmaste objektet i synfältet ifall det finns något
    private void readSightSensor(Sprite sprite) {
        SightSensor sens = (SightSensor) getSensor("SIGHT_SENSOR");

        if (sprite instanceof Tree) {
            sens.readValue(sprite, 50);
        } else {
            sens.readValue(sprite, 25);
        }
    }

    // Returnerar det närmaste objektet inom 200 (Pvector.dist)
    private SensorReading getLatestSightSensorReading() {
        return ((SightSensor) getSensor("SIGHT_SENSOR")).getLatestReading();
    }

    @Override
    public void checkCollision(Tree other){
        super.checkCollision(other);
        readSightSensor(other);
    }
    @Override
    public void checkCollision(Tank other){
        super.checkCollision(other);
        if (this.id != other.id) {
            readSightSensor(other);
        }
    }

    private boolean isFrontTowardsNextPosition(){
        PVector tempTargetPosition = nextPosition;
        PVector targetPosition = new PVector(tempTargetPosition.x,tempTargetPosition.y);
        PVector me = new PVector(this.position.x, this.position.y);
        PVector nextPositionDirection= PVector.sub(targetPosition, me);

        return heading == nextPositionDirection.heading();
    }



    // Tanken meddelas om kollision med trädet.
    public void message_collision(Tree other) {
        System.out.println("*** Team"+this.team_id+".Tank["+ this.getId() + "].collision(Tree)");
    }

    // Lägger till positionen i team:ets lista för redan sökta positioner
    @Override
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
    }


    @Override
    public void arrivedRotation() {
        super.arrivedRotation();
        lastPosition = new PVector(position.x, position.y);
        moveTo(nextPosition);
    }

    // Hanterar tankens logik, bestämmer ifall den ska söka eller försöka skjuta en fiende
    // Tar också hand om kollisioner för att se till att tanken inte fastnar
    @Override
    public void updateLogic() {
        // Ifall tanken krockade kolla ifall den kom längre än 25 dist från förra positionen, ifall inte så blockera den riktningen
        if (collided) {
            if (lastPosition.dist(position) < 25) {
                blockedPaths.add(lastDirection);
                System.out.println(id + " added " + lastDirection + " to blocked paths.");
            }
            collided = false;
        }

        // Läser av SightSensor
        readSensor();

        if(messageReceived){
            receiveMessage();
        }

        if(enemyLocated){
            battleState();
        }else{
            if(idle_state) {
                searchState();
            }
        }
        resetSightSensorReading();
    }

    // Skickar ett meddelande till laget ifall SightSensor upptäcker en fiende
    // lägger också till Träd i obstacles ifall ett sådant upptäcks
    private void readSensor(){
        latestSightSensorReadning = getLatestSightSensorReading();
        Sprite obj = latestSightSensorReadning.obj();
        if (obj != null) {
            if (getTp().debugOn) {
                SightSensor sens = (SightSensor)getSensor("SIGHT_SENSOR");
                sens.drawSensor(latestSightSensorReadning.obj().position);
            }


            if (obj instanceof Tank) {
                Tank tank = (Tank) obj;
                if(tank.getTeam().id != this.getTeam().id){
                    if(tank.health <= 0 && enemyLocatedAt.containsKey(tank)){
                        enemyLocatedAt.remove(tank);
                        if(enemyLocatedAt.isEmpty()){
                            enemyLocated = false;
                        }
                    }else {
                        enemyLocated = true;
                        enemyInfocus = tank;
                        if ((!enemyLocatedAt.containsKey(tank) || enemyLocatedAt.get(tank) != tank.position) && tank.health > 0) {
                            enemyLocatedAt.put(tank, tank.position);
                            sendMessageToTeam("Enemy Located!", tank.position);
                        }
                    }
                }
            } else {
                Tree tree = (Tree) obj;
                if(!obstacles.contains(obj)){
                    obstacles.add(tree);
                }
            }
        }
    }

    // Tanken gör en sökning för att hitta fienden
    // I början är fiendens position okänd och det är en slumpmässig sökning
    // sökningen försöker dock att undvika platser den redan varit på
    public void searchState(){
        if(!isDestroyed && !isImmobilized){
            nextPosition = getNextPosition();
            rotateTo(nextPosition);
        }else{
            turnTurretTowardsEnemy();
        }
    }

    private void turnTurretTowardsEnemy(){
        if(enemyInfocus != null && !aimingInRightDirection()){
            turnTurretLeft_state();
        }else{
            stopTurretTurning_state();
        }
    }

    // Lägger fiendens position från ett meddelande i en HashMap
    // Detta används senare i Utility Function:en för att välja riktning,
    // riktningar vilket är närmre fienden kommer föredras
    private void receiveMessage(){
        TankMessage tankMessage = getMessageReceived();
        if(tankMessage.getMessage() == MESSAGE[0]){
            System.out.println("Message from: " + tankMessage.getSender() + " - "  + tankMessage.getMessage()+  " " + tankMessage.getPosition().toString());
            enemyLocated = true;
            enemyLocatedAt.put(null, tankMessage.getPosition());
        }
    }

    // Returnerar tankens nästa position
    // Olika beronde på vilket state och input från sensorer
    // I början blir positionen slumpmässig
    // Ifall en fiende har lokaliserats föredras positioner nära fienden
    // Hanterar också blockerade vägar för att se till att tanken inte blir fast
    // Ifall t.ex. positionen norr är närmast fienden men denna väg är även blockerad måste tanken tvingas ta en annan
    private PVector getNextPosition(){
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

        PVector nextPosition = potentialPosition.get(Util.getRndDecision(potentialPosition.size()));

        // Ifall tanken kom mer än 100 dist ifrån förra positionen så är det lugnt och blockePaths clear:as
        if (lastPosition.dist(position) > 100) {
            if (!blockedPaths.isEmpty()) {
                blockedPaths.clear();
                System.out.println(id + " cleared blocked paths");
            }
        }

        // blockedPaths clear:as också ifall alla har testats för att försöka alla på nytt
        if (blockedPaths.size() == 8) {
            blockedPaths.clear();
        }

        //Sparar senaste riktningen för att seedan lägga till den om tanken krockade och inte kom längre än 25 dist (updateLogic)
        lastDirection = calculateDirection(nextPosition);

        System.out.println("Next: " + nextPosition + " enemy found: " + enemyLocated + " Actions: " + potentialPosition.size());

        return nextPosition;
    }

    // Returnerar riktningen i väderstreck
    private String calculateDirection(PVector nextPosition) {
        String direction = "";
        if (nextPosition.equals(new PVector(position.x, position.y-stepDist))) {
            direction = "N";
        } else if (nextPosition.equals(new PVector(position.x+stepDist, position.y-stepDist))) {
            direction = "NE";
        } else if (nextPosition.equals(new PVector(position.x+stepDist, position.y))) {
            direction = "E";
        } else if (nextPosition.equals(new PVector(position.x+stepDist, position.y+stepDist))) {
            direction = "SE";
        } else if (nextPosition.equals(new PVector(position.x, position.y+stepDist))) {
            direction = "S";
        } else if (nextPosition.equals(new PVector(position.x-stepDist, position.y+stepDist))) {
            direction = "SW";
        } else if (nextPosition.equals(new PVector(position.x-stepDist, position.y))) {
            direction = "W";
        } else if (nextPosition.equals(new PVector(position.x-stepDist, position.y-stepDist))) {
            direction = "NW";
        }
        return direction;
    }
    // Tar in en array med positioner och beräknar dessas utility. Det som returneras är en lista med den eller de positioner med högst utility.

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
        System.out.println("TEMP POSITIONS: " + tempPositions.size() + " TANK ID: " + id + " blocked paths: " + blockedPaths.size());
        return tempPositions;
    }

    //Avgör om en position är ett hinder eller ej.
    private boolean isObstacle(PVector pos){
        Iterator<Sprite> obst = obstacles.iterator();
        boolean isObst = false;
        while(obst.hasNext()){
            Sprite temp = obst.next();
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
    //Detta är det tillstånd som tanken kommer in i när den lokaliserat fienden, här finns 3 actions: searchState som används när tanken ska
   // förflytta sig, fire som säger sig själv och en sista action som är att tanken roterar för att sikta på fienden. Från getMostRewardingAction får tanken sitt beslut
    public void battleState(){
        getBestTarget();
        int decision = getMostRewardingAction();

        switch (decision){
            case 0:
                if(idle_state && !isMoving){
                    searchState();
                }
                break;
            case 1:
                stopMoving_state();
                fire();
                break;
            case 2:
                if(idle_state && !isMoving) {
                    rotateTo(enemyInfocus.position);
                }
                break;

            default:


        }

    }
    // Avgör vilken fiende tank som bör bekämpas först, detta baserat fiende tanksens hälsa
    private void getBestTarget(){
        if(enemyInfocus != null) {
            if (enemyInfocus.health <= 0) {
                enemyLocatedAt.remove(enemyInfocus);
                obstacles.add(enemyInfocus);
                enemyInfocus = null;
            }
            for (Tank enemy : enemyLocatedAt.keySet()) {
                if (enemy != null && (enemyInfocus == null || enemyInfocus.health > enemy.health)) {
                    enemyInfocus = enemy;
                }

            }
        }
    }

    //returnerar em int som representerar det val som är bäst för stunden
    private int getMostRewardingAction(){
        int[] actionUtils = new int[3];
        boolean hasClearShot = hasClearShot();
        actionUtils[0] = calcBattleStateMoveActionUtil(hasClearShot);
        actionUtils[1] = calculateFireActionUtil(hasClearShot);
        actionUtils[2] = calcRotationUtil();

        return argMax(actionUtils);
    }
    private int calculateFireActionUtil(boolean hasClearShot){
        int util = 0;

        if(hasShot){
            if(aimingInRightDirection()){
                if(hasClearShot){
                    util += 1000;
                }else {
                    util = Integer.MIN_VALUE;
                }
            }else{
                util = Integer.MIN_VALUE;
            }
        }else{
            util = Integer.MIN_VALUE;
        }
        return util;
    }
    private int calcBattleStateMoveActionUtil(boolean hasClearShot){
        int util = 0;
        if(hasShot){
            if(aimingInRightDirection()){
               if(hasClearShot){
                   util = Integer.MIN_VALUE;
               }else {
                   util = 1000;
               }
            }
        }else{
            util = 1000;
        }
        return util;
    }
    private int calcRotationUtil(){
        int util;
        if(hasShot && enemyInfocus != null){
             if(!aimingInRightDirection()){
                 util = 1000;
             }else{
                 util = Integer.MIN_VALUE;
             }
        }else{
            util = Integer.MIN_VALUE;
        }
        return util;
    }
    /**
     * @return true if the object returned by the latest sensor reading is the enemy in focus or null and the tank is aiming in the right direction.
     */

    public boolean hasClearShot(){
        Sprite obj = latestSightSensorReadning.obj;
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

    /**
     *
     * @return true if heading is within +-3 from the desired aim, else false
     */
    private boolean aimingInRightDirection(){
        PVector tempTarget = enemyLocatedAt.get(enemyInfocus);
        if (tempTarget != null) {
            PVector target = new PVector(tempTarget.x,tempTarget.y);
            PVector me = new PVector(this.position.x, this.position.y);
            PVector positionToAimAt = PVector.sub(target, me);

            if (getTp().degrees(heading) < getTp().degrees(positionToAimAt.heading())+3 && getTp().degrees(heading) > getTp().degrees(positionToAimAt.heading())-3) {
                return true;
            } else {
                return false;
            }
        }
        return false;
        //return heading == positionToAimAt.heading();
    }

    /**
     *
     * @param position
     * @return the utility of @param position
     */
    public int calcMoveActionUtil(PVector position){
        String direction = calculateDirection(position);

        int util = -1;
        if(isObstacle(position) || blockedPaths.contains(direction) || isOutOfBounds(position)){
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

    /**
     *
     * @param position
     * @return true if x and y values are less than 0, if x is larger than the PApplet's width or if y is larger
     * than the PApplet's height. True when @param position is null and false otherwise.
     */
    private boolean isOutOfBounds(PVector position){
        try{
            if(position.x < 0 || position.y < 0){
                return true;
            }else if(position.x > getTp().width || position.y > getTp().height){
                return true;
            }
        }catch(NullPointerException npe){
            return true;
        }
        return false;
    }


    public void drawSensor() {
        getTp().pushMatrix();
        getTp().fill(0);
        getTp().translate(position.x, position.y);
        getTp().rotate(heading);
        getTp().strokeWeight(2);
        getTp().line(0, 0, 200,0 );
        getTp().fill(255, 255, 0);
        getTp().ellipse(200, 0, 10, 10);
        getTp().popMatrix();
    }

    /**
     *
     * @param utility
     * @return max value of the array or 0 if length of the param array is 0
     */
    private int argMax(int[] utility){
        int max = 0;
        for(int i = 1; i < utility.length; i++ ){
            if(utility[max] < utility[i]){
                max = i;
            }
        }
        return max;
    }
}