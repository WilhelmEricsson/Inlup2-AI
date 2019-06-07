/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import processing.core.PImage;

import processing.core.PVector;

public class Turret {
    PImage img;
    float rotation_speed;
    float cannon_length;

    PVector position;
    //PVector velocity;
    //PVector acceleration;
    // Variable for heading!
    float heading;

    TankProg tp;

    Turret(float cannon_length, TankProg tp) {
        //this.img = loadImage("gunTurret2.png");
        this.position = new PVector(0, 0);
        this.tp = tp;
        this.cannon_length = cannon_length;
        this.heading = 0.0f;
        this.rotation_speed = tp.radians(1);
    }

    void turnLeft() {
        this.heading -= this.rotation_speed;
    }

    void turnRight() {
        this.heading += this.rotation_speed;
    }

    void drawTurret(){
        tp.strokeWeight(1);
        //fill(204, 50, 50);
        tp.ellipse(0, 0, 25, 25);
        tp.strokeWeight(3);
        tp.line(0, 0, this.cannon_length, 0);
    }

    void fire() {

    }

    void display() {
        this.position.x = tp.cos(this.heading);
        this.position.y = tp.sin(this.heading);

        tp.rotate(this.heading);
        //image(img, 20, 0);
        drawTurret();

    }
}

