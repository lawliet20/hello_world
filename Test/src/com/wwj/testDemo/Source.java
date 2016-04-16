package com.wwj.testDemo;

public class Source implements Runnable {

	private int money = 10;

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	@Override
	public synchronized  void run() {
		System.out.println("开始投资");
		System.out.println("当前线程>>>>>>>>>>" + Thread.currentThread().getName());
		int res = --money;
		for (int i = 0; i < 10; i++) {
			System.out.println(money--);
			if (res <= 0) {
				System.out.println("结束>>>>>>>>>>>>");
				return;
			}
		}
	}

}
