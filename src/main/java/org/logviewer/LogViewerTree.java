package org.logviewer;

import com.intellij.ui.treeStructure.Tree;
import org.logviewer.entity.Log;
import org.logviewer.listener.LogListener;
import org.logviewer.listener.LogNameListener;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@lombok.extern.java.Log
public class LogViewerTree extends JPanel implements LogListener {


    private MyTreeModel treeModel;

    private List<LogNameListener> listeners = new ArrayList<>();

    public void addLogNameListener(LogNameListener listener) {
        listeners.add(listener);
    }

    public void removeLogNameListener(LogNameListener listener) {
        listeners.remove(listener);
    }

    public LogViewerTree() {
        super(new BorderLayout());
        setMinimumSize(new Dimension(200, 100));
        setPreferredSize(new Dimension(200, 100));
        add(getTree(), BorderLayout.CENTER);
    }

    public JTree getTree() {
        if (tree == null) {
            tree = new Tree();
            treeModel = new MyTreeModel();
            tree.setModel(treeModel);
            tree.addTreeSelectionListener(e -> listeners.forEach((l) -> l.logNameChanged(convert(e.getPath()))));
        }
        return tree;
    }

    private String convert(TreePath path) {
        return Arrays.stream(path.getPath()).toList().stream().skip(1).map(Object::toString).collect(Collectors.joining("."));
    }

    private JTree tree;

    @Override
    public void logAdded(Log log) {
        treeModel.addPath(log.getLoggerName());
    }

    private class MyTreeModel extends DefaultTreeModel {


        public MyTreeModel() {
            super(new DefaultMutableTreeNode("root"));
        }

        void addPath(String path) {
            AtomicReference<DefaultMutableTreeNode> current = new AtomicReference<>(getRootNode());

            for (String p : path.split("\\.")) {
                Optional<DefaultMutableTreeNode> child = findToken(current.get(), p);

                child.ifPresentOrElse(c -> {
                    current.set(c);
                }, () -> {
                    DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(p);
                    current.get().add(newChild);
                    current.set(newChild);
                });

            }

        }


        Optional<DefaultMutableTreeNode> findToken(DefaultMutableTreeNode node, String token) {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = getChildAt(node, i);
                if (child.getUserObject().equals(token)) {
                    return Optional.of(child);
                }

            }
            return Optional.empty();
        }

        private DefaultMutableTreeNode getChildAt(DefaultMutableTreeNode node, int i) {
            return (DefaultMutableTreeNode) node.getChildAt(i);
        }

        private DefaultMutableTreeNode getRootNode() {
            return (DefaultMutableTreeNode) getRoot();
        }
    }
}
