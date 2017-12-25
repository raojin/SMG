package dsoap.web.action;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;

import dsoap.tools.webwork.Action;

/**
 * 
 * @author
 */
public class RegistAction extends Action {

    private static final long serialVersionUID = 1175412321901094803L;
    public String errStr = "";

    @Override
    public String execute() throws Exception {
        super.execute();
        String action = request.getParameter("action");
        if (null == action || "".equals(action)) {
            // String registCode = request.getParameter("registCode");
            // System.out.println(registCode);
            super.execute();
            Service service = new Service();
            try {
                Call call = (Call) service.createCall();
                call.setTargetEndpointAddress(new java.net.URL("http://192.168.1.126:8080/service/wfservice"));
                call.setOperationName(new QName("WFService", "GetRegCode"));// 类名，方法名
                // call.addParameter("sCode", XMLType.XSD_STRING, ParameterMode.IN); // 参数的类型
                call.setReturnType(XMLType.XSD_STRING); // 返回的数据类型
                errStr = call.invoke(new Object[] {}).toString(); // 执行调用
                // System.out.println(strXML);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String sNowWorkTime = request.getParameter("sNowWorkTime");
            int iTimeSpan = Integer.valueOf(request.getParameter("iTimeSpan"));
            int iTimeType = Integer.valueOf(request.getParameter("iTimeType"));
            errStr = dsoap.dsflow.DS_FLOWClass.getEndTime(sNowWorkTime, iTimeSpan, iTimeType);
        }
        return ERROR;
    }

}
