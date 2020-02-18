package concurrency_patterns;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HalfSyncHalfAsyncServer {

	final static int NUM_OF_THREADS = 6;

	public HalfSyncHalfAsyncServer() {
	}

	public void start() throws Exception {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(8000));
		Selector selector = Selector.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		ExecutorService threadPool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		while (true) {
			int readyChannels = selector.select();
			if (readyChannels == 0)
				continue;
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
			while (keyIterator.hasNext()) {
				SelectionKey key = keyIterator.next();
				if (key.isAcceptable()) {
					SocketChannel client = serverSocketChannel.accept();
					threadPool.execute(new Runnable() {

						@Override
						public void run() {
							try {
								client.write(ByteBuffer.wrap("Hello\n".getBytes()));
								client.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}

					});
				} else if (key.isConnectable()) {
					// a connection was established with a remote server.
				} else if (key.isReadable()) {
					// a channel is ready for reading
				} else if (key.isWritable()) {
					// a channel is ready for writing
				}
				keyIterator.remove();
			}
		}
	}

}
