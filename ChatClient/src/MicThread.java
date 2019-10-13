/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.Socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;



/**
 *
 * @author Patrick Balian
 */
public class MicThread extends Thread {

    private Socket socket;
    private ChatClient client;
    private TargetDataLine mic;
    public double amplify = 3.0;
    private ObjectOutputStream toVoiceServer;
    private boolean isThreadAlive=true;
    //client needs the objectoutbutstream

    public MicThread( ObjectOutputStream toVoiceServer,ChatClient chatClient) throws LineUnavailableException {
        this.client = chatClient;
        this.toVoiceServer = toVoiceServer;

        AudioFormat audioFormat = SoundPacket.defaultFormat ;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,null);//changed to null
        mic = (TargetDataLine) (AudioSystem.getLine(info));
        mic.open(audioFormat);
        mic.start();
        System.out.println(mic.toString());
        System.out.println("mic created and started");
    }

    @Override
    public void run(){
        while(isThreadAlive){

            if(mic.available() >= SoundPacket.defaultDataLenght){//we got enough data to send
                System.out.println("mic has enough data to send");
                byte[] buff = new byte[SoundPacket.defaultDataLenght];
                System.out.println("new buff created");

                while(mic.available() >= SoundPacket.defaultDataLenght){//flush old data
                    mic.read(buff, 0, buff.length);
                    System.out.println("mic old data flushed");
                }
            

                try {//this part is used to decide whether to send or not the packet. if volume is too low, an empty packet will be sent and the remote client will play some comfort noise
                    long tot = 0;
                    for(int i = 0;i < buff.length;i++){
                        buff[i] *= amplify;
                        tot += Math.abs(buff[i]);
                       
                    }
                    System.out.println("mic amplifyed");
                    tot *= 2.5;
                    tot /=buff.length;
                    //creat and send
                    Message pack = null ;
                    if(tot == 0){//send empty
                        pack =new Message(-1,-1,new SoundPacket(null));
                        System.out.println("mic prepare empty");
                    }
                    else{//send
                        //copress then send
                        System.out.println("mic prepare full comp");
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        GZIPOutputStream gop = new GZIPOutputStream(bout);
                        gop.write(buff);
                        gop.flush();
                        gop.close();
                        bout.flush();
                        bout.close();
                        pack = new Message(-1,-1,new SoundPacket(bout.toByteArray()));
                        System.out.println("mic prepare full comp done");

                    }
                    
                    //---------------------------
                    //toVoiceServer.flush();
                    toVoiceServer.writeObject(pack);//here is the problem
                    System.out.println("mic sent");

                }
                catch(Exception e){
                    //stop();
                    isThreadAlive=false;
                    System.out.println("MicThread connection error   :"+e);
                }
            }
            else{
                try{Thread.sleep(10);}catch(InterruptedException ex){}
                System.out.println("mic sleep");
            }


        }

    }


    
}
