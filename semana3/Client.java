import java.io.*;
import java.net.*;

public class Client{
	private String host_name;
	private int port_number;
	private String operation;
	private String arguments;
	private BufferedReader buffer;
	private Socket client_socket;
	private DataOutputStream outStream;
	
	public static void main(String args[]){
		if(args.length <4){
			System.out.println("usage: java Client <host_name> <port_number> <oper> <opnd>*");
			return ;
		}
		Client C=new Client(args);
		C.request();
		C.read_answer();
	}
	
	public Client(String args[]){
		host_name=args[0];
		port_number=Integer.parseInt(args[1]);
		operation=args[2];
		arguments=""; 
		for(int i=3; i<args.length; i++){
			arguments+=args[i]+" ";
		}
		arguments=arguments.trim();
		
		try{
			this.client_socket = new Socket("localhost", port_number);
			this.outStream = new DataOutputStream(this.client_socket.getOutputStream());
		}
		catch (IOException I){
			System.out.println("Couldn't create a Socket");
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