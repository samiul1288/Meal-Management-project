import java.sql.*;

public class MonthlySummary {

    public static void s(int uid){
        int m=MealCount.getTotalMeals(uid);
        double d=MealCash.gTD(uid);
        double b=Bazar.getTotalBazar();          // total bazar
        int lim=Bazar.getTotalMealsLimit();          // total meal limit

        double r=(lim > 0)?b/lim:0;
        double c=r*m;

        double fixedShare=getFixedShare();
        double bal=d-c-fixedShare;

        System.out.println("Meals: "+m);
        System.out.println("Deposit: "+d);
        System.out.println("Rate: " +String.format("%.2f",r));
        System.out.println("Cost: " + String.format("%.2f",c));
        System.out.println("Fixed Bill: "+String.format("%.2f",fixedShare));
        System.out.println("Balance: "+String.format("%.2f",bal));

        if (lim<gTMAll()) {
            System.out.println(" Limit < total meals! Please add new bazar.");
        }
    }

    public static void sAll() {
        try (Connection con = Database.getconnection()) {
            double b = Bazar.getTotalBazar();   // totalBazar
            int lim = Bazar.getTotalMealsLimit();   // totalMealLimit
            double r = lim > 0 ? b / lim : 0; // mealRate

            double fixedShare = getFixedShare(); // new

            String q = "select users.username, users.id, " +
                    "COALESCE(sum(meals.lunch + meals.dinner), 0) AS total_meals, " +
                    "COALESCE((select sum(amount)  from deposits where deposits.user_id = users.id), 0) as total_deposit " +
                    "from users left join meals on users.id = meals.user_id " +
                    "group by users.id";

            PreparedStatement pst=con.prepareStatement(q);
            ResultSet rs=pst.executeQuery();

            System.out.println("------- Summary -------");
            System.out.printf("%-15s %-10s %-10s %-10s %-10s%n","User","Meals","Deposit", "Cost", "Balance");

            while (rs.next()) {
                String n=rs.getString("username");
                int m=rs.getInt("total_meals");
                double d=rs.getDouble("total_deposit");
                double c=r*m;
                double balance=d-c-fixedShare;

                System.out.printf("%-15s %-10d %-9.2f %-9.2f %-9.2f%n", n, m, d, c, balance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int gTMAll() {
        int t=0;
        try (Connection con=Database.getconnection()){
            String q="select sum(lunch+dinner) as total from meals";
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery(q);
            if (rs.next()){
                t=rs.getInt("total");
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return t;
    }
    public static double getFixedShare() {
        double totalFixed=0;
        int totalUsers=1;

        try (Connection con=Database.getconnection()){
            String q1="select sum(amount) AS total from fixed_bills where month(month_tag)=month(CURDATE()) and year(month_tag)=year(CURDATE())";
            String q2="select count(*) as count from users";
            Statement st=con.createStatement();
            ResultSet rs1=st.executeQuery(q1);
            if (rs1.next()){
                totalFixed=rs1.getDouble("total");
            }
            ResultSet rs2=st.executeQuery(q2);
            if (rs2.next()){
                totalUsers=rs2.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalUsers>0?totalFixed/totalUsers:0;
    }
}
