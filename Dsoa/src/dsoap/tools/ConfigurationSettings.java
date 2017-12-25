package dsoap.tools;

import javax.servlet.http.HttpServletRequest;

public class ConfigurationSettings {
    public static String MACHINE = "http://localhost:8080";
    public static String DBMS = "ORACLE";
    // --------------------------------------------------------
    public static boolean showSearch = false;// 选人时是否提供查找功能
    public static boolean showClose = false;// 选人时是否显示关闭按钮
    public static boolean cascade = false;// 选人时是否支持级联选择
    public static boolean showCountdown = true;// 发送完是否显示倒计时
    public static int closeMode = 0;// 发送完关窗口模式（0:页面切换；1：关闭子窗口，刷新父窗口）
    public static boolean isFilterPerson = false;// 是否过滤正在处理的人
    public static boolean isCrossDept = false;// 跨部门兼职（g_infos 拟稿部门）过滤部门
    public static boolean isRoleDept = false;// 跨部门兼职（角色与部门关联）过滤人员
    public static boolean isStandaloneDept = false;// 是否支持 “子机构”
    public static boolean isPrivateGroup = false;// 是否支持 “私有群组”
    public static boolean isSqlUserShowDept = false;// sql选人时是否显示“部门”
    public static boolean isDraftOpinion = false;// 是否支持拟稿写意见
    public static int JoinOrder = 1;// 汇总顺序（0：在最后一个人处理时汇合；1：在第一个人处理时汇合，其他人跟随第一个人发）
    public static boolean removeAble = false;// 强制发送时是否可取消选择
    public static int timeStandard = 1;// 新增，更新的时间标准（1应用服务器，0,数据库服务器）
    public static int SelectUserMode = 0;//流程选人模式（0:先选节点后选人；1：不选节点直接选人
    public static boolean openInfoMJ = false;//是否启用文件密级
    // --------------------------------------------------------
    static {
        DBMS = xsf.Config.getConectionDictionary(xsf.Config.CONNECTION_KEY).getProperty("DIALECT").toUpperCase();
        if (DBMS.indexOf("MYSQL") > -1) {
            DBMS = "MYSQL";
        } else if (DBMS.indexOf("SQLSERVER") > -1) {
            DBMS = "SQLSERVER";
        } else if (DBMS.indexOf("ORACLE") > -1) {
            DBMS = "ORACLE";
        } else if (DBMS.indexOf("SYBASE") > -1) {
            DBMS = "SYBASE";
        }
        System.out.println(DBMS);
        //
        showSearch = xsf.resource.ResourceManager.getAppKey("showSearch", false);
        showClose = xsf.resource.ResourceManager.getAppKey("showClose", false);
        cascade = xsf.resource.ResourceManager.getAppKey("cascade", false);
        showCountdown = xsf.resource.ResourceManager.getAppKey("showCountdown", true);
        closeMode = xsf.resource.ResourceManager.getAppKey("closeMode", 0);
        isFilterPerson = xsf.resource.ResourceManager.getAppKey("isFilterPerson", false);
        isCrossDept = xsf.resource.ResourceManager.getAppKey("isCrossDept", false);
        isRoleDept = xsf.resource.ResourceManager.getAppKey("isRoleDept", false);
        isStandaloneDept = xsf.resource.ResourceManager.getAppKey("isStandaloneDept", false);
        isPrivateGroup = xsf.resource.ResourceManager.getAppKey("isPrivateGroup", false);
        isSqlUserShowDept = xsf.resource.ResourceManager.getAppKey("isSqlUserShowDept", false);
        isDraftOpinion = xsf.resource.ResourceManager.getAppKey("isDraftOpinion", false);
        JoinOrder = 1;//xsf.resource.ResourceManager.getAppKey("JoinOrder", 0);
        removeAble = xsf.resource.ResourceManager.getAppKey("removeAble", false);
        timeStandard = xsf.resource.ResourceManager.getAppKey("timeStandard", 1);
        SelectUserMode = xsf.resource.ResourceManager.getAppKey("SelectUserMode", 0);
        openInfoMJ = xsf.resource.ResourceManager.getAppKey("openInfoMJ", false);
        //
    }

    public static String AppSettings(String name) {
        if ("DBMS".equals(name)) {
            return DBMS;
        }
        return xsf.resource.ResourceManager.getAppKey(name);
    }

    public static String getServerInfo(HttpServletRequest request) {
        String HTTP_IP_PORT = MACHINE;
        if (request != null) {
            HTTP_IP_PORT = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        }
        // SOA回调 主要是流程调用 https 直接转化到 resource.xml 配置 PORTAL_OA_URL属性 2012.7.3 taolb
        if (HTTP_IP_PORT.startsWith("https:")) {
            String oaUrl = AppSettings("PORTAL_OA_URL");
            if (oaUrl != null && !"".equals(oaUrl)) {
                HTTP_IP_PORT = oaUrl;
            }
        }
        String soaMachine = AppSettings("SOA_MACHINE");
        if (!soaMachine.toUpperCase().startsWith("HTTP")) {
            if ("".equals(soaMachine) || "/".equals(soaMachine)) {
                soaMachine = HTTP_IP_PORT;
            } else {
                soaMachine = HTTP_IP_PORT + (soaMachine.startsWith("/") ? "" : "/") + soaMachine;
            }
        }
        return soaMachine;
    }

    public static void main(String[] args) {
        System.out.println(AppSettings("connectString"));
    }

}
