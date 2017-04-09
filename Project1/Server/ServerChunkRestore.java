package Project1.Server;

import Project1.Database.ServerDatabase;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

/* Generic received message: GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF> */
/* Generic message to send: CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body> */

public class ServerChunkRestore {
    private static final int maxWaitTime = 400; /* milliseconds */
    public static int chunkSize = 64000;

    public static byte[] requestChunk(ServerObject serverObject, String fileId, int chunkNo) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        Multicast mDataRecoveryCh = serverObject.getDataRecoveryChannel();

        StringBuilder st = new StringBuilder("GETCHUNK ");
        st.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");
        mControlCh.send(st.toString().getBytes());

        byte[] data = mDataRecoveryCh.receive();
        Message m = new Message(data);
        return m.getBody();
    }

    public static void chunkProvider(ServerObject serverObject) {
        String protocolVersion = serverObject.getProtocolVersion();
        int serverId = serverObject.getServerId();
        Multicast mControlCh = serverObject.getControlChannel();
        Multicast mDataRecoveryCh = serverObject.getDataRecoveryChannel();
        ServerDatabase db = serverObject.getDb();

        byte[] data = new byte[chunkSize];
        while (true) {
            try {
                byte[] request1 = mControlCh.receive(); //Restore request
                Message m = new Message(request1);

                //Notification that other server has attended the request first
                byte[] request2 = mDataRecoveryCh.receive(new Random().nextInt() % maxWaitTime);

                if(request2 != null) {
                    Message m2 = new Message(request2);
                    if (m2.getMessageType().equalsIgnoreCase("CHUNK")) //If the other server attended the same request
                        continue;
                }

                if (m.getMessageType().equalsIgnoreCase("GETCHUNK") && m.getVersion().equalsIgnoreCase(protocolVersion)) { //This server attends the request
                    String fileId = m.getFileId();
                    int chunkNo = Integer.parseInt(m.getChunkNo());

                    if(db.getStoredFileData(fileId).getFileChunkData(chunkNo) != null) {    // if this server stored the chunk
                        StringBuilder path = new StringBuilder(serverId);
                        path.append("/").append(fileId).append("/").append(chunkNo);
                        FileInputStream file = new FileInputStream(path.toString());
                        file.read(data);
                        file.close();

                        StringBuilder headerToSend = new StringBuilder("CHUNK ");
                        headerToSend.append(protocolVersion).append(" ").append(serverId).append(" ").append(fileId).append(" ").append(chunkNo).append("\r\n\r\n");

                        ByteArrayOutputStream msg = new ByteArrayOutputStream();
                        msg.write(headerToSend.toString().getBytes());
                        msg.write(data);
                        mDataRecoveryCh.send(msg.toByteArray());
                        msg.close();
                    }
                }
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
