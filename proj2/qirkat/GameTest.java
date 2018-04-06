package qirkat;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameTest {

    @Test
    public void doSetTest() {
        String poo = "w---w ----- ----- ----- bb---";
        Board b0 = new Board();
        PieceColor b = PieceColor.BLACK;
        b0.setPieces(poo, b);
    }

}
