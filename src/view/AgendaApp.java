/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package view;

import model.Agenda;
import database.DatabaseHelper;
import util.FileHandler;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author slozoy
 */
public class AgendaApp extends javax.swing.JFrame {
    
    // ========== ATTRIBUTES ==========
    private DatabaseHelper dbHelper;
    private DefaultTableModel tableModel;
    private int selectedAgendaId = -1; // -1 berarti tidak ada yang dipilih
    private javax.swing.Timer searchTimer; // Untuk debouncing search
    
    /**
     * Creates new form AgendaApp
     */
    public AgendaApp() {
        initComponents();
        customInit(); // Inisialisasi custom setelah initComponents
    }
    
    // ========== CUSTOM INITIALIZATION ==========
    
    /**
     * Custom initialization
     * Setup database, table, dan konfigurasi tambahan
     */
    private void customInit() {
        // Initialize Database
        dbHelper = DatabaseHelper.getInstance();
        
        // Set window properties
        setLocationRelativeTo(null); // Center window
        
        // Setup Table
        setupTable();
        
        // Setup Time Spinner
        setupTimeSpinner();
        
        // Setup Real-Time Search & Filter
        setupRealTimeSearchAndFilter();
        
        // Setup Keyboard Shortcuts
        setupKeyboardShortcuts();
        
        // Setup Table Highlighting
        setupTableHighlighting();
        
        // Load initial data
        loadAllAgenda();
        
        // Disable edit/hapus button initially
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
    }
    
    // ========== SETUP METHODS ==========
    
