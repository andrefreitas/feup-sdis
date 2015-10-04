import java.net.*;
import java.io.*;
import java.util.*; 

public class Server
{
	private final static int PACKETSIZE = 300;
	private DatagramSocket server_socket;
	private int port;
	private ArrayList<Plate> plates;
	
	public static void main(String args[]){
		if(args.length !=1){
			System.out.println("usage: java Server <port_number>");
			return ;
		}
		int port=Integer.parseInt(args[0]);
		Server ListenServer= new Server(port);
		try{
			ListenServer.listen();
		}
		catch (IOException I){
			System.out.println("Couldn't create a DatagramPacket");
		}
	}

	public Server(int port){
		this.port=port;
		try{
			this.server_socket=new DatagramSocket(this.port);
		}
		catch (IOException I){
			System.out.println("Couldn't create a DatagramSocket");
		}
		plates=new ArrayList<Plate>(); 
		
	}

	private void listen() throws IOException{
		
		
		System.out.println("Server listening at port " +  port + " ...");
		while(true)
		{
			byte[] receiveData= new byte[PACKETSIZE];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			server_socket.receive(receivePacket);
			handle_request(receivePacket);
			
		}
	}
	private String parse_owner_name(String[] words){
		String owner_name="";
		for(int i=2;i<words.length; i++){
			owner_name+=words[i];
			owner_name+=" ";
		}
		return owner_name.trim();
	}
	
	private void handle_request(DatagramPacket receivePacket){
		String message= new String(receivePacket.getData());
		message=message.trim();
		String[] words;
		String operation;
		String plate_number;
		String owner_name;
		int port=receivePacket.getPort();
		InetAddress IPAddress=receivePacket.getAddress();
		words=message.split(" ");
		operation=words[0];
		
		System.out.println("*"+message+"*");
		
			
		if(operation.toUpperCase().equals("REGISTER") & words.length>=3){
				plate_number=words[1];
				owner_name=parse_owner_name(words);
				if(words.length>=3  & register_is_valid(plate_number,owner_name) & !plate_number_exists(plate_number)){
					Plate P=new Plate(plate_number,owner_name);
					plates.add(P);
					ack_register_lookup(receivePacket,P.get_plate_number(),P.get_owner_name(),plates.size());
					
				} else{
					send_message("-1\n",port,IPAddress);
					
				}	
			
		}
		else if(operation.toUpperCase().equals("LOOKUP")  & words.length==2){
			plate_number=words[1];
			boolean found=false;
			for (Plate p:plates){
				if(p.get_plate_number().equals(plate_number)){
					ack_register_lookup(receivePacket,p.get_plate_number(),p.get_owner_name(),plates.size());
					found=true;
				}
			}
			if(!found)
				send_message("NOT_FOUND\n",port,IPAddress);
	
		}
		else{
			send_message("-1\n",port,IPAddress);
		}
	}
	
	private void ack_register_lookup(DatagramPacket receivePacket,String plate_number,String owner_name, int result){
		int port=receivePacket.getPort();
		InetAddress IPAddress=receivePacket.getAddress();
		String answer=""+result+"\n";
		answer+=plate_number+ " " +  owner_name+"\n";
		send_message(answer,port, IPAddress);
	}
	
	private boolean plate_number_exists(String plate_number){
		for(Plate p:plates){
			if(p.get_plate_number().equals(plate_number)){
				return true;
			}
		}
		return false;
	}
	private void send_message(String message,int port, InetAddress IPAddress){
		byte[] sendData= new byte[message.length()];
		sendData=message.getBytes();
		DatagramPacket sendPacket= new DatagramPacket(sendData,sendData.length,IPAddress,port);
		try{
			server_socket.send(sendPacket);
		}
		catch (IOException I){
			System.out.println("Couldn't send answer to client");
		}
	}
	
	private boolean register_is_valid(String plate_number,String owner_name){
		return (owner_name.length()<=256 & (plate_number.matches("^([A-Z0-9]{2}-){2}[A-Z0-9]{2}$")));	
	}
	
}