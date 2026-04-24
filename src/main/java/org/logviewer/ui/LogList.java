package org.logviewer.ui;

import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;
import org.logviewer.listener.LogTagListener;
import org.logviewer.listener.LogTagListSelectionListener;
import org.logviewer.model.LogListModel;
import org.logviewer.renderer.LogListRenderer;
import org.logviewer.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class LogList extends JList<Log> implements LogTagListSelectionListener {

    private final List<LogTagListener> listeners = new ArrayList<>();

    private final LogListRenderer cellRenderer;
    private LogTagContextMenu tagContextMenu;
    private LogContextMenu contextMenu;


    public void addLogTagListener(LogTagListener listener) {
        listeners.add(listener);
    }

    public void removeLogTabListener(LogTagListener listener) {
        listeners.remove(listener);
    }

    public LogList(Settings settings) {
        super();
        cellRenderer = new LogListRenderer(settings);
        setCellRenderer(cellRenderer);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseEvent(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // Pour le tooltip dynamique
                updateTooltip(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = locationToIndex(e.getPoint());
                    Log log = ((LogListModel) getModel()).getLogs().get(row);
                    LogListTagLabel label = getLabelAt(e);
                    if (label != null) {

                        listeners.forEach(l -> l.logTagValueDoubleClicked(label.getTag(), label.getText()));
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateTooltip(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });
    }

    @Override
    public void tagAdded(LogTag tag) {
        cellRenderer.addTag(tag);
        ((LogListModel) getModel()).refresh();
        revalidate();
        repaint();
    }

    @Override
    public void tagRemoved(LogTag tag) {
        cellRenderer.removeTag(tag);
        ((LogListModel) getModel()).refresh();
        revalidate();
        repaint();
    }

    private LogListTagLabel getLabelAt(MouseEvent e) {
        JList<Log> list = (JList<Log>) e.getSource();
        int index = list.locationToIndex(e.getPoint());
        if (index < 0) return null;

        // Récupérer le renderer rendu pour cet index
        LogListItem renderer = (LogListItem) list.getCellRenderer()
                .getListCellRendererComponent(list, list.getModel().getElementAt(index), index, false, false);

        // Translater le point dans le repère du renderer
        Rectangle cellBounds = list.getCellBounds(index, index);
        Point pointInCell = new Point(e.getX() - cellBounds.x, e.getY() - cellBounds.y);

        // Ici, avant de chercher le composant enfant
        renderer.setSize(cellBounds.getSize());
        renderer.doLayout();

        // Trouver le composant enfant sous le curseur
        Component child = SwingUtilities.getDeepestComponentAt(renderer, pointInCell.x, pointInCell.y);
        return (child instanceof LogListTagLabel) ? (LogListTagLabel) child : null;
    }

    private void updateTooltip(MouseEvent e) {
        JList<?> list = (JList<?>) e.getSource();
        LogListTagLabel label = getLabelAt(e);
        list.setToolTipText(label != null ? label.getTooltipText() : null);
    }

    private void handleMouseEvent(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        LogListTagLabel label = getLabelAt(e);

        int row = locationToIndex(e.getPoint());
        Log log = ((LogListModel) getModel()).getLogs().get(row);

        if (log == null) return;
        if (label == null) {
            getContextMenu().setLog(log);
            getContextMenu().show(e.getComponent(), e.getX(), e.getY());
            return;

        };
        getTagContextMenu().setStackTrace(log.getStackTrace());
        getTagContextMenu().setValue(label.getTooltipText());
        getTagContextMenu().setProject(log.getProject());
              // Afficher le menu
        getTagContextMenu().show(e.getComponent(), e.getX(), e.getY());
    }
    public LogTagContextMenu getTagContextMenu() {
        if (tagContextMenu == null) {
            tagContextMenu = new LogTagContextMenu();
        }
        return tagContextMenu;
    }

    public LogContextMenu getContextMenu() {
        if (contextMenu == null) {
            contextMenu = new LogContextMenu();
        }
        return contextMenu;
    }

    public void setWrap(boolean wrap) {
        cellRenderer.setWrap(wrap);
        ((LogListModel) getModel()).refresh();
        revalidate();
        repaint();
    }

    public void clear() {
        cellRenderer.clear();
    }
}
