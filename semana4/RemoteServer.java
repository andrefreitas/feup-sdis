
import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RemoteServer extends Remote {
	boolean plate_number_exists(String plate_number) throws RemoteException;
	int register(String plate_number,String owner_name) throws RemoteException;
	String lookup(String plate_number) throws RemoteException;
	boolean register_is_valid(String plate_number,String owner_name) throws RemoteException;
}
