import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;   
public class ClientHandler extends Thread{
	private Socket socket; 
	private int clientNumber;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	public ClientHandler(Socket socket, int clientNumber) {
		this.socket = socket;
		this.clientNumber = clientNumber; 
		System.out.println("New connection with client#" + clientNumber + " at" + socket);
	}
	public void run(){ 
		try {
			out = new DataOutputStream(socket.getOutputStream()); 
			out.writeUTF("Hello from server - you are client#" + clientNumber);
			boolean exit = false;
			in = new DataInputStream(socket.getInputStream());
			String currentPath = ".";
			while(!exit) {
				String userInput = in.readUTF();
				
				String localAddress = this.socket.getLocalAddress().toString();
				StringBuilder sb1 = new StringBuilder(localAddress);
				sb1.deleteCharAt(0);
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");  
				LocalDateTime now = LocalDateTime.now();
				System.out.println("[" + 
					sb1.toString() + ":" + 
					this.socket.getPort() + " - " + 
					dtf.format(now) + "] : " + 
					userInput);	
 
				
				String[] expressions = userInput.split(" ");
				switch(expressions[0]) {
				case "exit":
					exit = true;
					out.writeUTF("Vous avez été déconnecté avec succès.");
					break;
				case "hello":
					System.out.println("hello from client");
					out.writeUTF("hello from server");
					break;
				case "download":
					try {
						if(currentPath == "") {
							sendFile(expressions[1], out);
						}
						else {
							sendFile(currentPath+"/"+expressions[1], out);
						}
						out.writeUTF("Le fichier " + expressions[1] + " a bien été téléchargé.");
						}
					catch(Exception e) {
						out.writeUTF("ERROR téléchargement");
					}
					break;
				case "upload":
					try {
						if(currentPath == "") {
							receiveFile(expressions[1], in);
						}
						else {
							receiveFile(currentPath+"/"+expressions[1], in);
						}
						out.writeUTF("Le fichier " + expressions[1] + " a bien été téléversé.");
					}
					catch(Exception e) {
						out.writeUTF("ERROR téléversement");
					}
					break;
				case "mkdir":
					if(currentPath == "") {
						createDir(expressions[1]);
					}
					else {
						createDir(currentPath+"/"+expressions[1]);
					}
					out.writeUTF("Le dossier " + expressions[1] + " a été créé.");
					break;
				case "cd":
					try{
						currentPath = moveInto(currentPath, expressions[1], in, out);
					}
					catch(Exception e) {
						out.writeUTF("ERROR cd");
					}
					break;
				case "ls":
					try {listFolderContent(currentPath, out);}
					catch(Exception e) {
						out.writeUTF("ERROR ls");
					};
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
	
	private static void sendFile(String path, DataOutputStream out) throws Exception{
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
	
	private static void receiveFile(String fileName, DataInputStream in) throws Exception{
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
	
	private static void createDir(String folderName){
		File folder = new File(folderName);
		folder.mkdir();
	}
	
	private static String moveInto(String oldPath, String dir, DataInputStream in, DataOutputStream out) throws Exception{
		String newPath = "";
		if (dir.contains("..")) {
			String[] expressions = oldPath.split("/");
			if(expressions.length == 1) { newPath = oldPath; }
			else {
				for(int i = 0; i < expressions.length - 1; i++) {
					newPath += expressions[i];
					if(i < expressions.length - 2) {newPath += "/";}
				}
			}
			out.writeUTF("Vous êtes dans le dossier " + newPath);
			return newPath;
		}
		newPath = oldPath + "/" + dir;
		File folder = new File(newPath);
		
		if(folder.exists()){
			out.writeUTF("Vous êtes dans le dossier " + newPath);
			return newPath;
		}

		out.writeUTF("Dossier non créé");
		return oldPath;
	}
	
	private static void listFolderContent(String currentPath, DataOutputStream out) throws Exception{
		File dir = new File(currentPath);
		String[] listOfFiles = dir.list();
		String message = "";
		for(int i = 0; i< listOfFiles.length; i++) {
			if(listOfFiles[i].contains(".")) {
				message += "[File] ";
			}
			else {
				message += "[Folder] ";
			}
			message += listOfFiles[i] + "\n";
		}
		out.writeUTF(message);
	}
}
