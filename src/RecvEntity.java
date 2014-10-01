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
					
				}
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
