package db61b;



import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author Matthew Brennan
 */
class Table {
    /** A new Table whose columns are given by COLUMNTITLES, which may
     *  not contain duplicate names. */
    Table(String[] columnTitles) {
        if (columnTitles.length == 0) {
            throw error("table must have at least one column");
        }
        _size = 0;
        _rowSize = columnTitles.length;

        for (int i = columnTitles.length - 1; i >= 1; i -= 1) {
            for (int j = i - 1; j >= 0; j -= 1) {
                if (columnTitles[i].equals(columnTitles[j])) {
                    throw error("duplicate column name: %s",
                                columnTitles[i]);
                }
            }
        }

        _titles = columnTitles;
        _columns = new ValueList[_rowSize];
    }

    /** A new Table whose columns are give by COLUMNTITLES. */
    Table(List<String> columnTitles) {
        this(columnTitles.toArray(new String[columnTitles.size()]));
    }

    /** Return the number of columns in this table. */
    public int columns() {
        return _rowSize;
    }

    /** Return the title of the Kth column.  Requires 0 <= K < columns(). */
    public String getTitle(int k) {
        return _titles[k];
    }

    /** Return the number of the column whose title is TITLE, or -1 if
     *  there isn't one. */
    public int findColumn(String title) {
        int i = 0;
        for (String s: _titles) {
            if (title.equals(s)) {
                return i;
            } else {
                i += 1;
            }
        }
        return -1;
    }

    /** Return the number of rows in this table. */
    public int size() {
        return _size;
    }

    /** Return the value of column number COL (0 <= COL < columns())
     *  of record number ROW (0 <= ROW < size()). */
    public String get(int row, int col) {
        try {
            if (size() >= row && columns() >= col) {
                ValueList v = _columns[col];
                return v.get(row);
            } else {
                throw new IndexOutOfBoundsException();
            }

        } catch (IndexOutOfBoundsException excp) {
            throw error("invalid row or column");
        }

    }

    /** Add a new row whose column values are VALUES to me if no equal
     *  row already exists.  Return true if anything was added,
     *  false otherwise. */
    public boolean add(String[] values) {
        if (values.length != _rowSize) {
            return false;
        }
        if (this.size() == 0) {
            for (int m = 0; m < this.columns(); m += 1) {
                _columns[m] = new ValueList();
                _columns[m].add(values[m]);
            }
            _size += 1;
            return true;
        }
        for (int i = 0; i < _size; i += 1) {
            int amountCorrect = 0;
            for (int r = 0; r < _rowSize; r += 1) {
                if (this.get(i, r).equals(values[r])) {
                    amountCorrect += 1;
                }
                if (amountCorrect == _rowSize) {
                    return false;
                }
            }

        }
        for (int p = 0; p < _rowSize; p += 1) {
            _columns[p].add(values[p]);
        }
        _size += 1;
        return true;

    }
    /** Add a new row whose column values are extracted by COLUMNS from
     *  the rows indexed by ROWS, if no equal row already exists.
     *  Return true if anything was added, false otherwise. See
     *  Column.getFrom(Integer...) for a description of how Columns
     *  extract values. */
    public boolean add(List<Column> columns, Integer... rows) {
        String[] rowtoAdd = new String[_rowSize];
        for (int i = 0; i < _rowSize; i += 1) {
            String val = columns.get(i).getFrom(rows);
            rowtoAdd[i] = val;
        }
        return this.add(rowtoAdd);
    }

