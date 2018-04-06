package qirkat;

import java.util.ArrayList;

import static qirkat.PieceColor.*;

/** A Player that computes its own moves.
 *  @author Matt Brennan
 */
class AI extends Player {

    /**
     * Maximum minimax search depth before going to static evaluation.
     */
    private static final int MAX_DEPTH = 1;
    /**
     * A position magnitude indicating a win (for white if positive, black
     * if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI for GAME that will play MYCOLOR.
     */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        if (move == null) {
            return move;
        }
        String prompt = myColor() + " moves " + move.col0() + move.row0()
                + "-" + move.col1() + move.row1();
        Move next = move.jumpTail();
        while (next != null) {
            prompt = prompt + "-" + next.col1() + next.row1();
            next = next.jumpTail();
        }
        prompt = prompt + ".";
        System.out.println(prompt);
        return move;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == WHITE) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Move best;
        best = null;
        int value = 0;
        if (depth == 0 || board.gameOver()) {
            return staticScore(board);
        }
        ArrayList<Move> testMoves = board.getMoves();
        for (Move m : testMoves) {
            if (board().legalMove(m)) {
                board.makeMove(m);
                int recVal = findMove(board, depth - 1,
                        saveMove, -sense, alpha, beta);
                if (sense == 1) {
                    value = Math.max(value, recVal);
                    if (recVal >= beta) {
                        board.undo();
                        return recVal;
                    }
                    if (recVal > alpha) {
                        alpha = recVal;
                        best = m;
                        value = alpha;
                    }
                    board.undo();
                } else {
                    value = Math.min(value, recVal);
                    if (recVal <= alpha) {
                        board.undo();
                        return recVal;
                    }
                    if (recVal < beta) {
                        beta = recVal;
                        best = m;
                        value = beta;
                    }
                    board.undo();
                }

            }

        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return value;
    }


    /**
     * An arbitrary value of score based on my standards.
     * @param board <Board>
     * @return
     */
    private int score(Board board) {
        int whiteScore = 0;
        int blackScore = 0;
        int blackPieces = 0;
        int whitePieces = 0;
        String str = board.toString();
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }
        if (board().whoseMove() == WHITE) {
            whiteScore += 5;
        } else {
            blackScore += 5;
        }
        for (int k = 0; k < str.length(); k += 1) {
            if (str.charAt(k) == 'w') {
                whitePieces += 1;
            } else if (str.charAt(k) == 'b') {
                blackPieces += 1;
            } else {
                break;
            }
        }
        if (whitePieces > blackPieces) {
            whiteScore += 100;
        }
        if (whitePieces < blackPieces) {
            blackScore += 100;
        }
        return whiteScore - blackScore;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        if (board.gameOver() && board.whoseMove() == BLACK) {
            return WINNING_VALUE;
        } else if (board.gameOver() && board.whoseMove() == WHITE) {
            return -WINNING_VALUE;
        }
        return score(board);
    }
}
