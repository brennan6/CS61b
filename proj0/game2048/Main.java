package game2048;

import java.util.Random;
import java.util.Scanner;

import ucb.util.CommandArgs;

/** The main class for the 2048 game.
 *  @author P. N. Hilfinger
 */
public class Main {

    /** Number of squares on the side of a board. */
    static final int BOARD_SIZE = 4;
    /** Probability of choosing 2 as random tile (as opposed to 4). */
    static final double TILE2_PROBABILITY = 0.9;

    /** The main program.  ARGS may contain the options --seed=NUM,
     *  (random seed); --log (record moves and random tiles
     *  selected.); --testing (take random tiles and moves from
     *  standard input); and --no-display. */
    public static void main(String... args) {
        CommandArgs options =
            new CommandArgs("--seed=(\\d+) --log --testing --no-display",
                            args);
        if (!options.ok()) {
            System.err.println("Usage: java game2048.Main [ --seed=NUM ] "
                               + "[ --log ] [ --testing ] [ --no-display ]");
            System.exit(1);
        }

        Random gen = new Random();
        if (options.contains("--seed")) {
            gen.setSeed(options.getLong("--seed"));
        }

        Model model = new Model(BOARD_SIZE);

        GUI gui;

        if (options.contains("--no-display")) {
            gui = null;
        } else {
            gui = new GUI("2048 61B", model);
            gui.display(true);
        }

        InputSource inp;
        if (gui == null && !options.contains("--testing")) {
            System.err.println("Error: no input source.");
            System.exit(1);
            return;
        } else if (options.contains("--testing")) {
            inp = new TestSource(new Scanner(System.in));
        } else {
            inp = new GUISource(gui, gen, TILE2_PROBABILITY,
                                options.contains("--log"));
        }

        if (options.contains("--testing")) {
            BoardLogger logger = new BoardLogger();
            model.addObserver(logger);
        }

        Game game = new Game(model, inp);

        try {
            while (game.playing()) {
                game.playGame();
            }
        } catch (IllegalStateException excp) {
            System.err.printf("Internal error: %s%n", excp.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }

}
