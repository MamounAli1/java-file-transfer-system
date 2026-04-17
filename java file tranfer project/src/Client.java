import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    static final String HOST= "localhost";
    static final int PORT= 4000;
    static final String client_dir= "client_files"; // stores client files

    public static void main(String[] args) {
        File folder =new File(client_dir); // create client directory
        if(!folder.exists()) {
            folder.mkdir();
        }
        try(
                Socket socket= new Socket(HOST, PORT);                                   // connect to server
                DataInputStream in= new DataInputStream(socket.getInputStream());        // input from server
                DataOutputStream out =new DataOutputStream(socket.getOutputStream());   // output to server
                Scanner kb= new Scanner(System.in)) {
            while (true) {
                System.out.println("\n1. View server files");
                System.out.println("2. View client files");
                System.out.println("3. Upload file");
                System.out.println("4. Download file");
                System.out.println("5. Exit");
                System.out.print("Pick an option: ");
                String userInput= kb.nextLine();

                switch (userInput) {
                    case "1":
                        showServerFiles(in, out);
                        break;
                    case "2":
                        showClientFiles();
                        break;
                    case "3":
                        uploadFile(kb, in, out);
                        break;
                    case "4":
                        downloadFile(kb, in, out);
                        break;
                    case "5":
                        System.out.println("exit");
                        return;
                    default:
                        System.out.println("invalid option");
                        break;
                }
            }
        }catch (IOException e){
            System.out.println("error: "+ e.getMessage());
        }
    }
    //method to show server files
    static void showServerFiles(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeUTF("List");             // request file list from server
        int numOfFiles =in.readInt();
        if(numOfFiles== 0) {
            System.out.println("no files found");
            return;
        }
        System.out.println("server files: ");
        for(int i= 0; i< numOfFiles; i++) {
            String fileName= in.readUTF(); // read file name
            long fileSize =in.readLong(); // read file size
            System.out.println(fileName+ " (" +fileSize+ " bytes)");
        }}
    // method to show client files
    static void showClientFiles(){
        File[] files =new File(client_dir).listFiles();            // list files in client directory
        if (files ==null || files.length == 0) {
            System.out.println("no files found");
            return;
        }
        System.out.println("client files: ");
        for(File file : files) {
            System.out.println(file.getName()+ " ("+ file.length() + " bytes)");
        }
    }
    // method to upload file to server
    static void uploadFile(Scanner kb,DataInputStream in,DataOutputStream out) throws IOException {
        File[] files =new File(client_dir).listFiles();
        if(files== null || files.length == 0) {
            System.out.println("no files found");
            return;
        }
        // loop to display files
        System.out.println("client files: ");
        for (int i= 0; i< files.length; i++) {
            System.out.println(i + ". " + files[i].getName()+ " (" +files[i].length() + " bytes)");
        }
        int fileIndex = -1;      // index of file to upload
        while (true) {
            System.out.print("pick a file to upload: ");
            String input= kb.nextLine();
            try {
                fileIndex= Integer.parseInt(input);                  //vonvert to number
                if (fileIndex< 0 || fileIndex >= files.length) {
                    System.out.println("invalid file index");
                }else {
                    break;
                }
            }catch (NumberFormatException e) {
                System.out.println("please enter a valid number");
            }
        }
        File file =files[fileIndex];
        out.writeUTF("upload");
        out.writeUTF(file.getName());
        out.writeLong(file.length());
        out.writeBoolean(false);               // first time upload

        // check for duplicate
        String response= in.readUTF();
        if(response.equals("duplicate found")) {
            System.out.println("duplicate file found");
            System.out.print("upload duplicate? (y/n): ");
            String uploadDuplicate= kb.nextLine();
            if (!uploadDuplicate.equalsIgnoreCase("y")) {
                System.out.println("upload cancelled");
                return;
            }
            //retry with upload duplicate
            out.writeUTF("upload");
            out.writeUTF(file.getName());
            out.writeLong(file.length());
            out.writeBoolean(true);         // indicate duplicate upload
            response= in.readUTF();
            if(!response.equals("uploading")) {
                System.out.println("unexpected response: " +response);
                return;
            }
        }

        out.writeLong(file.length());
        try(FileInputStream fileIn= new FileInputStream(file)) {
            byte[] buffer = new byte[5000];
            int bytesRead;
            while ((bytesRead =fileIn.read(buffer)) != -1) {
                out.write(buffer,0, bytesRead);
            }
        }
        String completeMsg =in.readUTF();
        System.out.println(completeMsg);
    }

    static void downloadFile(Scanner kb , DataInputStream in,DataOutputStream out) throws IOException {
        out.writeUTF("List");
        int numOfFiles =in.readInt();
        if (numOfFiles== 0) {
            System.out.println("no files found");
            return;
        }
        String[] fileNames= new String[numOfFiles];
        long[] fileSizes =new long[numOfFiles];          // use long to store file size
        System.out.println("server files: ");
        for (int i = 0;i < numOfFiles ;i++) {
            fileNames[i] = in.readUTF();                  // read file name
            fileSizes[i] = in.readLong();                 // read file size
            System.out.println(i+ ". " + fileNames[i] + " (" + fileSizes[i]+" bytes)");
        }
        int fileIndex = -1;                  // index of file to download

        //validate file index
        while (true) {
            System.out.print("pick a file to download: ");
            String input =kb.nextLine();
            try {
                fileIndex= Integer.parseInt(input);
                if (fileIndex<0|| fileIndex >=numOfFiles) {
                    System.out.println("invalid file index");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("please enter a valid number");
            }
        }
        String fileName= fileNames[fileIndex];                                // select file to download
        File file =new File(client_dir + File.separator + fileName); // create file in client directory
        //duplicate check for download
        if (file.exists()) {
            System.out.print("download duplicate? (y/n): ");
            String choice = kb.nextLine().trim().toLowerCase();
            if (!choice.equals("y")) {
                System.out.println("download cancelled.");
                return;
            }
            // create a new file name
            String name= fileName;
            String extension ="";
            int i= name.lastIndexOf('.');
            if (i >0) {
                extension= name.substring(i);
                name =name.substring(0, i);
            }
            int j =1;
            File newFile;                // iyerate to find a new file name
            do {
                newFile= new File(client_dir + File.separator + name + "_copy" + j + extension);
                j++;
            } while(newFile.exists());
            file=newFile;
        }

        long existingFileSize= file.exists() ? file.length() : 0; //calculaye how much of the file already downloaded
        out.writeUTF("download");      //send command to server
        out.writeUTF(fileName);            //send file name to server
        out.writeLong(existingFileSize);  //tell server how much of the file already downloaded

        String response= in.readUTF();   //read response from server
        if (response.equals("file not found")) {
            System.out.println("file not found");
            return;
        }

        long remaining = in.readLong();
        try (FileOutputStream fileOut= new FileOutputStream(file, true)) {      //append to file if it exists
            byte[] buffer = new byte[5000];
            while (remaining > 0) {
                int bytesRead =in.read(buffer ,0,(int) Math.min(buffer.length ,remaining));   //use min to avoid overflow
                if (bytesRead== -1) break;
                fileOut.write(buffer, 0, bytesRead);
                remaining-= bytesRead;
            }}
        // signal completion
        String completeMsg = in.readUTF();
        System.out.println(completeMsg);
    }
}

