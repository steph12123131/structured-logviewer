package org.logviewer.ui;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;
import org.logviewer.listener.LogTagListener;
import org.logviewer.model.LogTableModel;
import org.jdesktop.swingx.JXTable;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogTable extends JXTable {

    private final List<LogTagListener> listeners = new ArrayList<>();

    public void addLogTagListener(LogTagListener listener) {
        listeners.add(listener);
    }

    public void removeLogTagListener(LogTagListener listener) {
        listeners.remove(listener);
    }

    public LogTagContextMenu getContextMenu() {
        if (contextMenu == null) {
            contextMenu = new LogTagContextMenu();
        }
        return contextMenu;
    }

    private LogTagContextMenu contextMenu;

    public LogTable() {


        ToolTipManager.sharedInstance().registerComponent(this);
        putClientProperty("useDTCRColorMemoryHack", false);
        // Ajout d'un listener pour capter les double-clics
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = rowAtPoint(e.getPoint());
                    int col = columnAtPoint(e.getPoint());
                    if (row >= 0 && col >= 0) {
                        Object value = getValueAt(row, col);
                        String colName = getModel().getColumnName(col);
                        listeners.forEach(l -> l.logTagValueDoubleClicked(new LogTag(Arrays.stream(colName.split("\\.")).toList()), value));

                    }
                }
            }
        });

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                Log log = ((LogTableModel) table.getModel()).getLogs().get(row);


                if (!isSelected) {
                    switch (log.getLevel()) {
                        case "ERROR":
                            comp.setBackground(JBColor.RED);
                            comp.setForeground(Color.WHITE);
                            break;
                        case "WARN":
                            comp.setBackground(JBColor.ORANGE);
                            comp.setForeground(Color.BLACK);
                            break;
                        default:
                            comp.setBackground(UIUtil.getTableBackground());
                            comp.setForeground(UIUtil.getTableForeground());
                            break;
                    }
                } else {
                    // Conserver les couleurs de sélection
                    comp.setBackground(UIUtil.getTableSelectionBackground());
                    comp.setForeground(UIUtil.getTableSelectionForeground());
                }

                return comp;
            }
        });

        addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mousePressed(MouseEvent e) {
                        displayMenuContextuel(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        displayMenuContextuel(e);
                    }

                    private void displayMenuContextuel(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            int row = rowAtPoint(e.getPoint());
                            int column = columnAtPoint(e.getPoint());
                            Log log = ((LogTableModel) getModel()).getLogs().get(row);
                            getContextMenu().setStackTrace(log.getStackTrace());

                            getContextMenu().setValue(getModel().getValueAt(row, column));
                            getContextMenu().setProject(((LogTableModel) getModel()).getLogs().get(row).getProject());
                            if (row >= 0 && row < getRowCount()) {
                                setRowSelectionInterval(row, row);
                            }

                            // Afficher le menu
                            getContextMenu().show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
        );
    }

    @Override
    public String getToolTipText(@NotNull MouseEvent event) {
        int row = rowAtPoint(event.getPoint());
        int col = columnAtPoint(event.getPoint());

        if (row > -1 && col > -1) {
            Object value = getValueAt(row, col);
            return "<html><pre>" + value + "</pre></html>";
        }
        return null;
    }

    @Override
    protected void resetDefaultTableCellRendererColors(Component renderer, int row, int column) {
        super.resetDefaultTableCellRendererColors(renderer, row, column);
    }
}


