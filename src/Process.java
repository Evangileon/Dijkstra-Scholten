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
import java.util.concurrent.Semaphore;

public class Process {
	public static final int READY_TIMEOUT = 30;

	int localTime;
	int id;

	Process parent;
	LinkedList<Process> childList;
	LinkedList<Integer> pendingAcks;

	HashMap<Integer, Process> allProcessList;
	boolean[] readyList;

	InetAddress processAddr;
	int processRecvPort;
	int processAcksPort;

	Semaphore sem;
	boolean isRealProcess;

	public Process() {
		isRealProcess = true;
		childList = new LinkedList<>();
		pendingAcks = new LinkedList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public HashMap<Integer, Process> getAllProcessList() {
		return allProcessList;
	}

	public void setAllProcessList(HashMap<Integer, Process> allProcessList) {
		this.allProcessList = allProcessList;
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
					if(readyList[i] == false) {
						areAllReady = false;
						break;
					}
				}
				
				if(areAllReady == true) {
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

	public void setReady(int id) {
		readyList[id] = true;
	}
	
	/**
	 * Send ready message to specific process
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
	 * @param remoteId
	 */
	public boolean containPendingMessage(int remoteId) {
		return pendingAcks.contains(remoteId);
	}
	
	public void removeFromPendingAcks(int remoteId) {
		pendingAcks.remove(Integer.valueOf(remoteId));
	}
	
	/**
	 * Send message so specific process
	 * @param receiverId
	 * @param message
	 */
	private void sendMessage(int receiverId, Message message) {
		Process receiverProcess = allProcessList.get(receiverId);
		try {
			Socket sock = new Socket(receiverProcess.getProcessAddr(), receiverProcess.getProcessRecvPort());
			DataOutputStream output = new DataOutputStream(sock.getOutputStream());
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
		
		// If wait for some process for too long, probably something wrong happens
		if (!waitForAllProcessReady()) {
			System.out.println("Timeout: some process not ready");
			return;
		}
		
		System.out.println("Everyone is ready: begin computational request");
		// then go to business logic
		
		
		try {
			recvThread.join();
			acksThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
