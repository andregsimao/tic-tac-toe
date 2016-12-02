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
        readData();
        print("saiu");
    }
    
    private void getStreams() throws IOException
    {
        // set up output stream for objects
        output = new DataOutputStream(client.getOutputStream());
        output.flush(); // flush output buffer to send header information

        // set up input stream for objects
        input = new DataInputStream(client.getInputStream());        
    } 
    public void readData() throws Exception{
        print( "Reading data" );
        Byte messageByte; 
        messageByte = input.readByte();
        switch(messageByte){
            case 0: //mensagem de conexao                
                String numberString=input.readUTF();
                print("Client set its id to "+numberString);
                clientId=Integer.parseInt(numberString);
                break;
            case 1: //resposta da pergunta se é o jogador da vez
                String answerYesOrNo=input.readUTF();
                
                messageByte = input.readByte(); 
                int clientId=Integer.parseInt(input.readUTF());
                
                messageByte = input.readByte();
                int mouseX=Integer.parseInt(input.readUTF());
                
                messageByte = input.readByte();
                int mouseY=Integer.parseInt(input.readUTF());
                if(answerYesOrNo.equals("yes"))
                    ticTacToe.processClick(mouseX, mouseY);               
                readData();
                break;
        }
    }
    
    void sendData( HashMap<Integer, String> data ) throws Exception
    {
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
    }
    
    void print(Object message){
        System.out.println("<<CLIENTE>> "+message);
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
