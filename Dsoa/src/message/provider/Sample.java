package message.provider;

import xsf.IDictionary;
import xsf.data.DataRow;
import message.IMessagerProvider;
import  message.Message;
import  message.dao.MessageDaoFactory;

public class Sample  implements IMessagerProvider {

	@Override
	public boolean send(Message message) {
		return true;
	}

	@Override
	public boolean receive(IDictionary dictionary) { //处理状态
		
		boolean result = false ;
		Object object = dictionary.get("row") ;
		if(object != null && object instanceof DataRow){
			DataRow row = (DataRow) object ;
			result = MessageDaoFactory.getInstance().saveReveMessage(row) ;
		}
		return result;
	}
}
