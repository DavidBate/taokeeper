package com.taobao.zookeeper.presstest;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.ZkClient;

/**
 * 
 * @author pingwei
 * 2014-3-31 下午10:55:38
 */

public class PressTest {

	static final int nodeCount = 3 * 1000;
	static ZkClient client = null;
	
	public static void main(String[] args) {
		initClient(args[0]);
		createNode();
		int readTps = Integer.parseInt(args[1]);
		int writeTps = Integer.parseInt(args[2]);
		doPress(readTps, writeTps);
	}
	
	public static void doPress(int read, int write){
		Random r = new Random(System.currentTimeMillis());
		while(true){
			for(int i = 0; i < read; i++){
				client.readData("/pingwei/test" + r.nextInt(nodeCount));
			}
			for(int i = 0; i < write; i++){
				client.writeData("/pingwei/test" + r.nextInt(nodeCount), "test + test".getBytes());
			}
		}
	}
	
	public static void initClient(String servers){
		client = new ZkClient(servers, 5000);
		client.waitUntilConnected(5000, TimeUnit.SECONDS);
	}
	
	public static void createNode(){
		for(int i =0; i < nodeCount; i++){
			if(!client.exists("/pingwei/test" + i)){
				client.createPersistent("/pingwei/test" + i, "test + test : ".getBytes());
			}
		}
	}
	
}


