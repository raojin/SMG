package dsoap.dsflow.model;

import java.util.HashMap;

public class DataRow extends HashMap<String, String> {

    private static final long serialVersionUID = -4412933084918569996L;

    private String height;

    private String style;

    private String colspan;

    private String backColor;

    private String foreColor;

    public String getBackColor() {
        return backColor;
    }

    public void setBackColor(String backColor) {
        this.backColor = backColor;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public String get(Object key) {
        key = key.toString().toLowerCase();
        return super.get(key);
    }

    @Override
    public String put(String key, String value) {

        key = key.toLowerCase();
        return super.put(key, value);
    }

    public String getForeColor() {
        return foreColor;
    }

    public void setForeColor(String foreColor) {
        this.foreColor = foreColor;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getColspan() {
        return colspan;
    }

    public void setColspan(String colspan) {
        this.colspan = colspan;
    }
}
