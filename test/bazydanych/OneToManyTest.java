/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bazydanych;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Wojtek
 */
public class OneToManyTest {

    public OneToManyTest() {
    }

    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String DB_URL = "jdbc:derby://localhost:1527/baza";

    //  Database credentials
    private static final String USER = "root";
    private static final String PASS = "password";

    private static Connection testConnection = null;
    private static Statement testStatement = null;

    private static SessionFactory factory;

    @BeforeClass
    public static void setUpConnections() {
        System.out.println("Przygotowanie polaczen");
        try {
            Class.forName(JDBC_DRIVER).newInstance();
            testConnection = DriverManager.getConnection(DB_URL, USER, PASS);
            testStatement = testConnection.createStatement();

            try {
                factory = new Configuration().configure().buildSessionFactory();
            } catch (Throwable ex) {
                System.err.println("Niedane polaczenie za pomoca HIBERNATE : " + ex);
                fail();
            }

        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException se) {
            se.printStackTrace();
            fail();
        }
    }

    @Before
    public void clearTables() { //Przywraca początkowy stan tablicy po każdym teście
        System.out.println("");
        try {
            testStatement.executeUpdate("drop table EMPLOYEE");
            System.out.println("Czyszczenie tablicy EMPLOYEE");
        } catch (SQLException e) {
            System.out.println("Tablica EMPLOYEE nie istnieje, tworze nowa");
        }
        String sql = "create table EMPLOYEE ("
                + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)"
                + ", first_name VARCHAR(20) default NULL"
                + ", last_name VARCHAR(20) default NULL"
                + ", salary INT default NULL)";
        try {
            testStatement.executeUpdate(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
        try {
            testStatement.executeUpdate("drop table CERTIFICATE");
            System.out.println("Czyszczenie tablicy CERTIFICATE");
        } catch (SQLException e) {
            System.out.println("Tablica CERTIFICATE nie istnieje, tworze nowa");
        }
        sql = "create table CERTIFICATE ("
                + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)"
                + ", certificate_name VARCHAR(30) default NULL"
                + ", employee_id INT default NULL)";
        try {
            testStatement.executeUpdate(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @AfterClass
    public static void closeConnections() {
        try {
            if (testStatement != null) {
                testStatement.close();
            }
        } catch (SQLException e) {
        }
        try {
            if (testConnection != null) {
                testConnection.close();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        System.out.println("\nPolaczenia zamkniete");
    }

    @Test
    public void createTest() {
        System.out.println("\nTest: createTest");

        ArrayList<String> certs1 = new ArrayList<>(3);
        certs1.add("HMA");
        certs1.add("POP");
        certs1.add("CRA");
        ArrayList<String> certs2 = new ArrayList<>(2);
        certs2.add("DRF");
        certs2.add("POP");

        HashSet set1 = new HashSet(3);
        for (String s : certs1) {
            set1.add(new Certificate(s));
        }
        HashSet set2 = new HashSet(2);
        for (String s : certs2) {
            set2.add(new Certificate(s));
        }

        addEmployee("Stefan", "Kajak", 1230, set1);
        addEmployee("Kazimierz", "Wiosło", 34030, set2);

        String sql = "select c.ID, c.CERTIFICATE_NAME, c.EMPLOYEE_ID "
                + "from CERTIFICATE c";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam:");

            while (result.next()) {
                System.out.println(result.getInt(1) + " : " + result.getString(2) + " : " + result.getInt(3));
                if (result.getInt(3) == 1) {    //Certyfikaty osoby 1 : Stefan Kajak
                    if (!certs1.isEmpty()) {
                        certs1.remove(result.getString(2));
                    } else {
                        fail("Osoba 1 ma za dużo certyfikatów");
                    }
                } else {                        //Certyfikaty osoby 2 : Kazimierz Wiosło
                    if (!certs2.isEmpty()) {
                        certs2.remove(result.getString(2));
                    } else {
                        fail("Osoba 2 ma za dużo certyfikatów");
                    }
                }
            }
            assert (certs1.isEmpty());
            assert (certs2.isEmpty());
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
        System.out.println("OK");
    }

    @Test
    public void readTest() {
        System.out.println("\nTest: readTest");

        ArrayList<String> certs = new ArrayList<>(3);
        certs.add("HMA");
        certs.add("POP");
        certs.add("CRA");

        try {
            String sql = "insert into EMPLOYEE (FIRST_NAME, LAST_NAME, SALARY) values ('Stefan', 'Kajak', 1000)";
            testStatement.executeUpdate(sql);
            System.out.println(sql);
            for (String s : certs) {
                sql = "insert into CERTIFICATE (CERTIFICATE_NAME, EMPLOYEE_ID) values ('" + s + "', 1)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        List list = listEmployees();
        assert (list.size() == 1);

        Set set = ((Employee) list.get(0)).getCertificates();
        Certificate cert;

        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            cert = (Certificate) iterator.next();
            System.out.println("Certyfikat : " + cert.getName());
            if (!certs.isEmpty()) {
                certs.remove(cert.getName());
            } else {
                fail("Za duzo certyfikatow w bazie");
            }
        }
        assert (certs.isEmpty());

        System.out.println("OK");
    }

    @Test
    public void updateTest() {
        System.out.println("\nTest: updateTest");

        ArrayList<String> certs = new ArrayList<>(3);
        certs.add("HMA");
        certs.add("POP");
        certs.add("CRA");

        String additional = "BRU";
        System.out.println("Additional certificate : " + additional);

        String sql;

        try {
            sql = "insert into EMPLOYEE (FIRST_NAME, LAST_NAME, SALARY) values ('Stefan', 'Kajak', 1000)";
            testStatement.executeUpdate(sql);
            System.out.println(sql);
            for (String s : certs) {
                sql = "insert into CERTIFICATE (CERTIFICATE_NAME, EMPLOYEE_ID) values ('" + s + "', 1)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            fail();
        }

        HashSet set = new HashSet(4);
        for (String s : certs) {
            set.add(new Certificate(s));
        }
        set.add(new Certificate(additional));

        updateEmployee(1, null, null, -1, set); //Update jedynie certyfikatów

        sql = "select c.ID, c.CERTIFICATE_NAME, c.EMPLOYEE_ID "
                + "from CERTIFICATE c";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam:");

            boolean isAdditionalFound = false;

            while (result.next()) {
                System.out.println(result.getInt(1) + " : " + result.getString(2) + " : " + result.getInt(3));
                if (result.getInt(3) == 1) {    //Certyfikaty osoby 1 : Stefan Kajak
                    String tmpNazwa = result.getString(2);
                    if (!certs.remove(tmpNazwa)) {
                        if (!isAdditionalFound && tmpNazwa.equals(additional)) {
                            isAdditionalFound = true;
                        } else {
                            fail("Znaleziono nieznany certyfikat " + tmpNazwa);
                        }
                    }
                }
            }
            assert (certs.isEmpty());
            assert (isAdditionalFound);
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        System.out.println("OK");
    }

    @Test
    public void deleteTest() {
        System.out.println("\nTest: deleteTest");

        ArrayList<String> certs1 = new ArrayList<>(3);
        certs1.add("HMA");
        certs1.add("POP");
        certs1.add("CRA");
        ArrayList<String> certs2 = new ArrayList<>(2);
        certs2.add("CRA");
        certs2.add("POP");

        String sql;
        try {
            sql = "insert into EMPLOYEE (FIRST_NAME, LAST_NAME, SALARY) values ('Stefan', 'Kajak', 1000)";
            testStatement.executeUpdate(sql);
            System.out.println(sql);
            sql = "insert into EMPLOYEE (FIRST_NAME, LAST_NAME, SALARY) values ('Kazimierz', 'Wiosło', 1000)";
            testStatement.executeUpdate(sql);
            System.out.println(sql);
            for (String s : certs1) {
                sql = "insert into CERTIFICATE (CERTIFICATE_NAME, EMPLOYEE_ID) values ('" + s + "', 1)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
            for (String s : certs2) {
                sql = "insert into CERTIFICATE (CERTIFICATE_NAME, EMPLOYEE_ID) values ('" + s + "', 2)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        deleteEmployee(1);  //Usuniecie Stefana Kajaka

        sql = "select c.ID, c.CERTIFICATE_NAME, c.EMPLOYEE_ID "
                + "from CERTIFICATE c";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam:");

            while (result.next()) {
                System.out.println(result.getInt(1) + " : " + result.getString(2) + " : " + result.getInt(3));
                if (result.getInt(3) == 2) {    //Certyfikaty osoby 2 : Kazimierz Wiosło
                    if (!certs2.isEmpty()) {
                        certs2.remove(result.getString(2));
                    } else {
                        fail("Znaleziono nieznany certyfikat");
                    }
                } else {
                    fail("Znaleziono certyfikat osoby ktora nie istnieje");
                }
            }
            assert (certs2.isEmpty());
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        System.out.println("OK");
    }

    private Integer addEmployee(String fname, String lname,
            int salary, Set cert) {
        Session session = factory.openSession();
        Transaction tx = null;
        Integer employeeID = null;
        try {
            tx = session.beginTransaction();
            Employee employee = new Employee(fname, lname, salary);
            employee.setCertificates(cert);
            employeeID = (Integer) session.save(employee);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            fail();
        } finally {
            session.close();
        }
        return employeeID;
    }

    private List listEmployees() {
        Session session = factory.openSession();
        Transaction tx = null;
        List employees = null;
        try {
            tx = session.beginTransaction();
            employees = session.createQuery("FROM Employee").list();
            for (Iterator iterator1
                    = employees.iterator(); iterator1.hasNext();) {
                Employee employee = (Employee) iterator1.next();
                Set certificates = employee.getCertificates();
                for (Iterator iterator2
                        = certificates.iterator(); iterator2.hasNext();) {
                    Certificate certName = (Certificate) iterator2.next();
                }
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            fail();
        } finally {
            session.close();
        }
        return employees;
    }

    private void updateEmployee(Integer EmployeeID, String fname, String lname,
            int salary, Set cert) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Employee employee
                    = (Employee) session.get(Employee.class, EmployeeID);
            if (fname != null) {
                employee.setFirstName(fname);
            }
            if (lname != null) {
                employee.setLastName(lname);
            }
            if (salary != -1) {
                employee.setSalary(salary);
            }
            if (cert != null) {
                employee.setCertificates(cert);
            }
            session.update(employee);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            fail();
        } finally {
            session.close();
        }
    }

    private void deleteEmployee(Integer EmployeeID) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Employee employee
                    = (Employee) session.get(Employee.class, EmployeeID);
            session.delete(employee);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            fail();
        } finally {
            session.close();
        }
    }

}
