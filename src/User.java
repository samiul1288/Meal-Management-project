import java.sql.*;
import java.util.Scanner;

public class User {

    public static int login(Scanner sc) {

        sc.nextLine();

        System.out.print("Username: ");
        String un = sc.nextLine().trim();

        System.out.print("Password: ");
        String pw = sc.nextLine().trim();

        if (un.isEmpty() || pw.isEmpty()) {
            System.out.println("Invalid Input");
            return -1;
        }

        String sql="select id, isAdmin from users where  username = ? AND password = ?";

        try (Connection con=Database.getconnection();
             PreparedStatement pst=con.prepareStatement(sql)) {

            pst.setString(1, un);
            pst.setString(2, pw);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int uid=rs.getInt("id");
                    int isAdmin=rs.getInt("isAdmin");
                    System.out.println("Login successful.UID="+uid+",isAdmin="+isAdmin);
                    return uid;
                } else {
                    System.out.println("Invalid username or password.");
                    return -1;
                }
            }
        } catch (SQLException e) {
            System.out.println("Login failed, please try again.");
            e.printStackTrace();
            return -1;
        }
    }


    public static void r(Scanner sc) {
        System.out.print("New Username: ");
        String un=sc.next();
        System.out.print("Password: ");
        String pw=sc.next();
        System.out.print("Is Admin? (true/false): ");
        boolean a=sc.nextBoolean();

        try (Connection con=Database.getconnection()) {
            String q="insert into users (username, password, isAdmin) values (?, ?, ?)";
            PreparedStatement pst=con.prepareStatement(q);
            pst.setString(1, un);
            pst.setString(2, pw);
            pst.executeUpdate();
            System.out.println("✅ User registered.");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean a(int uid)
    {
        try (Connection con = Database.getconnection()) {
            String q="select isAdmin from users where id = ?";
            PreparedStatement pst=con.prepareStatement(q);
            pst.setInt(1, uid);
            ResultSet rs=pst.executeQuery();
            if (rs.next())
            {
                return rs.getInt("isAdmin") == 1;
            }
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
