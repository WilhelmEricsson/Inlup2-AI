import processing.core.PApplet;
import processing.core.PVector;

import static processing.core.PApplet.*;

public class SightSensor extends Sensor {

    private PApplet tp;

    SightSensor(Tank t, PApplet tp) {
        super(t);
        this.tp = tp;
    }

    public SensorReading readValue(PVector checkPos){
        SensorReading sr = new SensorReading();
        PVector temp = tank.readSensor_distance(tank.getSensor("ULTRASONIC_FRONT")).obj.position;

        PVector t = new PVector(tank.position.x, tank.position.y);
        PVector sub = PVector.sub(temp, t);

        // https://forum.processing.org/two/discussion/90/point-and-line-intersection-detection
        // Checks if point intersects sub
        float a = sub.y / sub.x;
        float b = t.y - a * t.x;


        float A = (1 + a * a);
        float B = (2 * a *( b - checkPos.y) - 2 * checkPos.x);
        float C = (checkPos.x * checkPos.x + (b - checkPos.y) * (b - checkPos.y)) - (25 * 25);
        float delta = B * B - 4 * A * C;


        if (delta >= 0) {
            float x1 = (-B - sqrt(delta)) / (2 * A);
            float y1 = a * x1 + b;
            if ((x1 > min(t.x, temp.x)) && (x1 < max(t.x, temp.x)) && (y1 > min(t.y, temp.y)) && (y1 < max(t.y, temp.y))) {
                Sprite obj = new Sprite();
                obj.position = new PVector(x1, y1);
                sr = new SensorReading(obj, PVector.dist(t, checkPos), 0F);
            }
        }

        /*
        if ((checkPos.y > (a * checkPos.x + b - 5)) && (checkPos.y < (a * checkPos.x + b + 5))) {
            Sprite obj = new Sprite();
            obj.position = checkPos;
            sr = new SensorReading(obj, PVector.dist(tank.position, checkPos), 0F);
        }*/

        return sr;
    }
}
