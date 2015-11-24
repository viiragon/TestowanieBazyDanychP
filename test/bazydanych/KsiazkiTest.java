/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bazydanych;

import bazydanych.ksiazki.Bohater;
import bazydanych.ksiazki.Ksiazka;
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
public class KsiazkiTest {

    /*DLA TOBIASZA
    
    TEN TEST ROZNI SIE OD ONETOMANYTEST PRAKTYCZNIE TYLKO TYM (POMIJAJAC FAKT ZE UZYWAM INNE TABLICE)
    ZE SPRAWDZAM ROWNIEZ WARTOSCI KLASY KSIAZKA (NIE TYLKO BOHATER)
    
    APROPO:
    KSIAZKA ---< BOHATER
    */
    
    public KsiazkiTest() {
    }

    private static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    private static final String DB_URL = "jdbc:derby://localhost:1527/baza";

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
            testStatement.executeUpdate("drop table KSIAZKA");
            System.out.println("Czyszczenie tablicy KSIAZKA");
        } catch (SQLException e) {
            System.out.println("Tablica KSIAZKA nie istnieje, tworze nowa");
        }
        String sql = "create table KSIAZKA ("
                + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)"
                + ", tytul VARCHAR(20) default NULL)";
        try {
            testStatement.executeUpdate(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
        try {
            testStatement.executeUpdate("drop table BOHATER");
            System.out.println("Czyszczenie tablicy BOHATER");
        } catch (SQLException e) {
            System.out.println("Tablica BOHATER nie istnieje, tworze nowa");
        }
        sql = "create table BOHATER ("
                + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)"
                + ", imie VARCHAR(30) default NULL"
                + ", nazwisko VARCHAR(30) default NULL"
                + ", ksiazkaId INT default NULL)";
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

        String title1 = "Quo Vadis", title2 = "Pan Tadeusz";

        HashSet set1 = new HashSet(3);
        set1.add(new Bohater("Marek", "Winicjusz"));
        set1.add(new Bohater("Lidia", "Kallina"));
        set1.add(new Bohater("Ursus", ""));

        HashSet set2 = new HashSet(2);
        set2.add(new Bohater("Tadeusz", "Soplica"));
        set2.add(new Bohater("Protazy", "Brzechalski"));

        addBook(title1, set1);
        addBook(title2, set2);

        String sql = "select k.ID, k.TYTUL from KSIAZKA k order by 1";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam tablice KSIAZKA:");

            assert (result.next());
            System.out.println(result.getInt(1) + " : " + result.getString(2));
            assert (result.getInt(1) == 1);
            assert (result.getString(2).equals(title1));

            assert (result.next());
            System.out.println(result.getInt(1) + " : " + result.getString(2));
            assert (result.getInt(1) == 2);
            assert (result.getString(2).equals(title2));

            assert (!result.next());
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        sql = "select b.ID, b.IMIE, b.NAZWISKO, b.KSIAZKAID"
                + " from BOHATER b";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam tablice BOHATER:");

            while (result.next()) {
                System.out.println(result.getInt(1)
                        + " : " + result.getString(2)
                        + " : " + result.getString(3)
                        + " : " + result.getInt(4));
                if (result.getInt(4) == 1) {    //Bohaterowie ksiazki 1 : Quo Vadis
                    if (!set1.isEmpty()) {
                        set1.remove(new Bohater(result.getString(2), result.getString(3)));
                    } else {
                        fail("Ksiazka 1 ma za dużo bohaterow");
                    }
                } else {                        //Bohaterowie ksiazki 2 : Pan Tadeusz
                    if (!set2.isEmpty()) {
                        set2.remove(new Bohater(result.getString(2), result.getString(3)));
                    } else {
                        fail("Ksiazka 2 ma za dużo bohaterow");
                    }
                }
            }
            assert (set1.isEmpty());
            assert (set2.isEmpty());
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
        System.out.println("OK");
    }

    @Test
    public void readTest() {
        System.out.println("\nTest: readTest");

        String title = "Quo Vadis";

        ArrayList<Bohater> heroes = new ArrayList<>(3);
        heroes.add(new Bohater("Marek", "Winicjusz"));
        heroes.add(new Bohater("Lidia", "Kallina"));
        heroes.add(new Bohater("Ursus", ""));

        try {
            String sql = "insert into KSIAZKA (TYTUL) values ('" + title + "')";
            testStatement.executeUpdate(sql);
            System.out.println(sql);
            for (Bohater b : heroes) {
                sql = "insert into BOHATER (IMIE, NAZWISKO, KSIAZKAID) "
                        + "values ('" + b.getImie() + "', '" + b.getNazwisko() + "', 1)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        List list = listBooks();
        assert (list.size() == 1);
        Ksiazka book = (Ksiazka) list.get(0);
        System.out.println("Tytul : " + book.getTytul());
        assert (book.getTytul().equals(title));

        Set set = book.getBohaterowie();
        Bohater hero;

        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            hero = (Bohater) iterator.next();
            System.out.println("Bohater : " + hero.getImie() + " " + hero.getNazwisko());
            if (!heroes.isEmpty()) {
                heroes.remove(hero);
            } else {
                fail("Za duzo Bohaterow w bazie");
            }
        }
        assert (heroes.isEmpty());

        System.out.println("OK");
    }

    @Test
    public void updateTest() {
        System.out.println("\nTest: updateTest");

        String title = "Quo Vadis";

        ArrayList<Bohater> heroes = new ArrayList<>(3);
        heroes.add(new Bohater("Marek", "Winicjusz"));
        heroes.add(new Bohater("Lidia", "Kallina"));
        heroes.add(new Bohater("Ursus", ""));

        String titleAlternative = "Gdzie zmierzasz";
        Bohater additionalHero = new Bohater("Nero", "Ahenobarbus");

        String sql;

        try {
            sql = "insert into KSIAZKA (TYTUL) values ('" + title + "')";
            testStatement.executeUpdate(sql);
            System.out.println(sql);
            for (Bohater b : heroes) {
                sql = "insert into BOHATER (IMIE, NAZWISKO, KSIAZKAID) "
                        + "values ('" + b.getImie() + "', '" + b.getNazwisko() + "', 1)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        HashSet set = new HashSet(4);
        for (Bohater b : heroes) {
            set.add(b);
        }
        set.add(additionalHero);

        updateBook(1, titleAlternative, set);

        sql = "select k.ID, k.TYTUL "
                + "from KSIAZKA k";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam tablice KSIAZKA:");

            result.next();
            System.out.println(result.getInt(1)
                    + " : " + result.getString(2));
            assert (result.getString(2).equals(titleAlternative));
            assert (!result.next());

        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        sql = "select b.ID, b.IMIE, b.NAZWISKO, b.KSIAZKAID "
                + "from BOHATER b";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam tablice BOHATER:");

            boolean isAdditionalFound = false;

            while (result.next()) {
                System.out.println(result.getInt(1)
                        + " : " + result.getString(2)
                        + " : " + result.getString(3)
                        + " : " + result.getInt(4));
                if (result.getInt(4) == 1) {    //Bohaterzy ksiazki 1 : Quo vadis
                    Bohater tmpHero = new Bohater(result.getString(2), result.getString(3));
                    if (!heroes.remove(tmpHero)) {
                        if (!isAdditionalFound && tmpHero.equals(additionalHero)) {
                            isAdditionalFound = true;
                        } else {
                            fail("Znaleziono nieznanego bohatera : " + tmpHero.getImie() + " " + tmpHero.getNazwisko());
                        }
                    }
                }
            }
            assert (heroes.isEmpty());
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

        String title1 = "Quo Vadis", title2 = "Pan Tadeusz";

        HashSet heroes1 = new HashSet(3);
        heroes1.add(new Bohater("Marek", "Winicjusz"));
        heroes1.add(new Bohater("Lidia", "Kallina"));
        heroes1.add(new Bohater("Ursus", ""));

        HashSet heroes2 = new HashSet(2);
        heroes2.add(new Bohater("Tadeusz", "Soplica"));
        heroes2.add(new Bohater("Protazy", "Brzechalski"));

        String sql;
        try {
            sql = "insert into KSIAZKA (TYTUL) values ('" + title1 + "')";
            testStatement.executeUpdate(sql);
            sql = "insert into KSIAZKA (TYTUL) values ('" + title2 + "')";
            testStatement.executeUpdate(sql);
            System.out.println(sql);
            for (Object o : heroes1) {
                Bohater b = (Bohater) o;
                sql = "insert into BOHATER (IMIE, NAZWISKO, KSIAZKAID) "
                        + "values ('" + b.getImie() + "', '" + b.getNazwisko() + "', 1)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
            for (Object o : heroes2) {
                Bohater b = (Bohater) o;
                sql = "insert into BOHATER (IMIE, NAZWISKO, KSIAZKAID) "
                        + "values ('" + b.getImie() + "', '" + b.getNazwisko() + "', 2)";
                testStatement.executeUpdate(sql);
                System.out.println(sql);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        deleteBook(1);  //Usuniecie Quo vadis

        sql = "select k.ID, k.TYTUL "
                + "from KSIAZKA k";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam tablice KSIAZKA:");

            result.next();
            System.out.println(result.getInt(1)
                    + " : " + result.getString(2));
            assert (result.getInt(1) == 2);
            assert (result.getString(2).equals(title2));
            assert (!result.next());

        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }
        
        sql = "select b.ID, b.IMIE, b.NAZWISKO, b.KSIAZKAID "
                + "from BOHATER b";
        try {
            ResultSet result = testStatement.executeQuery(sql);
            System.out.println("Czytam tablice BOHATER:");

            while (result.next()) {
                System.out.println(result.getInt(1)
                        + " : " + result.getString(2)
                        + " : " + result.getString(3)
                        + " : " + result.getInt(4));
                if (result.getInt(4) == 2) {    //Certyfikaty osoby 2 : Kazimierz Wiosło
                    if (!heroes2.isEmpty()) {
                        heroes2.remove(new Bohater(result.getString(2), result.getString(3)));
                    } else {
                        fail("Znaleziono nieznanego bohatera : "
                                + result.getString(2) + " " + result.getString(3));
                    }
                } else {
                    fail("Znaleziono bohatera ksiazki ktora nie istnieje : "
                            + result.getString(2) + " " + result.getString(3));
                }
            }
            assert (heroes2.isEmpty());
        } catch (SQLException ex) {
            ex.printStackTrace();
            fail();
        }

        System.out.println("OK");
    }

    private Integer addBook(String title, Set heroes) {
        Session session = factory.openSession();
        Transaction tx = null;
        Integer bookId = null;
        try {
            tx = session.beginTransaction();
            Ksiazka book = new Ksiazka(title);
            book.setBohaterowie(heroes);
            bookId = (Integer) session.save(book);
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
        return bookId;
    }

    private List listBooks() {
        Session session = factory.openSession();
        Transaction tx = null;
        List books = null;
        try {
            tx = session.beginTransaction();
            books = session.createQuery("FROM Ksiazka").list();
            for (Iterator iterator1 = books.iterator(); iterator1.hasNext();) {
                Ksiazka book = (Ksiazka) iterator1.next();
                Set heroes = book.getBohaterowie();
                for (Iterator iterator2
                        = heroes.iterator(); iterator2.hasNext();) {
                    Bohater hero = (Bohater) iterator2.next();
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
        return books;
    }

    private void updateBook(Integer bookID, String title, Set heroes) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Ksiazka book = (Ksiazka) session.get(Ksiazka.class, bookID);
            if (title != null) {
                book.setTytul(title);
            }
            if (heroes != null) {
                book.setBohaterowie(heroes);
            }
            session.update(book);
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

    private void deleteBook(Integer bookId) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Ksiazka book = (Ksiazka) session.get(Ksiazka.class, bookId);
            session.delete(book);
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
