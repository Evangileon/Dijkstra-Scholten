import java.io.FileWriter;
import java.io.IOException;

public class Log {
	static String logFile = null;
	FileWriter writer = null;

	public static void setLogFileName(String fileName) {
		logFile = fileName;
	}

	private Log() {
		try {
			writer = new FileWriter(logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Log instance = null;

	private static Log instance() {
		if (instance == null && logFile != null) {
			instance = new Log();
		}
		return instance;
	}

	public static void sendComputationalMessage(int receiverId, int num) {
		try {
			instance().writer.write("[" + System.currentTimeMillis() + "] "
					+ "Send computional message: ID of receiver: " + receiverId
					+ " number of message received ACKs: " + num + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void receiveComputationalMessage(int senderId,
			boolean joinTree) {
		String whether = null;
		if (joinTree) {
			whether = "The process join the tree";
		} else {
			whether = "ACK is sent";
		}

		try {
			instance().writer.write("[" + System.currentTimeMillis() + "] "
					+ "Receive computional message: ID of sender: " + senderId
					+ " " + whether + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void receiveAckMessage(int senderId, int num) {

		try {
			instance().writer.write("[" + System.currentTimeMillis() + "] "
					+ "Receive ACK message: ID of sender: " + senderId
					+ " number of message received ACKs: " + num + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void fromActiveToIdle() {

		try {
			instance().writer.write("[" + System.currentTimeMillis() + "] "
					+ "From Active to Idle." + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendAckToParentAndDetachFromTree(int oldParent) {
		try {
			instance().writer.write("[" + System.currentTimeMillis() + "] "
					+ "Sending ACK to parent and detaching from tree, parent: "
					+ oldParent + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void determineTermination() {

		try {
			instance().writer.write("[" + System.currentTimeMillis() + "] "
					+ "Termination." + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void receiveTermination() {

		try {
			instance().writer.write("[" + System.currentTimeMillis() + "] "
					+ "Computation Terminated." + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
