import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.Scanner;
// Application client
public class Client {
	private static Socket socket;
	private static Scanner scanner = new Scanner(System.in);
	private static DataInputStream in = null;
	private static DataOutputStream out = null;
	public static void main(String[] args) throws Exception {
		// Adresse et port du serveur
		String serverAddress = "127.0.0.1";
		int port = 5000;
		// Création d'une nouvelle connexion aves le serveur
		socket = new Socket(serverAddress, port);
		System.out.format("Serveur lancé sur [%s:%d]", serverAddress, port);
		// Céatien d'un canal entrant pour recevoir les messages envoyés, par le serveur
		in = new DataInputStream(socket.getInputStream());
		// Attente de la réception d'un message envoyé par le, server sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		// fermeture de La connexion avec le serveur
		out = new DataOutputStream(socket.getOutputStream());
		//Scanner scanner = new Scanner(System.in);
		boolean exit = false;
		while(!exit) {
			String userInput = scanner.nextLine();
			String[] expressions = userInput.split(" ");
			switch (expressions[0]) {
			case "exit":
				out.writeUTF(userInput);
				exit = true;
				break;
			case "download":
				out.writeUTF(userInput);
				receiveFile(expressions[1]);
				System.out.println(in.readUTF());
				break;
			case "upload":
				out.writeUTF(userInput);
				sendFile(expressions[1]);
				System.out.println(in.readUTF());
				break;
			default:
				out.writeUTF(userInput);
				System.out.println(in.readUTF());
				break;
				
			}

		}
		scanner.close();
		socket.close();	
	}
	private static void sendFile(String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        //file.exist();
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
