/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import model.Agenda;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * Class untuk mengelola koneksi dan operasi database SQLite
 * Menerapkan konsep OOP: Abstraction, Single Responsibility
 * 
 * @author slozoy
 */
public class DatabaseHelper {
    
    // ========== CONSTANTS ==========
    private static final String DB_URL = "jdbc:sqlite:agenda.db";
    private static final String TABLE_NAME = "agenda";
    
    // ========== SINGLETON PATTERN ==========
    private static DatabaseHelper instance;
    private Connection connection;
    
    /**
     * Constructor private untuk singleton pattern
     */
    private DatabaseHelper() {
        initDatabase();
    }
    
    /**
     * Mendapatkan instance DatabaseHelper (Singleton)
     * @return instance DatabaseHelper
     */
    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }
    
    // ========== DATABASE CONNECTION ==========
    
    /**
     * Inisialisasi database dan membuat tabel jika belum ada
     */
    private void initDatabase() {
        try {
            // Load SQLite JDBC Driver
            Class.forName("org.sqlite.JDBC");
            
            // Create connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Create table if not exists
            createTableIfNotExists();
            
            System.out.println("Database connected successfully!");
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
    }
    
    /**
     * Membuat tabel agenda jika belum ada
     * Struktur: id, judul, deskripsi, tanggal_waktu, prioritas
     */
    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "judul TEXT NOT NULL,"
                + "deskripsi TEXT,"
                + "tanggal_waktu TEXT NOT NULL,"
                + "prioritas TEXT"
                + ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table checked/created successfully!");
        } catch (SQLException e) {
            System.err.println("Failed to create table!");
            e.printStackTrace();
        }
    }
    
    /**
     * Mendapatkan koneksi database
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    // ========== CRUD OPERATIONS ==========
    
    /**
     * CREATE - Menambah agenda baru ke database
     * @param agenda Objek agenda yang akan ditambahkan
     * @return true jika berhasil, false jika gagal
     */
    public boolean insertAgenda(Agenda agenda) {
        String sql = "INSERT INTO " + TABLE_NAME + 
                     " (judul, deskripsi, tanggal_waktu, prioritas) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, agenda.getJudul());
            pstmt.setString(2, agenda.getDeskripsi());
            pstmt.setString(3, agenda.getTanggalWaktu().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(4, agenda.getPrioritas());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Failed to insert agenda!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * READ - Mengambil semua agenda dari database
     * @return List of Agenda objects
     */
    public List<Agenda> getAllAgenda() {
        List<Agenda> agendaList = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY tanggal_waktu ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Agenda agenda = extractAgendaFromResultSet(rs);
                agendaList.add(agenda);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to retrieve agenda!");
            e.printStackTrace();
        }
        
        return agendaList;
    }
    
    /**
     * READ - Mengambil agenda berdasarkan ID
     * @param id ID agenda
     * @return Objek Agenda atau null jika tidak ditemukan
     */
    public Agenda getAgendaById(int id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractAgendaFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get agenda by ID!");
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * READ - Mengambil agenda berdasarkan tanggal
     * @param tanggal Tanggal dalam format LocalDateTime
     * @return List agenda pada tanggal tersebut
     */
    public List<Agenda> getAgendaByDate(LocalDateTime tanggal) {
        List<Agenda> agendaList = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE DATE(tanggal_waktu) = DATE(?) ORDER BY tanggal_waktu ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tanggal.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Agenda agenda = extractAgendaFromResultSet(rs);
                agendaList.add(agenda);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get agenda by date!");
            e.printStackTrace();
        }
        
        return agendaList;
    }
    
    /**
     * READ - Mencari agenda berdasarkan keyword
     * @param keyword Kata kunci pencarian
     * @return List agenda yang cocok
     */
    public List<Agenda> searchAgenda(String keyword) {
        List<Agenda> agendaList = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE judul LIKE ? OR deskripsi LIKE ? " +
                     "ORDER BY tanggal_waktu ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Agenda agenda = extractAgendaFromResultSet(rs);
                agendaList.add(agenda);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to search agenda!");
            e.printStackTrace();
        }
        
        return agendaList;
    }
    
    /**
     * READ - Filter agenda berdasarkan prioritas
     * @param prioritas Prioritas yang dicari
     * @return List agenda dengan prioritas tersebut
     */
    public List<Agenda> getAgendaByPrioritas(String prioritas) {
        List<Agenda> agendaList = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME + 
                     " WHERE prioritas = ? ORDER BY tanggal_waktu ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, prioritas);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Agenda agenda = extractAgendaFromResultSet(rs);
                agendaList.add(agenda);
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get agenda by prioritas!");
            e.printStackTrace();
        }
        
        return agendaList;
    }
    
    /**
     * UPDATE - Mengupdate data agenda
     * @param agenda Objek agenda dengan data baru
     * @return true jika berhasil, false jika gagal
     */
    public boolean updateAgenda(Agenda agenda) {
        String sql = "UPDATE " + TABLE_NAME + 
                     " SET judul=?, deskripsi=?, tanggal_waktu=?, prioritas=? WHERE id=?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, agenda.getJudul());
            pstmt.setString(2, agenda.getDeskripsi());
            pstmt.setString(3, agenda.getTanggalWaktu().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setString(4, agenda.getPrioritas());
            pstmt.setInt(5, agenda.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Failed to update agenda!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * DELETE - Menghapus agenda dari database
     * @param id ID agenda yang akan dihapus
     * @return true jika berhasil, false jika gagal
     */
    public boolean deleteAgenda(int id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Failed to delete agenda!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * DELETE - Hapus semua agenda (untuk testing)
     * @return true jika berhasil
     */
    public boolean deleteAllAgenda() {
        String sql = "DELETE FROM " + TABLE_NAME;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to delete all agenda!");
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Helper method untuk extract data dari ResultSet ke objek Agenda
     * @param rs ResultSet dari query
     * @return Objek Agenda
     * @throws SQLException jika ada error
     */
    private Agenda extractAgendaFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String judul = rs.getString("judul");
        String deskripsi = rs.getString("deskripsi");
        String tanggalWaktuStr = rs.getString("tanggal_waktu");
        String prioritas = rs.getString("prioritas");
        
        // Parse tanggal waktu
        LocalDateTime tanggalWaktu = LocalDateTime.parse(tanggalWaktuStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        return new Agenda(id, judul, deskripsi, tanggalWaktu, prioritas);
    }
    
    /**
     * Get jumlah total agenda
     * @return Jumlah agenda
     */
    public int getTotalAgenda() {
        String sql = "SELECT COUNT(*) as total FROM " + TABLE_NAME;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Failed to get total agenda!");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Menutup koneksi database
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed!");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection!");
            e.printStackTrace();
        }
    }
}
