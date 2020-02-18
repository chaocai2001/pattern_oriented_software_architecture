package concurrency_patterns;

import org.junit.jupiter.api.Test;

public class TestLeaderFollower {

	@Test
	void test_single_thread_server() throws Exception {
		SingleThreadServer sts = new SingleThreadServer();
		sts.start();
	}

	@Test
	void test_half_sync_half_async_server() throws Exception {
		HalfSyncHalfAsyncServer server = new HalfSyncHalfAsyncServer();
		server.start();
	}

	@Test
	void test_leader_follower_server() throws Exception {
		LeaderFollowerServer server = new LeaderFollowerServer();
		server.start();
		Thread.sleep(100 * 1000);
	}
}