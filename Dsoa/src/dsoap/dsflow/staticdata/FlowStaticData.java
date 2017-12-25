package dsoap.dsflow.staticdata;

import java.util.HashMap;

/**
 * 流程中特殊的配置数据
 * @author Umaydie
 *
 */
public class FlowStaticData {
	public static HashMap<String,String> RedirectSqls; //重定向选择节点的特定SQL
	
	static{
		RedirectSqls = new HashMap<String,String>();
		RedirectSqls.put("881474", "select case when id is null then '2,3,4,5' else '6,7,8,9' end as NODEID from g_infos where id=[INFO_ID]");
	}
	
}
