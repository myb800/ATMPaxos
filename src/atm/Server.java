package atm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Server implements Runnable {
	private ServerSocket serverSocket;
	private ServerAction sact;
	public Server(int port, ServerAction sact) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.sact = sact;
	}
	public void stop(){
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run() {
		
		
		Socket server = null;
		while (true) {
			try {
				server = serverSocket.accept();
				// System.out.println("Just connected to "
				// + server.getRemoteSocketAddress());
				DataInputStream inputStream = new DataInputStream(
						server.getInputStream());
				DataOutputStream outputStream = new DataOutputStream(
						server.getOutputStream());
				String data = inputStream.readUTF();
				// System.out.println("server recv:" + data);
				sact.onRecv(data, outputStream);
				server.close();
			} catch (SocketTimeoutException s) {
				System.out.println("Socket time out");
			} catch (IOException e) {
				if(e instanceof SocketException){
					break;
				}
				e.printStackTrace();
			}
		}
		try {
			if(server != null){
				server.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}