package tictactoe;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
public class Server
{
    private int flagFinish=0;
    private ServerSocket server;
    private Socket[] client; // connection to client
    private DataOutputStream[] output; // output stream to client
    private DataInputStream[] input; // input stream from client
    private int actualPlayer;
    public void runServer() throws Exception
    {
        client=new Socket[2];
        input=new DataInputStream[2];
        output=new DataOutputStream[2];
        actualPlayer=0;
        server = new ServerSocket( 12346, 72 );
        for ( int indexClient=0;indexClient<2;indexClient++) 
        {                   
            waitForConnection(indexClient);
            getStreams(indexClient); //set output and input
            HashMap<Integer, String> data = new HashMap();
            data.put(0, Integer.toString(indexClient));
            sendData(indexClient,data);
            print("Client "+indexClient+" connected");
        }     
        new Thread()
        {
            public void run() {
                while(flagFinish!=2)
                {
                    try {
                        Thread.sleep(1000);
                        int finish=readData(0);
                        if(finish==1)
                            break;
                    } catch (Exception ex) {
                        print("Failure to trigger client 0 listener");
                    }
                }
            }
        }.start();
        new Thread()
        {
            public void run() {
                while(flagFinish!=2)
                {
                    try {
                        Thread.sleep(100);
                        int finish=readData(1);
                        if(finish==1)
                            break;
                    } catch (Exception ex) {                        
                        print("Failure to trigger client 0 listener");
                    }
                }
            }
        }.start();
        while(flagFinish!=2){
            Thread.sleep(10);
        }
    } 
    private void closeConnection(int indexClient) throws Exception 
    {
        flagFinish++;
        print( "Terminating connection ");
        output[indexClient].close(); 
        input[indexClient].close(); 
        client[indexClient].close();         
    }
    void sendData(int indexClient,  HashMap<Integer, String> data ) throws Exception
    {
        print( "Sending data" );
        for (Map.Entry<Integer,String> entry : data.entrySet()) {
            int key = entry.getKey();
            String value = entry.getValue();
            output[indexClient].writeByte(key);
            output[indexClient].writeUTF(value);
        }
        output[indexClient].flush(); // Send off the data
        print( "Data sended" );
    }
    private void waitForConnection(int indexClient) throws Exception 
    {
        print( "Waiting for connection" );
        client[indexClient] = server.accept();             
        print( "Connection received from: " + 	client[indexClient].getInetAddress().getHostName() );        
    } 
    private void getStreams(int indexClient) throws IOException
    {
        output[indexClient] = new DataOutputStream(client[indexClient].getOutputStream());
        output[indexClient].flush(); 
        input[indexClient] = new DataInputStream(client[indexClient].getInputStream());
    } 
    public static void main( String args[] ) throws Exception
    {
        Server application = new Server();
        application.runServer();
    }
    void print(String message){
        System.out.println("<<SERVER>> "+message);
    }
    public int readData(int indexClient) throws Exception{
        print( "Reading data:" );
        Byte messageByte; 
        messageByte = input[indexClient].readByte();
        switch(messageByte){
            case 0: //mensagem de desconexao                
                print(input[indexClient].readUTF());
                closeConnection(indexClient);
                return 1;
            case 1: //
                break;
        }
        return 0;
    }
}