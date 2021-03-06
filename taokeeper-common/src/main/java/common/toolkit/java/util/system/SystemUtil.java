package common.toolkit.java.util.system;


import static common.toolkit.java.constant.SystemPropertyConstant.OS_ARCH;
import static common.toolkit.java.constant.SystemPropertyConstant.OS_NAME;
import static common.toolkit.java.constant.SystemPropertyConstant.OS_VERSION;
import static common.toolkit.java.constant.SystemPropertyConstant.USER_LANGYAGE;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import common.toolkit.java.constant.BaseConstant;
import common.toolkit.java.util.StringUtil;
import common.toolkit.java.util.io.FileUtil;

/**
 * 类说明: 系统信息工具类
 * 
 * @author 银时 yinshi.nc@taobao.com
 */
public class SystemUtil {

	/**
	 * 获取当前操作系统名称
	 * @return 操作系统名称，例如windows xp，linux等。
	 */
	public static String getOSName() {
		return StringUtil.trimToEmpty( System.getProperty( OS_NAME ) ).toLowerCase();
	}

	/**
	 * 获取当前操作系统的语言
	 * @return 操作系统语言，例如zh（中文），en（英文）
	 */
	public static String getOSLanguage() {
		return StringUtil.trimToEmpty( System.getProperty( USER_LANGYAGE ) );
	}

	/**
	 * 获取当前操作系统的版本
	 * @return 操作系统版本
	 */
	public static String getOSVersion() {
		return System.getProperty( OS_VERSION );
	}

	/**
	 * 获取当前系统架构
	 * @return 系统架构
	 */
	public static String getOSArch() {
		return System.getProperty( OS_ARCH );
	}

	/**
	 * 获取本机主机名称
	 * @return 本机主机名称
	 */

	public static String getHostName() {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getLocalHost();
			if ( null == inetAddress ) {
				return "Unknown Host";
			} else {
				return inetAddress.getHostName();
			}
		} catch ( UnknownHostException e ) {
			return "Unknown Host";
		}
	}

	/**
	 * 获取本机IP地址
	 * @return 本机IP
	 */
	public static String getIPAddress() {
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getLocalHost();
			if ( null == inetAddress ) {
				return "Unknow ip";
			} else {
				return inetAddress.getHostAddress();
			}
		} catch ( Exception e ) {
			return "Unknow ip";
		}
	}
	
	/**
	 * Load the *.properties for config
	 * @throws IOException 
	 * */
	public static Properties loadProperty() throws IOException{
		
//		String filePathOfProperty = StringUtil.trimToEmpty( System.getProperty( BaseConstant.SYSTEM_PROPERTY_CONFIG_FILE_PATH ) );
//		String filePathOfProperty = "/taokeeper-monitor-config.properties";
//		if( StringUtil.isBlank( filePathOfProperty ) ){
//			throw new FileNotFoundException( "Please defined,such as -DconfigFilePath=\"W:\\TaoKeeper\\taokeeper\\config\\config-test.properties\"" );
//		}
//		return FileUtil.readPropertyFile( filePathOfProperty );
		
		Properties p = new Properties();
		p.put("systemInfo.envName", "DAILY-SHINEMO");
		p.put("dbcp.driverClassName", "com.mysql.jdbc.Driver");
		p.put("dbcp.dbJDBCUrl", "jdbc:mysql://127.0.0.1:3306/taokeeper");
		p.put("dbcp.characterEncoding", "GBK");
		p.put("dbcp.username", "root");
		p.put("dbcp.password", "taobao1234");
		p.put("dbcp.maxActive", "30");
		p.put("dbcp.maxIdle", "10");
		p.put("dbcp.maxWait", "1000");
		p.put("SystemConstent.dataStoreBasePath", "/home/dobbox/taokeeper/taokeeper-monitor/datastore/");
		p.put("SystemConstant.userNameOfSSH", "root");
		p.put("SystemConstant.passwordOfSSH", "N2e3wsun");
		p.put("SystemConstant.configOfMsgCenter", "");
		
		return p;
	}
	
	
	
	
	
	
	
	
	
	

}