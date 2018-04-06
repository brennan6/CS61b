/* Author: Paul N. Hilfinger.  (C) 2008. */

package qirkat;

import org.junit.Test;
import static org.junit.Assert.*;

import static qirkat.Move.*;

/** Test Move creation.
 *  @author
 */
public class MoveTest {

    @Test
    public void testMove1() {
        Move m = move('a', '3', 'b', '2');
        assertNotNull(m);
        assertFalse("move should not be jump", m.isJump());
    }

    @Test
    public void testJump1() {
        Move m = move('a', '3', 'a', '5');
        assertNotNull(m);
        assertTrue("move should be jump", m.isJump());
    }

    @Test
    public void testMove2() {
        Move m = move('a', '3', 'c', '5');
        assertEquals('b', m.jumpedCol());
        assertEquals('4', m.jumpedRow());
        Move m2 = move('c', '3', 'a', '5');
        assertEquals('b', m2.jumpedCol());
        assertEquals('4', m2.jumpedRow());
        Move m3 = move('c', '3', 'c', '5');
        assertEquals('c', m3.jumpedCol());
        assertEquals('4', m3.jumpedRow());
        Move m4 = move('c', '3', 'a', '3');
        assertEquals('b', m4.jumpedCol());
        assertEquals('3', m4.jumpedRow());
        Move m5 = move('c', '3', 'e', '3');
        assertEquals('d', m5.jumpedCol());
        assertEquals('3', m5.jumpedRow());
        Move m6 = move('c', '3', 'd', '3');
        assertFalse(m6.isLeftMove());
        assertTrue(m6.isRightMove());
        Move m7 = move('c', '3', 'b', '3');
        assertFalse(m7.isRightMove());
        assertTrue(m7.isLeftMove());
    }

    @Test
    public void testString() {
        assertEquals("a3-b2", move('a', '3', 'b', '2').toString());
        assertEquals("a3-a5", move('a', '3', 'a', '5').toString());
        assertEquals("a3-a5-c3", move('a', '3', 'a', '5',
                                      move('a', '5', 'c', '3')).toString());
    }

    @Test
    public void testParseString() {
        assertEquals("a3-b2", parseMove("a3-b2").toString());
        assertEquals("a3-a5", parseMove("a3-a5").toString());
        assertEquals("a3-a5-c3", parseMove("a3-a5-c3").toString());
        assertEquals("a3-a5-c3-e1", parseMove("a3-a5-c3-e1").toString());
    }
}
