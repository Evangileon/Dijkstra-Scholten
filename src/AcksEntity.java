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
						process.removeFromPendingAcks(message.getSenderId());
					} else {
						System.out
								.println("Logical error: the process receives an Acks that is not in its pending list");
					}
				}

				input.close();
				clientSock.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
