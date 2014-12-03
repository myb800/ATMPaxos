package atm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class PaxosClientAll implements ServerAction{
	
	private HashMap<String,PaxosClient> sessions = null;
	private HashMap<String,Ballot> acceptBallot = null;
	private HashMap<String,Ballot> ballot;
	private HashMap<String,String> val;
	private Node[] clients;
	private PaxosOnDecide onDecide = null;
	
	public PaxosClientAll(Node[] clients) {
		sessions = new HashMap<String,PaxosClient>();
		ballot = new HashMap<String,Ballot>();
		acceptBallot = new HashMap<String,Ballot>();
		val = new HashMap<String,String>();
		this.clients = clients;
	}
	@Override
	public void onRecv(String data, DataOutputStream replyStream) {
		Log.log("paxos client receive:" + data);
		
		Message msg = Message.parse(data);
		try {
			if(msg.type.equals("prepare") && !sessions.containsKey(msg.id)){
				if(!val.containsKey(msg.varName)){
					val.put(msg.varName, "");
					acceptBallot.put(msg.varName, new Ballot(0,0));
					ballot.put(msg.varName, new Ballot(0,0));
				}
				if(ballot.get(msg.varName).greaterThan(msg.bNum)){
					return;
				}
				ballot.put(msg.varName, msg.bNum);
				PaxosClient newSession = new PaxosClient(msg.id,msg.varName);
				sessions.put(msg.id, newSession);
				newSession.setStatus("wait-proposal");
				Log.log("paxos client send back:" + "ack," + ballot.get(msg.varName).toString() + "," 
						   + acceptBallot.get(msg.varName).toString() + "," + val.get(msg.varName) + "," + msg.id + "," + msg.varName);
				replyStream.writeUTF("ack," + ballot.get(msg.varName).toString() + "," 
						   + acceptBallot.get(msg.varName).toString() + "," + val.get(msg.varName) + "," + msg.id + "," + msg.varName);
			} else if(msg.type.equals("prepare") && sessions.containsKey(msg.id)){
				if(ballot.get(msg.varName).greaterThan(msg.bNum)){
					return;
				}
				ballot.put(msg.varName, msg.bNum);
				Log.log("ack," + ballot.get(msg.varName).toString() + "," 
						   + acceptBallot.get(msg.varName).toString() + "," + val.get(msg.varName) + "," + msg.id + "," + msg.varName);
				replyStream.writeUTF("ack," + ballot.get(msg.varName).toString() + "," 
						   + acceptBallot.get(msg.varName).toString() + "," + val.get(msg.varName) + "," + msg.id + "," + msg.varName);
			} else if(msg.type.equals("accept")){
				PaxosClient curr;
				String varName = msg.varName;
				if(val.containsKey(msg.varName) == false){
					addRecord(varName, msg.val, msg.accp, msg.bNum);
				}
				if(sessions.containsKey(msg.id) == false){
					curr = new PaxosClient(msg.id, msg.varName);
					curr.setStatus("accepting");
					sessions.put(msg.id, curr);
				} else {
					curr = sessions.get(msg.id);
					if(ballot.get(varName).greaterThan(msg.bNum)){
						return;
					}
				}
				
				
				Log.log("paxos client send back:" + "accept," + ballot.get(varName).toString() + "," + val.get(varName) + "," + msg.id + "," + varName);
				replyStream.writeUTF("accept," + ballot.get(varName).toString() + "," + val.get(varName) + "," + msg.id + "," + varName);
				if(curr.isHasAccepted() == true){
					// TODO need to get dup source if there is recovery
					curr.setVote(curr.getVote() + 1);
				}
				if(curr.getVote() > clients.length / 2){
					Log.log("paxos client broadcast:" + "decide," + val.get(varName) + "," + msg.id + "," + varName);
					curr.setStatus("decide");
					if(onDecide != null){
						onDecide.onDecide(varName, msg.id, val.get(varName));
					}
					for(Node n : clients){
						Client.send(n.port, "decide," + val.get(varName) + "," + msg.id + "," + varName, n.address, null);
					}
				}
				if(curr.isHasAccepted() == false){
					acceptBallot.put(varName,msg.accp);
					val.put(varName,msg.val);
					Log.log("paxos broadcast:" + "accept," + ballot.get(varName).toString() + "," + val.get(varName) + "," + msg.id + "," + varName);
					for(Node n : clients){
						Client.send(n.port, "accept," + ballot.get(varName).toString() + "," + val.get(varName) + "," + msg.id + "," + varName, n.address, null);
					}
				}
				curr.setHasAccepted(true);
			} else if(msg.type.equals("decide")){
				PaxosClient curr = sessions.get(msg.id);
				if(!curr.getStatus().equals("decide")){
					curr.setStatus("decide");
					String varName = curr.getVarName();
					if(onDecide != null){
						onDecide.onDecide(varName, msg.id, val.get(varName));
					}
					Log.log("paxos client broadcast:" + "decide," + val.get(varName) + "," + msg.id + "," + varName);
					for(Node n : clients){
						Client.send(n.port, "decide," + val.get(varName) + "," + msg.id + "," + varName, n.address, null);
					}
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void setOnDecide(PaxosOnDecide onDecide) {
		this.onDecide = onDecide;
	}
	
	public void addRecord(String varName,String valu,Ballot acceptBallot,Ballot ballot){
		this.val.put(varName, valu);
		this.acceptBallot.put(varName, acceptBallot);
		this.ballot.put(varName, ballot);
	}

}
