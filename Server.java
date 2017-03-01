package SDIS;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.*;
import java.util.ArrayList;
import java.util.TimerTask;

public class Server {

	public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16
	
	private static MulticastSocket generateSocket() throws IOException {
		MulticastSocket socket = new MulticastSocket();
		socket.setTimeToLive(1); //To avoid network congestion
		return socket;
	}
	
	private static void advertiser(int servicePort, InetAddress multicastAddress, int multicastPort) {
		try {
			//Prepare the multicast message to diffuse
			StringBuilder mcastMessage = new StringBuilder("multicast:");
			mcastMessage.append(InetAddress.getLocalHost().getHostAddress()).append(" ").append(servicePort);
			String mcastMsg = mcastMessage.toString();

			MulticastSocket socket = generateSocket();
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
			DatagramPacket msgToDiffuse = new DatagramPacket(data, data.length, multicastAddress, multicastPort);
			msgToDiffuse.setData(mcastMsg.getBytes());

			while(true) {
				socket.send(msgToDiffuse);
				System.out.println(mcastMsg);
				//Just for avoiding overflood
				Thread.sleep(1000);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void requestsProcessor() {
		ArrayList<Plate> plateList = new ArrayList<>();

		try {
			MulticastSocket socket = generateSocket(); //ATENCAO Aqui não é DatagramSocket? Aqui é so um socket para uma ligação 1Cliente - 1Servidor
			byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
			DatagramPacket msgReceived = new DatagramPacket(data, data.length);

			while(true) {
				socket.receive(msgReceived);
				String msgText = new String(data, 0, msgReceived.getLength());
				System.out.println(msgText);

				//Prepare the response
				InetAddress clientAddress = msgReceived.getAddress();
				int clientPort = msgReceived.getPort();
				DatagramPacket msgToSend = new DatagramPacket(data, data.length, clientAddress, clientPort);
				String response = "";

				String splittedMsg[] = msgText.split(" ");
				String oper = splittedMsg[0];

				if(oper.equalsIgnoreCase("register") && splittedMsg.length == 3) {
					String plateNumber = splittedMsg[1];
					StringBuilder ownerName = new StringBuilder(splittedMsg[2]);
					for (int i = 3; i < splittedMsg.length; i++)
						ownerName.append(" ").append(splittedMsg[i]);
					Plate p = new Plate(plateNumber, ownerName.toString());
					if(plateList.contains(p)) {
						response = "-1 \nALREADY EXISTS";
					}
					else if(p.getPlateNumber().equalsIgnoreCase("INVALID")) {
						response = "-1 \nINVALID PLATE. Format XX-XX-XX. X = [A-Z0-9]";
					}
					else {
						plateList.add(p);
						response = Integer.toString(plateList.size());
					}
				}
				else if(oper.equalsIgnoreCase("lookup") && splittedMsg.length == 2) {
					String plateNumber = splittedMsg[1];
					boolean found = false;
					for(Plate p : plateList) {
						if(p.getPlateNumber().equals(plateNumber)) {
							found = true;
							response = p.getOwnerName();
						}
					}
					if(!found)
						response = "NOT_FOUND";
				}
				else {
					continue;	// Ignore malformed messages
				}
				msgToSend.setData(response.getBytes());
				socket.send(msgToSend);
				System.out.println(response);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Usage: java Server <srvc_port> <mcast_addr> <mcast_port>");
			return;
		}

		try {
			int servicePort = Integer.parseInt(args[0]);
			InetAddress multicastAddress = InetAddress.getByName(args[1]);
			int multicastPort = Integer.parseInt(args[2]);

			TimerTask t1 = new TimerTask() {
				@Override
				public void run() {
					advertiser(servicePort, multicastAddress, multicastPort);
				}
			};
			t1.run();

			TimerTask t2 = new TimerTask() {
				@Override
				public void run() {
					requestsProcessor();
				}
			}; //ATENCAO Não falta um t2.run()?
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}
