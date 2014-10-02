/**
 * Process entity running on each machine
 * @author Jun Yu
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Process {
	public static final int READY_TIMEOUT = 30;

	int localTime;
	int id;
	int numMessageReceivedAcks;
	int numMessageGenerated;

	Process parent;
	LinkedList<Integer> childList;
	LinkedList<Integer> pendingAcks;

	HashMap<Integer, Process> allProcessList;
	boolean[] readyList;

	InetAddress processAddr;
	int processRecvPort;
	int processAcksPort;

	Semaphore semState;
	boolean isRealProcess;

	public Process() {
		isRealProcess = true;
		childList = new LinkedList<>();
		pendingAcks = new LinkedList<>();
		semState = new Semaphore(1);
		numMessageReceivedAcks = 0;
		numMessageGenerated = 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	public synchronized LinkedList<Integer> getPendingAcks() {
		return pendingAcks;
	}

	public synchronized void setPendingAcks(LinkedList<Integer> pendingAcks) {
		this.pendingAcks = pendingAcks;
	}

	public synchronized Process getParent() {
		return parent;
	}

	public synchronized void setParent(Process parent) {
		this.parent = parent;
	}

	public HashMap<Integer, Process> getAllProcessList() {
		return allProcessList;
	}

	public void setAllProcessList(HashMap<Integer, Process> allProcessList) {
		this.allProcessList = allProcessList;
	}

	public synchronized int getNumMessageReceivedAcks() {
		return numMessageReceivedAcks;
	}

	public Process(int id, InetAddress addr, int recvPort, int acksPort) {
		this.id = id;
		processAddr = addr;
		processRecvPort = recvPort;
		processAcksPort = acksPort;
		isRealProcess = false;
	}

	public int getProcessRecvPort() {
		return processRecvPort;
	}

	public void setProcessRecvPort(int processRecvPort) {
		this.processRecvPort = processRecvPort;
	}

	public int getProcessAcksPort() {
		return processAcksPort;
	}

	public void setProcessAcksPort(int processAcksPort) {
		this.processAcksPort = processAcksPort;
	}

	/**
	 * Process should wait for all process online(ready) before it runs
	 * 
	 * @return
	 */
	private boolean waitForAllProcessReady() {
		readyList = new boolean[allProcessList.size() + 1];
		for (int i = 0; i < readyList.length; i++) {
			readyList[i] = false;
		}

		try {
			for (int i = 0; i < READY_TIMEOUT; i++) {
				boolean areAllReady = true;
				// zero index is dummy, so begin with 1
				for (int j = 1; j < readyList.length; j++) {
					if (readyList[i] == false) {
						areAllReady = false;
						break;
					}
				}

				if (areAllReady == true) {
					return true;
				}

				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;
	}

	public InetAddress getProcessAddr() {
		return processAddr;
	}

	public Semaphore getSemState() {
		return semState;
	}

	public void setReady(int id) {
		readyList[id] = true;
	}

	/**
	 * Send computational message to specific process
	 * 
	 * @param receiverId
	 */
	public void sendComputation(int receiverId) {
		Message computation = Message.computationalMessage(this.id,
				this.localTime);
		sendMessage(receiverId, computation);
	}

	public void sendAck(int receiverId) {
		if(receiverId == this.id) {
			return;
		}
		
		Message ack = Message.ackMessage(this.id, this.localTime);
		Process receiverProcess = allProcessList.get(receiverId);
		try {
			Socket sock = new Socket(receiverProcess.getProcessAddr(),
					receiverProcess.getProcessAcksPort());
			DataOutputStream output = new DataOutputStream(
					sock.getOutputStream());
			output.writeBytes(ack.toString());
			output.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Send ready message to specific process
	 * 
	 * @param receiverId
	 */
	private void sendReady(int receiverId) {
		Message ready = Message.readyMessage(this.id);
		sendMessage(receiverId, ready);
	}

	/**
	 * Send ready message to all process, including itself
	 */
	private void sendReadyToAll() {
		for (Entry<Integer, Process> pair : allProcessList.entrySet()) {
			int receiverId = pair.getKey();
			sendReady(receiverId);
		}
	}

	/**
	 * Does the pending acks list contain the specific process
	 * 
	 * @param remoteId
	 */
	public boolean containPendingMessage(int remoteId) {
		return pendingAcks.contains(remoteId);
	}

	public boolean removeFromPendingAcks(int remoteId) {
		return pendingAcks.remove(Integer.valueOf(remoteId));
	}
	
	public boolean removeFromChildList(int remoteId) {
		return childList.remove(Integer.valueOf(remoteId));
	}
	
	public void addToChildList(int remoteId) {
		childList.add(Integer.valueOf(remoteId));
	}

	/**
	 * Send message so specific process
	 * 
	 * @param receiverId
	 * @param message
	 */
	private void sendMessage(int receiverId, Message message) {
		if(receiverId == this.id) {
			return;
		}
		
		Process receiverProcess = allProcessList.get(receiverId);
		try {
			Socket sock = new Socket(receiverProcess.getProcessAddr(),
					receiverProcess.getProcessRecvPort());
			DataOutputStream output = new DataOutputStream(
					sock.getOutputStream());
			output.writeBytes(message.toString());
			output.close();
			sock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		RecvEntity recvEntity = new RecvEntity(processAddr, processRecvPort);
		recvEntity.setProcess(this);
		AcksEntity acksEntity = new AcksEntity(processAddr, processAcksPort);
		acksEntity.setProcess(this);
		Thread recvThread = new Thread(recvEntity);
		Thread acksThread = new Thread(acksEntity);

		recvThread.start();
		acksThread.start();

		// Tell everyone that online that I am ready
		sendReadyToAll();

		// If wait for some process for too long, probably something wrong
		// happens
		if (!waitForAllProcessReady()) {
			System.out.println("Timeout: some process not ready");
			return;
		}

		System.out.println("Everyone is ready: begin computational request");
		// then go to business logic
		boolean loop = true;
		while(loop) {
			// idle
		}

		try {
			recvThread.join();
			acksThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void activateComputation() {
		try {
			semState.acquire();
			boolean loop = true;
			Random randTime = new Random();
			Random randValue = new Random();
			Random randProcess = new Random();
			do {
				double elapseTime = 0.25 + 0.75 * randTime.nextDouble();
				Thread.sleep((long) (elapseTime * 1000));
				double value = randValue.nextDouble();
				if(value < 0.1) {
					loop = false;
				} else {
					int pj = 0;
					do {
						pj = 1 + randProcess.nextInt(15); // 1 + [0..14]
					} while (pj == this.id); // until i != j
					
					numMessageGenerated++;
					// add Pj to children list of this process
					this.addToChildList(pj);
					// send computation message to Pj
					this.sendComputation(pj);
				}
				
			} while (loop && numMessageGenerated < 25);

			semState.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
