package org.logviewer.renderer;

import org.logviewer.settings.Settings;
import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;
import org.logviewer.ui.LogListItem;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.synth.SynthListUI;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class LogListRenderer extends DefaultListCellRenderer {


    public static final int LINE_PX_LENGTH = 4096;
    LogListItem item = new LogListItem();
    private List<LogTag> tags;
    private final Settings settings;


    public LogListRenderer(Settings settings) {
        this.settings=settings;
        clear();
    }


    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setComponentOrientation(list.getComponentOrientation());

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            bg = UIManager.getColor("List.dropCellBackground");
            fg = UIManager.getColor("List.dropCellForeground");

            isSelected = true;
        }

        if (isSelected) {
            item.setBackground(bg == null ? list.getSelectionBackground() : bg);
        } else {
            item.setBackground(index % 2 == 0 ? list.getBackground() : list.getBackground().darker());
        }

        if (value instanceof Icon) {
            setIcon((Icon)value);
            setText("");
        }
        else {
            setIcon(null);
            setText((value == null) ? "" : value.toString());
        }

        if (list.getName() == null || !list.getName().equals("ComboBox.list")
                || !(list.getUI() instanceof SynthListUI)) {
            setEnabled(list.isEnabled());
        }

        setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = UIManager.getBorder("List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder();
        }
        setBorder(border);
        if (value instanceof Log log) {
            item.setValues(log);
        }
        int listWidth = list.getWidth();
        int layoutWidth = item.isWrap() && listWidth > 0 ? listWidth : LINE_PX_LENGTH;
        item.setSize(layoutWidth, Integer.MAX_VALUE);
        item.doLayout();
        return item;
    }

    private Border getNoFocusBorder() {
        return UIManager.getBorder("List.cellNoFocusBorder");
    }

    public void setWrap(boolean wrap) {
        item.setWrap(wrap);
    }

    public void addTag(LogTag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
        item.setTags(tags);
    }

    public void removeTag(LogTag tag) {
        tags.remove(tag);
        item.setTags(tags);
    }


    public void clear() {
        tags=new ArrayList<>(settings.getTags());
        item.setTags(tags);
    }
}
