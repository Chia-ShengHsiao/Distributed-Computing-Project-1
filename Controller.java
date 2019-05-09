// Name : Chia-Sheng Hsiao
// ID : 23399066
// Course : CSCI 715
// Project 1

import java.util.Random;
import java.util.Vector;


public class Controller extends Thread {
	private Random random;
	private Monitor monitor;
	private long finishTime;
	public static long time = System.currentTimeMillis();
	
	public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }
	
	public Controller(int id, Monitor monitor, long finishTime) {
        setName("Controller-" + id);
        this.monitor = monitor;
        this.random = new Random();
        this.finishTime = finishTime;
    }
	
	@Override
	public void run() {
		try {
			while (System.currentTimeMillis() < this.finishTime) {
				msg("is sleeping");
				sleep(random.nextInt(2000) + 2000);
				
				msg("open recharging station");
				Vector<Thread> group = this.monitor.controllerNotifyRecharge();
				if (group == null) {
					msg("no shuttles waiting for recharging");
					continue;
				}
				msg("wait shuttles to complete recharging");
				while (!this.monitor.controllerWaitCompleteRecharging(group)) {
					msg("refill recharging station to 200%");
					this.monitor.controllerNotifyFuel();
				}
			}
			msg("finish");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		    return;
		}
	}

}
