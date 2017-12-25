package dsoap.tools;

import xsf.Value;

public class CreatID {
	private static long BEGIN_POS = 0;
	private static long BEGIN_CID = 0;
	private static String FLOW_START = "1";// 流程开始id以1开始表示通过流程引擎发送
	static {
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		calendar.set(2013, 11, 1, 0, 0, 0);
		BEGIN_CID = (System.currentTimeMillis() - calendar.getTimeInMillis()) / 1000;
		System.out.println("流程引擎起始ID" + BEGIN_CID);
	}

	/**
	 * 生成10位唯一数 1+9位时间之间的秒差
	 * 
	 * @return
	 */
	public synchronized static long getID10() {

		String pos = String.valueOf(BEGIN_CID + (++BEGIN_POS));

		if (pos.length() > 9) {
			pos = pos.substring(0, 9);
		}
		pos = String.format("%09d", Value.getLong(pos));
		pos = FLOW_START + pos;

		return Value.getLong(pos);
	}

}
