package bot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

    // Retrieve database name and password from environment variables
    private static final String DB_URL = System.getenv("REMINDME_DB_URL");
    private static final String USER = System.getenv("REMINDME_DB_USER");
    private static final String PASSWORD = System.getenv("REMINDME_DB_PW");


    private static HikariConfig config = new HikariConfig();

    private static HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv("REMINDME_DB_URL"));
        config.setUsername(System.getenv("REMINDME_DB_USER"));
        config.setPassword(System.getenv("REMINDME_DB_PW"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(300000);
        config.setConnectionTimeout(120000);
        config.setLeakDetectionThreshold(300000);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
