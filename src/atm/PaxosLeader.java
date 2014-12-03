package atm;

public class PaxosLeader {
	
	private String state = "prepare";
	private Node[] clients;
	private String decidedVal;
	private boolean isMyProposalPermitted = true;
	private String paxosId;
	private String varName;
	private PaxosOnDecide paxosOnDecide = null;
	public PaxosLeader(Node[] clients,String paxosId,String varName){
		this.clients = clients;
		this.paxosId = paxosId;
		this.varName = varName;
	}
	
	public void runPaxos(String value,Ballot ballot){
		if(state.equals("prepare")){
			Prepare prepareAction = new Prepare();
			Log.log("paxos leader broadcast:" + "prepare," + ballot.toString() + "," + paxosId + "," + varName);
			for(Node node:clients){
				Client.send(node.port, "prepare," + ballot.toString() + "," + paxosId + "," + varName, node.address, prepareAction);				
			}
			if(prepareAction.getVote() > clients.length / 2){
				state = "propose";
				if(prepareAction.getVal().equals("") == false){ //not yet decided
					runPaxos(prepareAction.getVal(), ballot);
				} else {
					isMyProposalPermitted = true;
					runPaxos(value, ballot);
				}
			} else {
				ballot.ballotNum++;
				try {
					Thread.sleep((long) (1 + 2000*Math.random()));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				runPaxos(value, ballot);
			}
		} else if(state.equals("propose")){
			Propose proposeAction = new Propose();
			Log.log("paxos leader broadcast:" + "accept," + ballot.toString() + "," + value + "," + paxosId + "," + varName);
			for(Node node:clients){
				Client.send(node.port, "accept," + ballot.toString() + "," + value + "," + paxosId + "," + varName, node.address, proposeAction);				
			}
			if(proposeAction.getVote() > clients.length / 2){
				state = "decide";
				if(paxosOnDecide != null){
					paxosOnDecide.onDecide(varName, paxosId, value);
				}
				runPaxos(value, ballot);
			} else {
				state = "prepare";
				ballot.ballotNum++;
				runPaxos(value, ballot);
			}
		} else if(state.equals("decide")){
			decidedVal = value;
			Log.log("paxos leader broadcast:" + "decide," + value + "," + paxosId + "," + varName);
			for(Node node:clients){
				Client.send(node.port, "decide," + value + "," + paxosId + "," + varName, node.address, null);				
			}
		}
		
		
	}
	public void setPaxosOnDecide(PaxosOnDecide paxosOnDecide) {
		this.paxosOnDecide = paxosOnDecide;
	}
	private class Prepare implements ClientAction{
		
		private String val;
		private int vote = 0;
		private Ballot maxBallot = null;
		public int getVote(){
			return vote;
		}
		public String getVal(){
			return val;
		}
		@Override
		public void onRecv(String data) {
			// data = ack message
			Message msg = Message.parse(data);
			Log.log("paxos leader receive:" + data);
			if(maxBallot == null || msg.bNum.greaterThan(maxBallot)){
				maxBallot = msg.bNum;
				val = msg.val;
				isMyProposalPermitted = false;
			}
			vote++;
			
		}

		@Override
		public void onNotResponse() {
			
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
			Log.log("paxos leader receive:" + data);
			vote++;
		}

		@Override
		public void onNotResponse() {
			
		}
		
	}
	
	public String getDecidedVal() {
		return decidedVal;
	}
	
	public String getPaxosId() {
		return paxosId;
	}
	public boolean isMyProposedPermitted() {
		return this.isMyProposalPermitted;
	}
}
