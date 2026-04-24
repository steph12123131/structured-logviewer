package org.logviewer.model;

import org.logviewer.settings.Settings;

import javax.swing.*;

public class LogTagListSelectionModel extends DefaultListSelectionModel {
    public LogTagListSelectionModel(Settings settings) {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setSelectionInterval(0,settings.getTags().size()-1);
    }

    @Override
    public void setSelectionInterval(int index0, int index1) {
        if (isSelectedIndex(index0)) {
            removeSelectionInterval(index0, index1);
        } else {
            addSelectionInterval(index0, index1);
        }
    }

}