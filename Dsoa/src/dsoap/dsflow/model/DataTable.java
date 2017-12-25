package dsoap.dsflow.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataTable {

    public String tablename;

    public boolean visible;

    public Map<String, Class<?>> columns = new HashMap<String, Class<?>>();

    public List<DataRow> rows = new ArrayList<DataRow>();;

    public DataRow NewRow() {
        DataRow dr = new DataRow();
        return dr;
    }

    public boolean Add(DataRow dr) {
        rows.add(dr);
        return true;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public Map<String, Class<?>> getColumns() {
        return columns;
    }

    public void setColumns(Map<String, Class<?>> columns) {
        this.columns = columns;
    }

    public List<DataRow> getRows() {
        return rows;
    }

    public void setRows(List<DataRow> rows) {
        this.rows = rows;
    }

    public DataTable(String tablename) {
        super();
        this.tablename = tablename;
    }

    public List<DataRow> select(String columns, String value) {
        List<DataRow> list = new ArrayList<DataRow>();
        for (DataRow dr : this.rows) {
            if (value.equals(dr.get(columns))) {
                list.add(dr);
            }
        }
        return list;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
