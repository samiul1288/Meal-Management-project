import java.util.InputMismatchException;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.YearMonth;
public class Main {

    private static boolean isMonthEnd() {
        LocalDate d = LocalDate.now();
        return d.getDayOfMonth() == YearMonth.from(d).lengthOfMonth();
    }

    private static void printMealCloseIfMonthEnd() {
        if (isMonthEnd()) {
            System.out.println("\n====== Meal Close: Month end today. ======");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        printMealCloseIfMonthEnd();
        while (true) {
            System.out.println("\n--- Meal Panel ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            int ch;

            try {
                ch = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input, please enter a number.");
                sc.nextLine();
                continue;
            }

            switch (ch) {
                case 1:
                    User.r(sc);
                    break;
                case 2:
                    int uid=User.login(sc);
                    if (uid!=-1) {
                        boolean isAdmin = User.a(uid);


                        if (!MealCash.isFixedBillSetForThisMonth()) {
                            if (!isAdmin) {
                                System.out.println(" Fixed bills have not been added for this month.");
                                System.out.println(" Please contact admin to add them.");
                                break;
                            } else {
                                System.out.println(" Warning (Admin): No fixed bills found for this month.");
                                System.out.println(" Please add them as soon as possible.");
                            }
                        }



                        sm(sc, uid);
                    }
                    break;
                case 0:
                    System.out.println("Exit... Bye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }

    public static void sm(Scanner sc, int uid){
        boolean a = User.a(uid);

        while (true){
            printMealCloseIfMonthEnd();
            System.out.println("\n--- Menu ---");
            if (a) {
                System.out.println("1. Meal (any user)");
                System.out.println("2. Deposit (any user)");
                System.out.println("3. Add Bazar");
                System.out.println("7. Entry Meal for Multiple User");
            } else {
                System.out.println("1. Meal (self)");
                System.out.println("2. Deposit (self)");
            }
            System.out.println("4. My Summary");
            System.out.println("5. All Summary");
            System.out.println("6. All Deposits");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            int c;

            try {
                c = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input, please enter a number.");
                sc.nextLine();
                continue;
            }

            if (!a && (c == 1 || c == 2 || c == 3 || c == 7)) {
                System.out.println(" Not authorized.");
                continue;
            }

            switch (c) {
                case 1:
                    if (a) {
                        MealCount.getTotalMealLimit(sc);
                    } else {
                        MealCount.addMeal(sc, uid);
                    }
                    break;
                case 2:
                    if (a) {
                        MealCash.dFU(sc);
                    } else {
                        MealCash.d(sc, uid);
                    }
                    break;
                case 3:
                    Bazar.addBazar(sc, uid);
                    break;
                case 4:
                    MonthlySummary.s(uid);
                    break;
                case 5:
                    MonthlySummary.sAll();
                    break;
                case 6:
                    MealCash.EachUserTotalDeposite();
                    break;
                case 7:
                    MealCount.addMealsForMultipleUsers(sc);
                    break;
                case 0:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid choice, try again.");
            }
        }
    }
}
