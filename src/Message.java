/**
 * Message sent by processes. There are two types: REQUEST and ACK
 * @author Jun Yu
 *
 */

public class Message {
	int timeStamp;

	int senderId;
	MessageType messageType;

	
	static enum MessageType {
		COMPUTATION("COMPUTATION"), ACK("ACK"), READY("READY"), TERMINATION("TERMINATION");

		private final String name;

		private MessageType(String name) {
			this.name = name;
		}

		public boolean equalsName(String otherName) {
			return (otherName == null) ? false : name.equals(otherName);
		}

		public String toString() {
			return name;
		}

		public static MessageType fromString(String text) {
			if (text != null) {
				for (MessageType b : MessageType.values()) {
					if (text.equalsIgnoreCase(b.toString())) {
						return b;
					}
				}
			}
			return null;
		}
	}

	public String toString() {
		return "" + timeStamp + " " + senderId + " " + messageType.toString();
	}
	
	public boolean isValidMessage() {
		return (timeStamp > 0) && (senderId > 0) && (messageType != null);
	}

	public Message(String message) {
		String[] params = message.split(" ");
		this.timeStamp = Integer.parseInt(params[0]);
		this.senderId = Integer.parseInt(params[1]);
		this.messageType = MessageType.fromString(params[2]);
	}
	
	public boolean isComputation() {
		return (messageType == MessageType.COMPUTATION);
	}
	
	public boolean isAck() {
		return (messageType == MessageType.ACK);
	}
	
	public boolean isReady() {
		return (messageType == MessageType.READY);
	}
	
	public boolean isTermination() {
		return (messageType == MessageType.TERMINATION);
	}

	public int getTimeStamp() {
		return timeStamp;
	}
	
	public void setTimeStamp(int timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getSenderId() {
		return senderId;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
	public Message() {
		// TODO Auto-generated constructor stub
	}
	
	public static Message readyMessage(int senderId) {
		Message message = new Message();
		message.setSenderId(senderId);
		message.setMessageType(MessageType.READY);
		return message;
	}
	
	public static Message computationalMessage(int senderId, int timeStamp) {
		Message message = new Message();
		message.setSenderId(senderId);
		message.setTimeStamp(timeStamp);
		message.setMessageType(MessageType.COMPUTATION);
		return message;
	}
	
	public static Message ackMessage(int senderId, int timeStamp) {
		Message message = new Message();
		message.setSenderId(senderId);
		message.setTimeStamp(timeStamp);
		message.setMessageType(MessageType.ACK);;
		return message;
	}
	
	public static Message termMessage() {
		Message message = new Message();
		message.setMessageType(MessageType.TERMINATION);
		return message;
	}
}
