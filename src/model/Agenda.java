/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 *
 * Class Model untuk merepresentasikan objek Agenda
 * Menerapkan konsep OOP: Encapsulation
 * @author slozoy
 */
public class Agenda {
    
    // ========== ATTRIBUTES (ENCAPSULATION) ==========
    private int id;
    private String judul;
    private String deskripsi;
    private LocalDateTime tanggalWaktu;
    private String prioritas;
    
    // ========== CONSTRUCTORS ==========
    
    /**
     * Constructor default
     */
    public Agenda() {
        this.tanggalWaktu = LocalDateTime.now();
    }
    
    /**
     * Constructor dengan parameter lengkap
     * @param id ID agenda
     * @param judul Judul agenda
     * @param deskripsi Deskripsi agenda
     * @param tanggalWaktu Tanggal dan waktu agenda
     * @param prioritas Tingkat prioritas
     */
    public Agenda(int id, String judul, String deskripsi, 
                  LocalDateTime tanggalWaktu, String prioritas) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.tanggalWaktu = tanggalWaktu;
        this.prioritas = prioritas;
    }
    
    /**
     * Constructor tanpa ID (untuk insert baru)
     */
    public Agenda(String judul, String deskripsi, 
                  LocalDateTime tanggalWaktu, String prioritas) {
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.tanggalWaktu = tanggalWaktu;
        this.prioritas = prioritas;
    }
    
    // ========== GETTERS & SETTERS ==========
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getJudul() {
        return judul;
    }
    
    public void setJudul(String judul) {
        this.judul = judul;
    }
    
    public String getDeskripsi() {
        return deskripsi;
    }
    
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
    
    public LocalDateTime getTanggalWaktu() {
        return tanggalWaktu;
    }
    
    public void setTanggalWaktu(LocalDateTime tanggalWaktu) {
        this.tanggalWaktu = tanggalWaktu;
    }
    
    public String getPrioritas() {
        return prioritas;
    }
    
    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }
    
    // ========== BUSINESS METHODS ==========
    
    /**
     * Method untuk mendapatkan tanggal dalam format string
     * @return String tanggal terformat
     */
    public String getTanggalFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return tanggalWaktu.format(formatter);
    }
    
    /**
     * Method untuk mendapatkan waktu dalam format string
     * @return String waktu terformat
     */
    public String getWaktuFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return tanggalWaktu.format(formatter);
    }
    
    /**
     * Method untuk mendapatkan tanggal waktu lengkap
     * @return String tanggal waktu terformat
     */
    public String getTanggalWaktuFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return tanggalWaktu.format(formatter);
    }
    
    /**
     * Method untuk mendapatkan hari dalam bahasa Indonesia
     * @return String nama hari
     */
    public String getHariIndonesia() {
        String[] namaHari = {"Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"};
        return namaHari[tanggalWaktu.getDayOfWeek().getValue() % 7];
    }
    
    /**
     * Override toString untuk debugging
     * @return String representasi objek
     */
    @Override
    public String toString() {
        return "Agenda{" +
                "id=" + id +
                ", judul='" + judul + '\'' +
                ", tanggal=" + getTanggalFormatted() +
                ", prioritas='" + prioritas + '\'' +
                '}';
    }
}
