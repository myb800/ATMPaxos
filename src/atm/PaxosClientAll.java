package atm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class PaxosClientAll implements ServerAction{
	private HashMap<String,PaxosClient> sessions = null;
	private static Ballot acceptBallot = null;
	private static Ballot ballot = null;
	private static int val;
	private Node[] clients;
	public PaxosClientAll(Node[] clients) {
		sessions = new HashMap<String,PaxosClient>();
		this.clients = clients;
	}
	@Override
	public synchronized void onRecv(String data, DataOutputStream replyStream) {
		Message msg = Message.parse(data);
		try {
			if(msg.type.equals("prepare") && !sessions.containsKey(msg.id)){
				if(ballot != null && ballot.greaterThan(msg.bNum)){
					return;
				}
				ballot = msg.bNum;
				PaxosClient newSession = new PaxosClient(msg.id);
				sessions.put(msg.id, newSession);
				newSession.setStatus("wait-proposal");
				replyStream.writeUTF("ack," + ballot.toString() + "," 
								   + acceptBallot.toString() + "," + val + msg.id);
			} else if(msg.type.equals("prepare") && sessions.containsKey(msg.id)){
				ballot = msg.bNum;
				replyStream.writeUTF("ack," + ballot.toString() + "," 
						   + acceptBallot.toString() + "," + val + msg.id);
			} else if(msg.type.equals("accept")){
				if(ballot != null && ballot.greaterThan(msg.bNum)){
					return;
				}
				PaxosClient curr = sessions.get(msg.id);
				curr.setVote(curr.getVote() + 1);// TODO need to get dup source if there is recovery
				if(curr.getVote() > clients.length / 2){
					for(Node n : clients){
						Client.send(n.port, "decide," + val, n.address, null);
					}
				}
				acceptBallot = msg.accp;
				val = msg.val;
				if(curr.isHasAccepted() == false){
					for(Node n : clients){
						Client.send(n.port, "accept," + ballot.toString() + "," + val, n.address, null);
					}
				}
				curr.setHasAccepted(true);
			} else if(msg.type.equals("decide")){
				sessions.remove(msg.id);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

}
