/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bazydanych.ksiazki;

import java.util.Objects;

/**
 *
 * @author Wojtek
 */
public class Bohater {
    private int id;
    private String imie, nazwisko;
    private int ksiazkaId;

    public Bohater() {}
    public Bohater(String imie, String nazwisko) {
        this.imie = imie;
        this.nazwisko = nazwisko;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }

    public String getNazwisko() {
        return nazwisko;
    }

    public void setNazwisko(String nazwisko) {
        this.nazwisko = nazwisko;
    }
    
    public int getKsiazkaId() {
        return ksiazkaId;
    }

    public void setKsiazkaId(int id) {
        this.ksiazkaId = id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Bohater) {
            Bohater b = (Bohater) o;
            if (b.imie.equals(imie) && b.nazwisko.equals(nazwisko)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.imie);
        hash = 17 * hash + Objects.hashCode(this.nazwisko);
        return hash;
    }
}
