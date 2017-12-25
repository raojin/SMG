package message;

import xsf.bean.BeanManager;

public class MessagerProviderFactory {
	public IMessagerProvider create(String messagerProvider){
		return (IMessagerProvider) BeanManager.get(messagerProvider);
	}
}
