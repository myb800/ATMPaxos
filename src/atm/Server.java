package atm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server implements Runnable{
	   private ServerSocket serverSocket;
	   private ServerAction sact;
	   public Server(int port,ServerAction sact) {
	      try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      this.sact = sact;
	   }

	   public void run() {
	      while(true) {
	         try {
//	            System.out.println("Waiting for client on port " +
//	            serverSocket.getLocalPort() + "...");
	            Socket server = serverSocket.accept();
//	            System.out.println("Just connected to "
//	                  + server.getRemoteSocketAddress());
	            DataInputStream inputStream = new DataInputStream(server.getInputStream());
	            DataOutputStream outputStream = new DataOutputStream(server.getOutputStream());
	            sact.onRecv(inputStream.readUTF(), outputStream);
	            server.close();
	         } catch(SocketTimeoutException s) {
	            System.out.println("Socket time out");
	         } catch(IOException e) {
	            e.printStackTrace();
	         }
	      }
	   }
}