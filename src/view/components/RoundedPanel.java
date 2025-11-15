/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view.components;

import java.awt.*;
import javax.swing.*;
/**
 *
 * Custom JPanel dengan rounded corners untuk tampilan modern
 * Menerapkan konsep OOP: Inheritance, Encapsulation, Polymorphism
 * @author slozoy
 */

public class RoundedPanel extends JPanel {
    
    // ========== ATTRIBUTES ==========
    private Color backgroundColor;
    private int cornerRadius = 30;
    private boolean shadowEnabled = true;
    private Color shadowColor = new Color(0, 0, 0, 50);
    
    // ========== CONSTRUCTORS ==========
    
    /**
     * Constructor default
     */
    public RoundedPanel() {
        super();
        setOpaque(false); // Penting! Agar background transparan
        backgroundColor = Color.WHITE;
    }
    
    /**
     * Constructor dengan radius custom
     * @param radius Corner radius (default: 15)
     */
    public RoundedPanel(int radius) {
        this();
        this.cornerRadius = radius;
    }
    
    /**
     * Constructor dengan radius dan background color
     * @param radius Corner radius
     * @param bgColor Background color
     */
    public RoundedPanel(int radius, Color bgColor) {
        this(radius);
        this.backgroundColor = bgColor;
    }
    
    /**
     * Constructor lengkap dengan shadow
     * @param radius Corner radius
     * @param bgColor Background color
     * @param enableShadow Enable shadow effect
     */
    public RoundedPanel(int radius, Color bgColor, boolean enableShadow) {
        this(radius, bgColor);
        this.shadowEnabled = enableShadow;
    }
    
    // ========== GETTERS & SETTERS ==========
    
    /**
     * Set corner radius
     * @param radius Radius corner (px)
     */
    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }
    
    /**
     * Get corner radius
     * @return Corner radius
     */
    public int getCornerRadius() {
        return cornerRadius;
    }
    
    /**
     * Set background color
     * @param color Background color
     */
    @Override
    public void setBackground(Color color) {
        this.backgroundColor = color;
        repaint();
    }
    
    /**
     * Get background color
     * @return Background color
     */
    @Override
    public Color getBackground() {
        return backgroundColor;
    }
    
    /**
     * Enable/disable shadow
     * @param enabled Shadow enabled
     */
    public void setShadowEnabled(boolean enabled) {
        this.shadowEnabled = enabled;
        repaint();
    }
    
    /**
     * Set shadow color
     * @param color Shadow color (with alpha for transparency)
     */
    public void setShadowColor(Color color) {
        this.shadowColor = color;
        repaint();
    }
    
    // ========== PAINT METHODS ==========
    
    /**
     * Override paintComponent untuk menggambar rounded panel
     * @param g Graphics object
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Enable anti-aliasing untuk smooth edges
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
                           RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw shadow jika enabled
        if (shadowEnabled) {
            g2.setColor(shadowColor);
            g2.fillRoundRect(3, 3, width - 3, height - 3, cornerRadius, cornerRadius);
        }
        
        // Draw background dengan rounded corners
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, width - 3, height - 3, cornerRadius, cornerRadius);
        
        g2.dispose();
    }
    
    /**
     * Override paintBorder untuk menggambar border rounded
     * @param g Graphics object
     */
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw border (optional - bisa dikustomisasi)
        g2.setColor(new Color(230, 230, 230));
        g2.drawRoundRect(0, 0, width - 4, height - 4, cornerRadius, cornerRadius);
        
        g2.dispose();
    }
}