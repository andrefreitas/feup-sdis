/*
 * C:\Users\André\Dropbox\mieic\sdis\semana4\src>java -classpath C:\Users\André\Dropbox\mieic\sdis\semana4\src  -Djava.rmi.
server.codebase=file:C:\Users\André\Dropbox\mieic\sdis\semana4\src/ Server
 */

import java.net.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.util.*; 




public class Server implements RemoteServer
{
	private ArrayList<Plate> plates;

	public static void main(String args[]){
		try {
            Server obj = new Server();
            RemoteServer stub = (RemoteServer) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Server", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
	}

	public Server() {
		plates=new ArrayList<Plate>(); 
		
	}

	
	public boolean plate_number_exists(String plate_number){
		for(Plate p:plates){
			if(p.get_plate_number().equals(plate_number)){
				return true;
			}
		}
		return false;
	}
	
	public int register(String plate_number,String owner_name){
		if(register_is_valid(plate_number,owner_name) & !plate_number_exists(plate_number) ){
			Plate P=new Plate(plate_number,owner_name);
			plates.add(P);
			return plates.size();
		}
		return -1;
	}
	
	public String lookup(String plate_number){
		for(Plate p:plates){
			if(p.get_plate_number().equals(plate_number)){
				return p.get_owner_name();
			}
		}
		return null;
	}
	
	
	public boolean register_is_valid(String plate_number,String owner_name){
		return (owner_name.length()<=256 & (plate_number.matches("^([A-Z0-9]{2}-){2}[A-Z0-9]{2}$")));	
	}
	
}