/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bazydanych;

/**
 *
 * @author Wojtek
 */
public class Samochod {
    private int id;
    private String marka;
    private int cena;

    public Samochod() {}
    public Samochod(String marka, int cena) {
        this.marka = marka;
        this.cena = cena;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getMarka() {
        return marka;
    }

    public void setMarka(String marka) {
        this.marka = marka;
    }

    public int getCena() {
        return cena;
    }

    public void setCena(int cena) {
        this.cena = cena;
    }
}
