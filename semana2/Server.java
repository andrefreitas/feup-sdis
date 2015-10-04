// nc -u 127.0.0.1 1345
// iperf -c 225.4.5.6 -u -T 1 -i 1 -p 6701
import java.net.*;
import java.nio.channels.MulticastChannel;
import java.io.*;
import java.util.*; 

public class Server
{
	private final static int PACKETSIZE = 300;
	private DatagramSocket server_socket; // UDP socket
	private MulticastSocket server_mcast_socket; // UDP multicast socket
	private int port; // UDP port
	private int mcast_port; // UDP multicast port
	private String mcast_group; // Multicast IP (aka group)
	private ArrayList<Plate> plates;
	private static Server ListenServer;
	private final int TTL=1;
	
	public static void main(String args[]){
		if(args.length !=3){
			System.out.println("usage: java Server <srvc_port> <mcast_addr> <mcast_port> ");
			return ;
		}
		
		// Params
		int port=Integer.parseInt(args[0]);
		String mcast_group=args[1];
		int mcast_port=Integer.parseInt(args[2]);
		
		// Create Server
		ListenServer= new Server(port,mcast_group,mcast_port);
		
		// Listen
		Thread t1=new Thread(){
			@Override
			public void run() {
				try{
					ListenServer.listen();
					
				}
				catch (IOException I){
					System.out.println("Couldn't listen: " +I.getMessage());
				}
			}
		};
		
		// Listen Multicast
		Thread t2=new Thread(){
			@Override
			public void run() {
				try{
					ListenServer.listen_multicast();
					
				}
				catch (IOException I){
					System.out.println("Couldn't listen multicast: " +I.getMessage());
				}
			}
		};
		
		t1.start();
		t2.start();
	}

	public Server(int port,String group,int mcast_port ){
		this.port=port;
		this.mcast_group=group;
		this.mcast_port=mcast_port;
		try{
			this.server_socket=new DatagramSocket(this.port);
			this.server_mcast_socket = new MulticastSocket(this.mcast_port);
			server_mcast_socket.joinGroup(InetAddress.getByName(this.mcast_group));
		}
		catch (IOException I){
			System.out.println("Couldn't create a DatagramSocket or MulticastSocket");
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
			System.out.print("[UNICAST] ");
			handle_request(receivePacket);
			
		}
	}
	
	private void listen_multicast() throws IOException{
		System.out.println("Multicast server listening at port " + mcast_port + " ...");
		while (true) {
			byte[] receiveData = new byte[PACKETSIZE];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			server_mcast_socket.receive(receivePacket);
			System.out.print("[MULTICAST] ");
			handle_request_multicast(receivePacket);
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
	
	private void handle_request_multicast(DatagramPacket receivePacket){
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
					ack_register_lookup_multicast(receivePacket,P.get_plate_number(),P.get_owner_name(),plates.size());
					
				} else{
					send_message_multicast("-1\n",port,IPAddress);
					
				}	
			
		}
		else if(operation.toUpperCase().equals("LOOKUP")  & words.length==2){
			plate_number=words[1];
			boolean found=false;
			for (Plate p:plates){
				if(p.get_plate_number().equals(plate_number)){
					ack_register_lookup_multicast(receivePacket,p.get_plate_number(),p.get_owner_name(),plates.size());
					found=true;
				}
			}
			if(!found)
				send_message_multicast("NOT_FOUND\n",port,IPAddress);
	
		}
		else{
			send_message_multicast("-1\n",port,IPAddress);
		}
	}
	
	private void ack_register_lookup(DatagramPacket receivePacket,String plate_number,String owner_name, int result){
		int port=receivePacket.getPort();
		InetAddress IPAddress=receivePacket.getAddress();
		String answer=""+result+"\n";
		answer+=plate_number+ " " +  owner_name+"\n";
		send_message(answer,port, IPAddress);
	}
	
	private void ack_register_lookup_multicast(DatagramPacket receivePacket,String plate_number,String owner_name, int result){
		int port=receivePacket.getPort();
		InetAddress IPAddress=receivePacket.getAddress();
		String answer=""+result+"\n";
		answer+=plate_number+ " " +  owner_name+"\n";
		send_message_multicast(answer,port, IPAddress);
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
	
	private void send_message_multicast(String message,int port, InetAddress IPAddress){
		byte[] sendData= new byte[message.length()];
		sendData=message.getBytes();
		DatagramPacket sendPacket= new DatagramPacket(sendData,sendData.length,IPAddress,port);
		try{
			server_mcast_socket.setTimeToLive(TTL);
			server_mcast_socket.send(sendPacket);
		}
		catch (IOException I){
			System.out.println("Couldn't send answer to client");
		}
	}
	
	private boolean register_is_valid(String plate_number,String owner_name){
		return (owner_name.length()<=256 & (plate_number.matches("^([A-Z0-9]{2}-){2}[A-Z0-9]{2}$")));	
	}
	
	public void close() {
		try {
			server_mcast_socket.leaveGroup(InetAddress.getByName(this.mcast_group));
		} catch ( IOException e) {
			System.out.println("Can't leave multicast group");
		}
		server_mcast_socket.close();
		server_socket.close();
		
	}
	
}