    /**
     * Setup table model dan properties
     */
    private void setupTable() {
        // Define columns (sesuai dengan kebutuhan tanpa lokasi & kategori)
        String[] columns = {"ID", "Judul", "Tanggal", "Waktu", "Prioritas"};
        
        // Create table model (read-only)
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Set model to table
        tblAgenda.setModel(tableModel);
        
        // Set column widths
        tblAgenda.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tblAgenda.getColumnModel().getColumn(1).setPreferredWidth(200);  // Judul
        tblAgenda.getColumnModel().getColumn(2).setPreferredWidth(100);  // Tanggal
        tblAgenda.getColumnModel().getColumn(3).setPreferredWidth(80);   // Waktu
        tblAgenda.getColumnModel().getColumn(4).setPreferredWidth(100);  // Prioritas
        
        // Hide ID column (tetap ada tapi tidak ditampilkan)
        tblAgenda.getColumnModel().getColumn(0).setMinWidth(0);
        tblAgenda.getColumnModel().getColumn(0).setMaxWidth(0);
        tblAgenda.getColumnModel().getColumn(0).setWidth(0);
        
        // Add row sorter for sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        tblAgenda.setRowSorter(sorter);
        
        // Add selection listener
        tblAgenda.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableRowSelected();
            }
        });
        
        // Styling
        tblAgenda.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAgenda.setRowHeight(25);
        tblAgenda.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblAgenda.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    }
    
    /**
     * Setup JSpinner untuk waktu
     * Menggunakan SpinnerDateModel dengan format HH:mm
     */
    private void setupTimeSpinner() {
        // Create SpinnerDateModel untuk waktu
        SpinnerDateModel timeModel = new SpinnerDateModel();
        timeModel.setCalendarField(Calendar.MINUTE);
        
        spinnerWaktu.setModel(timeModel);
        
        // Format tampilan hanya jam:menit
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(spinnerWaktu, "HH:mm");
        spinnerWaktu.setEditor(timeEditor);
        
        // Set default value ke waktu sekarang
        spinnerWaktu.setValue(new Date());
        
        System.out.println("Time Spinner setup completed");
    }
    
    /**
     * Setup real-time search dan filter
     */
    private void setupRealTimeSearchAndFilter() {
        // ========== REAL-TIME SEARCH ==========

        // Set initial placeholder
        txtCari.setForeground(Color.GRAY);
        txtCari.setText("Cari agenda....");

        // DocumentListener untuk real-time search dengan debouncing
        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                scheduleSearch();
            }
        });

        // Setup debounce timer (300ms delay)
        searchTimer = new javax.swing.Timer(300, e -> performSearch());
        searchTimer.setRepeats(false);

        // Placeholder behavior - Improved
        txtCari.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                // Hapus placeholder saat focus
                if (txtCari.getText().equals("Cari agenda....")) {
                    txtCari.setText("");
                    txtCari.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                // Kembalikan placeholder jika kosong
                String text = txtCari.getText().trim();
                if (text.isEmpty()) {
                    txtCari.setForeground(Color.GRAY);
                    txtCari.setText("Cari agenda....");
                    // Trigger search untuk menampilkan semua data
                    performSearch();
                }
            }
        });

        // ========== REAL-TIME FILTER ==========

        // ComboBox filter otomatis trigger search
        cmbFilterPrioritas.addActionListener(e -> {
            if (cmbFilterPrioritas.getSelectedItem() != null) {
                performSearch();
            }
        });

        System.out.println("Real-time search & filter setup completed");
    }
    
    /**
     * Setup table highlighting untuk search results
     */
    private void setupTableHighlighting() {
        tblAgenda.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);

                if (!isSelected) {
                    String keyword = txtCari.getText().trim().toLowerCase();

                    // Highlight HANYA jika ada keyword valid (bukan placeholder dan bukan kosong)
                    boolean hasValidKeyword = !keyword.isEmpty() && 
                                             !keyword.equals("cari agenda....");

                    if (hasValidKeyword) {
                        if (value != null && value.toString().toLowerCase().contains(keyword)) {
                            c.setBackground(new Color(255, 255, 200)); // Yellow highlight
                        } else {
                            // Alternate row colors
                            c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                        }
                    } else {
                        // Alternate row colors (no highlight)
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                    }
                }

                return c;
            }
        });

        System.out.println("Table highlighting setup completed");
    }
    
    // ========== DATA METHODS ==========
    
    /**
     * Load semua agenda dari database ke table
     */
    private void loadAllAgenda() {
        // Clear table
        tableModel.setRowCount(0);
        
        // Get data from database
        List<Agenda> agendaList = dbHelper.getAllAgenda();
        
        // Populate table
        for (Agenda agenda : agendaList) {
            Object[] row = {
                agenda.getId(),
                agenda.getJudul(),
                agenda.getTanggalFormatted(),
                agenda.getWaktuFormatted(),
                agenda.getPrioritas()
            };
            tableModel.addRow(row);
        }
        
        System.out.println("Loaded " + agendaList.size() + " agenda(s)");
    }
    
    /**
     * Schedule search dengan debouncing
     * Menghindari search terlalu sering saat user masih mengetik
     */
    private void scheduleSearch() {
        if (searchTimer.isRunning()) {
            searchTimer.restart();
        } else {
            searchTimer.start();
        }
    }
    
    /**
     * Perform search and filter (Real-time)
     * Method utama untuk pencarian dan filtering
     * Diperbaiki: Search dan Filter bisa bekerja independen atau bersamaan
     */
    private void performSearch() {
        // Get search keyword
        String keyword = txtCari.getText().trim();

        // Cek apakah keyword adalah placeholder atau kosong
        if (keyword.equals("Cari agenda....") || keyword.isEmpty()) {
            keyword = ""; // Set kosong agar tidak mencari placeholder
        }

        // Get filter selection
        String selectedPrioritas = (String) cmbFilterPrioritas.getSelectedItem();

        // Clear table
        tableModel.setRowCount(0);

        // Get data from database
        List<Agenda> results;

        // Jika keyword kosong, ambil semua data
        if (keyword.isEmpty()) {
            results = dbHelper.getAllAgenda();
        } else {
            // Jika ada keyword, lakukan search
            results = dbHelper.searchAgenda(keyword);
        }

        // Apply filter prioritas HANYA jika ada pilihan valid
        List<Agenda> filteredResults;

        // Cek apakah filter prioritas dipilih (bukan default/placeholder)
        boolean hasValidPrioritasFilter = selectedPrioritas != null && 
                                          !selectedPrioritas.equals("-- Semua Prioritas --") &&
                                          !selectedPrioritas.equals("- Pilih Prioritas -") &&
                                          !selectedPrioritas.trim().isEmpty();

        if (hasValidPrioritasFilter) {
            // Filter by prioritas jika dipilih
            filteredResults = results.stream()
                    .filter(agenda -> agenda.getPrioritas().equals(selectedPrioritas))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // Tidak filter, gunakan semua hasil
            filteredResults = results;
        }

        // Populate table with filtered results
        for (Agenda agenda : filteredResults) {
            Object[] row = {
                agenda.getId(),
                agenda.getJudul(),
                agenda.getTanggalFormatted(),
                agenda.getWaktuFormatted(),
                agenda.getPrioritas()
            };
            tableModel.addRow(row);
        }

        // Build status message yang lebih informatif
        String statusMessage = buildSearchStatus(filteredResults.size(), keyword, 
                                                 hasValidPrioritasFilter, selectedPrioritas);
        System.out.println(statusMessage);
    }
    
    /**
     * Build status message untuk hasil search/filter
     * @param resultCount Jumlah hasil
     * @param keyword Kata kunci search
     * @param hasPrioritasFilter Apakah ada filter prioritas
     * @param prioritas Nilai prioritas yang dipilih
     * @return Status message
     */
   private String buildSearchStatus(int resultCount, String keyword, 
                                    boolean hasPrioritasFilter, String prioritas) {
       StringBuilder status = new StringBuilder();

       if (resultCount == 0) {
           status.append("Tidak ada agenda yang ditemukan");
       } else {
           status.append("Ditemukan ").append(resultCount).append(" agenda");
       }

       // Tambahkan info search keyword jika ada
       if (!keyword.isEmpty()) {
           status.append(" | Pencarian: \"").append(keyword).append("\"");
       }

       // Tambahkan info filter prioritas jika ada
       if (hasPrioritasFilter) {
           status.append(" | Filter: ").append(prioritas);
       }

       // Jika tidak ada filter sama sekali
       if (keyword.isEmpty() && !hasPrioritasFilter) {
           status.append(" (Semua data)");
       }

       return status.toString();
   }
    
    /**
     * Event handler ketika row di table dipilih
     */
    private void onTableRowSelected() {
        int selectedRow = tblAgenda.getSelectedRow();
        
        if (selectedRow != -1) {
            // Convert view index to model index (karena ada sorting)
            int modelRow = tblAgenda.convertRowIndexToModel(selectedRow);
            
            // Get ID from hidden column
            selectedAgendaId = (int) tableModel.getValueAt(modelRow, 0);
            
            // Load data ke form
            Agenda agenda = dbHelper.getAgendaById(selectedAgendaId);
            
            if (agenda != null) {
                txtJudul.setText(agenda.getJudul());
                txtDeskripsi.setText(agenda.getDeskripsi());
                
                // Set date
                Date date = Date.from(agenda.getTanggalWaktu()
                        .atZone(ZoneId.systemDefault()).toInstant());
                dateChooser.setDate(date);
                
                // Set waktu ke JSpinner
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, agenda.getTanggalWaktu().getHour());
                cal.set(Calendar.MINUTE, agenda.getTanggalWaktu().getMinute());
                spinnerWaktu.setValue(cal.getTime());
                
                // Set prioritas
                cmbPrioritas.setSelectedItem(agenda.getPrioritas());
                
                // Enable edit/delete buttons
                btnEdit.setEnabled(true);
                btnHapus.setEnabled(true);
                
                System.out.println("Agenda selected: " + agenda.getJudul());
            }
        } else {
            clearSelection();
        }
    }
    
    /**
     * Clear selection dan disable buttons
     */
    private void clearSelection() {
        selectedAgendaId = -1;
        tblAgenda.clearSelection();
        btnEdit.setEnabled(false);
        btnHapus.setEnabled(false);
    }
    
    /**
     * Bersihkan form input
     */
    private void clearForm() {
        // Clear form input
        txtJudul.setText("");
        txtDeskripsi.setText("");
        dateChooser.setDate(new Date());
        spinnerWaktu.setValue(new Date());
        cmbPrioritas.setSelectedIndex(0);

        // Clear selection
        clearSelection();

        // Reset search box ke placeholder
        txtCari.setForeground(Color.GRAY);
        txtCari.setText("Cari agenda....");

        // Reset filter prioritas ke default
        cmbFilterPrioritas.setSelectedIndex(0);

        // Reload semua data tanpa filter
        loadAllAgenda();
        System.out.println("Form cleared");
    }
    
    /**
     * Validasi input form
     * @return true jika valid, false jika tidak
     */
    private boolean validateInput() {
        // Validasi Judul
        if (txtJudul.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Judul agenda tidak boleh kosong!", 
                    "Validasi Error", 
                    JOptionPane.WARNING_MESSAGE);
            txtJudul.requestFocus();
            return false;
        }
        
        if (txtJudul.getText().trim().length() < 3) {
            JOptionPane.showMessageDialog(this, 
                    "Judul agenda minimal 3 karakter!", 
                    "Validasi Error", 
                    JOptionPane.WARNING_MESSAGE);
            txtJudul.requestFocus();
            return false;
        }
        
        // Validasi Tanggal
        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this, 
                    "Tanggal harus dipilih!", 
                    "Validasi Error", 
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Validasi Prioritas
        if (cmbPrioritas.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, 
                    "Prioritas harus dipilih!", 
                    "Validasi Error", 
                    JOptionPane.WARNING_MESSAGE);
            cmbPrioritas.requestFocus();
            return false;
        }
        
        // Validasi waktu tidak masa lalu
        Date selectedDate = dateChooser.getDate();
        Date selectedTime = (Date) spinnerWaktu.getValue();
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedTime);
        int jam = cal.get(Calendar.HOUR_OF_DAY);
        int menit = cal.get(Calendar.MINUTE);
        
        LocalDateTime selectedDateTime = selectedDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withHour(jam)
                .withMinute(menit)
                .withSecond(0);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (selectedDateTime.isBefore(now)) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Waktu yang dipilih sudah lewat.\nLanjutkan?",
                    "Peringatan Waktu",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if (confirm != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Konversi input form ke objek Agenda
     * @return Objek Agenda
     */
    private Agenda getAgendaFromForm() {
        String judul = txtJudul.getText().trim();
        String deskripsi = txtDeskripsi.getText().trim();
        String prioritas = (String) cmbPrioritas.getSelectedItem();
        
        // Parse tanggal
        Date selectedDate = dateChooser.getDate();
        LocalDateTime tanggalWaktu = selectedDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        // Parse waktu dari JSpinner
        Date selectedTime = (Date) spinnerWaktu.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedTime);
        
        int jam = cal.get(Calendar.HOUR_OF_DAY);
        int menit = cal.get(Calendar.MINUTE);
        
        tanggalWaktu = tanggalWaktu.withHour(jam).withMinute(menit).withSecond(0);
        
        if (selectedAgendaId == -1) {
            // New agenda
            return new Agenda(judul, deskripsi, tanggalWaktu, prioritas);
        } else {
            // Edit agenda
            return new Agenda(selectedAgendaId, judul, deskripsi, tanggalWaktu, prioritas);
        }
    }
    
    /**
     * Setup keyboard shortcuts
     * Panggil method ini di customInit()
     */
   private void setupKeyboardShortcuts() {
       // Ctrl+F untuk focus ke search
       txtCari.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
               .put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 
                    java.awt.event.InputEvent.CTRL_DOWN_MASK), "focusSearch");
       txtCari.getActionMap().put("focusSearch", new AbstractAction() {
           @Override
           public void actionPerformed(java.awt.event.ActionEvent e) {
               txtCari.requestFocus();
               if (!txtCari.getText().equals("Cari agenda....")) {
                   txtCari.selectAll();
               }
           }
       });

       System.out.println("Keyboard shortcuts setup completed (Ctrl+F)");
   }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblJudul = new javax.swing.JLabel();
        txtJudul = new javax.swing.JTextField();
        lblDeskripsi = new javax.swing.JLabel();
        scrollDeskripsi = new javax.swing.JScrollPane();
        txtDeskripsi = new javax.swing.JTextArea();
        lblTanggal = new javax.swing.JLabel();
        dateChooser = new com.toedter.calendar.JDateChooser();
        lblWaktu = new javax.swing.JLabel();
        lblPrioritas = new javax.swing.JLabel();
        cmbPrioritas = new javax.swing.JComboBox<>();
        btnEdit = new javax.swing.JButton();
        btnBersih = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnSimpan = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        scrollTable = new javax.swing.JScrollPane();
        tblAgenda = new javax.swing.JTable();
        txtCari = new javax.swing.JTextField();
        cmbFilterPrioritas = new javax.swing.JComboBox<>();
        btnExportJSON = new javax.swing.JButton();
        btnImportJSON = new javax.swing.JButton();
        spinnerWaktu = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Agenda Pribadi");
        setResizable(false);

        jLabel1.setText("2310010054 - Said Muhdaffa Hasyim");

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblTitle.setText("Aplikasi Agenda Pribadi");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        lblJudul.setText("Judul Agenda:");

        lblDeskripsi.setText("Deskripsi Agenda:");

        txtDeskripsi.setColumns(20);
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setRows(5);
        txtDeskripsi.setWrapStyleWord(true);
        scrollDeskripsi.setViewportView(txtDeskripsi);

        lblTanggal.setText("Tanggal:");

        dateChooser.setDateFormatString("d MMMM yyyy");

        lblWaktu.setText("Waktu:");

        lblPrioritas.setText("Prioritas:");

        cmbPrioritas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "- Pilih Prioritas -", "Rendah", "Sedang", "Tinggi", "Urgent" }));

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnBersih.setText("Reset");
        btnBersih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBersihActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        tblAgenda.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Judul", "Tanggal", "Waktu", "Prioritas"
            }
        ));
        scrollTable.setViewportView(tblAgenda);

        txtCari.setText("Cari agenda....");

        cmbFilterPrioritas.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "- Pilih Prioritas -", "Rendah", "Sedang", "Tinggi", "Urgent" }));

        btnExportJSON.setText("Ekspor ke JSON");
        btnExportJSON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportJSONActionPerformed(evt);
            }
        });

        btnImportJSON.setText("Muat JSON");
        btnImportJSON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportJSONActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnBersih, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblPrioritas)
                            .addComponent(lblJudul)
                            .addComponent(lblDeskripsi)
                            .addComponent(lblTanggal)
                            .addComponent(lblWaktu))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(scrollDeskripsi, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                            .addComponent(txtJudul)
                            .addComponent(dateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cmbPrioritas, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spinnerWaktu))))
                .addGap(32, 32, 32)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrollTable, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCari)
                            .addComponent(cmbFilterPrioritas, 0, 266, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnExportJSON, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnImportJSON, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(scrollTable, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnExportJSON))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbFilterPrioritas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnImportJSON))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblJudul)
                            .addComponent(txtJudul, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(scrollDeskripsi, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblDeskripsi)
                                .addGap(95, 95, 95)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTanggal))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblWaktu)
                            .addComponent(spinnerWaktu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblPrioritas)
                            .addComponent(cmbPrioritas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnBersih, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnHapus, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(32, 32, 32))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(359, 359, 359)
                        .addComponent(lblTitle))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(456, 456, 456)
                        .addComponent(jLabel1)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTitle)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    // ========== EVENT HANDLERS ==========
    /**
     * Handler untuk tombol Edit
     * Mengupdate agenda yang dipilih
     */
    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        if (selectedAgendaId == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Pilih agenda yang akan diedit terlebih dahulu!", 
                    "Peringatan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateInput()) {
            return;
        }
        
        Agenda agenda = getAgendaFromForm();
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin mengupdate agenda ini?", 
                "Konfirmasi Update", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dbHelper.updateAgenda(agenda)) {
                JOptionPane.showMessageDialog(this, 
                        "Agenda berhasil diupdate!", 
                        "Sukses", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                loadAllAgenda();
                clearForm();
                System.out.println("Agenda diupdate: " + agenda.getJudul());
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Gagal mengupdate agenda!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnEditActionPerformed
    
    /**
     * Handler untuk tombol Reset/Bersih
     * Membersihkan form, selection, search, dan filter
     */
    private void btnBersihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBersihActionPerformed
        // TODO add your handling code here:
        clearForm();
        JOptionPane.showMessageDialog(this, 
            "Form telah dibersihkan!\nPencarian dan filter direset.", 
            "Reset Berhasil", 
            JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_btnBersihActionPerformed
    
     /**
     * Handler untuk tombol Hapus
     * Menghapus agenda yang dipilih
     */
    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        if (selectedAgendaId == -1) {
            JOptionPane.showMessageDialog(this, 
                    "Pilih agenda yang akan dihapus terlebih dahulu!", 
                    "Peringatan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Apakah Anda yakin ingin menghapus agenda ini?\n" +
                "Tindakan ini tidak dapat dibatalkan!", 
                "Konfirmasi Hapus", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dbHelper.deleteAgenda(selectedAgendaId)) {
                JOptionPane.showMessageDialog(this, 
                        "Agenda berhasil dihapus!", 
                        "Sukses", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                loadAllAgenda();
                clearForm();
                System.out.println("Agenda dihapus (ID: " + selectedAgendaId + ")");
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Gagal menghapus agenda!", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnHapusActionPerformed
    
    /**
     * Handler untuk tombol Simpan
     * Menambah agenda baru ke database
     */
    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        if (!validateInput()) {
            return;
        }
        
        Agenda agenda = getAgendaFromForm();
        
        if (dbHelper.insertAgenda(agenda)) {
            JOptionPane.showMessageDialog(this, 
                    "Agenda berhasil disimpan!", 
                    "Sukses", 
                    JOptionPane.INFORMATION_MESSAGE);
            
            loadAllAgenda();
            clearForm();
            System.out.println("Agenda baru ditambahkan: " + agenda.getJudul());
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Gagal menyimpan agenda!", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSimpanActionPerformed
    
    /**
     * Handler untuk tombol Export JSON
     * Export data ke file JSON
     */
    private void btnExportJSONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportJSONActionPerformed
        // TODO add your handling code here:
        List<Agenda> agendaList = dbHelper.getAllAgenda();
        
        if (agendaList.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Tidak ada data untuk di-export!", 
                    "Peringatan", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (FileHandler.exportToJSON(agendaList, this)) {
            JOptionPane.showMessageDialog(this, 
                    "Data berhasil di-export ke JSON!\n" +
                    "Total: " + agendaList.size() + " agenda", 
                    "Sukses", 
                    JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Export to JSON successful (" + agendaList.size() + " agenda)");
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Export dibatalkan atau gagal!", 
                    "Info", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnExportJSONActionPerformed
    
    /**
     * Handler untuk tombol Import JSON
     * Import data dari file JSON
     */
    private void btnImportJSONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportJSONActionPerformed
        // TODO add your handling code here:
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Import akan menambahkan data baru ke database.\n" +
                "Data yang sudah ada tidak akan dihapus.\n\n" +
                "Lanjutkan?", 
                "Konfirmasi Import", 
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            List<Agenda> importedList = FileHandler.importFromJSON(this);
            
            if (importedList != null && !importedList.isEmpty()) {
                int successCount = 0;
                int failedCount = 0;
                
                for (Agenda agenda : importedList) {
                    // Reset ID untuk auto-increment (buat agenda baru)
                    Agenda newAgenda = new Agenda(
                        agenda.getJudul(),
                        agenda.getDeskripsi(),
                        agenda.getTanggalWaktu(),
                        agenda.getPrioritas()
                    );
                    
                    if (dbHelper.insertAgenda(newAgenda)) {
                        successCount++;
                    } else {
                        failedCount++;
                    }
                }
                
                JOptionPane.showMessageDialog(this, 
                        "Import selesai!\n\n" +
                        "Berhasil: " + successCount + " agenda\n" +
                        "Gagal: " + failedCount + " agenda\n" +
                        "Total: " + importedList.size() + " agenda", 
                        "Sukses", 
                        JOptionPane.INFORMATION_MESSAGE);
                
                loadAllAgenda();
                System.out.println("Import completed: " + successCount + "/" + importedList.size() + " success");
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Import dibatalkan atau file kosong/tidak valid!", 
                        "Info", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnImportJSONActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AgendaApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AgendaApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AgendaApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AgendaApp.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AgendaApp().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBersih;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExportJSON;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnImportJSON;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cmbFilterPrioritas;
    private javax.swing.JComboBox<String> cmbPrioritas;
    private com.toedter.calendar.JDateChooser dateChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblDeskripsi;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JLabel lblPrioritas;
    private javax.swing.JLabel lblTanggal;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblWaktu;
    private javax.swing.JScrollPane scrollDeskripsi;
    private javax.swing.JScrollPane scrollTable;
    private javax.swing.JSpinner spinnerWaktu;
    private javax.swing.JTable tblAgenda;
    private javax.swing.JTextField txtCari;
    private javax.swing.JTextArea txtDeskripsi;
    private javax.swing.JTextField txtJudul;
    // End of variables declaration//GEN-END:variables
}
