package atm;


public class PaxosClient {
	public boolean isHasAccepted() {
		return hasAccepted;
	}


	public void setHasAccepted(boolean hasAccepted) {
		this.hasAccepted = hasAccepted;
	}


	public int getVote() {
		return vote;
	}


	public void setVote(int vote) {
		this.vote = vote;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getPaxosId() {
		return paxosId;
	}


	private String status;
	private String paxosId;
	private int vote = 0;
	private boolean hasAccepted = false;
	
	public PaxosClient(String id) {
		this.paxosId = id;
		status = "idle";
	}
}
