// Name : Chia-Sheng Hsiao
// ID : 23399066
// Course : CSCI 715
// Project 1

import java.util.Random;
import java.util.Vector;


public class Shuttle extends Thread {
	private Random random;
	private Monitor monitor;
	private long finishTime;
	public static long time = System.currentTimeMillis();
	
	public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }
	
	public Shuttle(int id, Monitor monitor, long finishTime) {
        setName("Shuttle-" + id);
        this.monitor = monitor;
        this.random = new Random();
        this.finishTime = finishTime;
    }
	
	@Override
	public void run() {
		try {
			while (System.currentTimeMillis() < this.finishTime) {
				msg("is cruising");
				sleep(random.nextInt(1000) + 1000);
				
				msg("wait for recharging");
				Vector<Thread> group = this.monitor.shuttleWaitRecharge(this);
				sleep(1000);
				
				msg("is recharging");
				int fuelAmount = random.nextInt(50) + 50;
				this.monitor.shuttleDoRecharge(this, group, fuelAmount);
				
				msg("complete recharging");
				
				msg("wait for take-off");
				this.monitor.shuttleWaitTakeoff();
				
				msg("take off!");
			}
			msg("finish");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		    return;
		}
	}
}
