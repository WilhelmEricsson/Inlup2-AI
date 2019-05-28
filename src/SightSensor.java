import processing.core.PVector;

import static processing.core.PApplet.*;

public class SightSensor extends Sensor {

    PVector intersectPoint = null;

    SightSensor(Tank t) {
        super(t);
    }

    public PVector getIntersectPoint() {
        return intersectPoint;
    }

    public SensorReading readValue(Sprite read, int radius){
        PVector checkPos = read.position;
        SensorReading sr = new SensorReading();
        PVector temp = tank.readSensor_distance(tank.getSensor("ULTRASONIC_FRONT")).obj.position;

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
                intersectPoint = pos1;
            } else {
                intersectPoint = pos2;
            }

            if (posFound) {
                sr = new SensorReading(read, PVector.dist(t, checkPos), 0F);
            }
        }

        return sr;
    }
}
