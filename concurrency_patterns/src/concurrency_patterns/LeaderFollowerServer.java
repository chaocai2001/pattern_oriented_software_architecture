package concurrency_patterns;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class WorkerThread implements Runnable {
	private int workID;
	private Lock leaderToken;
	private Selector selector;
	private ServerSocketChannel serverSocketChannel;

	public WorkerThread(int workID, Lock leaderToken, Selector selector, ServerSocketChannel serverSocketChannel) {
		this.leaderToken = leaderToken;
		this.serverSocketChannel = serverSocketChannel;
		this.workID = workID;
		this.selector = selector;
	}

	@Override
	public void run() {
		while (true) {
			leaderToken.lock(); // 等待获取Leader token，获取后成为leader线程
			System.out.println("work " + this.workID + " got leader token.");
			try {
				out: while (true) { // check the ready channel
					int readyChannels;
					readyChannels = selector.select();
					if (readyChannels == 0)
						continue;
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
					while (keyIterator.hasNext()) {// handle the event
						SelectionKey key = keyIterator.next();
						if (key.isAcceptable()) {
							keyIterator.remove();
							SocketChannel client = serverSocketChannel.accept();
							leaderToken.unlock(); // 接收完事件后，释放Leader Token，让其它线程成为Leader
							// 继续处理事件
							System.out.println("work " + this.workID + " released leader token.");
							client.write(ByteBuffer.wrap("Hello\n".getBytes()));
							client.close();
							break out;
						}

					}

				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (leaderToken.tryLock()) {
					leaderToken.unlock();
				}
			}
		}

	}
}

public class LeaderFollowerServer {
	final static int NUM_OF_THREAD = 6;
	public void start() throws Exception {
		Lock leaderToken = new ReentrantLock();
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(8000));
		Selector selector = Selector.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		for (int i = 0; i < NUM_OF_THREAD; i++) {
			new Thread(new WorkerThread(i, leaderToken, selector, serverSocketChannel)).start();
		}
	}

}
