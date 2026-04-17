import java.io.*;
import java.net.*;

public class Server {
    static final int PORT= 4000;
    static final String Server_dir= "server_files"; // server files directory

    public static void main(String[] args){
        File folder= new File(Server_dir);          // create server directory
        if(!folder.exists()){
            folder.mkdir();
        }
        // start server at port 4000
        try(ServerSocket sSocket= new ServerSocket(PORT)){
            System.out.println("Server is running on port "+ PORT);
            while(true){
                Socket socket= sSocket.accept();
                System.out.println("client connected: "+ socket.getInetAddress());    // accept client connection and ip
                new Thread(() -> handleClient(socket)).start();          // handle client in a new thread
            }
        }catch(IOException e){
            System.out.println("Server error: "+ e.getMessage());
        }
    }

    private static void handleClient(Socket socket){
        try(
                DataInputStream in= new DataInputStream(socket.getInputStream());
                DataOutputStream out= new DataOutputStream(socket.getOutputStream())
        ){
            while(true){
                String command= in.readUTF();           //read command from client
                if(command.equalsIgnoreCase("List")) {
                    sendFileList(out);
                }else if (command.equalsIgnoreCase("upload")) {
                    handleUpload(in, out);
                }else if(command.equalsIgnoreCase("download")) {
                    handleDownload(in, out);
                }else{
                    out.writeUTF("invalid command: "+ command);
                }
            }
        } catch(IOException e) {
            System.out.println("client disconnected.");
        }}

    private static void sendFileList(DataOutputStream out) throws IOException {
        File[] files= new File(Server_dir).listFiles();
        if(files== null) files= new File[0];         // jandle empty folder
        out.writeInt(files.length);                  // send number of files
        for(File file : files) {
            out.writeUTF(file.getName());
            out.writeLong(file.length());
        }
    }
    // handle upload from client
    private static void handleUpload(DataInputStream in,DataOutputStream out) throws IOException{
        String fileName= in.readUTF();
        in.readLong();                                    // read file size
        boolean uploadDuplicate= in.readBoolean();        //check if want to make duplicste
        File file=new File(Server_dir+File.separator + fileName);

        //check if client does not want duplicate
        if(!uploadDuplicate&& file.exists()) {
            out.writeUTF("duplicate found");
            return;}

        //generate new file name if duplicate found
        if(uploadDuplicate&& file.exists()) {
            fileName= generateNewFileName(fileName);
            file= new File(Server_dir+ File.separator + fileName);   // create new file object with new name
        }
        out.writeUTF("uploading");         //signal client to start upload
        long remaining =in.readLong();         // read file size

        //open stream to write file
        try(FileOutputStream fileOut= new FileOutputStream(file)) {
            byte[] buffer= new byte[5000];
            while (remaining> 0) {
                int bytesRead= in.read(buffer,0,(int) Math.min(buffer.length, remaining));
                if (bytesRead== -1) break;
                fileOut.write(buffer,0,bytesRead);        // write to file
                remaining-= bytesRead;
            }
        }
        out.writeUTF("upload complete");
    }

    private static String generateNewFileName(String originalName) {
        int index= originalName.lastIndexOf(".");                                   // find last index of "."
        String name= (index ==-1) ? originalName : originalName.substring(0,index);    // get name without extension
        String extension =(index== -1) ? "" : originalName.substring(index);           // get extension
        int count = 1;

        File newFile;
        do {
            String newName= name +"_copy"+ count +extension;
            newFile = new File(Server_dir+ File.separator + newName);
            count++;
        } while (newFile.exists());        //keep generating new name until it is unique
        return newFile.getName();
    }

    private static void handleDownload(DataInputStream in, DataOutputStream out) throws IOException {
        String fileName=  in.readUTF();
        long alreadyDownloaded= in.readLong();        // read already downloaded bytes
        File file =new File(Server_dir+ File.separator +fileName);  // create file object
        if (!file.exists()){
            out.writeUTF("file not found");
            return;}

        out.writeUTF("downloading");       // signal client to start download
        long remaining= file.length() - alreadyDownloaded;   // resume download from where it left off
        out.writeLong(remaining);
        try (FileInputStream fileIn= new FileInputStream(file)) {
            fileIn.skip(alreadyDownloaded);         // skip already downloaded bytes
            byte[] buffer= new byte[5000];
            int bytesRead;
            while((bytesRead= fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        out.writeUTF("download complete");    // signal client that download is complete
    }
}
