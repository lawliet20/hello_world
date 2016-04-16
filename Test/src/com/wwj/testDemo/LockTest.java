package com.wwj.testDemo;

public class LockTest {

	public static void main(String[] args) {
		Source s = new Source();
		Thread t1 = new Thread(s);
		Thread t2 = new Thread(s);
		Thread t3 = new Thread(s);
		Thread t4 = new Thread(s);
		Thread t5 = new Thread(s);
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		
	}

}
