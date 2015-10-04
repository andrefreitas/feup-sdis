import java.net.*;
import java.io.*;
import java.util.*; 

public class Server
{
	private ServerSocket server_socket;
	private Socket connection;
	private int port;
	private ArrayList<Plate> plates;
    private BufferedReader stream_buffer;
    private DataOutputStream stream_buffer_out;
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
			System.out.println("Listen failed!");
		}
	}

	public Server(int port){
		this.port=port;
		try{
			this.server_socket=new ServerSocket(this.port);
			
		}
		catch (IOException I){
			System.out.println("Couldn't create a Server Socket");
		}
		plates=new ArrayList<Plate>(); 
		
	}

	private void listen() throws IOException{
		
		
		System.out.println("[TCP] Server listening at port " +  port + " ...");
		while(true)
		{
			String msg;
			this.connection = this.server_socket.accept();
			this.stream_buffer= new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
			this.stream_buffer_out = new DataOutputStream(this.connection.getOutputStream());
			msg=stream_buffer.readLine();
			handle_request(msg);
			this.connection.close();
			
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
	
	private void handle_request(String message){
		message=message.trim();
		String[] words;
		String operation;
		String plate_number;
		String owner_name;
		words=message.split(" ");
		operation=words[0];
		
		System.out.println("*"+message+"*");
		
			
		if(operation.toUpperCase().equals("REGISTER") & words.length>=3){
				plate_number=words[1];
				owner_name=parse_owner_name(words);
				if(words.length>=3  & register_is_valid(plate_number,owner_name) & !plate_number_exists(plate_number)){
					Plate P=new Plate(plate_number,owner_name);
					plates.add(P);
					ack_register_lookup(P.get_plate_number(),P.get_owner_name(),plates.size());
					System.out.println("Registered: " + plates.size());
				} else{
					send_message("-1\n");
					
				}	
			
		}
		else if(operation.toUpperCase().equals("LOOKUP")  & words.length==2){
			plate_number=words[1];
			boolean found=false;
			for (Plate p:plates){
				if(p.get_plate_number().equals(plate_number)){
					ack_register_lookup(p.get_plate_number(),p.get_owner_name(),plates.size());
					found=true;
				}
			}
			if(!found)
				send_message("NOT_FOUND\n");
	
		}
		else{
			send_message("-1\n");
		}
	}
	
	private void ack_register_lookup(String plate_number,String owner_name, int result){
		String answer=""+result+"\n";
		answer+=plate_number+ " " +  owner_name+"\n";
		send_message(answer);
	}
	
	private boolean plate_number_exists(String plate_number){
		for(Plate p:plates){
			if(p.get_plate_number().equals(plate_number)){
				return true;
			}
		}
		return false;
	}
	private void send_message(String message){
		try {
			stream_buffer_out.writeBytes(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean register_is_valid(String plate_number,String owner_name){
		return (owner_name.length()<=256 & (plate_number.matches("^([A-Z0-9]{2}-){2}[A-Z0-9]{2}$")));	
	}
	
}