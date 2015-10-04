
public class Plate {
	private String plate_number;
	private String owner_name;
	
	public Plate(String plate_number,String owner_name){
		this.plate_number= new String(plate_number);
		this.owner_name=new String(owner_name);
	}
	
	public void set_plate_number(String plate_number){
		this.plate_number=plate_number;
	}
	
	public void set_owner_name(String owner_name){
		this.owner_name=owner_name;
	}
	
	public String get_plate_number(){
		return this.plate_number;
	}
	
	public String get_owner_name(){
		return this.owner_name;
	}
}
