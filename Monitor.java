// Name : Chia-Sheng Hsiao
// ID : 23399066
// Course : CSCI 715
// Project 1

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;


public class Monitor {
	
	public static int numShuttle = 7;
	public static int numRecharge = 3;
	public static int numTrack = 3;
	
	public AtomicInteger remainingFuelAmount = new AtomicInteger(200);
		
	public Vector<Vector<Thread>> groups = new Vector<Vector<Thread>>();
						
	public Object signalToController = new Object();
	
	public Vector<Object> resourcesTakeoff = new Vector<>();

	public static void main(String[] args) {
		// specify the number of shuttles by the argument, or 7 for default
		if (args.length > 0)
			numShuttle =Integer.parseInt(args[0]);
		
		// finish time (end of the day)
		long finishTime = System.currentTimeMillis() + 60000;
		
		// shuttle station monitor
		Monitor monitor = new Monitor();
		
		// all threads
		List<Thread> threads = new ArrayList<Thread>();
		
		// initialize shuttle threads
		for (int i = 0; i < numShuttle; i++) {
            Shuttle shuttle = new Shuttle(i + 1, monitor, finishTime);
            shuttle.start();
            threads.add(shuttle);
        }
		
		// initialize the controller thread
		Controller controller = new Controller(1, monitor, finishTime);
		controller.start();
		threads.add(controller);
		
		// initialize the aircraft supervisor thread
		Supervisor supervisor = new Supervisor(1, monitor, finishTime);
		supervisor.start();
		threads.add(supervisor);
		
		// End of the day
		try {
			Thread.sleep(60000);
			for (Thread t: threads) {
				t.interrupt();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("End of the day");
	}

	public Vector<Thread> shuttleWaitRecharge(Shuttle shuttle) throws InterruptedException {
		Vector<Thread> group;
		synchronized (groups) {
			if (groups.isEmpty()) { // if no group available, create a new group
				group = new Vector<Thread>();
				group.add(shuttle);
				groups.add(group);
			}
			else {
				group = groups.lastElement();
				if (group.size() < numRecharge) { // if the group is not full, add the shuttle to the group
					group.add(shuttle);
				}
				else { // if the group is full, create a new group
					group = new Vector<Thread>();
					group.add(shuttle);
					groups.add(group);
				}
			}
		}
		synchronized (group) {
			group.wait(); // wait for the controller to open the recharging station
		}
		return group;
	}

	public synchronized void shuttleDoRecharge(Shuttle shuttle, Vector<Thread> group, int fuelAmount) throws InterruptedException {
		synchronized (remainingFuelAmount) {
			if (fuelAmount < remainingFuelAmount.get()) {
				remainingFuelAmount.addAndGet(-fuelAmount);
			}
			else {
				synchronized (signalToController){
					signalToController.notify(); // notify the controller to refill
				}
				remainingFuelAmount.wait(); // wait for enough fuel
			}
		}
		
		synchronized (group) {
			group.remove(shuttle);
			if (group.isEmpty()) { // last shuttle in the group
				synchronized (signalToController){
					signalToController.notify(); // notify the controller to complete re-charging
				}
			}
		}
	}

	public void shuttleWaitTakeoff() throws InterruptedException {
		Object resourceTakeoff = new Object();
		synchronized (resourceTakeoff){
			resourcesTakeoff.add(resourceTakeoff); // each shuttle has a different notification object for take-off
			resourceTakeoff.wait();
		}
	}

	public Vector<Thread> controllerNotifyRecharge() {
		Vector<Thread> group = null; // return null when no shuttle is waiting for re-charging
		synchronized (groups) {
			if (!groups.isEmpty()) {
				synchronized (groups.firstElement()){
					groups.firstElement().notifyAll(); // notify all shuttles in the current group
					group = groups.remove(0);
				}
			}
		}
		return group;
	}

	public boolean controllerWaitCompleteRecharging(Vector<Thread> group) throws InterruptedException {
		synchronized (signalToController){
			signalToController.wait();
		}
		return group.isEmpty(); // true for end of re-charge, false for lack of fuel 
	}

	public void controllerNotifyFuel() {
		synchronized (remainingFuelAmount){
			remainingFuelAmount.set(200); // refill to 200%
			remainingFuelAmount.notify();
		}
	}

	public void takeoff() {
		for (int i = 0; i < numTrack && !resourcesTakeoff.isEmpty(); i++) { // notify 'numTrack' shuttles to take off 
			synchronized (resourcesTakeoff.elementAt(0)){
				resourcesTakeoff.elementAt(0).notify();
				resourcesTakeoff.removeElementAt(0);
            }
		}
	}

}
