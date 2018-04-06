package game2048;

import java.util.Random;

/** A type of InputSource that receives commands from a GUI.
 *  @author P. N. Hilfinger
 */
class GUISource implements InputSource {

    /** Provides input from SOURCE, logging it iff LOG. Use RANDOMSOURCE to
     *  select random tiles having value 2 with probability PROBOF2. */
    GUISource(GUI source, Random randomSource, double probOf2, boolean log) {
        _source = source;
        _randomSource = randomSource;
        _probOf2 = probOf2;
        _log = log;
    }

    @Override
    public String getKey() {
        String command = _source.readKey();
        if (_log) {
            System.out.printf("K %s%n", command);
        }
        return command;
    }

    @Override
    public Tile getNewTile(int size) {
        int c = _randomSource.nextInt(size), r = _randomSource.nextInt(size);
        int v = _randomSource.nextDouble() <= _probOf2 ? 2 : 4;
        if (_log) {
            System.out.printf("T %d %d %d%n", v, c, r);
        }
        return Tile.create(v, c, r);
    }

    /** Input source. */
    private GUI _source;
    /** Random source for Tiles. */
    private Random _randomSource;
    /** Probabilty that value of new Tile is 2 rather than 4. */
    private double _probOf2;
    /** True iff logging inputs. */
    private boolean _log;

}
