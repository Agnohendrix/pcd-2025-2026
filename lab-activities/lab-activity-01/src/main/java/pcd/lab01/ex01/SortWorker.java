package pcd.lab01.ex01;

import java.util.Arrays;

public class SortWorker extends Thread {

	private final int[] array;
	private final int from;
	private final int to;
	
	private final Object lock;
	private final Counter counter;
	
	public SortWorker(int[] array, int from, int to, Object lock, Counter counter) {
		this.array = array;
		this.from = from;
		this.to = to;
		this.lock = lock;
		this.counter = counter;
	}
	
	@Override
	public void run() {
		Arrays.sort(array, from, to);
		
		synchronized(lock) {
			counter.completed++;
			lock.notifyAll();
		}
	}
}
