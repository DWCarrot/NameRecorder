package cat.nyaa.namerecorder;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerNameDatabase {

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS \"player_name\" (\n" +
            "\t\"index\"\tINTEGER NOT NULL UNIQUE,\n" +
            "\t\"uuid\"\tBLOB NOT NULL,\n" +
            "\t\"name\"\tTEXT NOT NULL,\n" +
            "\t\"changedToAt\"\tINTEGER NOT NULL DEFAULT 0,\n" +
            "\t\"source\"\tINTEGER NOT NULL DEFAULT 0,\n" +
            "\tPRIMARY KEY(\"index\" AUTOINCREMENT)\n" +
            ");";

    public static final String SQL_QUERY = "SELECT\n" +
            "\"uuid\",\n" +
            "\"name\",\n" +
            "max(\"changedToAt\") AS \"changedToAt\",\n" +
            "\"source\"\n" +
            "FROM \"player_name\"\n" +
            "WHERE \"uuid\" == ?";

    public static final String[] SQL_QUERY_IND1 = { "uuid", "name", "changedToAt", "source" };

    public static final String SQL_INSERT = "INSERT INTO \"player_name\" \n" +
            "(\"uuid\",\"name\",\"changedToAt\",\"source\")\n" +
            "VALUES (?,?,?,?)";

    public static final int[] SQL_INSERT_IND1 = { 1, 2, 3, 4 };

    private final Connection connection;

    public PlayerNameDatabase(String url) throws SQLException {
        this.connection = DriverManager.getConnection(url);
        this.connection.setAutoCommit(false);
        this.createTable();
    }

    public PlayerNameDatabase(String url, Properties info) throws SQLException {
        this.connection = DriverManager.getConnection(url, info);
        this.connection.setAutoCommit(false);
        this.createTable();
    }

    public void createTable() throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(SQL_CREATE);
        preparedStatement.execute();
        this.connection.commit();
    }

    public void update(PlayerNameRecord record, Logger logger) throws SQLException {
        this.updateWithoutCommit(record, logger);
        this.connection.commit();
    }

    public void updateBatch(Collection<PlayerNameRecord> records, Logger logger) throws SQLException, MultiSQLException {
        List<SQLException> exceptionList = new ArrayList<>(records.size());
        for(PlayerNameRecord record : records) {
            try {
                this.updateWithoutCommit(record, logger);
            } catch (SQLException e) {
                exceptionList.add(e);
            }
        }
        this.connection.commit();
        if(!exceptionList.isEmpty()) {
            throw new MultiSQLException(exceptionList);
        }
    }

    private void updateWithoutCommit(PlayerNameRecord record, Logger logger) throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(SQL_QUERY);
        record.serializeUUID(preparedStatement, 1);
        PlayerNameRecord r = null;
        try(ResultSet resultSet = preparedStatement.executeQuery()) {
            if(resultSet.next() && resultSet.getString("uuid") != null) {
                r = new PlayerNameRecord(resultSet, SQL_QUERY_IND1);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "unexpected", e);
        }
        if(r != null) {
            if((!r.name.equals(record.name)) && (r.changedToAt == null || r.changedToAt.isBefore(record.changedToAt))) {
                preparedStatement = this.connection.prepareStatement(SQL_INSERT);
                record.serialize(preparedStatement, SQL_INSERT_IND1);
                preparedStatement.execute();
                logger.log(Level.CONFIG, "update record: " + record.uuid.toString());
            }
        } else {
            preparedStatement = this.connection.prepareStatement(SQL_INSERT);
            record.serialize(preparedStatement, SQL_INSERT_IND1);
            preparedStatement.execute();
            logger.log(Level.CONFIG, "new record: " + record.uuid.toString());
        }
    }

    public static class MultiSQLException extends Exception {

        public final List<SQLException> exceptions;

        public MultiSQLException(List<SQLException> exceptions) {
            super();
            this.exceptions = exceptions;
        }
    }
}
