import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class MealCount {


    public static void addMeal(Scanner sc, int userId) {
        int totalMealLimit = Bazar.getTotalMealsLimit();
        int totalMealsTaken = getAllUserMealCount();

        java.sql.Date mealDate = readDate(sc, "Date (YYYY-MM-DD): ");
        int lunch  = readBit(sc, "Lunch (1/0): ");
        int dinner = readBit(sc, "Dinner (1/0): ");

        int delta = lunch + dinner;
        if (totalMealsTaken + delta > totalMealLimit) {
            System.out.println("❌ Bazar meal limit exceeded! Add new bazar before adding meals.");
            return;
        }

        String sql = "insert into meals (user_id, meal_date, lunch, dinner) values (?, ?, ?, ?)";

        try (Connection con = Database.getconnection();
             PreparedStatement pst = con.prepareStatement(sql)) {


            if (!userExists(con, userId)) {
                System.out.println("⚠️ User ID " + userId + " not found.");
                return;
            }

            pst.setInt(1, userId);
            pst.setDate(2, mealDate);
            pst.setInt(3, lunch);
            pst.setInt(4, dinner);
            pst.executeUpdate();
            System.out.println("✅ Meal added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static int getTotalMeals(int userId) {
        String sql = "select coalesce(sum(lunch + dinner), 0) as total from meals where  user_id = ?";
        try (Connection con = Database.getconnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static int getAllUserMealCount() {
        String sql = "select coalesce(sum(lunch + dinner), 0)  as total from meals";
        try (Connection con = Database.getconnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("total");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static void addMealForUser(Scanner sc) {
        System.out.print("Enter user ID to add meal for: ");
        int userId = sc.nextInt();
        addMeal(sc, userId);
    }


    public static void addMealsForMultipleUsers(Scanner sc) {
        java.sql.Date mealDate=readDate(sc, "Meal Date (YYYY-MM-DD): ");
        System.out.println("Choose mode: 1) Select N users  2) All users (ask per user)");
        int mode = readInt(sc, "Choice: ");

        String upsertSql=
                "insert into meals (user_id, meal_date, lunch, dinner) " +
                        "values (?, ?, ?, ?) " +
                        "on duplicate key update lunch=values(lunch), dinner=values(dinner)";

        int totalMealLimit = Bazar.getTotalMealsLimit();
        int totalMealsTaken = getAllUserMealCount();

        try (Connection con = Database.getconnection();
             Statement st = con.createStatement();
             PreparedStatement pst = con.prepareStatement(upsertSql)) {

            con.setAutoCommit(false);

            if (mode == 1) {
                int count = readInt(sc, "Number of users: ");

                String existsSql = "select 1 from users where id = ?";
                try (PreparedStatement chk = con.prepareStatement(existsSql)) {
                    for (int i = 0; i < count; i++) {
                        int uid = readInt(sc, "User ID: ");

                        // FK safe
                        chk.setInt(1, uid);
                        try (ResultSet rs = chk.executeQuery()) {
                            if (!rs.next()) {
                                System.out.println("User ID " + uid + " not found. Skipping.");
                                continue;
                            }
                        }

                        int lunch  = readBit(sc, "  Lunch (1/0): ");
                        int dinner = readBit(sc, "  Dinner (1/0): ");
                        int delta = lunch + dinner;

                        if (totalMealsTaken + delta > totalMealLimit) {
                            System.out.println("Bazar meal limit exceeded! Stopping further inserts.");
                            break;
                        }

                        pst.setInt(1, uid);
                        pst.setDate(2, mealDate);
                        pst.setInt(3, lunch);
                        pst.setInt(4, dinner);
                        pst.addBatch();

                        totalMealsTaken += delta; // limit tracking

                        if((i+1)%500==0) pst.executeBatch();
                    }
                }

            } else if (mode == 2) {

                String fetchUsers = "select id, username from users";

                try (ResultSet rs = st.executeQuery(fetchUsers)) {
                    while (rs.next()) {
                        int uid = rs.getInt("id");
                        String uname = rs.getString("username");

                        System.out.println("User ID: " + uid + " (" + uname + ")");
                        int lunch =readBit(sc, "  Lunch (1/0): ");
                        int dinner=readBit(sc, "  Dinner (1/0): ");
                        int delta=lunch+dinner;

                        if (totalMealsTaken+delta>totalMealLimit) {
                            System.out.println(" Bazar meal limit exceeded! Stopping further inserts.");
                            break;
                        }

                        pst.setInt(1, uid);
                        pst.setDate(2, mealDate);
                        pst.setInt(3, lunch);
                        pst.setInt(4, dinner);
                        pst.addBatch();

                        totalMealsTaken+=delta;
                    }
                }
            } else {
                System.out.println("Invalid option.");
                return;
            }

            pst.executeBatch();
            con.commit();
            System.out.println("✅ Meals saved/updated!");

        } catch (BatchUpdateException be) {
            System.out.println("Batch failed (likely FK).");
            be.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Operation failed.");
            e.printStackTrace();
        }
    }

    public static void getTotalMealLimit(Scanner sc) {
        int totalMealLimit=Bazar.getTotalMealsLimit();
        int totalMealsTaken=getAllUserMealCount();
        System.out.println("Meal limit : " + totalMealLimit);
        System.out.println("Taken      : " + totalMealsTaken);
        System.out.println("Remaining  : " + Math.max(0, totalMealLimit - totalMealsTaken));
    }



    private static boolean userExists(Connection con, int uid) throws SQLException {
        String q = "SELECT 1 FROM users WHERE id = ?";
        try (PreparedStatement pst = con.prepareStatement(q)) {
            pst.setInt(1, uid);
            try (ResultSet rs=pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static java.sql.Date readDate(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s=sc.next();
            try {
                return java.sql.Date.valueOf(LocalDate.parse(s));
            } catch (Exception ex) {
                System.out.println("Please enter a valid date (YYYY-MM-DD).");
            }
        }
    }

    private static int readBit(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextInt()) {
                int v=sc.nextInt();
                if (v==0||v==1) return v;
                System.out.println("Please enter 0 or 1.");
            } else {
                System.out.println("Please enter a number (0 or 1).");
                sc.next();
            }
        }
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (sc.hasNextInt()) return sc.nextInt();
            System.out.println("Please enter a valid number.");
            sc.next();
        }
    }
}
