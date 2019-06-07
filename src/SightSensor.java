/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import processing.core.PVector;

import static processing.core.PApplet.*;

// Används som en syn-sensor
// Sensorn kan läsa av 200 Pvector.dist framför tanken i dess riktning
// Ifall flera objekt befinner sig i den riktningen så syns bara det närmsta
public class SightSensor extends Sensor {

    Sprite closestObject = null;
    SensorReading latestReading;

    SightSensor(Tank t) {
        super(t);
        latestReading = new SensorReading();
    }

    // Returnerar det närmasta objektet i tankens färdriktning inom 200, ifall det finns
    // Denna ska bara anropas efter varje objekt på speplanen har gåtts igenom
    public SensorReading getLatestReading() {
        SensorReading temp = latestReading;
        return temp;
    }

    // Efter att alla Sprites har gåtts igenom måste sensorn återställas så att inte
    // det senaste hittade objektet blir kvar trots att den försvunnit ut synfältet
    public void reset() {
        closestObject = null;
        latestReading = new SensorReading();
    }

    public void drawSensor(PVector draw) {
        getTank().getTp().pushMatrix();
        getTank().getTp().fill(255, 255, 0);
        getTank().getTp().ellipse(draw.x, draw.y, 100, 100);
        getTank().getTp().popMatrix();
    }

    // Ska varje varv anropas av varje Tank en gång för varje Sprite på spelplanen (förutom sig själv)
    // När en Tank gått igenom alla andra Sprites kommer den kunna använda getLatestReading() för att
    // returnera den som är närmast ifall det finns någon
    public void readValue(Sprite read, int radius){
        PVector temp = tank.readSensor_distance(tank.getSensor("ULTRASONIC_FRONT")).obj.position;
        boolean hit = lineCircle(tank.position.x, tank.position.y, temp.x, temp.y, read.position.x, read.position.y, radius);
        if (hit) {
            if (tank.position.dist(read.position) < 200) {
                if (closestObject == null) {
                    closestObject = read;
                    latestReading = new SensorReading(read, PVector.dist(tank.position, read.position), 0);
                } else if (tank.position.dist(read.position) < tank.position.dist(closestObject.position)) {
                    closestObject = read;
                    latestReading = new SensorReading(read, PVector.dist(tank.position, read.position), 0);
                }
            }
        }
    }

    // Metoder nedan är inte skrivna av oss utan kommer härifrån:
    // http://jeffreythompson.org/collision-detection/line-circle.php

    // LINE/CIRCLE
    boolean lineCircle(float x1, float y1, float x2, float y2, float cx, float cy, float r) {

        // is either end INSIDE the circle?
        // if so, return true immediately
        boolean inside1 = pointCircle(x1,y1, cx,cy,r);
        boolean inside2 = pointCircle(x2,y2, cx,cy,r);
        if (inside1 || inside2) return true;

        // get length of the line
        float distX = x1 - x2;
        float distY = y1 - y2;
        float len = sqrt( (distX*distX) + (distY*distY) );

        // get dot product of the line and circle
        float dot = ( ((cx-x1)*(x2-x1)) + ((cy-y1)*(y2-y1)) ) / pow(len,2);

        // find the closest point on the line
        float closestX = x1 + (dot * (x2-x1));
        float closestY = y1 + (dot * (y2-y1));

        // is this point actually on the line segment?
        // if so keep going, but if not, return false
        boolean onSegment = linePoint(x1,y1,x2,y2, closestX,closestY);
        if (!onSegment) return false;

        // get distance to closest point
        distX = closestX - cx;
        distY = closestY - cy;
        float distance = sqrt( (distX*distX) + (distY*distY) );

        if (distance <= r) {
            return true;
        }
        return false;
    }


    // POINT/CIRCLE
    boolean pointCircle(float px, float py, float cx, float cy, float r) {

        // get distance between the point and circle's center
        // using the Pythagorean Theorem
        float distX = px - cx;
        float distY = py - cy;
        float distance = sqrt( (distX*distX) + (distY*distY) );

        // if the distance is less than the circle's
        // radius the point is inside!
        if (distance <= r) {
            return true;
        }
        return false;
    }


    // LINE/POINT
    boolean linePoint(float x1, float y1, float x2, float y2, float px, float py) {

        // get distance from the point to the two ends of the line
        float d1 = dist(px,py, x1,y1);
        float d2 = dist(px,py, x2,y2);

        // get the length of the line
        float lineLen = dist(x1,y1, x2,y2);

        // since floats are so minutely accurate, add
        // a little buffer zone that will give collision
        float buffer = 0.1f;    // higher # = less accurate

        // if the two distances are equal to the line's
        // length, the point is on the line!
        // note we use the buffer here to give a range,
        // rather than one #
        if (d1+d2 >= lineLen-buffer && d1+d2 <= lineLen+buffer) {
            return true;
        }
        return false;
    }
}
