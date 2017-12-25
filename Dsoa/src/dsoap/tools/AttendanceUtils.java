package dsoap.tools;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import xsf.data.DBManager;
import xsf.data.DataRow;
import xsf.data.DataTable;
import xsf.data.DbType;
import xsf.data.Sql;
import xsf.template.velocity.VelocityUtils;
import xsf.util.StringHelper;

public class AttendanceUtils {


	public static final String ALLDEPTSQL = "select ID,UNAME as DEPTNAME from G_USERS where UTYPE=2 and STATUS>=0 and HRCHANGED=1 order by USERORDERBY";
	public static final String[] WEEK = { "星期一", "星期二", "星期三", "星期四", "星期五",
			"星期六", "星期日" };
	public static final Map<String, Object> EXCLUDEDEPTNAMEMAPS = new HashMap<String, Object>();
	public static final Map<String, Integer> attendancePerssion = new HashMap<String, Integer>();
	public static final Map<Integer, String> deptAttendance = new HashMap<Integer, String>();

	public static final String startWork = ConfigurationSettings.AppSettings("WorkStartTime");
	public static final String middayWork = "12:00";	// 中午时间
	public static final String endWork = ConfigurationSettings.AppSettings("WorkEndTime");
	public static final String NOCARDMESSAGE = "无";// "未打卡";

	public static final String ALLHOLIDAYS = "SELECT YEAR, MONTH, DAY FROM AST_Holidays WHERE HolidaysTypeID<>1";

//	/**
//	 * 考勤权限
//	 */
//	static {
//		attendancePerssion.putAll(ASTConfig.attendancePerssion);
//		deptAttendance.putAll(ASTConfig.deptAttendance);
//		setExcludeDeptName(ASTConfig.AST_PARAMS);
//
//	}

	private static void setExcludeDeptName(Properties prop) {

		if (prop == null)
			return;

		String excludeDeptName = prop.getProperty("excludedeptname");

		if (excludeDeptName == null)
			return;

		String arr[] = excludeDeptName.split(",");
		for (String s : arr) {
			EXCLUDEDEPTNAMEMAPS.put(s, null);
		}

	}

	@SuppressWarnings("deprecation")
	public static String paintWeekGrid() {
		Calendar cal = Calendar.getInstance();
		/*System.out.println(cal.get(Calendar.DATE));
		System.out.println(cal.get(Calendar.DAY_OF_MONTH));
		System.out.println(cal.get(Calendar.DAY_OF_WEEK_IN_MONTH));*/
		Date date = new Date();
		/*System.out.println(date.getDate());
		System.out.println(date.getDay());*/
		return null;
	}

	@SuppressWarnings("deprecation")
	public static void paintMonthGrid(VelocityContext context) {
		Calendar calendar = Calendar.getInstance();
		int dateCount = calendar.getActualMaximum(Calendar.DATE);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		int day = calendar.getTime().getDay() - 1;
		Vector<String> arr = new Vector<String>();
		for (int i = 0; i < day; i++) {
			arr.add("&nbsp;");
		}
		for (int i = 0; i < dateCount; i++) {
			arr.add((i + 1) + "");
		}
		int lastDate = (dateCount + day) % 7;
		if (lastDate != 0) {
			int ld = 7 - lastDate;
			for (int i = 0; i < ld; i++) {
				arr.add("&nbsp;");
			}
		}
		context.put("monthGrids", arr);

	}

//	public static String process() {
//
//		try {
//			Template template = VelocityUtils.getTemplate(PERSON, "utf-8");
//			StringWriter out = new StringWriter();
//			VelocityContext context = new VelocityContext();
//			context.put("WEEK", WEEK);
//			paintMonthGrid(context);
//			template.merge(context, out);
//
//			return out.toString();
//
//		} catch (ResourceNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParseErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return "";
//	}

	public static Date getNextWorkDate(Date date, int num) {
		int[] array = getDateArray(date, num);
		if (array != null) {
			int nextNum = num;
			while (!isWorkDay(date)) {
				array = getDateArray(date, ++nextNum);
			}
			return getDate(date, nextNum).getTime();
		}
		return null;
	}

	/**
	 * 传入设定的日期对象，判断是否为工作日，宋骏
	 * 
	 * @param date
	 * @return true：工作日； false：非工作日
	 */
	public static boolean isWorkDay(Date date) {
		if(StringHelper.isNullOrEmpty(date)){
			return false;
		}
		Calendar now = Calendar.getInstance();
		now.setTime(date);
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		int day = now.get(Calendar.DAY_OF_MONTH);
		String sqlString = "select * from AST_Holidays where Year=?Year and Month=?Month and Day=?Day and HolidaysTypeID<>1";
		Sql sql = new Sql(sqlString);
		sql.getParameters().add("Year", year, DbType.NUMERIC);
		sql.getParameters().add("Month", month, DbType.NUMERIC);
		sql.getParameters().add("Day", day, DbType.NUMERIC);
		if (DBManager.getDataTable(sql).getRows().size() > 0) {
			return false;
		}
		return true;
	}

	public static Date getWorkDate(Date date, int num) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
//		cal.set(Calendar.HOUR_OF_DAY, 0);
//		cal.set(Calendar.MINUTE, 0);
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
		if (num == 0) {
			if (isWorkDay(date)) {
				return cal.getTime();
			} else {
				return null;
			}
		} else {
			Sql sql = new Sql(ALLHOLIDAYS);
			List<String> list = parseHolidayResult(DBManager.getDataTable(sql));

			if (num > 0) {
				int index = 0;
				while (index < num) {
					cal.add(Calendar.DAY_OF_MONTH, 1);
					if (!list.contains(cal.getTime().toString())) {
						index++;
					}
				}
			} else if (num < 0) {
				num = Math.abs(num);
				int index = 0;
				while (index < num) {
					cal.add(Calendar.DAY_OF_MONTH, -1);
					if (!list.contains(cal.getTime().toString())) {
						index++;
					}
				}
			}
			return cal.getTime();
		}
	}

	private static List<String> parseHolidayResult(DataTable data) {
		List<String> list = null;

		if (data.getTotal() > 0) {
			list = new ArrayList<String>();

			for (DataRow row : data.getRows()) {
				list.add(toDate(row.getString("YEAR") + "-" + row.getString("MONTH")
								+ "-" + row.getString("DAY")).toString());
			}
		}
		return list;
	}
	
	private static Date toDate(String str)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date=null;
		try {
			date=sdf.parse(str);
		} catch (Exception e) {
			try {
				date=new Date();
				String newDate=sdf.format(date);
				date=sdf.parse(newDate);
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}
		return date;
	}
	
	
	private static Calendar getDate(Date date, int num) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, num);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	private static int[] getDateArray(Date date, int num) {
		int[] array = null;
		if (date != null) {
			array = new int[3];

			Calendar cal = getDate(date, num);

			array[0] = cal.get(Calendar.YEAR);
			array[1] = cal.get(Calendar.MONTH) + 1;
			array[2] = cal.get(Calendar.DAY_OF_MONTH);
		}
		return array;
	}

	public static String getDateString(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		return format.format(date);
	}

	public static void main(String[] args) {
		Date date = new Date();
		SimpleDateFormat datefor = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = datefor.format(getWorkDate(new Date(), -3));
		System.out.println(s);

		System.out.println(isWorkDay(new Date()));
		// System.out.println(AttendanceUtils.isWorkDay(date));
	}
}
