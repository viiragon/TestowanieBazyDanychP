/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bazydanych.ksiazki;

import java.util.Set;

/**
 *
 * @author Wojtek
 */
public class Ksiazka {

    private int id;
    private String tytul;
    private Set bohaterowie;

    public Ksiazka() {
    }

    public Ksiazka(String tytul) {
        this.tytul = tytul;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTytul() {
        return tytul;
    }

    public void setTytul(String tytul) {
        this.tytul = tytul;
    }

    public Set getBohaterowie() {
        return bohaterowie;
    }

    public void setBohaterowie(Set bohaterowie) {
        this.bohaterowie = bohaterowie;
    }
}
