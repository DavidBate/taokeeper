package com.taobao.taokeeper.research.watcher;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.PropertyConfigurator;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.toolkit.java.util.ObjectUtil;
import common.toolkit.java.util.ThreadUtil;

/**
 *  这个测试类主要是测试哪些事件会触发NodeChildrenChangedEvent
 * @author nileader/nileader@gmail.com
 */
public class NodeChildrenChangedEvent implements Watcher{

	private static final Logger LOG = LoggerFactory.getLogger( NodeDataChangedEvent.class );
	AtomicInteger seq = new AtomicInteger();
	private static final int SESSION_TIMEOUT = 10000;
	private static final String CONNECTION_STRING = "test.zookeeper.connection_string:2181," +
			                                                                           "test.zookeeper.connection_string2:2181," +
			                                                                           "test.zookeeper.connection_string3:2181";
	private static final String ZK_PATH 				= "/nileader";
	private static final String CHILDREN_PATH 	= "/nileader/ch";
	private static final String LOG_PREFIX_OF_MAIN = "【Main】";
	
	private ZooKeeper zk = null;
	
	private CountDownLatch connectedSemaphore = new CountDownLatch( 1 );

	/**
	 * 创建ZK连接
	 * @param connectString	 ZK服务器地址列表
	 * @param sessionTimeout   Session超时时间
	 */
	public void createConnection( String connectString, int sessionTimeout ) {
		this.releaseConnection();
		try {
			zk = new ZooKeeper( connectString, sessionTimeout,this );
			LOG.info( LOG_PREFIX_OF_MAIN + "开始连接ZK服务器" );
			connectedSemaphore.await();
		} catch ( Exception e ) {}
	}

	/**
	 * 关闭ZK连接
	 */
	public void releaseConnection() {
		if ( !ObjectUtil.isBlank( this.zk ) ) {
			try {
				this.zk.close();
			} catch ( InterruptedException e ) {}
		}
	}

	/**
	 *  创建节点
	 * @param path 节点path
	 * @param data 初始数据内容
	 * @return
	 */
	public boolean createPath( String path, String data ) {
		try {
			LOG.info( LOG_PREFIX_OF_MAIN + "节点创建成功, Path: "
					+ this.zk.create( path, //
							                  data.getBytes(), //
							                  Ids.OPEN_ACL_UNSAFE, //
							                  CreateMode.PERSISTENT )
					+ ", content: " + data );
		} catch ( Exception e ) {}
		return true;
	}

	/**
	 * 删除指定节点
	 * @param path 节点path
	 */
	public void deleteNode( String path ) {
		try {
			this.zk.delete( path, -1 );
			LOG.info( LOG_PREFIX_OF_MAIN + "删除节点成功，path：" + path );
		} catch ( Exception e ) {
			//TODO
		}
	}
	
	/**
	 * 获取子节点
	 * @param path 节点path
	 */
	private List<String> getChildren( String path, boolean needWatch ) {
		try {
			return this.zk.getChildren( path, needWatch );
		} catch ( Exception e ) {return null;}
	}
	
	public void deleteAllTestPath(){
		this.deleteNode( CHILDREN_PATH );
		this.deleteNode( ZK_PATH );
	}
	
	
	public static void main( String[] args ) {

		PropertyConfigurator.configure("src/main/resources/log4j.properties");
		
		NodeChildrenChangedEvent sample = new NodeChildrenChangedEvent();
		sample.createConnection( CONNECTION_STRING, SESSION_TIMEOUT );
		//清理节点
		sample.deleteAllTestPath();
		if ( sample.createPath( ZK_PATH, System.currentTimeMillis()+"" ) ) {
			//读取子节点
			sample.getChildren( ZK_PATH, true );
		}
		ThreadUtil.sleep( 300000 );
		sample.releaseConnection();
	}


	/**
	 * 收到来自Server的Watcher通知后的处理。
	 */
	@Override
	public void process( WatchedEvent event ) {

		ThreadUtil.sleep( 200 );
		if ( ObjectUtil.isBlank( event ) ) {
			return;
		}
		// 连接状态
		KeeperState keeperState = event.getState();
		// 事件类型
		EventType eventType = event.getType();
		// 受影响的path
		String path = event.getPath();
		String logPrefix = "【Watcher-" + this.seq.incrementAndGet() + "】";

		LOG.info( logPrefix + "收到Watcher通知" );
		LOG.info( logPrefix + "连接状态:\t" + keeperState.toString() );
		LOG.info( logPrefix + "事件类型:\t" + eventType.toString() );

		if ( KeeperState.SyncConnected == keeperState ) {
			// 成功连接上ZK服务器
			if ( EventType.None == eventType ) {
				LOG.info( logPrefix + "成功连接上ZK服务器" );
				connectedSemaphore.countDown();
			} else if ( EventType.NodeCreated == eventType ) {
				LOG.info( logPrefix + "节点创建" );
			} else if ( EventType.NodeDataChanged == eventType ) {
				LOG.info( logPrefix + "节点数据更新" );
			} else if ( EventType.NodeChildrenChanged == eventType ) {
				LOG.info( logPrefix + "子节点变更" );
				LOG.info( logPrefix + "子节点列表：" + this.getChildren( ZK_PATH, true ) );
			} else if ( EventType.NodeDeleted == eventType ) {
				LOG.info( logPrefix + "节点 " + path + " 被删除" );
			}

		} else if ( KeeperState.Disconnected == keeperState ) {
			LOG.info( logPrefix + "与ZK服务器断开连接" );
		} else if ( KeeperState.AuthFailed == keeperState ) {
			LOG.info( logPrefix + "权限检查失败" );
		} else if ( KeeperState.Expired == keeperState ) {
			LOG.info( logPrefix + "会话失效" );
		}

		LOG.info( "--------------------------------------------" );

	}

}

