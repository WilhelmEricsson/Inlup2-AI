/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import processing.core.PVector;

import static processing.core.PApplet.*;

public class SightSensor extends Sensor {

    Sprite closestObject = null;
    SensorReading latestReading;

    SightSensor(Tank t) {
        super(t);
        latestReading = new SensorReading();
    }

    public SensorReading getLatestReading() {
        SensorReading temp = latestReading;
        return temp;
    }

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

    // ty http://jeffreythompson.org/collision-detection/line-circle.php
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


        /*
        PVector checkPos = read.position;
        SensorReading sr = new SensorReading();

        PVector temp = PVector.fromAngle(tank.heading);
                //tank.readSensor_distance(tank.getSensor("ULTRASONIC_FRONT")).obj.position;
        temp.normalize();
        temp.mult(200);
        //System.out.println("TEST: " + temp.toString());
        tank.getTp().pushMatrix();
        tank.getTp().translate(tank.position.x,tank.position.y);
        tank.getTp().ellipse(temp.x,temp.y,20 ,20);
        tank.getTp().line(0, 0, temp.x, temp.y);
        tank.getTp().popMatrix();

        PVector t = new PVector(tank.position.x, tank.position.y);
        PVector sub = PVector.sub(temp, t);

        // https://forum.processing.org/two/discussion/90/point-and-line-intersection-detection
        // Checks if sub is intersected by an ellipse which position is checkPos with the giver radius
        float a = sub.y / sub.x;
        float b = t.y - a * t.x;

        float A = (1 + a * a);
        float B = (2 * a *( b - checkPos.y) - 2 * checkPos.x);
        float C = (checkPos.x * checkPos.x + (b - checkPos.y) * (b - checkPos.y)) - (radius * radius);
        float delta = B * B - 4 * A * C;

        PVector pos1 = new PVector(0,0);
        PVector pos2 = new PVector(0,0);
        boolean posFound = false;

        if (delta >= 0) {
            float x1 = (-B - sqrt(delta)) / (2 * A);
            float y1 = a * x1 + b;

            float x2 = (-B + sqrt(delta)) / (2 * A);
            float y2 = a * x2 + b;

            if ((x1 > min(t.x, temp.x)) && (x1 < max(t.x, temp.x)) && (y1 > min(t.y, temp.y)) && (y1 < max(t.y, temp.y))) {
                pos1 = new PVector(x1,y1);
                posFound = true;
            }
            if ((x2 > min(t.x, temp.x)) && (x2 < max(t.x, temp.x)) && (y2 > min(t.y, temp.y)) && (y2 < max(t.y, temp.y))) {
                pos2 = new PVector(x2,y2);
                posFound = true;
            }

            if (t.dist(pos1) < t.dist(pos2)) {
                tank.getTp().pushMatrix();
                tank.getTp().fill(255, 255, 0);
                tank.getTp().ellipse(pos1.x, pos1.y, 25, 25);
                tank.getTp().popMatrix();
                intersectPoint = pos1;
            } else {
                tank.getTp().pushMatrix();
                tank.getTp().fill(255, 255, 0);
                tank.getTp().ellipse(pos2.x, pos2.y, 25, 25);
                tank.getTp().popMatrix();
                intersectPoint = pos2;
            }

            if (posFound) {
                sr = new SensorReading(read, PVector.dist(t, checkPos), 0F);
            }
        }
        latestReading = sr;
        return sr;
        */
    }

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
