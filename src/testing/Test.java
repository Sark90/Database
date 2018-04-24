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
    private ResultSet rs;

    public void run() {
        connect();
        authorize();
        close();
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
            rs = stmt.executeQuery("SELECT F_NAME, L_NAME FROM students");
            boolean studentExists = false;
            while (rs.next()) {
                if (fName.equals(rs.getString("F_NAME"))
                        && lName.equals(rs.getString("L_NAME"))) {
                    studentExists = true;
                    System.out.println("Студент " + fName + " " + lName + " авторизован.");
                    break;
                }
            }
            rs.close();
            if (!studentExists) addStudent(fName, lName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void addStudent(String fName, String lName) {
        try {
            rs = stmt.executeQuery("insert into students (F_NAME, L_NAME) values ("
                    + fName + ", " + lName + ");");
            rs.close();
            System.out.println("Студент " + fName + " " + lName + " зарегистрирован.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
