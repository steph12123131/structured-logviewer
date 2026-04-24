package org.logviewer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.OnePixelSplitter;
import org.logviewer.entity.Log;
import org.logviewer.filter.FilterBuilder;
import org.logviewer.filter.LogNamesFilter;
import org.logviewer.filter.LogQueryFilter;
import org.logviewer.listener.LogListener;
import org.logviewer.listener.LogNameListener;
import org.logviewer.model.LogTableModel;
import org.logviewer.model.LogTagListModel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.logviewer.model.LogListModel;
import org.logviewer.model.LogTagListSelectionModel;
import org.logviewer.settings.Settings;
import org.logviewer.ui.LogList;
import org.logviewer.ui.LogTable;
import org.logviewer.ui.LogTagList;
import org.logviewer.ui.LogTree;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class LogViewer extends JPanel implements LogListener, LogNameListener {

    private JTextField messageSearchText;
    private List<Log> logs = new ArrayList<>();
    private LogTableModel logTableModel;
    private LogNamesFilter logNamesFilter = new LogNamesFilter(List.of(""));
    private Predicate<Log> logMessageFilter = new LogQueryFilter("");
    private final Settings settings;
    private JButton clearButton;
    private JButton clearSettingsButton;
    private JToggleButton wrapButton;
    @Setter
    private int limit;
    private LogTable table;
    private LogList list;
    private LogTagList tagList;
    private LogTree tree;
    private LogTagListModel tagListModel;
    private LogListModel logListModel;
    private LogTagListSelectionModel tagListSelectionModel;

    public LogViewer(Settings settings) {
        this(10000, settings);
    }

    public LogViewer(int limit, Settings settings) {
        super(new BorderLayout());
        OnePixelSplitter verticalSplitter = new OnePixelSplitter(false);
        verticalSplitter.setDividerPositionStrategy(Splitter.DividerPositionStrategy.KEEP_FIRST_SIZE);
        this.settings = settings;
        if (settings.getTags().isEmpty()) {
            settings.clear();
        }
        this.limit = limit;
        this.add(verticalSplitter, BorderLayout.CENTER);
        verticalSplitter.setSecondComponent(new JScrollPane(getList()));
        clearButton = new JButton("Clear");
        clearSettingsButton = new JButton("Clear Settings");
        wrapButton = new JToggleButton("UnWrap", true);
        clearButton.addActionListener((e) -> {
            logs.clear();
            refreshLogs();
        });
        clearSettingsButton.addActionListener((e) -> {
            settings.clear();
            getLogTagListSelectionModel().clearSelection();
            getLogTagListModel().clear();
            getLogTagListSelectionModel().setSelectionInterval(0,settings.getTags().size()-1);
            getLogTableModel().clear();
            getList().clear();
        });
        wrapButton.addActionListener((e) -> getList().setWrap(wrapButton.isSelected()));
        JButton clearMessageSearchButton = new JButton("Clear");
        clearMessageSearchButton.addActionListener((e) -> {
            getMessageSearch().setText("");
            logMessageFilter = new FilterBuilder().query(getMessageSearch().getText()).build();
            refreshLogs();
        });
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(getMessageSearch(),BorderLayout.CENTER);
        panel2.add(clearMessageSearchButton,BorderLayout.LINE_END);
        this.add(panel2, BorderLayout.PAGE_START);
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(wrapButton);
        panel.add(clearButton);
        panel.add(clearSettingsButton);
        this.add(panel, BorderLayout.PAGE_END);
        tree = new LogTree();
        tree.addLogNameListener(this);

        // Définir les composants

        OnePixelSplitter splitter = new OnePixelSplitter(true);
        splitter.setFirstComponent(getTree());
        splitter.setSecondComponent(new JScrollPane(getTagList()));
        verticalSplitter.setFirstComponent(splitter);
        verticalSplitter.setProportion(0.25f);


        if (!settings.getWidths().isEmpty()) {
            for (int i = 0; i < settings.getWidths().size(); i++) {

                //getTable().getColumnModel().getColumn(i).setWidth(settings.getColumnWidths().get(i));
            }

        }
        getTable().addLogTagListener((tag, value) -> {
            if (getMessageSearch().getText().length() > 0) {
                getMessageSearch().setText(getMessageSearch().getText() + " and ");
            }
            getMessageSearch().setText(getMessageSearch().getText() + String.join(".", tag.getPath()) + "=" + value);
            logMessageFilter = new FilterBuilder().query(getMessageSearch().getText()).build();
            refreshLogs();
        });

        getList().addLogTagListener((tag, value) -> {
            if (getMessageSearch().getText().length() > 0) {
                getMessageSearch().setText(getMessageSearch().getText() + " and ");
            }
            getMessageSearch().setText(getMessageSearch().getText() + String.join(".", tag.getPath()) + "=" + value);
            logMessageFilter = new FilterBuilder().query(getMessageSearch().getText()).build();
            refreshLogs();
        });

        // Activer le DnD sur le panel
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                // On accepte uniquement des fichiers
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    StringBuilder sb = new StringBuilder("<html>Fichiers déposés :<br>");
                    for (File file : files) {
                        sb.append(file.getAbsolutePath()).append("<br>");
                        load(Files.newInputStream(file.toPath()));
                    }
                    sb.append("</html>");

                    return true;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                return false;
            }
        });
    }

    public LogViewer() {
        this(10000, null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LogViewer logViewer = new LogViewer();
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("Log Viewer");
            frame.getContentPane().add(logViewer);
            frame.pack();
            frame.setSize(500, 500);
            frame.setVisible(true);
        });
    }

    private @NotNull JTextField getMessageSearch() {
        if (messageSearchText == null) {
            messageSearchText = new JTextField();
            messageSearchText.addKeyListener(new KeyAdapter() {

                @Override
                public void keyReleased(KeyEvent e) {
                    logMessageFilter = new FilterBuilder().query(getMessageSearch().getText()).build();
                    refreshLogs();
                }
            });

        }
        return messageSearchText;
    }

    private void refreshLogs() {
        logTableModel.setLogs(logs.stream().filter(logNamesFilter.and(logMessageFilter)).toList());
        getLogViewerListModel().setLogs(logs.stream().filter(logNamesFilter.and(logMessageFilter)).toList());
    }

    public LogTableModel getLogTableModel() {
        if (logTableModel == null) {
            logTableModel = new LogTableModel(settings);
            logTableModel.setLogs(
                    logs
            );
        }
        return logTableModel;
    }

    public LogListModel getLogViewerListModel() {
        if (logListModel == null) {
            logListModel = new LogListModel();
            logListModel.setLogs(
                    logs
            );
        }
        return logListModel;
    }

    public LogTagListModel getLogTagListModel() {
        if (this.tagListModel == null) {
            this.tagListModel = new LogTagListModel(settings);
        }
        return this.tagListModel;
    }

    public LogTagListSelectionModel getLogTagListSelectionModel() {
        if (this.tagListSelectionModel == null) {
            this.tagListSelectionModel = new LogTagListSelectionModel(settings);
        }
        return this.tagListSelectionModel;
    }

    public LogTagList getTagList() {
        if (tagList == null) {
            tagList = new LogTagList(getLogTagListModel(),getLogTagListSelectionModel());
        }
        return tagList;
    }

    public LogTree getTree() {
        if (tree == null) {
            tree = new LogTree();
        }
        return tree;
    }

    public LogTable getTable() {
        if (table == null) {
            table = new LogTable();

            table.setModel(getLogTableModel());
            table.getColumnModel().addColumnModelListener(new MyTableColumnModelListener(table, settings));

            getTagList().addTagListSelectionListener(getLogTableModel());


        }
        return table;
    }

    public LogList getList() {
        if (list == null) {
            list = new LogList(settings);
            list.setModel(getLogViewerListModel());
            getTagList().addTagListSelectionListener(getList());
        }
        return list;
    }

    @Override
    public void logAdded(Log log) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> logAdded(log));
            return;
        }
        logs.add(log);
        if (logs.size() > limit) {
            logs.remove(0);
        }
        getTree().logAdded(log);
        getTagList().logAdded(log);

        refreshLogs();
    }

    @Override
    public void logNameChanged(List<String> logName) {
        logNamesFilter = new LogNamesFilter(logName);
        refreshLogs();
    }

    private void load(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode node = mapper.readTree(inputStream);
        ArrayNode arrayNode = (ArrayNode) node;
        logs.clear();
        arrayNode.elements().forEachRemaining(e -> {
            JsonNode payload = e.get("jsonPayload");
            if (payload != null) {
                Log log = null;
                try {
                    log = mapper.treeToValue(payload, Log.class);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }

                logAdded(log);
            }
        });
    }

    @Slf4j
    static private class MyTableColumnModelListener implements TableColumnModelListener {
        private final LogTable list;
        private final Settings settings;

        public MyTableColumnModelListener(LogTable list, Settings settings) {
            this.list = list;
            this.settings = settings;
        }

        @Override
        public void columnAdded(TableColumnModelEvent e) {
            //settings.getColumns().add(LogTag.fromStringPath(list.getColumnModel().getColumn(e.getToIndex()).getHeaderValue().toString()));
            log.info("Added " + e);
        }

        @Override
        public void columnRemoved(TableColumnModelEvent e) {
            if (e.getToIndex() >= settings.getTags().size()) {
                return;
            }
            //settings.getColumns().remove(e.getToIndex());
            log.info("Removed " + e);
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) {
            if (e.getFromIndex()>= settings.getTags().size()) {
                return;
            }
            if (e.getToIndex()>= settings.getTags().size()) {
                return;
            }
            /*
            if (e.getFromIndex() > e.getToIndex()) {
                LogTag value = settings.getColumns().remove(e.getFromIndex());
                settings.getColumns().add(e.getToIndex(), value);
            } else if (e.getFromIndex() < e.getToIndex()) {
                LogTag value = settings.getColumns().remove(e.getFromIndex());
                settings.getColumns().add(e.getToIndex() - 1, value);
            }*/
            log.info("Moved " + e);

        }

        @Override
        public void columnMarginChanged(ChangeEvent e) {
            Enumeration<TableColumn> columns = ((TableColumnModel) e.getSource()).getColumns();

            List<Integer> result = new ArrayList<>();
            while (columns.hasMoreElements()) {
                result.add(columns.nextElement().getWidth());
            }
            settings.getWidths().clear();
            settings.getWidths().addAll(result);
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent e) {

        }
    }
}
