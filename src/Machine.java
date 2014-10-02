/**
 * @author Jun Yu
 */

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Program entry class
 * 
 * @author Jun Yu
 * 
 */
public class Machine {

	String hostname;
	int recvPort;
	int acksPort;
	
	int id;
	boolean isRealMachine;

	HashMap<Integer, Machine> allMachineList;

	public Machine(int id, String hostname, int recvPort, int sendPort) {

		this.id = id;
		this.recvPort = recvPort;
		this.hostname = hostname;
		isRealMachine = false;
	}

	public Machine() {
		isRealMachine = true;
	}

	public String getHostname() {
		return hostname;
	}

	public int getId() {
		return id;
	}
	
	public int getRecvPort() {
		return recvPort;
	}
	
	public int getSendPort() {
		return acksPort;
	}

	/**
	 * Parse XML to acquire hostname, ports of process
	 * @param filename
	 */
	private void parseXML(String filename) {
		allMachineList = new HashMap<>();

		File fXmlFile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement();
			if (!doc.hasChildNodes()) {
				System.exit(0);
			}
			doc.normalize();

			// System.out.println(doc.getDocumentElement().getNodeName());
			Node machines = doc.getElementsByTagName("machines").item(0);
			// System.out.println(machines.item(0).getNodeName());
			NodeList machineList = machines.getChildNodes();

			for (int i = 0; i < machineList.getLength(); i++) {
				Node oneMachine = machineList.item(i);
				if (oneMachine.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				
				NodeList machineConfig = oneMachine.getChildNodes();
				int id = 0;
				String hostname = null;
				int recvPort = 0;
				int acksPort = 0;

				for (int j = 0; j < machineConfig.getLength(); j++) {
					Node oneConfig = machineConfig.item(j);
					if(oneConfig.getNodeType() != Node.ELEMENT_NODE) {
						continue;
					}
					
					if (oneConfig.getNodeName().equals("id")) {
						id = Integer.parseInt(oneConfig.getTextContent());
					}
					if (oneConfig.getNodeName().equals("hostname")) {
						hostname = oneConfig.getTextContent();
					}
					if(oneConfig.getNodeName().equals("recvPort")) {
						recvPort = Integer.parseInt(oneConfig.getTextContent());
					}
					if(oneConfig.getNodeName().equals("acksPort")) {
						acksPort = Integer.parseInt(oneConfig.getTextContent());
					}
				}
				this.allMachineList.put(id, new Machine(id, hostname, recvPort, acksPort));
			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isThisMachine(String hostname) {
		if (this.hostname.equals(hostname)) {
			return true;
		}
		return false;
	}

	/**
	 * Identify the machine object with ID and hostname, by compare the system
	 * function gethostname and the machine list.
	 */
	private void identifyItself() {
		try {
			String localHostname = InetAddress.getLocalHost().getHostName();
			for (Entry<Integer, Machine> pair : allMachineList.entrySet()) {
				Machine oneMachine = pair.getValue();
				if (oneMachine.isThisMachine(localHostname)) {
					this.hostname = localHostname;
					this.id = oneMachine.getId();
					this.recvPort = oneMachine.getRecvPort();
					this.acksPort = oneMachine.getSendPort();
					Log.setLogFileName("f" + this.id);
					return;
				}
			}

			// Otherwise, something wrong in the configs.xml
			System.err.println("Can NOT identify this machine");
			System.exit(-1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Parse the machine configs, and generate a list of virtual process,
	 * so that the physical process running on this machine can easily
	 * get information about other processes
	 * @return
	 */
	private HashMap<Integer, Process> generateProcessList() {
		HashMap<Integer, Process> allProcessList = new HashMap<>();

		try {
			for (Entry<Integer, Machine> pair : allMachineList.entrySet()) {
				Machine machine = pair.getValue();
				int id = machine.getId();
				InetAddress addr = InetAddress.getByName(machine.getHostname());
				int recvPort = machine.getRecvPort();
				allProcessList.put(id, new Process(id, addr, recvPort, 0));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return allProcessList;
	}
	
	/**
	 * Read configs from configs.xml and initialize the environmental variables
	 */
	public void initializeMachine() {
		parseXML("configs.xml");
		identifyItself();
	}
	
	/**
	 * Launch the process running on this machine
	 */
	public void launchProcess() {
		HashMap<Integer, Process> allProcessList = generateProcessList();
		Process process = new Process();
		process.setId(this.getId());
		process.setAllProcessList(allProcessList);
		process.setProcessRecvPort(this.getRecvPort());
		process.setProcessAcksPort(this.getSendPort());
		process.run();
	}

	public static void main(String[] args) {
		Machine machine = new Machine();
		machine.initializeMachine();
		machine.launchProcess();
	}

}
