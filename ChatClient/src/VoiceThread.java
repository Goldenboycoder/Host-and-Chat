/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


/**
 *
 * @author Patrick Balian
 */
public class VoiceThread extends Thread {
    private ArrayList<VoiceChannel> chs = new ArrayList<VoiceChannel>();
    private MicThread mic;
    private Socket voiceSocket;
    private ChatClient chatClient;

    public VoiceThread (String hostname,int port,ChatClient client) throws UnknownHostException,IOException{
        voiceSocket=new Socket(hostname, port);
        this.chatClient=client;
        System.out.println("VoiceThread created");
    }

    @Override
    public void run(){
        try{
            ObjectInputStream fromVoiceServer =new ObjectInputStream(voiceSocket.getInputStream());
            ObjectOutputStream toVoiceServer =new ObjectOutputStream(voiceSocket.getOutputStream());
            System.out.println("object in/out created");
            try{
                try{Thread.sleep(100);}catch(InterruptedException ex){}
                mic = new MicThread(toVoiceServer,chatClient);
                mic.start();
                System.out.println("mic created");

            }catch(Exception e){
                System.out.println("Microphone unavailable");
              
                
            }
            while(true){
                //-----------------------------------
                if(voiceSocket.getInputStream().available() > 0){//got somewthing from server
                    System.out.println("receiving");
                    Message in = (Message) (fromVoiceServer.readObject());
                    //which voicechannel ?!
                    VoiceChannel sendto = null;
                    for(VoiceChannel ch : chs){
                        if(ch.getChId() == in.getChId()){
                            sendto = ch;
                             System.out.println("ChId match");
                        }
                    }
                    if(sendto != null){
                        sendto.addToQueue(in);
                        System.out.println("sendto added to queue");
                    }
                    else{//create new channel
                        VoiceChannel ch = new VoiceChannel(in.getChId());
                        ch.addToQueue(in);
                        ch.start();
                        System.out.println("new Voicechannel created");
                        chs.add(ch);
                    }

                }
                else{//kill appropriate channels
                   //problem here
                    //System.out.println("killing");
                    ArrayList<VoiceChannel> killMe =new ArrayList<VoiceChannel>();
                    for(VoiceChannel c : chs){
                        if(c.canKill()){ 
                            killMe.add(c);
                        }
                    }
                    for(VoiceChannel c:killMe){
                        c.closeAndKill();
                        chs.remove(c);
                    }
                    try{Thread.sleep(1);}catch(InterruptedException ex){}
                }


            }

        }
        catch(IOException e1){
            //JOptionPane.showMessageDialog(null,"IO error","IO",JOptionPane.WARNING_MESSAGE);
            System.out.println("voiceth io error");
        }
        catch(Exception e){//connection error
            //JOptionPane.showMessageDialog(null,"Connection error1","Connection",JOptionPane.WARNING_MESSAGE);
            System.out.println("voiceth connection error");
        }

    }
}
