package org.logviewer.ui;

import org.logviewer.entity.Log;
import org.logviewer.entity.LogTag;
import org.logviewer.helper.LogHelper;

import javax.swing.*;
import java.awt.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LogListItem extends JPanel {
    public static final LogTag MESSAGE = new LogTag(List.of("message"));
    private Map<LogTag, LogListTagLabel> tags = Map.of();
    private JLabel messageLabel = new JLabel();
    private boolean wrap = true;

    private List<LogListTagLabel> items = List.of(
            new LogListTagLabel(new Color(0xEEEDFE), new Color(0x3C3489), new Color(0xAFA9EC)),
            new LogListTagLabel(new Color(0xE1F5EE), new Color(0x085041), new Color(0x5DCAA5)),
            new LogListTagLabel(new Color(0xFAECE7), new Color(0x712B13), new Color(0xF0997B)),
            new LogListTagLabel(new Color(0xE6F1FB), new Color(0x0C447C), new Color(0x85B7EB)),
            new LogListTagLabel(new Color(0xFAEEDA), new Color(0x633806), new Color(0xEF9F27)),
            new LogListTagLabel(new Color(0xEAF3DE), new Color(0x27500A), new Color(0x97C459)),
            new LogListTagLabel(new Color(0xFBEAF0), new Color(0x72243E), new Color(0xED93B1)),
            new LogListTagLabel(new Color(0xFCEBEB), new Color(0x791F1F), new Color(0xF09595)),
            new LogListTagLabel(new Color(0xF1EFE8), new Color(0x444441), new Color(0xB4B2A9)),
            new LogListTagLabel(new Color(0xE6F1FB), new Color(0x0C447C), new Color(0x85B7EB)),
            new LogListTagLabel(new Color(0xEEEDFE), new Color(0x3C3489), new Color(0xAFA9EC)),
            new LogListTagLabel(new Color(0xE1F5EE), new Color(0x085041), new Color(0x5DCAA5)),
            new LogListTagLabel(new Color(0xFAECE7), new Color(0x712B13), new Color(0xF0997B)),
            new LogListTagLabel(new Color(0xE6F1FB), new Color(0x0C447C), new Color(0x85B7EB)),
            new LogListTagLabel(new Color(0xFAEEDA), new Color(0x633806), new Color(0xEF9F27)),
            new LogListTagLabel(new Color(0xEAF3DE), new Color(0x27500A), new Color(0x97C459)),
            new LogListTagLabel(new Color(0xFBEAF0), new Color(0x72243E), new Color(0xED93B1)),
            new LogListTagLabel(new Color(0xFCEBEB), new Color(0x791F1F), new Color(0xF09595)),
            new LogListTagLabel(new Color(0xF1EFE8), new Color(0x444441), new Color(0xB4B2A9)),
            new LogListTagLabel(new Color(0xE6F1FB), new Color(0x0C447C), new Color(0x85B7EB)),
            new LogListTagLabel(new Color(0xEEEDFE), new Color(0x3C3489), new Color(0xAFA9EC)),
            new LogListTagLabel(new Color(0xE1F5EE), new Color(0x085041), new Color(0x5DCAA5)),
            new LogListTagLabel(new Color(0xFAECE7), new Color(0x712B13), new Color(0xF0997B)),
            new LogListTagLabel(new Color(0xE6F1FB), new Color(0x0C447C), new Color(0x85B7EB)),
            new LogListTagLabel(new Color(0xFAEEDA), new Color(0x633806), new Color(0xEF9F27)),
            new LogListTagLabel(new Color(0xEAF3DE), new Color(0x27500A), new Color(0x97C459)),
            new LogListTagLabel(new Color(0xFBEAF0), new Color(0x72243E), new Color(0xED93B1)),
            new LogListTagLabel(new Color(0xFCEBEB), new Color(0x791F1F), new Color(0xF09595)),
            new LogListTagLabel(new Color(0xF1EFE8), new Color(0x444441), new Color(0xB4B2A9)),
            new LogListTagLabel(new Color(0xE6F1FB), new Color(0x0C447C), new Color(0x85B7EB)));


    public void setTags(List<LogTag> tags) {
        Arrays.stream(this.getComponents()).forEach(this::remove);
        List<LogTag> dedupedTags = tags.stream().filter(o -> !MESSAGE.equals(o)).distinct().toList();
        int count = Math.min(dedupedTags.size(), items.size());
        this.tags = IntStream.range(0, count)
                .boxed()
                .collect(Collectors.toMap(dedupedTags::get, index -> {
                    LogListTagLabel logListTagLabel = items.get(index);
                    logListTagLabel.setTag(dedupedTags.get(index));
                    return logListTagLabel;
                }));
        items.subList(0, count).forEach(this::add);
        this.add(messageLabel);

    }

    public LogListItem() {
        super(new FlowLayout(FlowLayout.LEFT, 2, 2));
        setOpaque(true);
    }
    
    public void setValues(Log log) {
        tags.forEach((logTag, logListTagLabel) -> {
            Object valueFromTag = LogHelper.getValueFromTag(log, logTag);
            logListTagLabel.setText(valueFromTag == null ? null : valueFromTag.toString());
            logListTagLabel.setDisableBorder(false);
        });
        messageLabel.setText(log.getMessage());
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }

    public boolean isWrap() {
        return wrap;
    }

    @Override
    public Dimension getPreferredSize() {
        int h = 0;
        for (Component c : getComponents()) {
            if (c.isVisible()) {
                Rectangle b = c.getBounds();
                h = Math.max(h, b.y + b.height);
            }
        }
        return new Dimension(getWidth(), h + 4);
    }
}
