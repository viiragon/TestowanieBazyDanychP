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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author Wojtek
 */

/*create table samochod (id integer not null generated always as identity (start with 1, increment by 1), marka varchar(40), cena int)*/
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
            testStatement.executeUpdate("drop table samochod");
            System.out.println("Czyszczenie tablicy samochod");
        } catch (SQLException e) {
            System.out.println("Tablica samochod nie istnieje, tworze nowa");
        }
        String sql = "create table samochod ("
                + "id integer not null generated always as identity (start with 1, increment by 1)"
                + ", marka varchar(40)"
                + ", cena int)";
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

        String carName1 = "Fiat 125p", carName2 = "Mercedes Benz";
        int carPrice1 = 540, carPrice2 = 143000;

        System.out.println("Dodany samochod " + carName1 + " id : " + createCar(carName1, carPrice1));
        System.out.println("Dodany samochod " + carName2 + " id : " + createCar(carName2, carPrice2));

        String sql = "select s.ID, s.MARKA, s.CENA from samochod s";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam:");

            assert (result.next());
            System.out.println(result.getInt(1) + " : " + result.getString(2) + " : " + result.getInt(3));
            assert (result.getInt(1) == 1);
            assert (result.getString(2).equals(carName1));
            assert (result.getInt(3) == carPrice1);

            assert (result.next());
            System.out.println(result.getInt(1) + " : " + result.getString(2) + " : " + result.getInt(3));
            assert (result.getInt(1) == 2);
            assert (result.getString(2).equals(carName2));
            assert (result.getInt(3) == carPrice2);

            assert (!result.next());
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
        System.out.println("OK");
    }

    @Test
    public void readTest() {
        System.out.println("\nTest: readTest");
        String carName1 = "Fiat 125p", carName2 = "Mercedes Benz";
        int carPrice1 = 540, carPrice2 = 143000;

        String sql;
        try {
            sql = "insert into samochod (marka, cena) values ('" + carName1 + "', " + carPrice1 + ")";
            testStatement.executeUpdate(sql);
            System.out.println("Dodany samochod " + carName1 + " cena: " + carPrice1);
            sql = "insert into samochod (marka, cena) values ('" + carName2 + "', " + carPrice2 + ")";
            testStatement.executeUpdate(sql);
            System.out.println("Dodany samochod " + carName2 + " cena: " + carPrice2);
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
        System.out.println("Czytam:");

        Samochod[] list = readCars();
        Samochod tmp;
        assert (list.length == 2);

        tmp = list[0];
        System.out.println(tmp);
        assert (tmp.getId() == 1);
        assert (tmp.getMarka().equals(carName1));
        assert (tmp.getCena() == carPrice1);

        tmp = list[1];
        System.out.println(tmp);
        assert (tmp.getId() == 2);
        assert (tmp.getMarka().equals(carName2));
        assert (tmp.getCena() == carPrice2);

        System.out.println("OK");
    }

    @Test
    public void updateTest() {
        System.out.println("\nTest: updateTest");
        try {
            Connection conn = null;
            Class.forName(JDBC_DRIVER).newInstance();
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            String sql = "UPDATE CERTIFICATE SET" //zmiana nazwy certyfikatu
                    + "CERTIFICATE_NAME=SPR"
                    + "WHERE ID = 2";
            stmt.executeUpdate(sql);
            stmt.close();
            String sql2 = "SELECT CERTIFICATE_NAME" //odczytywanie nowej wartości
                    + "FROM CERTIFICATE WHERE ID = 2";
            Statement stmt2 = conn.createStatement();
            stmt2.executeUpdate(sql2);
            ResultSet rs = stmt2.getResultSet();
            while (rs.next()) {
                String s1 = rs.getString("CERTIFICATE_NAME");
                String s2 = "SPR";
                assertTrue(s1.equals(s2)); //sprawdzanie poprawności podstawienia
            }
            rs.close();
            Statement stmt3 = conn.createStatement();
            String sql3 = "UPDATE CERTIFICATE SET"//powót do stanu początkowego bazy danych
                    + "CERTIFICATE_NAME=PMP"
                    + "WHERE ID = 2";
            stmt3.executeUpdate(sql3);

            stmt.close();
            conn.close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(CRUDTest.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    private Samochod[] readCars() {
        Session session = factory.openSession();
        Transaction tx = null;
        Samochod[] retList = null;
        try {
            tx = session.beginTransaction();
            List cars = session.createQuery("FROM Samochod").list();
            int s = cars.size();
            retList = new Samochod[s];
            for (int i = 0; i < s; i++) {
                retList[i] = (Samochod) cars.get(i);
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
        return retList;
    }

}