    /** Read the contents of the file NAME.db, and return as a Table.
     *  Format errors in the .db file cause a DBException. */
    static Table readTable(String name) {
        BufferedReader input;
        Table table;
        input = null;
        try {
            input = new BufferedReader(new FileReader(name + ".db"));
            String header = input.readLine();
            if (header == null) {
                throw error("missing header in DB file");
            }
            String[] columnNames = header.split(",");
            table = new Table(columnNames);
            String item;
            while ((item = input.readLine()) != null) {
                String[] info = item.split((","));

                if (info.length != columnNames.length) {
                    throw error("lengths of data and _rowsize do not match");
                }
                table.add(info);
            }
        } catch (FileNotFoundException e) {
            throw error("could not find %s.db", name);
        } catch (IOException e) {
            throw error("problem reading from %s.db", name);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    throw error("the input is not null");
                }
            }
        }
        return table;
    }

    /** Write the contents of TABLE into the file NAME.db. Any I/O errors
     *  cause a DBException. */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            output = new PrintStream(name + ".db");
            if (_size == 0) {
                throw error("no rows have been added to _columns");
            }
            for (int k = 0; k < _titles.length; k += 1) {
                if (k != _titles.length - 1) {
                    output.print(getTitle(k) + ",");
                } else {
                    output.print(getTitle(k));
                }

            }
            output.println();
            for (int i = 0; i < _size; i +=1) {
                for (int r = 0; r < _rowSize; r += 1) {
                    if (r != _rowSize - 1) {
                        output.print((_columns[r].get(i) + ", "));
                    } else {
                        output.print(_columns[r].get(i));
                    }
                }
                output.println();
            }

        } catch (IOException e) {
            throw error("trouble writing to %s.db", name);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /** Print my contents on the standard output, separated by spaces
     *  and indented by two spaces. */
    void print() {

        int[] order = new int[_size];
        for (int i = 0; i < _size; i++) {
            int orderVals = 0;
            for (int r = 0; r < _size; r++) {
                int value = compareRows(i, r);
                if (value <= 0) {
                    orderVals += 0;
                } else {
                    orderVals += 1;
                }

            }
            order[orderVals] = i;
        }
        for (int i = 0; i < _size; i += 1) {
            int rowNum = order[i];
            String space = " ";
            for (int r = 0; r < _rowSize; r += 1) {
                space = space + " " + this.get(rowNum, r);

            }
            System.out.println(space);
        }


    }

    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table resultTable = new Table(columnNames);
        List<Column> colIndexes = new ArrayList<>();

        for (String s: columnNames) {
            Column col = new Column(s, this);
            colIndexes.add(col);
        }
        ValueList originalTab = _columns[findColumn((columnNames.get(0)))];
        for (int i = 0; i < originalTab.size(); i += 1) {
            if (conditions == null || Condition.test(conditions, i)) {
                resultTable.add(colIndexes, i);
            }
        }
        return resultTable;
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected
     *  from pairs of rows from this table and from TABLE2 that match
     *  on all columns with identical names and satisfy CONDITIONS. */
    Table select(Table table2, List<String> columnNames,
                 List<Condition> conditions) {
        List<Column> col = new ArrayList<>();
        Table result = new Table(columnNames);

        for (int i = 0; i < columnNames.size(); i += 1) {
            col.add(new Column(columnNames.get(i), this, table2));
        }


        List<Column> common1 = new ArrayList<>();
        List<Column> common2 = new ArrayList<>();
        for (int k = 0; k < columns(); k += 1) {
            String s = this.getTitle(k);
            if (table2.findColumn(s) != -1) {
                Column col1 = new Column(s, this);
                common1.add(col1);
                Column col2 = new Column(s, table2);
                common2.add(col2);
            }
        }


        for (int i = 0; i < _size; i += 1) {
            for (int r = 0; r < table2.size(); r += 1) {
                if (conditions == null) {
                    result.add(common1, i, r);
                } else {
                    if (equijoin(common1, common2, i, r)) {
                        if (Condition.test(conditions, i, r)) {
                            result.add(col, i, r);
                        }
                    }
                }
            }
        }

        return result;
    }


    /** Return <0, 0, or >0 depending on whether the row formed from
     *  the elements _columns[0].get(K0), _columns[1].get(K0), ...
     *  is less than, equal to, or greater than that formed from elememts
     *  _columns[0].get(K1), _columns[1].get(K1), ....  This method ignores
     *  the _index. */
    private int compareRows(int k0, int k1) {
        for (int i = 0; i < _columns.length; i += 1) {
            int c = _columns[i].get(k0).compareTo(_columns[i].get(k1));
            if (c != 0) {
                return c;
            }
        }
        return 0;
    }

    /** Return true if the columns COMMON1 from ROW1 and COMMON2 from
     *  ROW2 all have identical values.  Assumes that COMMON1 and
     *  COMMON2 have the same number of elements and the same names,
     *  that the columns in COMMON1 apply to this table, those in
     *  COMMON2 to another, and that ROW1 and ROW2 are indices, respectively,
     *  into those tables. */
    private static boolean equijoin(List<Column> common1, List<Column> common2,
                                    int row1, int row2) {
        ArrayList<String> compare1 = new ArrayList<>();
        ArrayList<String> compare2 = new ArrayList<>();
        for (int i = 0; i < common1.size(); i += 1){
            Column col1 = common1.get(i);
            compare1.add(col1.getFrom(row1));
            Column col2 = common2.get(i);
            compare2.add(col2.getFrom(row2));
        }
        for (int i = 0; i < common2.size(); i += 1) {
            if (!compare1.get(i).equals(compare2.get(i))) {
                return false;
            }
        }
        return true;
    }

    /** A class that is essentially ArrayList<String>.  For technical reasons,
     *  we need to encapsulate ArrayList<String> like this because the
     *  underlying design of Java does not properly distinguish between
     *  different kinds of ArrayList at runtime (e.g., if you have a
     *  variable of type Object that was created from an ArrayList, there is
     *  no way to determine in general whether it is an ArrayList<String>,
     *  ArrayList<Integer>, or ArrayList<Object>).  This leads to annoying
     *  compiler warnings.  The trick of defining a new type avoids this
     *  issue. */
    private static class ValueList extends ArrayList<String> {
    }

    /** My column titles. */
    private final String[] _titles;
    /** My columns. Row i consists of _columns[k].get(i) for all k. */
    private final ValueList[] _columns;

    /** Rows in the database are supposed to be sorted. To do so, we
     *  have a list whose kth element is the index in each column
     *  of the value of that column for the kth row in lexicographic order.
     *  That is, the first row (smallest in lexicographic order)
     *  is at position _index.get(0) in _columns[0], _columns[1], ...
     *  and the kth row in lexicographic order in at position _index.get(k).
     *  When a new row is inserted, insert its index at the appropriate
     *  place in this list.
     *  (Alternatively, we could simply keep each column in the proper order
     *  so that we would not need _index.  But that would mean that inserting
     *  a new row would require rearranging _rowSize lists (each list in
     *  _columns) rather than just one. */
    private final ArrayList<Integer> _index = new ArrayList<>();

    /** My number of rows (redundant, but convenient). */
    private int _size;
    /** My number of columns (redundant, but convenient). */
    private final int _rowSize;

}
