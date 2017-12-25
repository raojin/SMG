package message.dao;

public class MessageDaoFactory {
	private static MessageDAO  dao ;
	
	private MessageDaoFactory(){
		
	}
	
	public static MessageDAO getInstance(){
		if(dao == null){
			dao = new MessageDAO();
		}
		return dao ;
	}
}
