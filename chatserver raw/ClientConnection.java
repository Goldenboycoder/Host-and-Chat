import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;


public class ClientConnection extends Thread {
    private SVoiceThread serv; //instance of server, needed to put messages in the server's broadcast queue
    private Socket s; //connection to client 
    private ObjectInputStream in; //object streams to/from client
    private ObjectOutputStream out;
    private long chId; //unique id of this client, generated in the costructor
    private boolean isThreadAlive=true;


    private ArrayList<Message> toSend = new ArrayList<Message>(); //queue of messages to be sent to the client

    public InetAddress getInetAddress() { //returns this client's ip address
        return s.getInetAddress();
    }

    public int getPort() { //returns this client's tcp port
        return s.getPort();
    }

    public long getChId() { //return this client's unique id
        return chId;
    }

    public ClientConnection(SVoiceThread serv, Socket s) {
        this.serv = serv;
        this.s = s;
        byte[] addr = s.getLocalAddress().getAddress();//i changed getInetAddress to getLocalAddress
        chId = (addr[0] << 48 | addr[1] << 32 | addr[2] << 24 | addr[3] << 16) + s.getPort(); //generate unique chId from client's IP and port
       // System.out.println("connection created");
    }

    public void addToQueue(Message m) { //add a message to send to the client
        try {
            toSend.add(m);
        } catch (Throwable t) {
            //mutex error, ignore because the server must be as fast as possible
        }
    }
    @Override
    public void run(){
        //System.out.println("connection running");
        try {
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        } catch (IOException e) {//connection error
            try{
                //System.out.println("connection error1");
                s.close();
            }catch(IOException ex2){
               // System.out.println("connection error2");
            }
            //stop();
            isThreadAlive=false;
        }
        while(isThreadAlive){
            try{
                //if(s.getInputStream().available() > 0){//recieved something from client 
                if(s.getInputStream().available() > 0){
                    //System.out.println("connection recieved");
                    
                    Message toBroadcast = (Message) in.readObject();//read data from client
                    if(toBroadcast.getChId() == -1){//set it's chid and timestamp  and pass to server
                        toBroadcast.setChId(chId);
                        toBroadcast.setTimestamp(System.nanoTime() / 1000000L );
                        serv.addToBroadcastQueue(toBroadcast);
                    }
                    else{
                        //System.out.println("connection invalid");
                        continue;//message not valid
                    }
                   

                }
                try{
                    if(!toSend.isEmpty()){
                       // System.out.println("connection sending");
                        Message toClient = toSend.get(0);//got something to send
                        if(!(toClient.getData() instanceof SoundPacket) || toClient.getTimestamp()+ toClient.getTtl() < System.nanoTime()/ 1000000L){//is the message of unknown type or too old
                            continue;
                        }
                        out.writeObject(toClient);//send message
                        //System.out.println("connection sent");
                        toSend.remove(toClient);//and remove it from queue
                    }
                    else{
                       // System.out.println("connection waiting");
                        try{Thread.sleep(10);}catch(InterruptedException ex){}
                    }

                }
                catch(Throwable t){
                    if(t instanceof IOException){//connection close or error
                        //System.out.println("connection error3");
                        throw (Exception) t;
                        
                    }
                    else{
                        //System.out.println("Mutex error 4 ");
                        continue;
                    }

                }
               

            }
            catch(IOException exception){
               // System.out.println("error7");
                System.out.println(exception.getMessage());
            }
            catch(Exception ex){//connection closed or connection error , kill thread
                //System.out.println(ex.getMessage());
                //ex.printStackTrace();
                JOptionPane.showMessageDialog(null,ex.getMessage());
                try{
                   // System.out.println("connection error5");//connection killed here why ????
                    
                    //System.out.println("Expected error : " + ex.toString());
                   // System.out.println(ex);
                    //System.out.println("Expected error : "+ex.getMessage());
                    //System.out.println("connection error55");
                    s.close();
                   // System.out.println("connection error555");
                }catch(IOException ex2){
                    //System.out.println("connection error6");
                }
                //stop();
                //System.out.println("connection dead");
                isThreadAlive=false;
            }


        }

    }



}