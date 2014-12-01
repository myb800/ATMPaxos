package atm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class PaxosClientAll implements ServerAction{
	private HashMap<String,PaxosClient> sessions = null;
	private static Ballot acceptBallot = null;
	private static Ballot ballot;
	private static int val = -1;
	private Node[] clients;
	public PaxosClientAll(Node[] clients) {
		sessions = new HashMap<String,PaxosClient>();
		ballot = new Ballot(0, 0);
		acceptBallot = new Ballot(0, 0);
		this.clients = clients;
	}
	@Override
	public synchronized void onRecv(String data, DataOutputStream replyStream) {
		Log.log("paxos client receive:" + data);
		Message msg = Message.parse(data);
		try {
			if(msg.type.equals("prepare") && !sessions.containsKey(msg.id)){
				if(ballot.greaterThan(msg.bNum)){
					return;
				}
				ballot = msg.bNum;
				PaxosClient newSession = new PaxosClient(msg.id);
				sessions.put(msg.id, newSession);
				newSession.setStatus("wait-proposal");
				Log.log("paxos client send back:" + "ack," + ballot.toString() + "," 
						   + acceptBallot.toString() + "," + val + "," + msg.id);
				replyStream.writeUTF("ack," + ballot.toString() + "," 
								   + acceptBallot.toString() + "," + val + "," + msg.id);
			} else if(msg.type.equals("prepare") && sessions.containsKey(msg.id)){
				if(ballot.greaterThan(msg.bNum)){
					return;
				}
				ballot = msg.bNum;
				Log.log("ack," + ballot.toString() + "," 
						   + acceptBallot.toString() + "," + val + "," + msg.id);
				replyStream.writeUTF("ack," + ballot.toString() + "," 
						   + acceptBallot.toString() + "," + val + "," + msg.id);
			} else if(msg.type.equals("accept")){
				if(ballot.greaterThan(msg.bNum)){
					return;
				}
				Log.log("paxos client send back:" + "accept," + ballot.toString() + "," + val + "," + msg.id);
				replyStream.writeUTF("accept," + ballot.toString() + "," + val + "," + msg.id);
				PaxosClient curr = sessions.get(msg.id);
				if(curr.isHasAccepted() == true){
					// TODO need to get dup source if there is recovery
					curr.setVote(curr.getVote() + 1);
				}
				if(curr.getVote() > clients.length / 2){
					Log.log("paxos client broadcast:" + "decide," + val + "," + msg.id);
					for(Node n : clients){
						Client.send(n.port, "decide," + val + "," + msg.id, n.address, null);
					}
				}
				if(curr.isHasAccepted() == false){
					acceptBallot = msg.accp;
					val = msg.val;
					Log.log("paxos broadcast:" + "accept," + ballot.toString() + "," + val + "," + msg.id);
					for(Node n : clients){
						Client.send(n.port, "accept," + ballot.toString() + "," + val + "," + msg.id, n.address, null);
					}
				}
				curr.setHasAccepted(true);
			} else if(msg.type.equals("decide")){
				if(sessions.containsKey(msg.id)){
					Log.log("paxos client broadcast:" + "decide," + val + "," + msg.id);
					for(Node n : clients){
						Client.send(n.port, "decide," + val + "," + msg.id, n.address, null);
					}
					sessions.remove(msg.id);
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

}
