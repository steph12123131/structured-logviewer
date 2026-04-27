package org.logviewer.ui;

import org.logviewer.entity.LogTag;

import java.awt.*;

public class LogListTagLabel extends RoundedTagLabel {


    public static final int LIMIT = 256;
    private String fullText;

    public LogTag getTag() {
        return tag;
    }

    public void setTag(LogTag tag) {
        this.tag = tag;
    }

    private LogTag tag;

    public LogListTagLabel(Color backgroundColor, Color textColor, Color borderColor) {
        super(null, backgroundColor, textColor, borderColor);
    }

    @Override
    public void setText(String text) {
        fullText = text;

        if (text != null && text.length()> LIMIT) {
            super.setText(text.substring(0, LIMIT)+" ...");
            setVisible(true);
        }
        else if (text  == null) {
            super.setText("\uD83D\uDEAB");
            setVisible(false);
        }
        else  {
            super.setText(text);
            setVisible(true);
        }

    }

    public String getTooltipText() {
        return fullText;
    }
}
