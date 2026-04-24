package org.logviewer.model;

import org.logviewer.settings.Settings;
import org.logviewer.entity.LogTag;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

public class LogTagListModel extends AbstractListModel {

    private final Settings settings;

    public NavigableSet<LogTag> getTags() {
        return tags;
    }

    private NavigableSet<LogTag> tags = new TreeSet<>();

    public LogTagListModel(Settings settings) {
        this.settings = settings;
        this.tags.addAll(settings.getTags());
    }

    public void add(List<String> tag) {
        int size = tags.size();
        tags.add(LogTag.builder().path(tag).build());
        if (size != tags.size()) {
            fireContentsChanged(this, 0, size);
        }
    }

    public void addAll(Collection<List<String>> tags) {
        int size = this.tags.size();
        this.tags.addAll(tags.stream().map(tag -> LogTag.builder().path(tag).build()).collect(Collectors.toSet()));
        if (size != this.tags.size()) {
            fireContentsChanged(this, 0, this.tags.size());
        }
    }

    @Override
    public int getSize() {
        return tags.size();
    }

    @Override
    public Object getElementAt(int index) {
        if (index > tags.size()) {
            return null;
        }
        return tags.stream().skip(index).findFirst().get();
    }

    public void clear() {
        this.tags.clear();
        this.tags.addAll(settings.getTags());
        fireContentsChanged(this, 0, tags.size());
    }
}
