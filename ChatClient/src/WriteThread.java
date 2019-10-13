/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import javax.swing.JOptionPane;
/**
 *
 * @author Patrick Balian
 */
public class WriteThread extends Thread {
    private PrintWriter writer;
    private Socket socket;
    private ChatClient client;
    String text = ".";

    public WriteThread(Socket socket, ChatClient client) {
        this.socket = socket;
	    this.client = client;
	    try {
		    OutputStream output = socket.getOutputStream();
		    writer = new PrintWriter(output, true);
	    } catch (IOException ex) {
		    System.out.println("Error getting output stream: " + ex.getMessage());
		    ex.printStackTrace();
	    }
    }

    public void run() {

	String userName = client.getUsername();
        System.out.println(userName);
	writer.println(userName);
        boolean sent=false;
        
        client.gui.send.addActionListener(new ActionListener(){  
            public void actionPerformed(ActionEvent e){  
                if(!client.gui.inputText.getText().isEmpty()){
                    text=client.gui.inputText.getText();
                    writer.println(text);
                    client.gui.inputText.setText("");
                }
                else{
                    JOptionPane.showMessageDialog(null,"Message empty","Incomplet info",JOptionPane.WARNING_MESSAGE);
                }
            }  
        });
        
	
	do {
		
		
	} while (!text.equals("bye"));

	try {
		socket.close();
	} 
        catch (IOException ex) {
		System.out.println("Error writing to server: " + ex.getMessage());
	}
    }
}
