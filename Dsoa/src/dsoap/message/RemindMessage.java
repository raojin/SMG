package dsoap.message;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dsoap.dsflow.RemindInfo;

import xsf.ID;
import xsf.data.CommandCollection;
import xsf.data.CommandType;
import xsf.data.DBManager;
import xsf.data.DataRow;
import xsf.data.DataTable;
import xsf.data.DbType;
import xsf.data.Sql;
import xsf.util.StringHelper;

public class RemindMessage {

    public String Url;

    public String PutMessage(RemindInfo ri) {
    	boolean ok=false;
    	if(StringHelper.isNullOrEmpty(ri.getContent())){
    		
    	}else{
    		if(StringHelper.isNullOrEmpty(ri.getModuleid())){
        		ri.setModuleid(DBManager.getFieldStringValue("select module_id from g_infos where id='"+ri.getInfoID()+"'"));
        	}
        	String sql1="select id,MOBILE,name from g_userinfo where rowstate>-1 and id in("+ri.getRuserid()+") and (MOBILE is not null or MOBILE!='')";
        	DataTable dt=DBManager.getDataTable(sql1);
        	long id=ID.get16bID();
        	if(dt.getTotal()>0){
        		Map<String,String> map=new HashMap<String,String>();
        		for(DataRow dr:dt.getRows()){
        			map.put(dr.getString("id"), dr.getString("MOBILE")+",;"+dr.getString("name"));
        		}
        		if(map.size()<=0){ 
        			return String.valueOf(ok);
        		}
        		String strUname=DBManager.getFieldStringValue("select name from g_userinfo where rowstate>-1 and id="+ri.getUserID());
        		String[] uid=ri.getRuserid().split(",");
        		CommandCollection sqls = new CommandCollection();
        		sql1="insert into INT_SMS(ID,CONTENT,SENDTIME,SENDSTATUS,SENDUSERID,SENDUSERNAME,INFO_ID,MODULE_ID,RECEIVEUSERID,RECEIVEUSERNAME,MAINUNIT) " +
                		"values(?ID,?CONTENT,?SENDTIME,?SENDSTATUS,?SENDUSERID,?SENDUSERNAME,?INFO_ID,?MODULE_ID,?RECEIVEUSERID,?RECEIVEUSERNAME,?MAINUNIT)";
                Sql sql = new Sql(sql1);
                sql.getParameters().add("ID",id,DbType.NUMERIC);
                sql.getParameters().add("CONTENT",ri.getContent());
                sql.getParameters().add("SENDTIME","now()",DbType.DATE);
                sql.getParameters().add("SENDSTATUS",1);
                sql.getParameters().add("SENDUSERID",ri.getUserID());
                sql.getParameters().add("SENDUSERNAME",strUname);
                sql.getParameters().add("INFO_ID",ri.getInfoID());
                sql.getParameters().add("MODULE_ID",ri.getModuleid());
                sql.getParameters().add("RECEIVEUSERID",ri.getRuserid(),DbType.NUMERIC);
                sql.getParameters().add("RECEIVEUSERNAME",ri.getRusername());
                sql.getParameters().add("MAINUNIT",ri.getMainUnit());
                sql.setCommandType(CommandType.XQL);
                sqls.add(sql);
                for(int i=0;i<uid.length;i++){
                	String mobile=map.get(uid[i]);
                	
                	if(mobile==null){
                		continue;
                	}else{
                		sql1="insert into INT_SMS_DETAIL(ID,SMSID,MOBILE,RECEIVEUSERNAME,CONTENT,SENDTIME,SENDSTATUS) " +
                        		"values(?ID,?SMSID,?MOBILE,?RECEIVEUSERNAME,?CONTENT,?SENDTIME,?SENDSTATUS)";
                		sql=new Sql(sql1);
                		String[] mobi=mobile.split(",;");
                        sql.getParameters().add("ID",ID.get16bID(),DbType.NUMERIC);
                        sql.getParameters().add("SMSID",id);
                        sql.getParameters().add("MOBILE",mobi[0]);
                        sql.getParameters().add("RECEIVEUSERNAME",mobi[1]);
                        sql.getParameters().add("CONTENT",ri.getContent());
                        sql.getParameters().add("SENDTIME","now()",DbType.DATE);
                        sql.getParameters().add("SENDSTATUS",0);
                        sql.setCommandType(CommandType.XQL);
                        sqls.add(sql);
                	}
                }
               ok = DBManager.execute(sqls);
               sqls = null;            
        	}
    	}
    	
    	
    	return String.valueOf(ok);
    }
}
