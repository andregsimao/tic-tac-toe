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
    private int currentPlayer;
    public void runServer() throws Exception
    {
        client=new Socket[2];
        input=new DataInputStream[2];
        output=new DataOutputStream[2];
        currentPlayer=0;
        server = new ServerSocket( 12346, 72 );
//        new Thread()
//        {
//            public void run() {
//                try {
//                    Thread.sleep(50);
//                    TicTacToe.main(null);
//                    TicTacToe.main(null);
//                } 
//                catch (Exception ex) {
//                    print("It wasn't possible open two windows of tic-tac-toe");
//                }
//            }
//        }.start();
        for ( int indexClient=0;indexClient<2;indexClient++) 
        {                   
            waitForConnection(indexClient);
            getStreams(indexClient); //set output and input
            HashMap<Integer, String> data = new HashMap();
            data.put(-1, Integer.toString(indexClient));
            sendData(indexClient,data);
            print("Client "+indexClient+" connected");
        }     
        readDataThread(0);
        readDataThread(1);
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
        print("-------------------------");
        print( "Sending data" );
        for (Map.Entry<Integer,String> entry : data.entrySet()) {
            int key = entry.getKey();
            String value = entry.getValue();            
            print(key+": "+value);
            output[indexClient].writeByte(key);
            output[indexClient].writeUTF(value);
        }
        output[indexClient].flush(); // Send off the data
        print( "Data sended" );
        print("-------------------------");
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
    public void readDataThread(int indexClient) throws Exception{  
        new Thread()
        {
            public void run() {
                try {
                    readData(indexClient);
                } catch (Exception ex) {                        
                    print("Failure to trigger readData to client "+ indexClient);
                }
            }
        }.start();        
    }
    public void readData(int indexClient) throws Exception{        
        print("--------------------------------------------------");
        print( "Reading data:" );
        Byte messageByte; 
        messageByte = input[indexClient].readByte();
        switch(messageByte){
            case -1: //mensagem de desconexao  
                String aux=input[indexClient].readUTF();
                //print(aux);
                print(messageByte+": "+aux);
                closeConnection(indexClient);
                return;
            case 0: //trocou de jogador
                String otherPlayerSt=input[indexClient].readUTF();
                //print("Trocou para jogador "+otherPlayerSt);
                if(currentPlayer==1)
                    currentPlayer= 0 ;
                else 
                    currentPlayer=1;                
                print(messageByte+": "+otherPlayerSt);
                readDataThread(indexClient);
                break;
            case 3: //pergunta se é o jogador da vez         
                int mouseX = Integer.parseInt(input[indexClient].readUTF());
                print(messageByte+": "+mouseX);
                
                messageByte = input[indexClient].readByte();
                int mouseY =Integer.parseInt(input[indexClient].readUTF());
                print(messageByte+": "+mouseY);
                
                HashMap<Integer, String> data = new HashMap();
                if(indexClient==currentPlayer)  
                    data.put(1, "yes");
                else
                    data.put(1, "no");
                data.put(2, Integer.toString(indexClient));
                data.put(3, Integer.toString(mouseX));
                data.put(4, Integer.toString(mouseY));                            
                if(indexClient==currentPlayer)
                {
                    sendData(0,data);
                    sendData(1,data);
                }                  
                readDataThread(indexClient);
                break;            
            default:
                print(" "+messageByte);
        }
        print( "Data readed" );        
        print("--------------------------------------------------");
    }
}