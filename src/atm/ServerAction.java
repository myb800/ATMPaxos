package atm;


import java.io.DataOutputStream;

public interface ServerAction {
	public void onRecv(String data,DataOutputStream replyStream);
}
