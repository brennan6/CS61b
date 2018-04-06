package db61b;

import java.util.HashMap;

/** A collection of Tables, indexed by name.
 *  @author Matthew Brennan */
class Database {
    /** A new Database as a collection of tables */
    private HashMap<String, Table> _tableNames;
    /** An empty database. */
    public Database() {
        _tableNames = new HashMap<String, Table>();
    }

    /** Return the Table whose name is NAME stored in this database, or null
     *  if there is no such table. */
    public Table get(String name) {
        for (String key: _tableNames.keySet()) {
            if (key.equals(name)) {
                return _tableNames.get(key);
            }
        }
        return null;
    }

    /** Set or replace the table named NAME in THIS to TABLE.  TABLE and
     *  NAME must not be null, and NAME must be a valid name for a table. */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        _tableNames.put(name, table);
    }


}
