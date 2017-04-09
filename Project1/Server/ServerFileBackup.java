package Project1.Server;

import Project1.General.Constants;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

/* Generic received message: PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body> */

public class ServerFileBackup {
	
	public static void backup(ServerObject serverObject, String filePath, String fileId, int replicationDegree) throws IOException {
		//Responsavel por 
		// - Abrir o ficheiro
		// - Dividir em chunks
		// - Criar chunk no e Message
		// - Chamar o ChunkBackup
		
		//1 thread por ficheiro
		
		//Abrir o ficheiro
		FileInputStream fis = new FileInputStream(filePath);
		
		int bytesRead = Constants.maxChunkSize;
		System.out.println("\n");
		
		for(int chunkNumber = 0; bytesRead == Constants.maxChunkSize; chunkNumber++){
			
			//Import 64kbits from the file
			byte[] chunkData = new byte[Constants.maxChunkSize];
			bytesRead = fis.read(chunkData, 0, Constants.maxChunkSize);
			System.out.println("Chunk number:" + chunkNumber);
	
			//Send data to Chunk Backup
			ServerChunkBackup.putChunk(serverObject, fileId, chunkData, replicationDegree, chunkNumber);
		}
		
		fis.close();
	}
}
