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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Wojtek
 */
public class CRUDTest {

    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String DB_URL = "jdbc:derby://localhost:1527/baza";

    //  Database credentials
    private static final String USER = "root";
    private static final String PASS = "password";

    private static Connection testConnection = null;
    private static Statement testStatement = null;

    private static SessionFactory factory;

    public CRUDTest() {
    }

    @BeforeClass
    public static void setUpConnections() {
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

            try {
                testStatement.executeUpdate("drop table samochod");
                System.out.println("Czyszczenie tablicy samochod");
            } catch (SQLException e) {
                System.out.println("Tablica samochod nie istnieje, tworze nowa");
            }
            String sql = "create table samochod (\n"
                    + " id integer not null generated always as identity (start with 1, increment by 1)\n"
                    + ", marka varchar(40)\n"
                    + ", cena int\n"
                    + ")";
            testStatement.executeUpdate(sql);
        } catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException se) {
            se.printStackTrace();
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
    }

    @Test
    public void createTest() {
        System.out.println("\nTest: createTest");
        
        String carName1 = "Fiat 125p", carName2 = "Mercedes Benz";
        int carPrice1 = 540, carPrice2 = 143000;
        
        System.out.println("Dodany samochod " + carName1 + " id : " + createCar(carName1, carPrice1));
        System.out.println("Dodany samochod " + carName2 + " id : " + createCar(carName2, carPrice2));

        String sql = "select s.ID, s.MARKA, s.CENA from samochod s";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam:");
            
            assert(result.next());
            System.out.println(result.getInt(1) + " : " + result.getString(2) + " : " + result.getInt(3));
            assert(result.getInt(1) == 1);
            assert(result.getString(2).equals(carName1));
            assert(result.getInt(3) == carPrice1);
            
            assert(result.next());
            System.out.println(result.getInt(1) + " : " + result.getString(2) + " : " + result.getInt(3));
            assert(result.getInt(1) == 2);
            assert(result.getString(2).equals(carName2));
            assert(result.getInt(3) == carPrice2);
            
            assert(!result.next());
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void readTest() {
        System.out.println("\nTest: readTest");
        assert (true);
    }

    @Test
    public void updateTest() {
        System.out.println("\nTest: updateTest");
        assert (true);
    }

    @Test
    public void deleteTest() {
        System.out.println("\nTest: deleteTest");
        assert (true);
    }

    private int createCar(String marka, int cena) {
        Session session = factory.openSession();
        Transaction tx = null;
        Integer id = null;
        try {
            tx = session.beginTransaction();
            Samochod car = new Samochod(marka, cena);
            id = (Integer) session.save(car);
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
        return id;
    }

}
