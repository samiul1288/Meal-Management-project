import java.sql.*;
import java.util.Scanner;
public class Bazar {

    public static void addBazar(Scanner sc, int uid) {
        System.out.print("Bazar Date (YYYY-MM-DD): ");
        String dt = sc.next();
        System.out.print("Amount: ");
        double amt = sc.nextDouble();
        System.out.print("How many meals this bazar supports: ");
        int lim = sc.nextInt();

        double allDeposit = MealCash.gTDAll();
        double totalBazar = Bazar.getTotalBazar();
        double newTotalBazar = totalBazar + amt;

        if (newTotalBazar > allDeposit) {
            System.out.println(" Total Bazar exceeds total deposit from all users!");
            System.out.printf("All deposits: ৳%.2f | Bazar total after this: ৳%.2f%n", allDeposit, newTotalBazar);
            return;
        }

        try (Connection con = Database.getconnection()) {
            String q = "insert into bazar (user_id, amount, bazar_date, meal_limit) values (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(q);
            pst.setInt(1, uid);
            pst.setDouble(2, amt);
            pst.setString(3, dt);
            pst.setInt(4, lim);
            pst.executeUpdate();
            System.out.println("✅ Bazar added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static double getTotalBazar() {
        double tot = 0;
        try (Connection con = Database.getconnection()) {
            String q = "select sum(amount) as total from bazar";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(q);
            if (rs.next()) {
                tot = rs.getDouble("total"); // total
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tot;
    }

    public static int getTotalMealsLimit() {
        int tot = 0;
        try (Connection con = Database.getconnection()) {
            String q = "select sum(meal_limit) as total from bazar";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(q);
            if (rs.next()) {
                tot = rs.getInt("total"); // total
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tot;
    }
}




