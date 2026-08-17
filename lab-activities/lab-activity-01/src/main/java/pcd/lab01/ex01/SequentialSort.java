package pcd.lab01.ex01;

import java.util.*;

public class SequentialSort {

	static final int VECTOR_SIZE = 400_000_000;
	
	public static void main(String[] args) {
	
		log("Num elements to sort: " + VECTOR_SIZE);
		log("Generating array.");
		var v = genArray(VECTOR_SIZE);
		var v2 = v;
		log("Array generated.");
		log("Sorting.");
	
		long t0 = System.nanoTime();		
		Arrays.sort(v, 0, v.length);
		long t1 = System.nanoTime();
		log("Done. Time elapsed: " + ((t1 - t0) / 1000000) + " ms");
		
		Object lock = new Object();
		Counter counter = new Counter();
		
		int mid = v.length / 2;
		
		SortWorker w1 = new SortWorker(v2, 0, mid, lock, counter);
		SortWorker w2 = new SortWorker(v2, mid, v2.length, lock, counter);
		
		long t2 = System.nanoTime();
		
		w1.start();
		w2.start();
		
		synchronized(lock) {
			while(counter.completed < 2) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		merge(v2, mid);
		
		long t3 = System.nanoTime();
		
		log("Done concurrent, Time elapsed: " + ((t3 - t2) / 1_000_000) + " ms");
		
		// dumpArray(v);
	}
	
	private static void merge(int[] v, int mid) {
		int[] tmp = new int[v.length];
		
		int i = 0;
		int j = mid;
		int k = 0;
		
		while(i< mid && j < v.length) {
			if(v[i] <= v[j]) {
				tmp[k++] = v[i++];
			} else {
				tmp[k++] = v[j++];
			}
		}
		
		while(i < mid) {
			tmp[k++] = v[i++];
		}
		
		while(j < v.length) {
			tmp[k++] = v[j++];
		}
		
		System.arraycopy(tmp,  0,  v,  0, v.length);
	}


	private static int[] genArray(int n) {
		Random gen = new Random(System.currentTimeMillis());
		var v = new int[n];
		for (int i = 0; i < v.length; i++) {
			v[i] = gen.nextInt();
		}
		return v;
	}

	private static void dumpArray(int[] v) {
		for (var l:  v) {
			System.out.print(l + " ");
		}
		System.out.println();
	}

	private static void log(String msg) {
		System.out.println(msg);
	}
}
