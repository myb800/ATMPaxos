package atm;

public class Ballot {
	int ballotNum;
	int processId;
	boolean greaterThan(Ballot ballot){
		if(ballotNum > ballot.ballotNum){
			return true;
		} else if(ballotNum == ballot.ballotNum && processId > ballot.processId){
			return true;
		}
		return false;
	}
	public String toString(){
		return ballotNum + "," + processId;
	}
	public static Ballot parse(String ballotNum,String processId){
		return new Ballot(Integer.parseInt(ballotNum), Integer.parseInt(processId));
	}
	Ballot(int ballotNum,int processId){
		this.ballotNum = ballotNum;
		this.processId = processId;
	}
}
