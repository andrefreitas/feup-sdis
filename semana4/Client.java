import java.io.*;
import java.net.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client{
	private String host_name;
	private int port_number;
	private String operation;
	private String arguments;
	private BufferedReader buffer;
	private Socket client_socket;
	private DataOutputStream outStream;
	
	public static void main(String args[]){
		
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1");
            RemoteServer stub = (RemoteServer) registry.lookup("Server");
            
            if (args[0].equalsIgnoreCase("register")) {
            	int response = stub.register(args[1], args[2]);
                System.out.println("response: " + response);
            }
            else {
            	String response = stub.lookup(args[1]);
            	System.out.println("response: " + response);
            }
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
	}

	
	private void request(){
		send_message(operation+" "+arguments+"\n");
		System.out.print(operation+" "+arguments+":");
	}
	
	private void read_answer(){
		String result;
		try{
			buffer = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
			result=buffer.readLine();
			if(result.equals("-1"))
				System.out.println("ERROR");
			else 
				System.out.print(result);
	
		} catch(IOException i){
			System.out.println("Couldn't read answer");
		}
		
		
	}
	
	private void send_message(String message){
		try {
			outStream.writeBytes(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}