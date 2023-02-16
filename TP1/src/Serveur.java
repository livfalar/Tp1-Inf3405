import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Scanner;
public class Serveur {
	private static ServerSocket Listener; 
	private static Scanner scanner = new Scanner(System.in);
	public static void main(String[] args) throws Exception {
		int clientNumber = 0;
		String serverAddress = ""; //"127.0.0.1"; 
		int serverPort = 0;
		
		while(!isValidPort(serverPort)) {
			System.out.println("Veuillez entrer un port: ");
			String portInput = scanner.nextLine();
			try {
				serverPort = Integer.parseInt(portInput);
				if(!isValidPort(serverPort)) {
					System.out.println("Le port doit être entre 5000 et 5050");
				}
			}
			catch(Exception e) {
				System.out.print("Le port entré n'est pas un nombre");
			}
		}
			
		while(!isValidAddress(serverAddress)) {
			System.out.println("Veuillez entrer une adresse: ");
			serverAddress = scanner.nextLine();
			if(!isValidAddress(serverAddress)) {
				System.out.println("L'adresse doit être du format: a.b.c.d");
			}
		}
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		Listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		try {
			while (true) {
				new ClientHandler(Listener.accept(), clientNumber++).start();
			}
			
		}finally {
			Listener.close();
		}
		
	}
	
	private static boolean isValidPort(int port) {
		return port >= 5000 && port <= 5050;
	}
	
	private static boolean isValidAddress(String address) {
		String[] elements = address.split("\\.");
		return elements.length == 4;
	}
}
