package qirkat;

import static qirkat.PieceColor.*;
import static qirkat.Move.*;
import java.util.Observable;
import java.util.ArrayList;
import java.util.Observer;
import java.util.Formatter;

/** A Qirkat board.   The squares are labeled by column (a char value between
 *  'a' and 'e') and row (a char value between '1' and '5'.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (with row 0 being the bottom row)
 *  counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Matthew Brennan
 */
class Board extends Observable {

    /** A new, cleared board at the start of the game. */
    Board() {
        clear();
    }

    /** A copy of B. */
    Board(Board b) {
        internalCopy(b);
    }

    /** Return a constant view of me (allows any access method, but no
     *  method that modifies it). */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions. */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;

        matrix = new PieceColor[][]{{WHITE, WHITE, WHITE, WHITE, WHITE},
            {WHITE, WHITE, WHITE, WHITE, WHITE},
            {WHITE, WHITE, EMPTY, BLACK, BLACK},
            {BLACK, BLACK, BLACK, BLACK, BLACK},
            {BLACK, BLACK, BLACK, BLACK, BLACK}};

        lrShifts = new PieceColor[][]{{EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}};

        setChanged();
        notifyObservers();
    }

    /** Copy B into me. */
    void copy(Board b) {
        internalCopy(b);
    }

    /** Copy B into me. */
    private void internalCopy(Board b) {
        matrix = b.matrix;
        _whoseMove = b._whoseMove;
        _gameOver = b._gameOver;
        pastMoves = b.pastMoves;
        lrShifts = b.lrShifts;
    }

    /** Set my contents as defined by STR.  STR consists of 25 characters,
     *  each of which is b, w, or -, optionally interspersed with whitespace.
     *  These give the contents of the Board in row-major order, starting
     *  with the bottom row (row 1) and left column (column a). All squares
     *  are initialized to allow horizontal movement in either direction.
     *  NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }
        clear();
        _whoseMove = nextMove;

        for (int k = 0; k < str.length(); k += 1) {
            switch (str.charAt(k)) {
            case '-':
                this.set(k, EMPTY);
                this.change(k, EMPTY);
                break;
            case 'b': case 'B':
                this.set(k, BLACK);
                this.change(k, EMPTY);
                break;
            case 'w': case 'W':
                this.set(k, WHITE);
                this.change(k, EMPTY);
                break;
            default:
                break;
            }
        }
        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if the current player has
     *  no moves. */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current contents of square C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.  */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return get(index(c, r));
    }

    /** Return the current contents of the square at linearized index K. */
    PieceColor get(int k) {
        assert validSquare(k);
        int i = Math.floorDiv(k, 5);
        int j = k % 5;
        return this.matrix[i][j];
    }

    /** Return the current contents of lrshift C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.
     *  @param c <char>
     *  @param r <char>*/
    PieceColor capture(char c, char r) {
        assert validSquare(c, r);
        return capture(index(c, r));
    }

    /** Return the current lrshifts at linearized index k.
     * @return
     * @param k <int value>*/
    PieceColor capture(int k) {
        assert validSquare(k);
        int i = Math.floorDiv(k, 5);
        int j = k % 5;
        return this.lrShifts[i][j];
    }

    /** Change the values of the lrShift matrix.
     * @param c <char>
     * @param r <char>
     * @param v <Piececolor>*/
    private void change(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Change the values of the lrShift matrix.
     * @param k <int value>
     * @param v <Piececolor v>*/
    private void change(int k, PieceColor v) {
        assert validSquare(k);
        int i = Math.floorDiv(k, 5);
        int j = inverseSide(k % 5);
        this.lrShifts[i][j] = v;
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'e', and
     *  '1' <= R <= '5'. */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Set get(K) to V, where K is the linearized index of a square. */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        int i = Math.floorDiv(k, 5);
        int j = inverseSide(k % 5);
        this.matrix[i][j] = v;
    }

    /** Reverses the number in order to change the matrix.
     * @return
     * @param m <int value>
     * */
    private int inverseSide(int m) {
        if (m == 4) {
            return 0;
        } else if (m == 3) {
            return 1;
        } else if (m == 2) {
            return 2;
        } else if (m == 1) {
            return 3;
        } else {
            return 4;
        }

    }

    /** Return true iff MOV is legal on the current board. */
    boolean legalMove(Move mov) {
        char col0 = inverseSide(mov.col0());
        char row0 = mov.row0();
        char col1 = inverseSide(mov.col1());
        char row1 = mov.row1();
        char jumpedCol = inverseSide(mov.jumpedCol());
        char jumpedRow = mov.jumpedRow();
        int kVal = index(mov.col0(), mov.row0());
        boolean jumpPossible = true;
        PieceColor turn = whoseMove();
        if ((mov.isRightMove() || mov.isLeftMove())
                && turn == BLACK && kVal < 5) {
            return false;
        }
        if ((mov.isRightMove() || mov.isLeftMove())
                && turn == WHITE && kVal > 5 * 4) {
            return false;
        }
        if (mov.isRightMove()
                && this.capture(mov.col0(), mov.row0()) == RIGHT) {
            return false;
        }
        if (mov.isLeftMove() && this.capture(mov.col0(), mov.row0()) == LEFT) {
            return false;
        }
        if (mov.jumpTail() == null) {
            if (mov.isJump()) {
                jumpPossible = (this.get(jumpedCol, jumpedRow) != whoseMove()
                        && this.get(jumpedCol, jumpedRow) != EMPTY);
            }
            return (validSquare(col1, row1)
                    && this.get(col0, row0) == whoseMove()
                    && this.get(col1, row1) == EMPTY && jumpPossible);
        }
        while (mov.jumpTail() != null) {
            if (mov.isJump()) {
                jumpPossible = (this.get(jumpedCol, jumpedRow) != whoseMove()
                        && this.get(jumpedCol, jumpedRow) != EMPTY);
            }
            if (validSquare(col1, row1) && this.get(col0, row0) == whoseMove()
                    && this.get(col1, row1) == EMPTY && jumpPossible) {
                mov = mov.jumpTail();
            } else {
                return false;
            }
        }
        return true;
    }

    /** Reverses the side in order to adjust for matrix.
     * @return
     * @param c <char value>
     * */
    public char inverseSide(char c) {
        if (c == 'a') {
            return 'e';
        } else if (c == 'b') {
            return 'd';
        } else if (c == 'c') {
            return 'c';
        } else if (c == 'd') {
            return 'b';
        } else {
            return 'a';
        }
    }

    /** Return a list of all legal moves from the current position. */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /** Add all legal moves from the current position to MOVES. */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            getJumps(moves, k);
        }
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            getMoves(moves, k);
        }
    }

    /** Adds moves that are legal.
     * @param moves <ArrayList>
     * @param other <ArrayList> */
    private void legalAdd(ArrayList<Move> moves, ArrayList<Move> other) {
        for (Move mov: other) {
            if (legalMove(mov)) {
                moves.add(mov);
            }
        }
    }

    /** Add all legal non-capturing moves from the position
     *  with linearized index K to MOVES. */
    private void getMoves(ArrayList<Move> moves, int k) {
        ArrayList<Move> possibleMoves = new ArrayList<>();
        if (whoseMove() == WHITE) {
            if (k < 5 * 4) {
                Move up = move(Move.col(k),
                        Move.row(k), Move.col(k), Move.row(k + 5));
                possibleMoves.add(up);
            }
            if (col(k) > 'a' && k > 4) {
                Move left = move(Move.col(k),
                        Move.row(k), Move.col(k - 1), Move.row(k));
                possibleMoves.add(left);
            }
            if (col(k) < 'e' && k > 4) {
                Move right = move(Move.col(k),
                        Move.row(k), Move.col(k + 1), Move.row(k));
                possibleMoves.add(right);
            }
            if (k < 5 * 4 && col(k) < 'e') {
                Move upRightDiag = move(Move.col(k),
                        Move.row(k), Move.col(k + 1), Move.row(k + 6));
                possibleMoves.add(upRightDiag);
            }
            if (k < 5 * 4 && col(k) > 'a') {
                Move upLeftDiag = move(Move.col(k),
                        Move.row(k), Move.col(k - 1), Move.row(k + 4));
                possibleMoves.add(upLeftDiag);
            }
        } else {
            if (k > 4) {
                Move down = move(Move.col(k),
                        Move.row(k), Move.col(k), Move.row(k - 5));
                possibleMoves.add(down);
            }
            if (col(k) > 'a' && k < 5 * 4) {
                Move left = move(Move.col(k),
                        Move.row(k), Move.col(k - 1), Move.row(k));
                possibleMoves.add(left);
            }
            if (col(k) < 'e' && k < 5 * 4) {
                Move right = move(Move.col(k),
                        Move.row(k), Move.col(k + 1), Move.row(k));
                possibleMoves.add(right);
            }
            if (k > 4 && col(k) < 'e') {
                Move downRightDiag = move(Move.col(k),
                        Move.row(k), Move.col(k + 1), Move.row(k - 4));
                possibleMoves.add(downRightDiag);
            }
            if (k > 4 && col(k) > 'a') {
                Move downLeftDiag = move(Move.col(k),
                        Move.row(k), Move.col(k - 1), Move.row(k - 6));
                possibleMoves.add(downLeftDiag);
            }
        }
        legalAdd(moves, possibleMoves);
    }

    /** Add all legal captures from the position with linearized index K
     *  to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        int i = Math.floorDiv(k, 5); int j = k % 5;
        ArrayList<Move> possibleMoves = new ArrayList<>();
        if (k % 2 == 0) {
            if (i <= 2 && j <= 2) {
                Move upRightDiag = move(Move.col(k),
                        Move.row(k), Move.col(k + 2), Move.row(k + 12));
                possibleMoves.add(upRightDiag);
            } else if (i <= 2 && j >= 2) {
                Move upLeftDiag = move(Move.col(k),
                        Move.row(k), Move.col(k - 2), Move.row(k + 8));
                possibleMoves.add(upLeftDiag);
            } else if (i >= 2 && j <= 2) {
                Move downRightDiag = move(Move.col(k),
                        Move.row(k), Move.col(k + 2), Move.row(k - 8));
                possibleMoves.add(downRightDiag);
            } else if (i >= 2 && j >= 2) {
                Move downLeftDiag = move(Move.col(k),
                        Move.row(k), Move.col(k - 2), Move.row(k - 12));
                possibleMoves.add(downLeftDiag);
            }
        }
        if (j <= 2) {
            Move right = move(Move.col(k),
                    Move.row(k), Move.col(k + 2), Move.row(k));
            possibleMoves.add(right);
        } else if (j >= 2) {
            Move left = move(Move.col(k)
                    , Move.row(k), Move.col(k - 2), Move.row(k));
            possibleMoves.add(left);
        } else if (i >= 2) {
            Move up = move(Move.col(k)
                    , Move.row(k), Move.col(k), Move.row(k + 10));
            possibleMoves.add(up);
        } else if (i <= 2) {
            Move down = move(Move.col(k),
                    Move.row(k), Move.col(k), Move.row(k - 10));
            possibleMoves.add(down);
        }
        for (Move mov: possibleMoves) {
            if (legalMove(mov)) {
                moves.add(mov);
            }
        }
    }

    /** New equals to determine what qualifies as a comparable board.
     * @return
     * @param obj <the comparison object>
     * */
    public boolean equals(Object obj) {
        Board b0 = (Board) obj;
        if (matrix.equals(b0.matrix)
                && this._whoseMove.equals(b0._whoseMove)
                && pastMoves.equals(b0.pastMoves)) {
            return true;
        }
        return false;
    }

    /** Random hashcode to fulfill need.
     * @return
     * */
    @Override
    public int hashCode() {
        return 1;
    }

    /** Return true iff MOV is a valid jump sequence on the current board.
     *  MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     *  could be continued and are valid as far as they go.  */
    boolean checkJump(Move mov, boolean allowPartial) {
        return (mov == null);
    }

    /** Return true iff a jump is possible for a piece at position C R. */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /** Return true iff a jump is possible for a piece at position with
     *  linearized index K. */
    boolean jumpPossible(int k) {
        int i = Math.floorDiv(k, 5);
        int j = k % 5;
        boolean isEven = (k % 2 == 0);
        if (isEven) {
            if (i <= 2 && j <= 2) {
                return true;

            } else if (i <= 2 && j >= 2) {
                return true;

            } else if (j <= 2) {
                return true;

            } else if (j >= 2) {
                return true;

            } else if (i >= 2) {
                return true;

            } else if (i <= 2) {
                return true;

            } else if (i >= 2 && j <= 2) {
                return true;

            } else {
                return (i >= 2 && j >= 2);
            }
        } else {
            if (j <= 2) {
                return true;

            } else if (j >= 2) {
                return true;

            } else if (i >= 2) {
                return true;

            } else {
                return (i <= 2);
            }

        }
    }

    /** Return true iff a jump is possible from the current board. */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /** Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     *  Assumes the result is legal. */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }

    /** Make the Move MOV on this Board, assuming it is legal. */
    void makeMove(Move mov) {
        if (legalMove(mov)) {
            int k = index(mov.col0(), mov.row0());
            if (!mov.isJump()) {
                this.set(mov.col0(), mov.row0(), EMPTY);
                this.set(mov.col1(), mov.row1(), whoseMove());
                this.change(k, EMPTY);
            } else {
                int jumped =  index(mov.jumpedCol(), mov.jumpedRow());
                this.set(mov.col0(), mov.row0(), EMPTY);
                this.set(mov.jumpedCol(), mov.jumpedRow(), EMPTY);
                this.set(mov.col1(), mov.row1(), whoseMove());
                this.change(k, EMPTY);
                this.change(jumped, EMPTY);
                Move next = mov.jumpTail();
                while (next != null) {
                    int nextStart = index(next.col0(), next.row0());
                    int nextJumped = index(next.jumpedCol(), next.jumpedRow());
                    this.set(next.col0(), next.row0(), EMPTY);
                    this.set(next.jumpedCol(), next.jumpedRow(), EMPTY);
                    this.set(next.col1(), next.row1(), whoseMove());
                    this.change(nextStart, EMPTY);
                    this.change(nextJumped, EMPTY);
                    next = next.jumpTail();
                }
            }
            if (whoseMove() == WHITE) {
                _whoseMove = BLACK;
            } else {
                _whoseMove = WHITE;
            }
            int shiftIndex = index(mov.col0(), mov.row0());
            if (mov.isRightMove()) {
                this.change(shiftIndex + 1, LEFT);
            } else if (mov.isLeftMove()) {
                this.change(shiftIndex - 1, RIGHT);
            }
            pastMoves.add(mov);
            setChanged();
            notifyObservers();
        }
    }

    /** Undo the last move, if any. */
    void undo() {
        if (!pastMoves.isEmpty()) {
            Move pastMove = pastMoves.get(pastMoves.size() - 1);
            PieceColor other;
            if (whoseMove() == BLACK) {
                _whoseMove = WHITE;
                other = BLACK;
            } else {
                _whoseMove = BLACK;
                other = WHITE;
            }
            if (!pastMove.isJump()) {
                this.set(pastMove.col0(), pastMove.row0(), whoseMove());
                this.set(pastMove.col1(), pastMove.row1(), EMPTY);
            } else {
                this.set(pastMove.col0(), pastMove.row0(), whoseMove());
                this.set(pastMove.jumpedCol(), pastMove.jumpedRow(), other);
                this.set(pastMove.col1(), pastMove.row1(), EMPTY);
            }
            Move next = pastMove.jumpTail();
            while (next != null) {
                this.set(next.col0(), next.row0(), EMPTY);
                this.set(next.jumpedCol(), next.jumpedRow(), other);
                this.set(next.col1(), next.row1(), EMPTY);
                next = next.jumpTail();
            }
            pastMoves.remove(pastMoves.size() - 1);
        }
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        out.format(" ");
        for (int i = SIDE - 1; i > -1; i -= 1) {
            for (int j = SIDE - 1; j > -1; j -= 1) {
                out.format(" %s", this.matrix[i][j].shortName());
            }
            if (i > 0) {
                out.format("\n ");
            }
        }
        if (legend) {
            out.format("%s\n", "");
            out.format("   %s", "a b c d e");
        }
        return out.toString();

    }

    /** Return true iff there is a move for the current player. */
    private boolean isMove() {
        ArrayList<Move> moves = this.getMoves();
        return moves.size() > 0;
    }


    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Matrix to keep track of the data. */
    private PieceColor[][] matrix;

    /** Matrix to keep track of left/right bad situations. */
    private PieceColor[][] lrShifts;

    /** All of the past moves. */
    private ArrayList<Move> pastMoves = new ArrayList<>();


    /** Set true when game ends. */
    private boolean _gameOver;


    /** Convenience value giving values of pieces at each ordinal position. */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();

    /** One cannot create arrays of ArrayList<Move>, so we introduce
     *  a specialized private list type for this purpose. */
    private static class MoveList extends ArrayList<Move> {
    }

    /** A read-only view of a Board. */
    private class ConstantBoard extends Board implements Observer {
        /** A constant view of this Board. */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /** Undo the last move. */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}
