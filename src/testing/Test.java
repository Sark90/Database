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
        menu();
        close();
    }

    private void menu() {
        System.out.print("1. Тестирование\n2. Результаты пройденных тестов\nВведите № команды: ");
        int n;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            n = scanner.nextInt();
            if (n == 1 || n == 2) break;
        }
        if (n == 1) runTest();
        else showResults();
    }

    private void showResults() {
        try {
            Statement s1 = con.createStatement();
            ResultSet rsTests = s1.executeQuery("select T.test_id, TH.theme, Q.question, AN.answer, " +
                    "AN.is_right from themes TH, questions Q, answers AN, tests T where TH.id=T.theme_id " +
                    "and Q.id=T.question_id and AN.id=T.answer_id and T.student_id=" + studentID +
                    "order by test_id");  //order by theme_id, question_id
            int testsNum = 0, rightNum = 0;
            while (rsTests.next()) {
                System.out.println("Тест №" + rsTests.getInt(1));
                System.out.println("Тема: " + rsTests.getString(2));
                System.out.println("Вопрос: " + rsTests.getString(3));
                System.out.print("Ответ: " + rsTests.getString(4) + " (");
                testsNum++;
                if (rsTests.getBoolean(5)) {
                    System.out.println("верно)");
                    rightNum++;
                } else System.out.println("неверно)");
            }
            System.out.println("\tВерных ответов " + rightNum + " из " + testsNum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void runTest() {
        System.out.println("Темы для тестов:");
        try {
            Statement s3 = con.createStatement();
            ResultSet rsMaxID = s3.executeQuery("select max(test_id) from tests");
            rsMaxID.next();
            int testID = rsMaxID.getInt(1);
            testID++;
            s3.close();
            ResultSet rsThemes = stmt.executeQuery("SELECT * FROM themes");
            while (rsThemes.next()) {
                System.out.println(rsThemes.getString("id") + ". " +
                        rsThemes.getString("theme"));
            }
            rsThemes.close();
            System.out.print("Выберите тему: ");
            Scanner scanner = new Scanner(System.in);
            int themeID = scanner.nextInt();
            Statement s = con.createStatement();
            ResultSet rsQuestions = s.executeQuery("SELECT * FROM questions where theme_id=" +
                    themeID);
            while (rsQuestions.next()) {
                Statement s1 = con.createStatement();
                ResultSet rsAnswers = s1.executeQuery("SELECT * FROM answers where question_id=" +
                        rsQuestions.getString("ID") + "order by variant");
                System.out.println(rsQuestions.getString("question"));
                while (rsAnswers.next()) {
                    System.out.println(rsAnswers.getString("variant") + ". " +
                            rsAnswers.getString("answer"));
                }
                System.out.print("Выберите вариант ответа (A/B/C): ");
                char variant;
                while (true) {
                    variant = scanner.next().charAt(0);
                    if (variant == 'A' || variant == 'B' || variant == 'C') break;
                }
                Statement s2 = con.createStatement();
                ResultSet rsUserAnswer = s2.executeQuery("SELECT * FROM answers where question_id=" +
                        rsQuestions.getString("ID") + " and variant=\'" + variant + "\';");
                rsUserAnswer.next();
                if (rsUserAnswer.getBoolean("is_right")) {
                    System.out.println("Верно.");
                }
                else System.out.println("Неверно.");
                stmt.execute("insert into TESTS (test_id, student_id, theme_id, question_id, answer_id)" +
                        " values (" + testID + ", " + studentID + ", " + themeID + ", " +
                        rsQuestions.getInt("ID") + ", " + rsUserAnswer.getInt("ID") +
                        ");");
                rsUserAnswer.close();
                s2.close();
                rsAnswers.close();
                s1.close();
            }
            rsQuestions.close();
            s.close();
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
