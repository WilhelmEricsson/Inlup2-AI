import processing.core.PVector;

class Team {

    Tank[] tanks = new Tank[3];
    int id; // team red 0, team blue 1.
    int tank_size;
    PVector tank0_startpos = new PVector();
    PVector tank1_startpos = new PVector();
    PVector tank2_startpos = new PVector();

    float homebase_x;
    float homebase_y;
    float homebase_width = 150;
    float homebase_height = 350;

    int team_color;

    int numberOfHits; // sammalagda antalet bekräftade träffar på andra lagets tanks.

    TankProg tp;

    Team (TankProg tp, int team_id, int tank_size, int c,
          PVector tank0_startpos, int tank0_id, CannonBall ball0,
          PVector tank1_startpos, int tank1_id, CannonBall ball1,
          PVector tank2_startpos, int tank2_id, CannonBall ball2)
    {
        this.tp = tp;
        this.id = team_id;
        this.tank_size = tank_size;
        this.team_color = c;
        this.tank0_startpos.set(tank0_startpos);
        this.tank1_startpos.set(tank1_startpos);
        this.tank2_startpos.set(tank2_startpos);

        this.numberOfHits = 0;

        tanks[0] = new Tank(tank0_id, this, this.tank0_startpos, this.tank_size, ball0, tp);
        tanks[1] = new Tank(tank1_id, this, this.tank1_startpos, this.tank_size, ball1, tp);
        tanks[2] = new Tank(tank2_id, this, this.tank2_startpos, this.tank_size, ball2, tp);


        if (this.id==0) {this.homebase_x = 0; this.homebase_y = 0;}
        else if (this.id==1) {this.homebase_x = tp.width - 151; this.homebase_y = tp.height - 351;}

    }

    int getId() {
        return this.id;
    }

    int getColor() {
        return this.team_color;
    }

    void messageSuccessfulHit() {
        this.numberOfHits += 1;
    }

    void updateLogic() {

    }


    // Används inte.
    // Hemma i homebase
    //boolean isInHomebase(PVector pos) {
    //  return true;
    //}

    void displayHomeBaseTeam() {
        tp.strokeWeight(1);
        //fill(204, 50, 50, 15);
        tp.fill(this.team_color, 15);
        //rect(0, 0, 150, 350);
        tp.rect(this.homebase_x, this.homebase_y, this.homebase_width, this.homebase_height);
    }


    void displayHomeBase(){
        displayHomeBaseTeam();
    }

}
