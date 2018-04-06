package game2048;

import java.util.Observer;
import java.util.Observable;

/** An observer that prints changes to a Model.
 *  @author P. N. Hilfinger
 */
class BoardLogger implements Observer {

    @Override
    public void update(Observable obs, Object arg) {
        Model model = (Model) obs;

        System.out.printf("%s %d %d %d",
                          model.gameOver() ? "E" : "B",
                          model.size(), model.score(),
                          model.maxScore());
        for (int row = 0; row < model.size(); row += 1) {
            for (int col = 0; col < model.size(); col += 1) {
                System.out.printf(" %d",
                                  model.tile(col, row) == null ? 0
                                  : model.tile(col, row).value());
            }
        }
        System.out.println();
    }

}
