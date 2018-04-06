package game2048;

import java.util.InputMismatchException;
import java.util.Scanner;

/** A type of InputSource that receives commands from a Scanner.
 *  @author P. N. Hilfinger
 */
class TestSource implements InputSource {

    /** Provides input from SOURCE, logging it iff LOG. This kind of input
     *  source is intended for testing.  It expects to be asked for command
     *  strings and new Tiles in a particular order, and raises an exception
     *  if the calls on its get and getNewTile methods do not correspond to
     *  contents of its source Scanner. */
    TestSource(Scanner source) {
        _source = source;
    }

    /** Returns a command string read from my source.  Throws an
     *  IllegalStateException if a command is not the next input from
     *  my Scanner.  Returns "Quit" if there is no further input. */
    @Override
    public String getKey() {
        String c = nextInputType();
        if (c == null) {
            return "Quit";
        } else if (!"K".equals(c)) {
            throw new IllegalStateException("unexpected Key request");
        } else {
            try {
                return _source.nextLine().trim();
            } catch (InputMismatchException excp) {
                throw new IllegalStateException("malformed Key input");
            }
        }
    }

    /** Returns a Tile read from my source.  Throws an IllegalStateException
     *  if a tile is not the next input from my Scanner. */
    @Override
    public Tile getNewTile(int size) {
        String c = nextInputType();
        if (!"T".equals(c)) {
            throw new IllegalStateException("unexpected Tile request");
        }
        try {
            return Tile.create(_source.nextInt(), _source.nextInt(),
                               _source.nextInt());
        } catch (InputMismatchException excp) {
            throw new IllegalStateException("malformed Tile input");
        }
    }

    /** Return flag indicating type of next input line: "T" for tile, "K"
     *  for key, or null for end of input.  Skips preceding comment lines
     *  and whitespace. */
    private String nextInputType() {
        while (_source.hasNext()) {
            String c = _source.next();
            switch (c) {
            case "K": case "T":
                return c;
            case "#":
                _source.nextLine();
                break;
            default:
                throw new IllegalStateException("bad test input file: " + c);
            }
        }
        return null;
    }


    /** Input source. */
    private Scanner _source;
}
