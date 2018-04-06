package qirkat;

import static qirkat.PieceColor.*;
import static qirkat.Command.Type.*;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Matt Brennan
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
        _prompt = myColor + ": ";
    }

    @Override
    Move myMove() {
        Command first = game().getMoveCmnd(_prompt);
        Move mov = null;
        if (first != null) {
            mov = Move.parseMove(first.operands()[0]);
            if (!board().legalMove(mov)) {
                game().reportError("Move is Illegal.");
                mov = null;
            }
        }
        return mov;
    }

    /** Identifies the player serving as a source of input commands. */
    private String _prompt;
}

