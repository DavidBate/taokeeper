import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Test {
	
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		System.out.println("enter: ip port command");
		while(true){
			String line = s.nextLine();
			args = line.split(" ");
			System.out.println(execCmdBySockset(args[0], Integer.parseInt(args[1]), args[2]));
		}
	}
	
	public static String execCmdBySockset(String ip, int port , String cmd){
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
			byte[] bs = new byte[1024 * 1024];
			int len = -1;
			try {
				while((len = in.read(bs)) >= 0){
					byteOut.write(bs, 0, len);
				}
			} catch (Exception e) {
				System.out.println("exec command :" + cmd + " failed ." + e.getMessage());
			}
			return new String(byteOut.toByteArray());
		} catch (UnknownHostException e) {
			System.out.println("exec command :" + cmd + " failed ." + e.getMessage());
		} catch (IOException e) {
			System.out.println("exec command :" + cmd + " failed ." + e.getMessage());
		} finally {
			try {
				if(out != null){
					out.close();
				}
				if(in != null){
					in.close();
				}
				if (so != null) {
					so.close();
				}
				byteOut.close();
			} catch (Exception e) {
			}
		}
		return "";
	}
}


