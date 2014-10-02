/**
 * Thread entity that receive computational message
 * @author Jun Yu
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class RecvEntity implements Runnable {
	ServerSocket recvSock;
	InetAddress addr;
	int port;
	
	Process process;

	public void setProcess(Process process) {
		this.process = process;
	}
	
	private void handleComputationalMessage(Message message) {
		int remoteId = message.getSenderId();
		if(process.getSemState().tryAcquire()) {
			// the process is idle
			process.setParent(process.getAllProcessList().get(remoteId));
			Log.receiveComputationalMessage(remoteId, true);
			// TODO then active process
			process.activateComputation();
			process.getSemState().release();
			
		} else {
			// The process is in active
			// then return ACK immediately
			Log.receiveComputationalMessage(remoteId, false);
			process.sendAck(remoteId);
		}
	}

	@Override
	public void run() {
		try {	
			while(!process.isTerminated()) {
				Socket clientSock = recvSock.accept();
				//System.out.println("Just connected to "
		        //          + clientSock.getRemoteSocketAddress());
				BufferedReader input = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
				String msg = input.readLine();
				Message message = new Message(msg);
				if(message.isReady()) {
					process.setReady(message.getSenderId());
				}
				if(message.isComputation()) {
					handleComputationalMessage(message);
				}
				if(message.isTermination()) {
					process.goingToTerminate();
					Log.receiveTermination();
					Thread.sleep(1000);
					System.exit(0);
				}
				
				input.close();
				clientSock.close();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public RecvEntity(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;
		try {
			recvSock = new ServerSocket(port);
			System.out.println("message receive bind to " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
