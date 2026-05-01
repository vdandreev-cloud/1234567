package by.psu;

import by.psu.db.ConnectionManager;
import by.psu.db.JdbcHelper;
import by.psu.model.Excursion;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        try (var connectionManager = new ConnectionManager()) {
            var connection = connectionManager.getConnection();
            var metadata = connection.getMetaData();
            var infoString = "Database: " + metadata.getDatabaseProductName()
                    + "\nversion: " + metadata.getDatabaseMajorVersion() + '.' + metadata.getDatabaseMinorVersion();
            System.out.println(infoString);

            connection.setAutoCommit(false);

            var jdbcHelper = new JdbcHelper(connection);
            
            var excursion = jdbcHelper.findExcursionById(1);
            System.out.println(excursion);

            var newExcursion = new Excursion(null, "travel", new BigDecimal("123.45"),
                    LocalDate.now(), LocalDate.now().plusDays(2L), "F.E. Cgan",
                    "Auto", true);
            jdbcHelper.saveExcursion(newExcursion);

            newExcursion.setLunchIncluded(false);
            jdbcHelper.saveExcursion(newExcursion);

            var list = jdbcHelper.findAllExcursions();
            System.out.println(list);

            connection.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}