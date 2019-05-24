import processing.core.PImage;
import processing.core.PVector;

public class Tree extends Sprite {

    //PVector position;
    //String name;

    PImage img;
    //PVector hitArea;
    //float diameter, radius, m;
    //float m;

    TankProg tp;

    //**************************************************
    Tree(int posx, int posy, TankProg tp) {
        this.tp = tp;
        this.img = tp.loadImage("tree01_v2.png");
        this.position = new PVector(posx, posy);
        //this.hitArea = new PVector(posx, posy); // Kanske inte kommer att användas.
        this.diameter = this.img.width/2;
        this.radius = diameter/2;
        //this.m = radius*.1;

        this.name = "tree";
    }

    //**************************************************
    void checkCollision(Tank other) {


        // Get distances between the balls components
        PVector distanceVect = PVector.sub(other.position, position);

        // Calculate magnitude of the vector separating the balls
        float distanceVectMag = distanceVect.mag();

        // Minimum distance before they are touching
        float minDistance = radius + other.radius;

        if (distanceVectMag < minDistance) {
            System.out.println("! collision med en tank [Tree]");

        }

    }

    //**************************************************
    void display() {
        tp.pushMatrix();
        tp.translate(this.position.x, this.position.y);

        tp.fill(204, 102, 0, 100);
        int diameter = this.img.width/2;
        //ellipse(this.position.x, this.position.y, diameter, diameter);
        tp.ellipse(0, 0, diameter, diameter);
        //image(img, this.position.x, this.position.y);
        tp.image(img, 0, 0);

        if(tp.debugOn){
            tp.noFill();
            tp.stroke(255, 0, 0);
            tp.ellipse(0, 0, this.diameter(), this.diameter());
        }
        tp.popMatrix();

    }
}
