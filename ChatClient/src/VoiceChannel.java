/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Patrick Balian
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class VoiceChannel extends Thread{
    private long chId; //an id unique for each user. generated by IP and port
    private ArrayList<Message> queue = new ArrayList<Message>(); //queue of messages to be played
    private int lastSoundPacketLen = SoundPacket.defaultDataLenght;
    private long lastPacketTime = System.nanoTime();
    private boolean isThreadAlive=true;

    public boolean canKill() { //returns true if it's been a long time since last received packet
        if (System.nanoTime() - lastPacketTime > 5000000000L) {
            return true; //5 seconds with no data
        } else {
            return false;
        }
    }

    public void closeAndKill() {
        if (speaker != null) {
            speaker.close();
        }
        //stop();
        isThreadAlive=false;
    }

    public VoiceChannel(long chId) {
        this.chId = chId;
        System.out.println("VoiceChannel created");
    }

    public long getChId() {
        return chId;
    }

    public void addToQueue(Message m) { //adds a message to the play queue
        queue.add(m);
    }

    private SourceDataLine speaker = null; //speaker

    @Override
    public void run(){
        try{
            AudioFormat af = SoundPacket.defaultFormat;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class,af);
            speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(af);
            speaker.start();
            System.out.println("VoiceChannel speaker started");
            System.out.println("VoiceChannel speaker : "+ speaker.toString());

            while(isThreadAlive){
                if(queue.isEmpty()){
                    try{Thread.sleep(10);}catch(InterruptedException ex){}
                    System.out.println("VoiceChannel speaker nothhing to play");
                    continue;
                }
                else{
                    System.out.println("VoiceChannel speaker got something to play");
                    lastPacketTime = System.nanoTime();
                    Message in = queue.get(0);
                    queue.remove(in);
                    if(in.getData() instanceof SoundPacket){
                        System.out.println("VoiceChannel speaker got a soundpacket");
                        SoundPacket m = (SoundPacket) (in.getData());
                        if(m.getData() == null){//sender skiped a packet , play rand noise
                            byte[] noise = new byte[lastSoundPacketLen];
                            for(int i=0;i<noise.length;i++){
                                noise[i]=(byte) ((Math.random() *3 )-1);
                            }
                            speaker.write(noise, 0, noise.length);
                            System.out.println("VoiceChannel speaker random noise playing");
                        }
                        else{
                            System.out.println("VoiceChannel decompressing");
                            //decompression
                            GZIPInputStream gis =new GZIPInputStream(new ByteArrayInputStream(m.getData()));
                            ByteArrayOutputStream baos =new ByteArrayOutputStream();
                            while(true){
                                int b = gis.read();
                                if(b == -1){
                                    break;
                                }
                                else{
                                    baos.write((byte)b);
                                }
                            }
                            //play decompressed data
                            byte[] toplay = baos.toByteArray();
                            speaker.write(toplay, 0, toplay.length);
                            System.out.println("VoiceChannel speaker playing data");
                            lastSoundPacketLen = m.getData().length;
                        }

                    }
                    else{//trash
                        System.out.println("VoiceChannel we got trash");
                        continue;
                    }
                }
            }

        }catch(Exception e){//soundcard or connection error
            System.out.println("VoiceChannel error  : "+"ChId - "+chId+"    stack : "+e);
            if(speaker != null){
               speaker.close();
               System.out.println("VoiceChannel speaker closed");
            }
            //stop();
            isThreadAlive=false;
        }
    }


}
