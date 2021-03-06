package Project1.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRMI extends Remote {

    void backup(String filePath, int replicationDegree) throws RemoteException;

    void restore(String filePath) throws RemoteException;

    void delete(String filePath) throws RemoteException;

    void manageStorage(long newStorageSpace) throws RemoteException;

    String state() throws RemoteException;
}
