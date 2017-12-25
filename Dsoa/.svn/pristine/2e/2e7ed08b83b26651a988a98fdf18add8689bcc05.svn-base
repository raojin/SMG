package dsoap.tools;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import xsf.data.DBManager;
import xsf.data.DataRow;
import xsf.data.DataSourceFactory;
import xsf.data.DataTable;
import xsf.data.ICommand;
import xsf.data.IDataSource;
import xsf.data.Sql;
import xsf.resource.ResourceManager;

public class SysDataSource {
    private static SAXReader saxreader = new SAXReader();
    private static IDataSource sqlDataSource = null;
    //增加静态语句块，在初始化时加载SQL语句配置文件
    static 
    {
    	try {
    		String url = SysDataSource.class.getResource("").getPath().replaceAll("%20", " ");
            String version = ResourceManager.getAppKey("SYS_SQL_VERSION");
            version = version == null || "".equals(version) ? "0" : version;
            String path = url.substring(0, url.indexOf("WEB-INF")) + "WEB-INF/config/SYS_SQL_" + version + ".xml";
            Document dom = saxreader.read(new File(path));
            String xml = dom.asXML();
            sqlDataSource = DataSourceFactory.createByXML(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static IDataSource getSysDataSource() {
        try {
            if (sqlDataSource == null) {
                String url = SysDataSource.class.getResource("").getPath().replaceAll("%20", " ");
                String version = ResourceManager.getAppKey("SYS_SQL_VERSION");
                version = version == null || "".equals(version) ? "0" : version;
                String path = url.substring(0, url.indexOf("WEB-INF")) + "WEB-INF/config/SYS_SQL_" + version + ".xml";
                Document dom = saxreader.read(new File(path));
                String xml = dom.asXML();
                sqlDataSource = DataSourceFactory.createByXML(xml);
            }
            return sqlDataSource;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        IDataSource sqlDataSource = getSysDataSource();
        ICommand command = (ICommand) sqlDataSource.getSelectCommands().get("getRoles");
        DataTable dt = DBManager.getDataTable((Sql) command);
        for (DataRow dr : dt.getRows()) {
            System.out.println(dr.get("ROLENAME"));
        }

        sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("INFO_ID", "1328249267500000");
        sqlDataSource.setParameter("WF_ID", "(SELECT WF_ID FROM G_INFOS WHERE ID = 1328249267500000)");
        Sql sql = (Sql) sqlDataSource.getSelectCommands().get("getGPnodes");
        dt = DBManager.getDataTable(sql);
        for (DataRow dr : dt.getRows()) {
            System.out.println(dr.get("UNAME"));
        }

        sqlDataSource = SysDataSource.getSysDataSource();
        sqlDataSource.setParameter("INFO_ID", "1328249267500000");
        sqlDataSource.setParameter("WF_ID", "(SELECT WF_ID FROM G_INFOS WHERE ID = 1328249267500000)");
        dt = sqlDataSource.query("getGPnodes");
        for (DataRow dr : dt.getRows()) {
            System.out.println(dr.get("UNAME"));
        }
    }
}
