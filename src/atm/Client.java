package atm;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;


public class Client {
	public static void send(int p,String msg,String serverName,ClientAction cact) {
		int port = p;
//		System.out.println("Connecting to " + serverName + " on port " + port);
		Socket client = null;
		try {
			client = new Socket(serverName,port);
			client.setSoTimeout(2000);
//			System.out.println("Just connected to "
//					+ client.getRemoteSocketAddress());
			OutputStream outputStream = client.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			dataOutputStream.writeUTF(msg);
			InputStream inputStream = client.getInputStream();
			DataInputStream datain = new DataInputStream(inputStream);
			if(cact != null){
				cact.onRecv(datain.readUTF());
			}
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(e instanceof SocketTimeoutException ||
					e instanceof EOFException || e instanceof ConnectException || e instanceof SocketException) {
				if(cact != null){
					cact.onNotResponse();
				}
				return;
			}
			e.printStackTrace();
		}
		
	}
}
