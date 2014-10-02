import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Thread entity that receive ACK message
 * 
 * @author Jun Yu
 * 
 */
public class AcksEntity implements Runnable {
	ServerSocket acksSock;
	InetAddress addr;
	int port;

	Process process;

	public void setProcess(Process process) {
		this.process = process;
	}
	
	private boolean checkTermination() {
		if(!process.getSemState().tryAcquire()) {
			// process active, not terminated
			return false;
		}
		
		boolean terminated = false;
		if(process.getPendingAcks().size() == 0) {
			terminated = true;
		} else {
			terminated = false;
		}
		process.getSemState().release();
		return terminated;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket clientSock = acksSock.accept();
				DataInputStream input = new DataInputStream(
						clientSock.getInputStream());
				String msg = input.readUTF();
				Message message = new Message(msg);
				if (message.isAck()) {
					if (process.containPendingMessage(message.getSenderId())) {
						// remove from pending ACK list
						process.removeFromPendingAcks(message.getSenderId());
						// remove from child list
						process.removeFromChildList(message.getSenderId());
						Log.receiveAckMessage(message.getSenderId(), process.getNumMessageReceivedAcks());
						
						if(checkTermination()) {
							// satisfy the termination condition for this process
							// then send ACK to parent;
							Process parent = process.getParent();
							process.sendAck(parent.getId());
							// detach from parent
							process.setParent(null);
							Log.sendAckToParentAndDetachFromTree(parent.getId());
						}
						
					} else {
						System.out
								.println("Logical error: the process receives an Acks that is not in its pending list");
					}
				}

				input.close();
				clientSock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public AcksEntity(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;
		try {
			acksSock = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
