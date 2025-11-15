/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import model.Agenda;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Class untuk menangani export dan import data
 * Menerapkan konsep OOP: Utility Class, Static Methods
 * Fitur Tantangan: Export/Import ke JSON
 * @author slozoy
 */
public class FileHandler {
    
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    // ========== EXPORT METHOD ==========
    
    /**
     * Export data agenda ke file JSON
     * @param agendaList List agenda yang akan di-export
     * @param parentComponent Component parent untuk dialog
     * @return true jika berhasil
     */
    public static boolean exportToJSON(List<Agenda> agendaList, java.awt.Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Ekspor Agenda ke JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        fileChooser.setSelectedFile(new File("agenda_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json"));
        
        int userSelection = fileChooser.showSaveDialog(parentComponent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .json extension
            if (!fileToSave.getName().toLowerCase().endsWith(".json")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".json");
            }
            
            try (Writer writer = new FileWriter(fileToSave)) {
                // Create export data wrapper (optional - for metadata)
                ExportData exportData = new ExportData();
                exportData.setExportDate(LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                exportData.setTotalAgenda(agendaList.size());
                exportData.setAgendaList(agendaList);
                
                // Export dengan metadata
                gson.toJson(exportData, writer);
                
                System.out.println("Export to JSON successful: " + fileToSave.getAbsolutePath());
                return true;
                
            } catch (IOException e) {
                System.err.println("Failed to export to JSON!");
                e.printStackTrace();
                return false;
            }
        }
        
        return false;
    }
    
    // ========== IMPORT METHOD ==========
    
    /**
     * Import data agenda dari file JSON
     * @param parentComponent Component parent untuk dialog
     * @return List agenda yang di-import atau null jika gagal
     */
    public static List<Agenda> importFromJSON(java.awt.Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Impor Agenda JSON");
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Files (*.json)", "json"));
        
        int userSelection = fileChooser.showOpenDialog(parentComponent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            
            try (Reader reader = new FileReader(fileToOpen)) {
                // Try to parse as ExportData (with metadata)
                try {
                    ExportData exportData = gson.fromJson(reader, ExportData.class);
                    
                    if (exportData != null && exportData.getAgendaList() != null) {
                        System.out.println("Import from JSON successful!");
                        System.out.println("  Export Date: " + exportData.getExportDate());
                        System.out.println("  Total Agenda: " + exportData.getTotalAgenda());
                        return exportData.getAgendaList();
                    }
                } catch (Exception e) {
                    // Fallback: Try to parse as plain List<Agenda>
                    System.out.println("Trying to parse as plain list...");
                }
                
                // Fallback: Parse directly as List<Agenda>
                try (Reader reader2 = new FileReader(fileToOpen)) {
                    List<Agenda> agendaList = gson.fromJson(reader2, 
                            new TypeToken<List<Agenda>>(){}.getType());
                    
                    System.out.println("Import from JSON successful (plain format)!");
                    return agendaList;
                }
                
            } catch (IOException e) {
                System.err.println("Failed to import from JSON!");
                e.printStackTrace();
                return null;
            }
        }
        
        return null;
    }
    
    // ========== HELPER CLASS ==========
    
    /**
     * Wrapper class untuk export data dengan metadata
     */
    private static class ExportData {
        private String exportDate;
        private int totalAgenda;
        private String appVersion = "1.0";
        private List<Agenda> agendaList;
        
        public String getExportDate() {
            return exportDate;
        }
        
        public void setExportDate(String exportDate) {
            this.exportDate = exportDate;
        }
        
        public int getTotalAgenda() {
            return totalAgenda;
        }
        
        public void setTotalAgenda(int totalAgenda) {
            this.totalAgenda = totalAgenda;
        }
        
        public String getAppVersion() {
            return appVersion;
        }
        
        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }
        
        public List<Agenda> getAgendaList() {
            return agendaList;
        }
        
        public void setAgendaList(List<Agenda> agendaList) {
            this.agendaList = agendaList;
        }
    }
}

/**
 * Adapter untuk serialisasi/deserialisasi LocalDateTime ke JSON
 * Menggunakan format ISO standard
 */
class LocalDateTimeAdapter extends com.google.gson.TypeAdapter<LocalDateTime> {
    
    private static final DateTimeFormatter formatter = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void write(com.google.gson.stream.JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(formatter));
        }
    }
    
    @Override
    public LocalDateTime read(com.google.gson.stream.JsonReader in) throws IOException {
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
}
