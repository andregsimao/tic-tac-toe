/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoe;

/**
 *
 * @author Andre Simao
 */
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
public class Cliente {
    private DataOutputStream output; // output stream to client
    private DataInputStream input; // input stream from client
    Socket client;
    int clientId=-1;
    TicTacToe ticTacToe;
    public Cliente(TicTacToe ticTacToe){
        this.ticTacToe=ticTacToe;
    }
    public void run() throws Exception{        
        client=new Socket("localhost",12346);
        getStreams();        
        readDataThread();
    }
    
    private void getStreams() throws IOException
    {
        // set up output stream for objects
        output = new DataOutputStream(client.getOutputStream());
        output.flush(); // flush output buffer to send header information

        // set up input stream for objects
        input = new DataInputStream(client.getInputStream());        
    } 
    public void readDataThread() throws Exception{  
        new Thread()
        {
            public void run() {
                try {
                    readData();
                } catch (Exception ex) {                        
                    print("Failure to trigger client 0 listener");
                }
            }
        }.start();        
    }
    void readData() throws Exception{
        print("waiting data from server");
        Byte messageByte; 
        messageByte = input.readByte();
        print("--------------------------------------------------");
        print( "Reading data" );
        switch(messageByte){
            case -1: //mensagem de conexao                  
                String numberString=input.readUTF();
                clientId=Integer.parseInt(numberString);
                ticTacToe.mySymbol= (clientId==0) ? 'O' : 'X';
                print(messageByte+": connected as client "+numberString);                
                break;
            case 2: //resposta afirmativa da pergunta se é o jogador da vez 
                int clientId=Integer.parseInt(input.readUTF());
                print("move confirmation received as (sourceClient, x, y ):");
                print(messageByte+": "+clientId);
                
                messageByte = input.readByte();
                int mouseX=Integer.parseInt(input.readUTF());
                print(messageByte+": "+mouseX);
                
                messageByte = input.readByte();
                int mouseY=Integer.parseInt(input.readUTF());
                print(messageByte+": "+mouseY);
                
                ticTacToe.processClick(clientId,mouseX, mouseY);           
                break;
        }        
        print( "Data readed" );        
        print("--------------------------------------------------");
        readData();
    }
    void sendData( HashMap<Integer, String> data ) throws Exception
    {
        print("-------------------------");
        print( "Sending data" );
        for (Map.Entry<Integer,String> entry : data.entrySet()) {
            int key = entry.getKey();
            String value = entry.getValue();
            print(key+": "+value);
            output.writeByte(key);
            output.writeUTF(value);
        }
        output.flush(); // Send off the data
        print( "Data sended" );
        print("-------------------------");
    }
    
    void print(Object message){
        System.out.println("<<CLIENT "+clientId+">> "+message);
    }
    void closeConnection() 
    {
        print( "Terminating client "+clientId );
        try 
        {
            output.close(); 
            input.close(); 
            client.close();
        }
        catch ( IOException ioException ) 
        {
            ioException.printStackTrace();
        } 
    } 
}
