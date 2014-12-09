package atm;

public class initCheck implements ClientAction{
	private int replyNum = 0;
	public int getreplyNum(){
		return replyNum;
	}
	@Override
	public synchronized void onRecv(String data) {
		replyNum++;
	}
	@Override
	public void onNotResponse() {
		
	}
}
