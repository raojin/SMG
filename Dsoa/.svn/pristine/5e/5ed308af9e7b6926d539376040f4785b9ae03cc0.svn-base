package message.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import message.Message;
import message.vo.MessageProvider;

import org.apache.commons.lang.StringUtils;

import xsf.ID;
import xsf.data.CommandCollection;
import xsf.data.DBManager;
import xsf.data.DataRow;
import xsf.data.DataRowCollections;
import xsf.data.DataTable;
import xsf.data.DbType;
import xsf.data.Parameter;
import xsf.data.ParameterCollection;
import xsf.data.Sql;

import xsf.resource.ResourceManager;

/**
 * 信息数据库操作
 */
public class MessageDAO {
	/**
	 * 获取用户mail
	 * @param user
	 * @return
	 */
	public String getMail(long user){
		Sql sql = new Sql("select MAIL from G_USERINFO where id = ? ");
		sql.getParameters().add(new Parameter("ID",user)) ;
		return DBManager.getFieldStringValue(sql) ;
	}

	/**
	 * 获取待发送信息体
	 * @return
	 */
	public DataRowCollections getSendMessages(){
		StringBuilder sql = new StringBuilder();
		sql.append(" select MSG_MESSAGE_USER.TARGET,MSG_MESSAGE.SENDTIME,MSG_MESSAGE_USER.ID,MSG_MESSAGE_USER.PROVIDER,MESSAGE_ID,MSG_MESSAGE_USER.USER_ID,MSG_MESSAGE.TITLE,BODY,USERID  ") 
		   .append(" from  MSG_MESSAGE_USER   ")
		   .append(" inner join MSG_MESSAGE ON MSG_MESSAGE_USER.MESSAGE_ID = MSG_MESSAGE.ID ")
		   .append(" where   MSG_MESSAGE_USER.STATUS = 0 and MSG_MESSAGE_USER.TYPE = 1 and (MSG_MESSAGE_USER.APPSYSTEM IS NULL OR MSG_MESSAGE_USER.APPSYSTEM = '' )");
		DataTable table = DBManager.getDataTable(sql.toString()) ;
		DataRowCollections rows = null ;
		if(table != null){
			rows = table.getRows() ;
		}
		return rows ;
	}
	
	/**
	 * 更新状态 
	 * @param messageUserId 发送者用户ID 或 接受者用户ID
	 * @param status   状态  1--发送成功  2--已读
	 */
	public void updateSendStatus(long messageUserId,int status){
		Sql sql = new Sql("update MSG_MESSAGE_USER set STATUS = ? where ID = ? ");
		sql.getParameters().add(new Parameter("STATUS",status)) ;
		sql.getParameters().add(new Parameter("ID",messageUserId)) ;
		DBManager.execute(sql) ;
	}
	
	public void updateMessageStatus(String messageIds, int status) {
		Sql sql = new Sql(
				"update MSG_MESSAGE_USER set STATUS =? where ID=?");
		String[] _messageIds = messageIds.split(",");
		Parameter staParameter= new Parameter("STATUS", status);
		for (String _messageId : _messageIds) {
			ParameterCollection p = new ParameterCollection();
			p.add(staParameter);
			p.add(new Parameter("ID", _messageId));
			sql.addParameters(p);
		}
		if (_messageIds.length > 0) {
			DBManager.execute(sql);
		}

	}
	
	/**
	 * 保存发送信息
	 * @param message
	 * @return
	 */
	public boolean saveSendMessage(Message message) {
		
		List<MessageProvider> messageProviders = message.getMessageProviders() ;
		
		if(messageProviders == null || messageProviders.size() == 0){
			return false ;
		}
		
		CommandCollection sqls = new CommandCollection();
	
		long messageId = message.getId() ;
		
		ParameterCollection params = new ParameterCollection();
		Sql sql = new Sql("insert into MSG_MESSAGE(ID,TITLE,BODY,USERID,CREATETIME,SENDTIME,URGENT,FINISHTYPE,FINISHTIME,URL,INFO_ID) values(?,?,?,?,?,?,?,?,?,?,?)");
		params.add(new Parameter("ID",messageId)) ;
		params.add(new Parameter("TITLE",message.getTitle())) ;
		params.add(new Parameter("BODY",message.getBody())) ;
		params.add(new Parameter("USERID",message.getFrom())) ;
		params.add(new Parameter("CREATETIME", new Timestamp(new Date().getTime()), DbType.DATE)) ;
		
		Timestamp timestamp = null ;
		if(message.getSendTime() != null){
			timestamp = new Timestamp(message.getSendTime().getTime()) ;
		}
		params.add(new Parameter("SENDTIME",timestamp, DbType.DATE)) ;
		
		params.add(new Parameter("URGENT",message.getUrgent())) ;
		params.add(new Parameter("FINISHTYPE",message.getFinishType())) ;
		
		timestamp = null ;
		if(message.getFinishTime() != null){
			timestamp = new Timestamp(message.getFinishTime().getTime()) ;
		}
		params.add(new Parameter("FINISHTIME",timestamp,DbType.DATE)) ;
		
		params.add(new Parameter("URL",message.getUrl())) ;
		params.add(new Parameter("INFO_ID",message.getInfo_id())) ;
		
		
		/**
		 * 插入信息体
		 */
		
		
		sql.addParameters(params);
		sqls.add(sql) ;
		
		
		/**
		 * 插入发送者
		 */
		sql = new Sql("insert into MSG_MESSAGE_USER(ID,MESSAGE_ID,PROVIDER,USER_ID,STATUS,TYPE,TARGET,APPSYSTEM,RELATION)  values(?,?,?,?,0,1,?,?,?)");
		for(MessageProvider messageProvider : messageProviders ){
			params = new ParameterCollection();
			params.add(new Parameter("ID",ID.get16bID())) ;
			params.add(new Parameter("MESSAGE_ID",messageId)) ;
			params.add(new Parameter("PROVIDER",messageProvider.getProvider())) ;
			params.add(new Parameter("USER_ID",messageProvider.getUser())) ;
			params.add(new Parameter("TARGET",messageProvider.getTarget())) ;
			
			String appSystem = message.getAppSystem() ;
			if(StringUtils.isEmpty(appSystem)){
				appSystem =  ResourceManager.getAppKey(messageProvider.getProvider() + "_appSystem") ;
			}
			
			params.add(new Parameter("APPSYSTEM",appSystem)) ;
			params.add(new Parameter("RELATION",messageProvider.getRelation())) ;
			
			
			sql.addParameters(params) ;
		}
		sqls.add(sql) ;
		return DBManager.execute(sqls) ;
	}
	
