import java.sql.*;
import java.util.Scanner;
public class MealCash {
    public static void dFU(Scanner sc) {
        System.out.print("User ID: ");
        int uid = sc.nextInt();
        sc.nextLine();
        d(sc, uid);
    }
    public static void d(Scanner sc, int uid) {
        System.out.print("Amount: ");
        double amt = sc.nextDouble();

        System.out.print("Deposit Date (YYYY-MM-DD): ");
        String dateStr=sc.next();
        try (Connection con=Database.getconnection()) {
            String q="insert into deposits(user_id, amount, deposit_date) values(?, ?, ?)";
            PreparedStatement pst=con.prepareStatement(q);
            pst.setInt(1, uid);
            pst.setDouble(2, amt);
            pst.setDate(3, java.sql.Date.valueOf(dateStr));
            pst.executeUpdate();
            System.out.println("✅ Deposit added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void EachUserTotalDeposite() {
        try (Connection con=Database.getconnection()) {
            String q="select users.username, coalesce(sum(deposits.amount), 0) as total from users left join deposits on users.id = deposits.user_id group by users.id, users.username";
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery(q);
            System.out.println("-- All Deposits --");
            while (rs.next()) {
                String name=rs.getString("username");
                double total=rs.getDouble("total");
                System.out.println(name + " => ৳" + total);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public static double gTD(int uid){
        double total=0;
        try (Connection con=Database.getconnection()) {
            String q="select sum(amount) as total from deposits where user_id=?";
            PreparedStatement pst=con.prepareStatement(q);
            pst.setInt(1,uid);
            ResultSet rs=pst.executeQuery();
            if (rs.next()) {
                total=rs.getDouble("total");
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return total;
    }

    public static double gTDAll(){
        double total=0;
        try (Connection con=Database.getconnection()) {
            String q="select sum(amount) as total from deposits";
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery(q);
            if (rs.next()){
                total=rs.getDouble("total");
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return total;
    }

    public static boolean isFixedBillSetForThisMonth(){
        boolean exists=false;
        try (Connection con=Database.getconnection()){
            String q="select count(*) as total from fixed_bills where month(month_tag)=month(curdate()) and year(month_tag)=year(curdate())";
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery(q);
            if (rs.next()){
                exists=rs.getInt("total") > 0;
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return exists;
    }

    public static double getMealRate(){
        double totalBazar=0;
        int totalMealLimit=0;

        try (Connection con=Database.getconnection()){
            String q1="select sum(amount) as total from bazar";
            String q2="select sum(meal_limit) as limit_total from bazar";

            Statement st=con.createStatement();

            ResultSet rs1=st.executeQuery(q1);
            if (rs1.next()){
                totalBazar=rs1.getDouble("total");
            }

            ResultSet rs2=st.executeQuery(q2);
            if (rs2.next()){
                totalMealLimit=rs2.getInt("limit_total");
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        if (totalMealLimit==0) return 0.0;
        return totalBazar/totalMealLimit;
    }

    public static double getBalance(int uid){
        double deposit=gTD(uid);
        int mealsTaken=MealCount.getTotalMeals(uid);
        double mealRate=getMealRate();
        return deposit-(mealsTaken*mealRate);
    }
}
