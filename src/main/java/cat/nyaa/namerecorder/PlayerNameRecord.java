package cat.nyaa.namerecorder;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class PlayerNameRecord {

    public final UUID uuid; // NonNull

    public final String name; // NonNull

    public final Instant changedToAt; // NonNull for input; Nullable for database

    public final int source; // =1

    public PlayerNameRecord(UUID uuid, String name, Instant changedToAt, int source) {
        this.uuid = uuid;
        this.name = name;
        this.changedToAt = changedToAt;
        this.source = source;
    }

    public PlayerNameRecord(ResultSet r, String[] columnNames) throws SQLException {
        assert columnNames.length >= 4;
        this.uuid = PlayerNameRecord.fromBytes(r.getBytes(columnNames[0]));
        this.name = r.getString(columnNames[1]);
        long t = r.getLong(columnNames[2]);
        this.changedToAt = t == 0 ? null : Instant.ofEpochMilli(t);
        this.source = r.getInt(columnNames[3]);
    }

    public void serialize(PreparedStatement s, int[] parameterIndices) throws SQLException {
        assert parameterIndices.length >= 4;
        s.setBytes(parameterIndices[0], PlayerNameRecord.asBytes(this.uuid));
        s.setString(parameterIndices[1], this.name);
        s.setLong(parameterIndices[2], this.changedToAt.toEpochMilli());
        s.setInt(parameterIndices[3], this.source);
    }

    public void serializeUUID(PreparedStatement s, int parameterIndex) throws SQLException {
        s.setBytes(parameterIndex, PlayerNameRecord.asBytes(this.uuid));
    }

    public void serializeName(PreparedStatement s, int parameterIndex) throws SQLException {
        s.setString(parameterIndex, this.name);
    }

    public void serializeChangedToAt(PreparedStatement s, int parameterIndex) throws SQLException {
        s.setLong(parameterIndex, this.changedToAt.toEpochMilli());
    }

    public void serializeSource(PreparedStatement s, int parameterIndex) throws SQLException {
        s.setInt(parameterIndex, this.source);
    }

    public static byte[] asBytes(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] data = new byte[16];
        for(int i = 15; i >= 8; i--) {
            data[i] = (byte) (lsb & 0xff);
            lsb = lsb >> 8;
        }
        for(int i = 7; i >= 0; i--) {
            data[i] = (byte) (msb & 0xff);
            msb = msb >> 8;
        }
        return data;
    }

    public static UUID fromBytes(byte[] data) {
        long msb = 0;
        long lsb = 0;
        assert data.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (data[i] & 0xff);
        }
        return new UUID(msb, lsb);
    }
}
