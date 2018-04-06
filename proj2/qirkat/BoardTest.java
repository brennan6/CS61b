package qirkat;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

/** Tests of the Board class.
 *  @author
 */
public class BoardTest {

    private static final String INIT_BOARD =
        "  b b b b b\n  b b b b b\n  b b - w w\n  w w w w w\n  w w w w w";

    private static final String[] GAME1 =
    { "c2-c3", "c4-c2",
      "c1-c3", "a3-c1",
      "c3-a3", "c5-c4",
      "a3-c5-c3",
    };

    private static final String[] GAME2 =
    { "a4-b4", "c3-b3", "b4-a4", "b3-c3"
    };


    private static final String GAME1_BOARD =
        "  b b - b b\n  b - - b b\n  - - w w w\n  w - - w w\n  w w b w w";

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(Move.parseMove(s));
            System.out.print(b);
        }
    }

    @Test
    public void testInit1() {
        Board b0 = new Board();
        assertEquals(INIT_BOARD, b0.toString());
    }

    @Test
    public void testMoves1() {
        Board b0 = new Board();
        makeMoves(b0, GAME1);
        assertEquals(GAME1_BOARD, b0.toString());
    }

    @Test
    public void testgetMoves() {
        Board b0 = new Board();
        ArrayList<Move> moveList = b0.getMoves();
        assertTrue(moveList.size() != 0);
    }


    @Test
    public void testGet() {
        Board b0 = new Board();
    }

    @Test
    public void testToString() {
        Board b0 = new Board();
        System.out.print(b0.toString());
        Board b1 = new Board(b0);
        System.out.print(b1.toString());
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);
        System.out.print(b2.toString());
    }

    @Test
    public void testUndo() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);
        for (int i = 0; i < GAME1.length; i += 1) {
            b0.undo();
        }
        assertEquals("failed to return to start", b1, b0);
        makeMoves(b0, GAME1);
        assertEquals("second pass failed to reach same position", b2, b0);
    }

}
