import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String U = "jdbc:mysql://localhost:3306/meal1";
    private static final String UN = "root";
    private static final String PW = "1288";

    public static Connection getconnection() throws SQLException {
        return DriverManager.getConnection(U, UN, PW);
    }


}
