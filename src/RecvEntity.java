/**
 * Thread entity that receive computational message
 * @author Jun Yu
 */

import java.io.DataInputStream;
import java.io.IOException;
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
			while(true) {
				Socket clientSock = recvSock.accept();
				System.out.println("Just connected to "
		                  + clientSock.getRemoteSocketAddress());
				DataInputStream input = new DataInputStream(clientSock.getInputStream());
				String msg = input.readUTF();
				Message message = new Message(msg);
				if(message.isReady()) {
					process.setReady(message.getSenderId());
				}
				if(message.isComputation()) {
					handleComputationalMessage(message);
				}
				if(message.isTermination()) {
					Log.receiveTermination();
					return;
				}
				
				input.close();
				clientSock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RecvEntity(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;
		try {
			recvSock = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
