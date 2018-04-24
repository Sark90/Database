package testing;

import java.sql.*;
import java.util.Scanner;

public class Test {
    private static String driver = "org.postgresql.Driver";
    private static String url = "jdbc:postgresql://localhost:5432/TESTING";
    private static String login = "postgres";
    private static String password = "admin";
    private Connection con;
    private Statement stmt;
    private int studentID;

    public void run() {
        connect();
        authorize();
        runTest();
        close();
    }

    private void runTest() {
        System.out.println("Темы для тестов:");
        try {
            Scanner scanner = new Scanner(System.in);
            ResultSet rsThemes = stmt.executeQuery("SELECT * FROM themes");
            while (rsThemes.next()) {
                System.out.println(rsThemes.getString("id") + ". " +
                        rsThemes.getString("theme"));
            }
            rsThemes.close();
            System.out.print("Выберите тему: ");
            int themeID = scanner.nextInt();
            ResultSet rsQuestions = stmt.executeQuery("SELECT * FROM questions where theme_id=" +
                    themeID);
            while (rsQuestions.next()) {
                Statement s1 = con.createStatement();
                ResultSet rsAnswers = s1/*stmt*/.executeQuery("SELECT * FROM answers where question_id=" +
                        rsQuestions.getString("ID"));
                System.out.println(rsQuestions.getString("question"));
                while (rsAnswers.next()) {
                    System.out.println(rsAnswers.getString("variant") + ". " +
                            rsAnswers.getString("answer"));
                }
                System.out.print("Выберите вариант ответа (A/B/C): ");
                char variant = scanner.next().charAt(0);
                Statement s2 = con.createStatement();
                ResultSet rsUserAnswer = /*stmt*/s2.executeQuery("SELECT * FROM answers where question_id=" +
                        rsQuestions.getString("ID") + " and variant=\'" + variant + "\';");
                rsUserAnswer.next();
                if (rsUserAnswer.getBoolean("is_right")) System.out.println("Верно.");
                else System.out.println("Неверно.");
                stmt.execute("insert into TESTS (student_id, theme_id, question_id, answer_id)" +
                        " values (" + studentID + ", " + themeID + ", " +
                        rsQuestions.getInt("ID") + ", " + rsUserAnswer.getInt("ID") +
                        ");");
                rsUserAnswer.close();
                rsAnswers.close();
            }
            rsQuestions.close();    //TODO: all ResultSets close in close()?
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            Class.forName(driver);
            con = DriverManager.getConnection(url, login, password);
            stmt = con.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void close() {
        try {
            stmt.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void authorize() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите Вашу фамилию: ");
        String fName = scanner.nextLine();
        System.out.print("Введите Ваше имя: ");
        String lName = scanner.nextLine();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            boolean studentExists = false;
            while (rs.next()) {
                if (fName.equals(rs.getString("F_NAME"))
                        && lName.equals(rs.getString("L_NAME"))) {
                    studentExists = true;
                    studentID = rs.getInt("ID");
                    System.out.println("Студент " + fName + " " + lName + " авторизован.");
                    break;
                }
            }
            rs.close();
            if (!studentExists) {
                System.out.println("Студента " + fName + " " + lName + " не существует.");
                addStudent(fName, lName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addStudent(String fName, String lName) {
        try {
            stmt.execute("INSERT INTO students (f_name, l_name) VALUES (\'"
                    + fName + "\', \'" + lName + "\');");
            ResultSet rs = stmt.executeQuery("SELECT ID FROM students WHERE f_name=" +
            "\'" + fName + "and l_name=" + "\'" + lName + "\'");
            studentID = rs.getInt("ID");
            rs.close();
            System.out.println("Студент " + fName + " " + lName + " зарегистрирован.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
