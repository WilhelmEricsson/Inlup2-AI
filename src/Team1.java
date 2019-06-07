/**
 * Wilhelm Ericsson
 * Ruben Wilhelmsen
 */
import processing.core.PVector;

import java.util.HashSet;

class Team1 extends Team {
    private HashSet<PVector> searchedArea;
    Team1(TankProg tp, int team_id, int tank_size, int c,
          PVector tank0_startpos, int tank0_id, CannonBall ball0,
          PVector tank1_startpos, int tank1_id, CannonBall ball1,
          PVector tank2_startpos, int tank2_id, CannonBall ball2) {

        super(tp, team_id, tank_size, c, tank0_startpos, tank0_id, ball0, tank1_startpos, tank1_id, ball1, tank2_startpos, tank2_id, ball2);

        searchedArea = new HashSet<>();
        tanks[0] = new Tank4(tank0_id, this, this.tank0_startpos, this.tank_size, ball0, tp);
        tanks[1] = new Tank4(tank1_id, this, this.tank1_startpos, this.tank_size, ball1, tp);
        tanks[2] = new Tank4(tank2_id, this, this.tank2_startpos, this.tank_size, ball2, tp);

        //this.homebase_x = 0;
        //this.homebase_y = 0;
    }



    public void addSearchedArea(PVector position){
        searchedArea.add(position);
    }
    public boolean isPosistionSearched(PVector position){
        return searchedArea.contains(position);
    }


}
