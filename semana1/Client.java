import java.io.*;
import java.net.*;

public class Client{
	private final static int PACKETSIZE = 300;
	private String host_name;
	private int port_number;
	private String operation;
	private String arguments;
	private DatagramSocket client_socket;
	
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
			this.client_socket=new DatagramSocket();
		}
		catch (IOException I){
			System.out.println("Couldn't create a DatagramSocket");
		}
	
	}
	
	private void request(){
		try{
			InetAddress IPAddress = InetAddress.getByName(host_name);
			send_message(operation+" "+arguments+"\n",port_number, IPAddress);
		}
		catch(UnknownHostException H){
			System.out.println("Error: Unknow host");
		}
		
		System.out.print(operation+" "+arguments+":");
	}
	
	private void read_answer(){
		byte[] receiveData= new byte[PACKETSIZE];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		try{
			client_socket.receive(receivePacket);
		} catch(IOException i){
			System.out.println("Couldn't read answer");
		}
		String message= new String(receivePacket.getData());
		String result=message.split("\n")[0];
		if(result.equals("-1"))
			System.out.println("ERROR");
		else 
			System.out.print(result);
	}
	
	private void send_message(String message,int port, InetAddress IPAddress){
		byte[] sendData= new byte[message.length()];
		sendData=message.getBytes();
		DatagramPacket sendPacket= new DatagramPacket(sendData,sendData.length,IPAddress,port);
		try{
			client_socket.send(sendPacket);
		}
		catch (IOException I){
			System.out.println("Couldn't send answer to client");
		}
	}
}