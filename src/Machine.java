import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Jun Yu
 */

/**
 * Program entry class
 * @author Jun Yu
 *
 */
public class Machine {
	
	String hostname;
	int id;
	boolean isRealMachine;
	
	ArrayList<Machine> allMachineList;
	
	public Machine(int id, String hostname) {
		
		this.id = id;
		this.hostname = hostname;
		isRealMachine = false;
	}
	
	public Machine() {
		isRealMachine = true;
	}

	private void parseXML(String filename) {
		allMachineList = new ArrayList<>();
		
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
			
			//System.out.println(doc.getDocumentElement().getNodeName());
			Node machines = doc.getElementsByTagName("machines").item(0);
			//System.out.println(machines.item(0).getNodeName());
			NodeList machineList = machines.getChildNodes();
			
			for (int i = 0; i < machineList.getLength(); i++) {
				Node oneMachine = machineList.item(i);
				NodeList machineConfig = oneMachine.getChildNodes();
				int id = 0;
				String hostname = null;
				
				for (int j = 0; j < machineConfig.getLength(); j++) {
					Node oneConfig = machineConfig.item(j);
					if(oneConfig.getNodeName().equals("id")) {
						id = Integer.parseInt(oneConfig.getTextContent());
					}
					if(oneConfig.getNodeName().equals("hostname")) {
						hostname = oneConfig.getTextContent();
					}
				}
				this.allMachineList.add(new Machine(id, hostname));
			}
			
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isThisMachine(String hostname) {
		if(this.hostname.equals(hostname)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Identify the machine object with ID and hostname, by compare the system
	 * function gethostname and the machine list.
	 */
	public void identifyItself() {
		try {
			String localHostname = InetAddress.getLocalHost().getHostName();
			for (Machine oneMachine : allMachineList) {
				if(oneMachine.isThisMachine(localHostname)) {
					this.hostname = localHostname;
					this.id = oneMachine.getId();
					return;
				}
			}
			
			// Otherwise, something wrong in the configs.xml
			System.err.println("Can NOT identify this machine");
			System.exit(-1);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getHostname() {
		return hostname;
	}

	public int getId() {
		return id;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Machine machine = new Machine();
		machine.parseXML("configs.xml");
		machine.identifyItself();
		
	}

}
