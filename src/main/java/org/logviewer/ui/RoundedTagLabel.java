package org.logviewer.ui;

import javax.swing.*;
import java.awt.*;

public class RoundedTagLabel extends JLabel {

    private Color backgroundColor;
    private Color borderColor;
    private int arcRadius;

    public RoundedTagLabel(String text, Color backgroundColor, Color textColor, Color borderColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.arcRadius = 999; // pleinement arrondi (pill shape)

        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.PLAIN, 10));
        setOpaque(false); // important : laisser paintComponent gérer le fond
        setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    }

    public void setDisableBorder(boolean disable) {
        setOpaque(disable);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = Math.min(arcRadius, h); // evite un arc trop grand

        // fond
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // bordure
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }

}