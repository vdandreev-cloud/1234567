package by.psu.db;

import by.psu.model.Excursion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcHelper {
    private final Connection conn;

    public JdbcHelper(Connection conn) {
        this.conn = conn;
    }

    public Excursion findExcursionById(Integer id) throws SQLException {
        var sql = "SELECT * FROM public.excursion WHERE id = ?";
        try (var statement = conn.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    return mapToExcursion(result);
                }
                return null;
            }
        }
    }

    public List<Excursion> findAllExcursions() throws SQLException {
        var sql = "SELECT * FROM public.excursion";
        var list = new ArrayList<Excursion>();
        try (var statement = conn.createStatement();
             var result = statement.executeQuery(sql)) {
            while (result.next()) {
                list.add(mapToExcursion(result));
            }
        }
        return list;
    }

    public void saveExcursion(Excursion excursion) throws SQLException {
        if (excursion.getId() == null) {
            var id = insertExcursion(excursion);
            excursion.setId(id);
        } else {
            updateExcursion(excursion);
        }
    }

    private Integer insertExcursion(Excursion excursion) throws SQLException {
        var sql = """
                insert into public.excursion (name, price, "from", "to", guide_name, excursion_type, lunch_included)
                values (?, ?, ?, ?, ?, ?, ?)""";
        try (var statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setExcursionParams(statement, excursion);
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void updateExcursion(Excursion excursion) throws SQLException {
        var sql = """
                update public.excursion set 
                    name = ?,
                    price = ?,
                    "from" = ?,
                    "to" = ?,
                    guide_name = ?,
                    excursion_type = ?,
                    lunch_included = ?
                where id = ?""";
        try (var statement = conn.prepareStatement(sql)) {
            setExcursionParams(statement, excursion);
            statement.setInt(8, excursion.getId());
            statement.executeUpdate();
        }
    }

    private void setExcursionParams(PreparedStatement statement, Excursion excursion) throws SQLException {
        statement.setString(1, excursion.getName());
        statement.setBigDecimal(2, excursion.getPrice());
        statement.setObject(3, excursion.getFrom());
        statement.setObject(4, excursion.getTo());
        statement.setString(5, excursion.getGuideName());
        statement.setString(6, excursion.getExcursionType());
        statement.setBoolean(7, excursion.isLunchIncluded());
    }

    private Excursion mapToExcursion(ResultSet rs) throws SQLException {
        return new Excursion(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getBigDecimal("price"),
                rs.getObject("from", LocalDate.class),
                rs.getObject("to", LocalDate.class),
                rs.getString("guide_name"),
                rs.getString("excursion_type"),
                rs.getBoolean("lunch_included"));
    }
}