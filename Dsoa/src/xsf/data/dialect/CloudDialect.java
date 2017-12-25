package xsf.data.dialect;

import xsf.data.Parameter;
import xsf.data.Sql;

/**
 * 西古数据库
 * @author Glacier
 *
 */
public class CloudDialect extends OracleDialect {

	public CloudDialect() {
		super();
		//registerFunction("str",new SQLFunctionTemplate(XTypes.STRING, "?1") );
		registerKeyword("class");
		registerKeyword("function");
		registerKeyword("ref");
		registerKeyword("package");
		registerKeyword("rowid");
		
		registerKeyword("precision");
		registerKeyword("day");
	}
	public char closeQuote() {
		return '"';
	}
	public char openQuote() {
		return '"';
	}
	public String formatKeyWord(String columnName) {
		if(columnName != null && sqlKeywords.contains(columnName.toLowerCase()))
			return openQuote() + columnName.toUpperCase() + closeQuote();
		return columnName;
	}
	public String formatKeyWords(String sql) {
		if(sql != null){
			for(String keyword : sqlKeywords){
				sql = sql.replaceAll("\\b(?i)" + keyword.toUpperCase() + "\\b", formatKeyWord(keyword));
			}
		}
		return sql;
	}
	@Override
	public Sql getFieldSelectCommand(String scheme, String tableName) {
		Sql sql = new Sql("SELECT C.COL_NAME AS COLUMN_NAME, C.TYPE_NAME AS DATA_TYPE, C.COMMENTS AS COLUMN_COMMENT, DECODE(C.NOT_NULL, 'true', 1, 0) AS IS_NULLABLE, C.COL_NO AS COLUMN_ID, C.SCALE AS NUMERIC_SCALE FROM ALL_COLUMNS C INNER JOIN ALL_TABLES T ON C.TABLE_ID=T.TABLE_ID WHERE T.TABLE_NAME = ?");
		sql.getParameters().add(new Parameter("TABLE_NAME", tableName));
		return sql;
	}
	
	@Override
	public Sql getFieldSelectCommand(String scheme) {
		return new Sql("SELECT COL_NAME AS COLUMN_NAME, TYPE_NAME AS DATA_TYPE, COMMENTS AS COLUMN_COMMENT, DECODE(NOT_NULL, 'true', 1, 0) AS IS_NULLABLE, COL_NO AS COLUMN_ID, SCALE AS NUMERIC_SCALE FROM ALL_COLUMNS");
	}
	
	/**
	 * 获取所有的表信息
	 */
	@Override
	public Sql getTableSelectCommand(String scheme) {
		return new Sql("SELECT TABLE_NAME, TABLE_TYPE, COMMENTS AS TABLE_COMMENT FROM USER_TABLES");
	}
}