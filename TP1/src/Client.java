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
		int port = 0;
		while(!isValidPort(port)) {
			System.out.println("Veuillez entrer un port: ");
			String portInput = scanner.nextLine();
			try {
				port = Integer.parseInt(portInput);
				if(!isValidPort(port)) {
					System.out.println("Le port doit être entre 5000 et 5050");
				}
			}
			catch(Exception e) {
				System.out.print("Le port entré n'est pas un nombre");
			}
		}
		
		String serverAddress = "";
		
		while(!isValidAddress(serverAddress)) {
			System.out.println("Veuillez entrer une adresse: ");
			serverAddress = scanner.nextLine();
			if(!isValidAddress(serverAddress)) {
				System.out.println("L'adresse doit être du format: a.b.c.d");
			}
		}
		
		socket = new Socket(serverAddress, port);
		System.out.format("Serveur lancé sur [%s:%d]", serverAddress, port);
		in = new DataInputStream(socket.getInputStream());
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);		
		out = new DataOutputStream(socket.getOutputStream());
		boolean exit = false;
		while(!exit) {
			String userInput = scanner.nextLine();
			String[] expressions = userInput.split(" ");
			switch (expressions[0]) {
			case "exit":
				out.writeUTF(userInput);
				System.out.println(in.readUTF());
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
        FileInputStream fileInputStream = new FileInputStream(file);
        
        out.writeLong(file.length());  
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
        
        long size = in.readLong();     
        byte[] buffer = new byte[4*1024];
        while (size > 0 && (bytes = in.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            size -= bytes;      
        }
        fileOutputStream.close();
    }
	
	private static boolean isValidPort(int port) {
		return port >= 5000 && port <= 5050;
	}
	
	private static boolean isValidAddress(String address) {
		String[] elements = address.split("\\.");
		return elements.length == 4;
	}
}
