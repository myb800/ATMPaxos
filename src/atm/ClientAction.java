package atm;


import java.io.Serializable;

public interface ClientAction {
	public void onRecv(String data);
	public void onTimeOut();
}
