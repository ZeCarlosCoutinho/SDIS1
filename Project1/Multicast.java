import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class Multicast {

    public static final int UDP_DATAGRAM_MAX_LENGTH = 65536; //2^16
    private MulticastSocket socket;
    private String multicastAddress;
    private int multicastPort;

    public Multicast() {
        try {
            socket = new MulticastSocket();
            socket.setTimeToLive(1); //To avoid network congestion
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Multicast(String groupAddress, int groupPort, boolean timeout) {
        try {
            socket = new MulticastSocket(groupPort);
            socket.joinGroup(InetAddress.getByName(groupAddress));
            if(timeout)
                socket.setSoTimeout(400);
            socket.setTimeToLive(1);    //To avoid network congestion
            multicastAddress = groupAddress;
            multicastPort = groupPort;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] message) {
        try {
            if (message.length > UDP_DATAGRAM_MAX_LENGTH)
                throw new IllegalArgumentException("Message too big.");
            DatagramPacket msgToDiffuse = new DatagramPacket(message, message.length, InetAddress.getByName(multicastAddress), multicastPort);
            msgToDiffuse.setData(message);
            socket.send(msgToDiffuse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] receive() {
        byte[] data = new byte[UDP_DATAGRAM_MAX_LENGTH];
        try {
            DatagramPacket mcastReceive = new DatagramPacket(data, data.length);
            socket.receive(mcastReceive);
            data = Arrays.copyOfRange(mcastReceive.getData(), 0, mcastReceive.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void close() {
        socket.close();
    }
}
