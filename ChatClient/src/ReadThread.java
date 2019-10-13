/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;

/**
 *
 * @author Patrick Balian
 */
public class ReadThread extends Thread {
    
    private BufferedReader reader;
    private Socket socket;
    private ChatClient client;

    public ReadThread(Socket socket, ChatClient client) {
	this.socket = socket;
	this.client = client;

	try {
		InputStream input = socket.getInputStream();
		reader = new BufferedReader(new InputStreamReader(input));
	} catch (IOException ex) {
		System.out.println("Error getting input stream: " + ex.getMessage());
		ex.printStackTrace();
	}
    }

    public void run() {
	while (true) {
		try {
                    String response = reader.readLine();
                    /*Could not work because rows are set to 10 for jtextarea , 
                    *if it doesn't work use getText() to save old then append to it the new text preceeded by \n and then setText()
                    */
                     if(response == null){
                        JOptionPane.showMessageDialog(null,"You have been Disconnected !","Disconnected",JOptionPane.WARNING_MESSAGE);
                        break;
                    }
                     
                    client.gui.outputText.append(response+"\n");

                    /* prints the username after displaying the server's message
                    if (client.getUserName() != null) {
                            System.out.print("[" + client.getUserName() + "]: ");
                    }*/
                   
		} 
                catch (IOException ex) {
                    System.out.println("Error reading from server: " + ex.getMessage());
                    ex.printStackTrace();
                    break;
		}
	}
    }
}
