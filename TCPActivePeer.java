/**
*Test is all based on linux system, not tried on windows system
*Preceding running this program, TCPPassivePeer.java should be running already
*accpet tow command input:IP address and folder path
*This program can be used to synchronize the files in both peer and its own designated folders
*@author LIXIN
*@since 2016.3.12
*/
import java.io.*;
import java.net.*;
import java.lang.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
class TCPActivePeer{
    public static class com{
        /**
        *Variables initializtion
        *@param t_fList:filenames need synchornization
        *@param c_fList:filenames in directory
        *@param p2pSocket_list:socket used to transfer filename
        *@param p2pSocket_file:socket used to transfer file stream
        *@param listToPeer:filename DataOutputStream 
        *@param fileToPeer:file DataOutputStream
        *@param welcomeSocket_list:serverSocket accpet filename transfer
        *@param welcomeSocket_file:serverSocket accpet file transfer
        *@param connectionSocket_list:accpet filename transfer socket
        *@param connectionSocket_file:accpet file transfer socket
        *@param listRespFromPeer:BufferedReader for filename need to synchronize
        *@param listFromPeer:BufferedReader for filename from the other peer
        *@param listRespToPeer:DataOuputStream for filename need to synchronize
        *@param fileFromPeer:DataInputStream for file transferred from the other peer
        *@param path:command input, the folder path
        *@param IP:command input, the host you want to connect to
        */
        public static ArrayList<String> t_fList=new ArrayList<String>();
        public static ArrayList<String> c_fList = new ArrayList<String>();
        public static Socket p2pSocket_list;
        public static Socket p2pSocket_file;
        public static DataOutputStream listToPeer;
        public static DataOutputStream fileToPeer;
        public static BufferedReader listRespFromPeer;
        public static ServerSocket welcomeSocket_list;
        public static ServerSocket welcomeSocket_file;
        public static Socket connectionSocket_list;
        public static Socket connectionSocket_file;
        public static DataOutputStream listRespToPeer;
        public static BufferedReader listFromPeer;
        public static DataInputStream fileFromPeer;
        public static String path;
        public static String IP;
    }
    public static void main(String argv[]) throws Exception
    {   /**
        *start four threads in main fucntion
        */
        com.IP=argv[0];
        com.path=argv[1];
        getFileName g_thread=new getFileName();
        g_thread.start();
        getFile gf_thread=new getFile();
        gf_thread.start();
        sendFileName s_thread=new sendFileName();
        s_thread.start();
        sendFile sf_thread=new sendFile();
        sf_thread.start();
        }
     static class sendFileName implements Runnable{
        /**
        *sendFileName thread:read the filenames of given folder and send them to its connected *peer,waiting for this thread to complete before the following thread can run
        */
        private Thread t_s;
        sendFileName(){}
        public void run(){
            File[] files = new File(com.path).listFiles();
            try{
                com.p2pSocket_list=new Socket(com.IP,6001);
                for (File file:files) {
                    if (file.isFile()) {
                        com.listToPeer=new DataOutputStream(com.p2pSocket_list.getOutputStream());
                        com.c_fList.add(file.getName());
                        com.listToPeer.writeBytes(file.getName()+"\n");
                    }
                    com.listToPeer.flush();
                }
            }catch(IOException e){e.printStackTrace();}
        }
        public void start(){
            t_s=new Thread(this);
            t_s.start();
            try{
            t_s.join();
            }catch(InterruptedException ie){ie.printStackTrace();}
        }
    }
    static class getFileName implements Runnable{
        /**
        *getFileName thread:get the filenames of its connected peer,find out the filename does *not exits in given folder now and send their names to its connected peer as well as *adding them into t_fList for future usage 
        */
        private Thread t_g;
        getFileName(){}   
        public void run(){
            try{
                com.welcomeSocket_list = new ServerSocket(6789);
            }catch(IOException e){e.printStackTrace();}
                File[] files = new File(com.path).listFiles();
                for (File file:files) {
                    if (file.isFile()) {
                        com.c_fList.add(file.getName());
                    }
                }
                while(true) {
                    try{
                        com.connectionSocket_list = com.welcomeSocket_list.accept();
                        com.listRespToPeer = new DataOutputStream(com.connectionSocket_list.getOutputStream());
                        com.listFromPeer =new BufferedReader(new InputStreamReader(com.connectionSocket_list.getInputStream()));
                        String accept_fileName = com.listFromPeer.readLine();
                        while(accept_fileName!=null){
                            int count=0;
                            for(String file:com.c_fList){
                                if(file.contentEquals(accept_fileName))
                                    count++;
                            }
                            if(count==0){
                                com.listRespToPeer.writeBytes(accept_fileName+"\n");
                                com.t_fList.add(accept_fileName);
                            }
                            accept_fileName = com.listFromPeer.readLine();
                        }
                    }catch(IOException e){e.printStackTrace();}
                }
        }
        public void start(){
            t_g=new Thread(this);
            t_g.start();
        }
    }
    static class sendFile implements Runnable{
        /**
        *sendFile thread:get the filenames need to send to its connected peer at first, then *establish a new socket to transfer the corresponding files to its connected peer
        */
        private Thread t_sf;
        sendFile(){}
        public void run(){
            try{
            com.p2pSocket_file=new Socket(com.IP,6002);
            }catch(IOException e){e.printStackTrace();}
            try{
                com.listRespFromPeer=new BufferedReader(new InputStreamReader(com.p2pSocket_list.getInputStream()));   
                FileInputStream inFromFile=null;
                byte[] byteFile=null;
                byte[] byteFileSize=null;
                File sendFile=null;
                String send_fileName=com.listRespFromPeer.readLine();   
                while(send_fileName!=null){
                    sendFile = new File(com.path+"/"+send_fileName);
                    com.fileToPeer=new DataOutputStream(com.p2pSocket_file.getOutputStream());
                    inFromFile=new FileInputStream(sendFile);
                    byteFileSize=ByteBuffer.allocate(5).putInt((int)sendFile.length()).array();
                    com.fileToPeer.write(byteFileSize,0,5); 
                    byteFile = new byte[1024];
                    int byteRead=0;
                    while((byteRead=inFromFile.read(byteFile,0,byteFile.length))!=-1){
                    com.fileToPeer.write(byteFile,0,byteRead);
                    }
                    com.fileToPeer.flush();
                    send_fileName=com.listRespFromPeer.readLine(); 
                }
            }catch(IOException e){e.printStackTrace();}
            }
        public void start(){
            t_sf=new Thread(this);
            t_sf.start();
        }
    }
    static class getFile implements Runnable{
        /**
        *getFile thread:get the file through file transfer socket and write them into give *folder according to path command parameter,filename can be obtained from t_fList
        */
        private Thread t_gf;
        getFile(){}
        public void run(){
            try{
                com.welcomeSocket_file=new ServerSocket(7002);
            }catch(IOException e){e.printStackTrace();}
            while(true){
                try{  
                    com.connectionSocket_file = com.welcomeSocket_file.accept();
                    try{
                    Thread.sleep(1000);
                    }catch(InterruptedException ie){ie.printStackTrace();}
                    com.fileFromPeer = new DataInputStream(com.connectionSocket_file.getInputStream()); 
                    byte[] file_buffer=null;
                    byte[] byteSize_buffer=null;
                    int byteSize=0;
                    int readSize=0;
                    FileOutputStream newFile=null; 
                    for(String file_rec:com.t_fList){
                        com.c_fList.add(file_rec);
                        newFile = new FileOutputStream(com.path+"/"+file_rec);
                        byteSize_buffer=new byte[5];
                        com.fileFromPeer.readFully(byteSize_buffer,0,5);
                        byteSize=ByteBuffer.wrap(byteSize_buffer).getInt();
                        int byteRead=0;
                        int byteTotal=0;
                        file_buffer=new byte[1024];
                        while(byteSize-byteTotal>1024){
                            byteRead=com.fileFromPeer.read(file_buffer,0,file_buffer.length);
                            newFile.write(file_buffer,0,byteRead);
                            byteTotal=byteRead+byteTotal;
                        }
                        while((byteRead=com.fileFromPeer.read(file_buffer,0,byteSize-byteTotal))!=0){
                            newFile.write(file_buffer,0,byteRead);
                            byteTotal=byteRead+byteTotal;
                        }
                        newFile.flush();
                        newFile.close();
                    }
                    com.t_fList.clear();
                    com.fileFromPeer.close();
                    com.connectionSocket_file.close();
                    System.out.println("File Synchronization Succeed");
                }catch(IOException e){e.printStackTrace();}		
            }
        }
        public void start(){
            t_gf=new Thread(this);
            t_gf.start();
        }
    }
}
