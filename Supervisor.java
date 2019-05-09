// Name : Chia-Sheng Hsiao
// ID : 23399066
// Course : CSCI 715
// Project 1

import java.util.Random;


public class Supervisor extends Thread {
	private Random random;
	private Monitor monitor;
	private long finishTime;
	public static long time = System.currentTimeMillis();
	
	public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }
	
	public Supervisor(int id, Monitor monitor, long finishTime) {
        setName("Supervisor-" + id);
        this.monitor = monitor;
        this.random = new Random();
        this.finishTime = finishTime;
    }
	
	@Override
	public void run() {
		try {
			while (System.currentTimeMillis() < this.finishTime) {
				msg("is sleeping");
				sleep(random.nextInt(3000) + 3000);
				
				msg("time to take off");
				this.monitor.takeoff();
			}
			msg("finish");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		    return;
		}
	}

}