	/**
	 * 保存接受数据
	 * @param message
	 * @return
	 */
	public boolean saveReveMessage(DataRow reveMessage){
		long sendMessage = reveMessage.getLong("ID");
		if(!isReveMessage(sendMessage)){
			Sql sql = new Sql("insert into MSG_MESSAGE_USER(ID,MESSAGE_ID,PROVIDER,USER_ID,STATUS,TYPE,RELATION)  values(?,?,?,?,2,2,?)") ;
			ParameterCollection params = new ParameterCollection();
			params.add(new Parameter("ID",ID.get16bID())) ;
			params.add(new Parameter("MESSAGE_ID",reveMessage.getLong("MESSAGE_ID"))) ;
			params.add(new Parameter("PROVIDER",reveMessage.getString("PROVIDER"))) ;
			params.add(new Parameter("USER_ID",reveMessage.getLong("USER_ID"))) ;
			params.add(new Parameter("RELATION",reveMessage.getLong("ID"))) ;
			sql.addParameters(params);
			
			return DBManager.execute(sql) ;
		}
		return false ;
	}
	
	public boolean isReveMessage(long sendId){
		StringBuilder sql = new StringBuilder() ;
		sql.append("select count(*) as T from MSG_MESSAGE_USER where RELATION = ").append(sendId) ;
		
		return DBManager.getFieldLongValue(sql.toString()) > 0 ? true : false ;
	}
	
	public DataRowCollections getMessages(long type,long status,String provider){
		Sql sql = new Sql("select * from MSG_MESSAGE_USER where TYPE= ? and STATUS = ? and PROVIDER = ? and APPSYSTEM IS NULL");
		sql.getParameters().add(new Parameter("TYPE",type)) ;
		sql.getParameters().add(new Parameter("STATUS",status)) ;
		sql.getParameters().add(new Parameter("PROVIDER",provider)) ;
		DataTable table = DBManager.getDataTable(sql) ;
		DataRowCollections rows = null ;
		if(table != null){
			rows = table.getRows();
		}
		return rows ;
	}
	
	public DataRowCollections getMessages(long type,long status){
		Sql sql = new Sql("select * from MSG_MESSAGE_USER where TYPE= ? and STATUS = ? and APPSYSTEM IS NULL ");
		sql.getParameters().add(new Parameter("TYPE",type)) ;
		sql.getParameters().add(new Parameter("STATUS",status)) ;
		DataTable table = DBManager.getDataTable(sql) ;
		DataRowCollections rows = null ;
		if(table != null){
			rows = table.getRows();
		}
		return rows ;
	}
	
	
	
	public void updateMessagesStatus(int finishType,Date finishDate){
		Sql sql = new Sql("update  MSG_MESSAGE set ROWSTATE = -1 where FINISHTYPE = ? and FINISHTIME <= ? and ROWSTATE <> -1 ");
		sql.getParameters().add(new Parameter("FINISHTYPE",finishType)) ;
		sql.getParameters().add(new Parameter("FINISHTIME",finishDate,DbType.DATE)) ;
		DBManager.execute(sql) ;
	}
	
	public boolean updateMessagesStatus(int finishType,String messageIds){
		StringBuffer sql = new StringBuffer();
		sql.append(" update MSG_MESSAGE set ROWSTATE = -1 where ")
		   .append(" FINISHTYPE = ").append(finishType)
		   .append(" and ID in (").append(messageIds).append(") and ROWSTATE <> -1") ;
		return DBManager.execute(sql.toString()) ;
	}
	
	
	public static void main(String[] args) {
		MessageDAO dao = new MessageDAO() ;
		dao.updateMessageStatus("1776903700160002,177690370111,1776903222,177690370016333", 1);
	}
	
}
