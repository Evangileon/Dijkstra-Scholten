/**
 * Process entity running on each machine
 * @author Jun Yu
 */

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Process {
	public static final int READY_TIMEOUT = 30;

	int localTime;
	int id;

	Process parent;
	LinkedList<Process> childList;
	LinkedList<Message> pendingAcks;

	HashMap<Integer, Process> allProcessList;
	boolean[] readyList;

	InetAddress processAddr;
	int processRecvPort;
	int processSendPort;

	Semaphore sem;
	boolean isRealProcess;

	public Process() {
		isRealProcess = true;
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

	public Process(int id, InetAddress addr, int recvPort, int sendPort) {
		this.id = id;
		processAddr = addr;
		processRecvPort = recvPort;
		processSendPort = sendPort;
		isRealProcess = false;
	}

	public int getProcessRecvPort() {
		return processRecvPort;
	}

	public void setProcessRecvPort(int processRecvPort) {
		this.processRecvPort = processRecvPort;
	}

	public int getProcessSendPort() {
		return processSendPort;
	}

	public void setProcessSendPort(int processSendPort) {
		this.processSendPort = processSendPort;
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
	
	public void setReady(int id) {
		readyList[id] = true;
	}

	public void run() {
		RecvEntity recvEntity = new RecvEntity(processAddr, processRecvPort);
		recvEntity.setProcess(this);
		Thread recvThread = new Thread(recvEntity);
		recvThread.start();
		
		if (!waitForAllProcessReady()) {
			System.out.println("Timeout: some process not ready");
			return;
		}
	}
}
