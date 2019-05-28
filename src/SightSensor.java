import processing.core.PVector;

import static processing.core.PApplet.*;

public class SightSensor extends Sensor {

    SightSensor(Tank t) {
        super(t);
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

        if (delta >= 0) {
            float x1 = (-B - sqrt(delta)) / (2 * A);
            float y1 = a * x1 + b;
            if ((x1 > min(t.x, temp.x)) && (x1 < max(t.x, temp.x)) && (y1 > min(t.y, temp.y)) && (y1 < max(t.y, temp.y))) {
                sr = new SensorReading(read, PVector.dist(t, checkPos), 0F);
            }
        }

        return sr;
    }
}
