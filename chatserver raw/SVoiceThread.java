
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;


public class SVoiceThread extends Thread {
    private ArrayList<Message> broadCastQueue = new ArrayList<Message>();    
    private ArrayList<ClientConnection> clients = new ArrayList<ClientConnection>();
    private int port;
    private String addrs;
    
    
    public void addToBroadcastQueue(Message m) { //add a message to the broadcast queue. this method is used by all ClientConnection instances
        try {
            broadCastQueue.add(m);
        } catch (Throwable t) {
            //mutex error, try again
            try{Thread.sleep(1);}catch(InterruptedException ex){}
            addToBroadcastQueue(m);
        }
    }
    private ServerSocket s;
    public SVoiceThread (int port,String LIp) {
        this.port=port;
        addrs=LIp;
        /*if(upnp){
            u=new UpnpServiceImpl(new PortMappingListener(new PortMapping(port, ipAddress, PortMapping.Protocol.TCP)));
            u.getControlPoint().search();
        }*/
        try{
            s=new ServerSocket(this.port,1,InetAddress.getByName(addrs));
           // System.out.println("Svoice created");
        }
        catch(IOException ex){
            //throw new Exception("Error "+ex);
        }
    }


    @Override
    public void run(){
        new BroadcastThread().start(); //create a BroadcastThread and start it
        //System.out.println("Broadcast started");
        while(true){
            try{
                Socket c =s.accept();
                ClientConnection cc = new ClientConnection(this, c);
                cc.start();
                addToClients(cc);
            }
            catch(Exception ex){
               // System.out.println("Svoice no connection");
            }
        }

    }
    private void addToClients(ClientConnection cc) {
        try {
            clients.add(cc); //add the new connection to the list of connections
        } catch (Throwable t) {
            //mutex error, try again
            try{Thread.sleep(1);}catch(InterruptedException ex){}
            addToClients(cc);
        }
    }
    private class BroadcastThread extends Thread{
        public BroadcastThread(){
        }

        @Override
        public void run(){
           // System.out.println("Broadcast created and running");
            while(true){
                try{
                    ArrayList<ClientConnection> toRemove = new ArrayList<ClientConnection>();//dead connections
                    for(ClientConnection cc : clients){
                        if(!cc.isAlive()){//dead connection need to be removed
                            toRemove.add(cc);
                        }
                    }
                    clients.removeAll(toRemove);//delete all dead connections
                    if(broadCastQueue.isEmpty()){//nothing to send
                        try{Thread.sleep(10);}catch(InterruptedException ex){}
                       // System.out.println("Broadcast empty");
                        continue;
                    }
                    else{//we have something to broadcast
                        //System.out.println("Broadcast sending");
                        Message m = broadCastQueue.get(0);
                        for(ClientConnection cc : clients){//broadcast the message
                            if(cc.getChId() != m.getChId()){
                                cc.addToQueue(m);
                            }

                        }
                        broadCastQueue.remove(m);//
                    }

                }
                catch(Throwable t){
                    //mutex error , try again
                   // System.out.println("Broadcast error");
                }

            }



        }
    }


    
}