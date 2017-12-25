package dsoap.tools;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class TypeConversion {

    /**
     * 方法将RowSet数据结果集进行数据转换成XML文件 StringBuffer 进行字符缓存 ResultSetMetaData 获得RowSet的列名 xml文件格式如下：
     * 
     * @param RowSet
     * @return string
     */
    public static String convertResultSetToXML(ResultSet rs, String name) {
        StringBuffer sb = new StringBuffer();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            if (rsmd.getColumnCount() >= 0) {
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                sb.append("<NewDataSet>\n");
                while (rs.next()) {
                    sb.append("<" + name + ">\n");
                    for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                        String columnLabel = rsmd.getColumnLabel(j).toUpperCase();
                        String value = rs.getObject(j) == null ? "" : rs.getObject(j).toString();
                        // -----------------------------------------------------------
                        value = xsf.Value.getString(value, xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("charset"), xsf.Config.CHARSET);
                        // -----------------------------------------------------------
                        sb.append("<" + columnLabel + ">").append(value).append("</" + columnLabel + ">\n");
                    }
                    sb.append("</" + name + ">\n");
                }
                sb.append("</NewDataSet>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
