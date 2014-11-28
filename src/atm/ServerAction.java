package atm;


import java.io.DataOutputStream;
import java.net.Socket;

public interface ServerAction {
	public void onRecv(String data,DataOutputStream replyStream);
}
