/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import javax.swing.JOptionPane;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author Patrick Balian
 */
public class ChatClient {
    public static NewJFrame gui;
    private String hostname;
    private int port;
    public String username;
    //private Socket soc;
    public ChatClient chat=this;
    //private ObjectOutputStream toVoiceServer ;
    //private ObjectInputStream fromVoiceServer;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public ChatClient(String hostname, int port) {
	this.hostname = hostname;
	this.port = port;
        
    }
    public void execute() {
	try {
        setUsername(gui.username.getText());

        Socket socket = new Socket(hostname, port);
        //soc=new Socket(hostname,port+1);

        //Socket voiceSocket =new Socket(hostname,port+1);
        //toVoiceServer = new ObjectOutputStream(voiceSocket.getOutputStream());
        //fromVoiceServer = new ObjectInputStream(voiceSocket.getInputStream());

		System.out.println("Connected to the chat server");
                

		new ReadThread(socket, this).start();
		new WriteThread(socket, this).start();
                

	} catch (UnknownHostException ex) {
		System.out.println("Server not found: " + ex.getMessage());
	} 
        catch (IOException ex) {
		System.out.println("I/O Error: " + ex.getMessage());
	}

    }
    public  void startVoice(){
        try{
            System.out.println("startVoice");
            //Socket soc=new Socket(hostname,port+1);
            
            new VoiceThread(hostname,port+1, this).start();
            System.out.println("voicethread started");
            JOptionPane.showMessageDialog(null,"Connection to voice server successful","Connection",JOptionPane.INFORMATION_MESSAGE);
            
        }catch(Exception ex){
            System.out.println("socket can't be created");
        }
        
    }
    
    
    public static void main(String[] args){
       gui=new NewJFrame();
       gui.connect.addActionListener(new ActionListener(){  
        public void actionPerformed(ActionEvent e){  
            if(!gui.hostname.getText().isEmpty()&&!gui.portnum.getText().isEmpty()&&!gui.username.getText().isEmpty()){
                int port=Integer.parseInt(gui.portnum.getText());
                //maybe new ChatClient will reset gui ?
                ChatClient client = new ChatClient(gui.hostname.getText(),port);
                client.execute();
                System.out.println("chat activated");
                client.startVoice();
                System.out.println("voice started");
            }
            else{
                JOptionPane.showMessageDialog(null,"Input Hostname , Port and Username","Incomplet info",JOptionPane.WARNING_MESSAGE);
            }
        }  
        });
      
       
       gui.setLocationRelativeTo(null);
       gui.setVisible(true);
        
    }
    
    
}
