package atm;

import java.io.DataOutputStream;

public class PaxosLeader {
	private String state = "idle";
	private Node[] clients;
	private int proposedValue = 0;
	public PaxosLeader(Node[] clients){
		this.clients = clients;
	}
	
	public void runPaxos(int value,Ballot ballot,String paxosId){
		if(state.equals("prepare")){
			Prepare prepareAction = new Prepare();
			for(Node node:clients){
				Client.send(node.port, "prepare," + ballot.toString() + paxosId, node.address, prepareAction);				
			}
			if(prepareAction.getVote() > clients.length / 2){
				state = "propose";
				runPaxos(prepareAction.getVal(), ballot, paxosId);
			} else {
				ballot.ballotNum++;
				runPaxos(value, ballot, paxosId);
			}
		} else if(state.equals("propose")){
			Propose proposeAction = new Propose();
			for(Node node:clients){
				Client.send(node.port, "propose," + ballot.toString() + paxosId, node.address, proposeAction);				
			}
			if(proposeAction.getVote() > clients.length / 2){
				state = "decide";
				runPaxos(value, ballot, paxosId);
			} else {
				state = "prepare";
				ballot.ballotNum++;
				runPaxos(value, ballot, paxosId);
			}
		}
		
		
	}

	private class Prepare implements ClientAction{
		
		private int val;
		private int vote = 0;
		private Ballot maxBallot = null;
		public int getVote(){
			return vote;
		}
		public int getVal(){
			return val;
		}
		@Override
		public void onRecv(String data) {
			// data = ack message
			Message msg = Message.parse(data);
			if(msg.bNum == null || msg.bNum.greaterThan(maxBallot)){
				maxBallot = msg.bNum;
				val = msg.val;
			}
			vote++;
			
		}

		@Override
		public void onTimeOut() {
			
		}
		
	}
	private class Propose implements ClientAction{
		
		private int vote = 0;
		public int getVote(){
			return vote;
		}
		@Override
		public void onRecv(String data) {
			// data = accept message
			vote++;
		}

		@Override
		public void onTimeOut() {
			
		}
		
	}
}
