package com.taobao.taokeeper.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.taokeeper.model.SrvrInfo;

/**
 * 
 * @author pingwei 2014-2-26 下午1:18:24
 */

public class ZKDataUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ZKDataUtil.class);
	public static final String RUOK = "ruok";
	public static final String SRVR = "srvr";

	public static boolean ruok(String ip, int port) {
		return execCmdBySocket(ip, port, RUOK).contains("imok");
	}

	public static SrvrInfo srvr(String ip, int port) {
		return SrvrInfo.parse(execCmdBySocket(ip, port, SRVR));
	}

	public static void srvr(String ip, int port, SrvrInfo srvr) {
		SrvrInfo.parse(execCmdBySocket(ip, port, SRVR), srvr);
	}

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 5; i++) {
			String content = execCmdBySocket("10.232.6.30", 2181, "ruok");
			System.out.println("content len = "+ content.length());
		}
	}
	
	/**
	 * 通过socket连接发送命令，获取输出结果
	 * 
	 * @param ip
	 * @param port
	 * @param cmd
	 * @return
	 */
	public static String execCmdBySocket(String ip, int port, String cmd) {
		Socket so = null;
		PrintStream out = null;
		InputStream in = null;
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			so = new Socket(ip, port);
			out = new PrintStream(so.getOutputStream());
			in = so.getInputStream();
			out.println(cmd);
			out.flush();
			byte[] bs = new byte[1024*1024];
			int len = -1;
			try {
				while ((len = in.read(bs)) >= 0) {
					byteOut.write(bs, 0, len);
				}
			} catch (Exception e) {
				LOG.error("exec command failed :" + ip + ":" + port + "\t" + cmd + ". " + e.getMessage(), e.getCause());
			}
			return new String(byteOut.toByteArray());
		} catch (UnknownHostException e) {
			LOG.error("exec command failed :" + ip + ":" + port + "\t" + cmd + ". " + e.getMessage(), e.getCause());
		} catch (IOException e) {
			LOG.error("exec command failed :" + ip + ":" + port + "\t" + cmd + ". " + e.getMessage(), e.getCause());
		} finally {
			try {
				if (so != null) {
					so.shutdownOutput();
					so.shutdownInput();
					so.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally{
				try {
					so.close();
				} catch (IOException e) {
				}
			}
		}
		return "";
	}

}
