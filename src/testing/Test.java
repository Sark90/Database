package testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Test {
    private static final String DB_NAME = "TESTING";
    private static final String INIT_SQL = "initial_data.sql";
    private Connection con;
    private int studentID;

    public void run() {
        connect();
        initDB();
        authorize();
        menu();
        close();
    }

    private void initDB() {
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from themes");
            if (rs.next()) rs = stmt.executeQuery("select * from questions");
            if (rs.next()) rs = stmt.executeQuery("select * from answers");
            if (rs.next()) return;
            List<String> insetScript = new ArrayList<>();
            try {
                insetScript = Files.readAllLines(Paths.get(INIT_SQL));
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (String s: insetScript) {
                stmt.execute(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void menu() {
        System.out.print("1. Тестирование\n2. Результаты пройденных тестов\nВведите № команды: ");
        int n;
        Scanner scanner = new Scanner(System.in);
        while (true) {
            n = scanner.nextInt();
            if (n == 1 || n == 2) break;
        }
        System.out.println();
        if (n == 1) runTest();
        else showResults();
    }

    private void showResults() {
        try {
            Statement sTests = con.createStatement();
            ResultSet rsTests = sTests.executeQuery("select T.test_id, TH.theme, Q.question, AN.answer, " +
                    "AN.is_right from themes TH, questions Q, answers AN, tests T where TH.id=T.theme_id " +
                    "and Q.id=T.question_id and AN.id=T.answer_id and T.student_id=" + studentID +
                    "order by test_id");  //order by theme_id, question_id
            int testsNum = 0, rightNum = 0;
            while (rsTests.next()) {
                System.out.println("\tТест ID " + rsTests.getInt(1));
                System.out.println("Тема: " + rsTests.getString(2));
                System.out.println("Вопрос: " + rsTests.getString(3));
                System.out.print("Ответ: " + rsTests.getString(4) + " (");
                testsNum++;
                if (rsTests.getBoolean(5)) {
                    System.out.println("верно)");
                    rightNum++;
                } else System.out.println("неверно)");
            }
            rsTests.close();
            sTests.close();
            System.out.println("\tВерных ответов " + rightNum + " из " + testsNum);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void runTest() {
        System.out.println("Темы для тестов:");
        try {
            Statement sMaxID = con.createStatement();
            ResultSet rsMaxID = sMaxID.executeQuery("select max(test_id) from tests");
            rsMaxID.next();
            int testID = rsMaxID.getInt(1);
            testID++;
            rsMaxID.close();
            sMaxID.close();
            Statement sThemes = con.createStatement();
            ResultSet rsThemes = sThemes.executeQuery("SELECT * FROM themes");
            while (rsThemes.next()) {
                System.out.println(rsThemes.getString("id") + ". " +
                        rsThemes.getString("theme"));
            }
            rsThemes.close();
            sThemes.close();
            System.out.print("Выберите тему: ");
            Scanner scanner = new Scanner(System.in);
            int themeID = scanner.nextInt();
            Statement sQuestions = con.createStatement();
            ResultSet rsQuestions = sQuestions.executeQuery("SELECT * FROM questions where theme_id=" +
                    themeID);
            while (rsQuestions.next()) {
                Statement sAnswers = con.createStatement();
                ResultSet rsAnswers = sAnswers.executeQuery("SELECT * FROM answers where question_id=" +
                        rsQuestions.getString("ID") + "order by variant");
                System.out.println("\n" + rsQuestions.getString("question"));
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
                Statement sUserAnswer = con.createStatement();
                ResultSet rsUserAnswer = sUserAnswer.executeQuery("SELECT * FROM answers where question_id=" +
                        rsQuestions.getString("ID") + " and variant=\'" + variant + "\';");
                rsUserAnswer.next();
                if (rsUserAnswer.getBoolean("is_right")) {
                    System.out.println("Верно.");
                }
                else System.out.println("Неверно.");
                Statement stmt = con.createStatement();
                stmt.execute("insert into TESTS (test_id, student_id, theme_id, question_id, answer_id)" +
                        " values (" + testID + ", " + studentID + ", " + themeID + ", " +
                        rsQuestions.getInt("ID") + ", " + rsUserAnswer.getInt("ID") +
                        ");");
                rsUserAnswer.close();
                sUserAnswer.close();
                rsAnswers.close();
                sAnswers.close();
                stmt.close();
            }
            rsQuestions.close();
            sQuestions.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/" + DB_NAME;
            String login = "postgres";
            String password = "admin";
            con = DriverManager.getConnection(url, login, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
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
            Statement stmt = con.createStatement();
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
            stmt.close();
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
            Statement stmt = con.createStatement();
            stmt.execute("INSERT INTO students (f_name, l_name) VALUES (\'"
                    + fName + "\', \'" + lName + "\');");
            ResultSet rs = stmt.executeQuery("SELECT ID FROM students WHERE f_name=" +
            "\'" + fName + "\' and l_name=\'" + lName + "\'");
            rs.next();
            studentID = rs.getInt("ID");
            rs.close();
            stmt.close();
            System.out.println("Студент " + fName + " " + lName + " зарегистрирован.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
