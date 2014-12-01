package atm;


public class Message {
	String type = null;
	Ballot bNum = null;
	Ballot accp = null;
	Integer val = null;
	String id = null;
	String varName = null;
	public String toString(){
		StringBuffer sbf = new StringBuffer();
		sbf.append(type);
		if(bNum != null){
			sbf.append(",");
			sbf.append(bNum.toString());
		}
		if(accp != null){
			sbf.append(",");
			sbf.append(accp.toString());
		}
		if(val != null){
			sbf.append("," + Integer.toString(val));
		}
		sbf.append(id);
		return sbf.toString();
	}
	/*
	 * messages:
	 * prepare,bal
	 * ack,bal,acceptbal,acceptval
	 * accept,bal,val
	 * decide,val
	 */
	public static Message parse(String str){
		String[] tokens = str.split(",");
		Message msg = new Message();
		msg.type = tokens[0];
		if(msg.type.equals("prepare")){
			msg.bNum = Ballot.parse(tokens[1], tokens[2]);
			msg.id = tokens[3];
			msg.varName = tokens[4];
		} else if(msg.type.equals("ack")){
			msg.bNum = Ballot.parse(tokens[1], tokens[2]);
			msg.accp = Ballot.parse(tokens[3], tokens[4]);
			msg.val  = Integer.parseInt(tokens[5]);
			msg.id   = tokens[6];
		} else if(msg.type.equals("accept")){
			msg.bNum = Ballot.parse(tokens[1], tokens[2]);
			msg.val  = Integer.parseInt(tokens[3]);
			msg.id   = tokens[4];
		} else if(msg.type.equals("decide")){
			msg.val = Integer.parseInt(tokens[1]);
			msg.id  = tokens[2];
		}
		return msg;
	}
}
