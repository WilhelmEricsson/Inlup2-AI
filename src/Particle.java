import processing.core.PVector;

public class Particle {
    PVector position;
    PVector velocity;
    PVector acceleration;
    float lifespan;

    TankProg tp;

    Particle(PVector l, TankProg tp) {
        this.tp = tp;
        this.acceleration = new PVector(0, 1);
        this.position = new PVector().set(l);
        this.velocity = new PVector(tp.random(-1, 1), tp.random(-1, 1));
        //position = l.get();

        lifespan = 255.0f;
    }

    void run() {
        update();
        display();
    }

    // Method to update position
    void update() {
        //velocity.add(acceleration);
        position.add(velocity);
        lifespan -= 7.0;
    }

    // Method to display
    void display() {
        //println("lifespan: " + lifespan);
        tp.stroke(0, lifespan);
        tp.strokeWeight(2);
        tp.fill(127, lifespan);
        tp.ellipse(position.x, position.y, 100-lifespan, 100-lifespan);
    }

    // Is the particle still useful?
    boolean isDead() {
        if (lifespan < 0.0) {
            return true;
        }
        else {
            return false;
        }
    }
}
