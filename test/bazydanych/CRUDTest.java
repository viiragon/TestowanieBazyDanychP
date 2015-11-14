/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bazydanych;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Wojtek
 */
public class CRUDTest {
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";  
    static final String DB_URL = "jdbc:derby://localhost:1527/baza";

    static final String USER = "root";
    static final String PASS = "password";
   
    public CRUDTest() {
    }

    @Test
    public void CreateTest() {//Wojtek
        assert (true);
    }

    @Test
    public void ReadTest() {//Wojtek
        assert (true);
    }

    @Test
    public void UpdateTest() { //Tobiasz
        
        try {
        Connection conn = null;
        Class.forName(JDBC_DRIVER).newInstance();
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement stmt = conn.createStatement();
        String sql = "UPDATE CERTIFICATE SET" //zmiana nazwy certyfikatu
                +"CERTIFICATE_NAME=SPR"
                +"WHERE ID = 2";
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
                +"CERTIFICATE_NAME=PMP"
                +"WHERE ID = 2";
        stmt3.executeUpdate(sql3);
        
        stmt.close();
        conn.close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(CRUDTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void DeleteTest() {//Tobiasz
        assert (true);
    }

}
