package atm;


public interface ClientAction {
	public void onRecv(String data);
	public void onNotResponse();
}
