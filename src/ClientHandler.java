import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
public class ClientHandler extends Thread{
	private Socket socket; 
	private int clientNumber;
	private static DataInputStream in = null;
	private static DataOutputStream out = null;
	public ClientHandler(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber; 
		System.out.println("New connection with client#" + clientNumber + " at" + socket);
	}
	public void run(){ // Création de thread qui envoi un message à un client
		try {
			out = new DataOutputStream(socket.getOutputStream()); // création de canal d’envoi 
			out.writeUTF("Hello from server - you are client#" + clientNumber);// envoi de message
			boolean exit = false;
			in = new DataInputStream(socket.getInputStream());
			while(!exit) {
				String userInput = in.readUTF();
				String[] expressions = userInput.split(" ");
				switch(expressions[0]) {
				case "exit":
					exit = true;
					break;
				case "hello":
					System.out.println("hello from client");
					out.writeUTF("hello from server");
					break;
				case "download":
					try {sendFile(expressions[1]);}
					catch(Exception e) {}
					break;
				default:
					out.writeUTF("Enter an appropriate input");
					break;
				}
				
			}
			
		}catch (IOException e) {
			System.out.println("Error handling client# " + clientNumber + ": " + e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("Couldn't close a socket, what's going on?");
			}
			System.out.println("Connection with client# " + clientNumber+ " closed");
		}
	}
	private static void sendFile(String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        
        // send file size
        out.writeLong(file.length());  
        // break file into chunks
        byte[] buffer = new byte[4*1024];
        while ((bytes=fileInputStream.read(buffer))!=-1){
            out.write(buffer,0,bytes);
            out.flush();
        }
        fileInputStream.close();
    }
	private static void receiveFile(String fileName) throws Exception{
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        
        long size = in.readLong();     // read file size
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }
}
