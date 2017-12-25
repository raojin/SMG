package dsoap.test;

import xsf.data.DBManager;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//xsf.platform.RuntimeContext.startupAplication();
		xsf.data.DataTable dt = DBManager.getDataTable("select * from g_inbox ");
        if (dt.getRows().size() > 0) {
            xsf.data.DataRow dr = dt.getRows().get(0);
            String s=dr.getString(0);
            System.out.println(s);
        }
	}

}
