
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * @author Abdullah Ejaz
 *
 */

public class Listener implements Runnable {

    ServerSocket global_ss;
    Socket global_socket;
    Integer peerPortNumber;
    HitQueryProp global_msg;
    BufferedReader gBufferedReader = null;
    ObjectInputStream gObjectInputStream;

    public Listener(int pPeerPort) {
        this.peerPortNumber = pPeerPort;
        System.out.println("Listening to Port Number..."+ pPeerPort);
    }

    public Listener(Socket pSocket, int pPeerPort) {
        global_socket = pSocket;
        this.peerPortNumber = pPeerPort;
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    public void run() {
        boolean vFlag = Boolean.TRUE;
        String vPeerIP = global_socket.getInetAddress().getHostName();

        System.out.println("** Peer " + vPeerIP + " is connected..\n");
        try {
            //Initialize the reader
            gObjectInputStream = new ObjectInputStream(global_socket.getInputStream());
            global_msg = (HitQueryProp) gObjectInputStream.readObject();
            for (HitQueryProp vMsg : MainServices.message_received) {
                //If the message already exists set the flag to false
                if (vMsg.getUnique_key().equals(global_msg.getUnique_key()))
                    vFlag = Boolean.FALSE;
            }
            if (vFlag) {
                if (global_msg.getCommand_action().equals(EnumCommand.SEARCH)) {
                    query_search();
                } else if (global_msg.getCommand_action().equals(EnumCommand.FOUND)) {
                    forwardMessage();
                } else if (global_msg.getCommand_action().equals(EnumCommand.INVALID)) {
                    invalidateMessage();
                } else if (global_msg.getCommand_action().equals(EnumCommand.DOWNLOAD)) {
                    downloadListeners();
                }
                gObjectInputStream.close();
                global_socket.close();
            } else {
                System.out.println("Message " + global_msg.getUnique_key() + " has been dropped !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    //Method to listen the broadcasted query message, it sends result if the file is found else it rebroadcasts it
    private void query_search() {
        NeighborProperties var_neighPeer = null;

        //To check if the message exist in the registered files list.
        if(MainServices.global_registered_files.contains(global_msg.getMessage_content().trim())) {
            System.out.println("File "+ global_msg.getMessage_content().trim()+" has been found on this peer ! \n");
            String[] var_splitter = global_msg.message_header.split("-");
            try {
                for (int i = 0; i < MainServices.list_Neighbors.size(); i++) {
                    if (MainServices.list_Neighbors.get(i).getPeerId_Neighbor().equals(var_splitter[var_splitter.length - 1])) {
                        var_neighPeer = MainServices.list_Neighbors.get(i);
                    }
                }

                Socket vSocket = new Socket(var_neighPeer.getPeerIpAddress_Neighbor(), var_neighPeer.getPeerPort_Neighbor());

                //Initializing the writer
                ObjectOutputStream var_oos = new ObjectOutputStream(vSocket.getOutputStream());
                var_oos.flush();


                HitQueryProp var_msg = new HitQueryProp();                         //Creating message object from the HitQueryProp class
                var_msg.setPeer_Id(global_msg.getPeer_Id());                       //Setting peer ID
                var_msg.setPeer_IpAddress(global_msg.getPeer_IpAddress());         //Setting IP Address
                var_msg.setCommand_action(EnumCommand.FOUND);
                var_msg.setMessage_data(MainServices.peerID);    //Setting message data
                var_msg.setPeer_port(MainServices.peerPortNumber);                 //Setting Peer Port
                var_msg.setTtl(0);   //Setting Time To Live
                var_msg.setMessage_content(global_msg.getMessage_content());       //Setting message Content
                var_msg.setMessage_header(global_msg.getMessage_header());         //Setting message header
                var_oos.writeObject(var_msg);                          //Set the message object and pass it to the output stream.
                var_oos.flush();
                var_oos.close();                                       //Closing the reader
                vSocket.close();                                                   //Closing the connection

                System.out.println("Hit query has been sent back to the main searcher !\n");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            MainServices vUtility = new MainServices();
            System.out.println("The searched File "+ global_msg.getMessage_content()+" does not exist on this Peer !");
            vUtility.broadcast(global_msg);
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    // For forwarding the hit message to the path where it has been sent from.
    private void forwardMessage() {
        NeighborProperties var_neighPeer = null;

        //Checking if message peer ID passed is equal to local peer ID
        if(!global_msg.getPeer_Id().equals(MainServices.peerID)) {
            global_msg.removeHeader();
            String[] vSplitter = global_msg.message_header.split("-");
            for (int i = 0; i < MainServices.list_Neighbors.size(); i++) {
                if (MainServices.list_Neighbors.get(i).getPeerId_Neighbor().equals(vSplitter[vSplitter.length - 1])) {
                    var_neighPeer = MainServices.list_Neighbors.get(i);
                }
            }
            Socket vSocket;
            try {
                assert var_neighPeer != null;
                vSocket = new Socket(var_neighPeer.getPeerIpAddress_Neighbor(), var_neighPeer.getPeerPort_Neighbor());

                ObjectOutputStream var_oos = new ObjectOutputStream(vSocket.getOutputStream());  //Initializing the writer
                var_oos.flush();
                var_oos.writeObject(global_msg);
                var_oos.flush();
                var_oos.close();   //Closing the reader
                vSocket.close();               //Closing the connection
                System.out.println("Hit query forwarded back to " + var_neighPeer.getPeerIpAddress_Neighbor() + " - " + var_neighPeer.getPeerPort_Neighbor() + "peer !\n");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("The searched File: "+ global_msg.getMessage_content()+" found on peer: "+ global_msg.getMessage_data()+" in "+System.currentTimeMillis()+" ms");
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    // Method to send the updated query when a master file changes
    private void invalidateMessage() {
        if(MainServices.global_registered_files.contains(global_msg.getMessage_content().trim())) {
            File vFile = new File(MainServices.dataFolder + "/downloads/" + global_msg.getMessage_content().trim());
            System.out.println("vFile.exists(): "+vFile.exists());

            //Checking if file exists in the data folder or not.
            if (vFile.exists()) {
                System.out.println("Invalidate copy received from " + global_msg.getPeer_Id() + " peer !\n");
                System.out.println("File: " + global_msg.getMessage_content() + " has been deleted from local!\n");
                for (int i = 0; i < MainServices.global_registered_files.size(); i++) {
                    if(MainServices.global_registered_files.get(i).equals(vFile.getName())) {
                        MainServices.global_registered_files.remove(i);
                    }
                    //Deleting the file from downloads folder
                    vFile.delete();
                    //Copying the updated file in the downloads folder
                    downloadFileFromPeer();
                }
            }
        } else {
            MainServices vUtility = new MainServices();
            System.out.println("File: " + global_msg.getMessage_content() + " not found on your machine !");
            vUtility.broadcast(global_msg);
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

   //Method to download files from one peer to another
    private void downloadListeners() {
        String vContent = null;
        String vValue=new String();
        try {
            //Initializing the writer
            ObjectOutputStream var_oos = new ObjectOutputStream(global_socket.getOutputStream());
            var_oos.flush();
            //Creating the file reader object
            FileReader vFileReader =  null;
            if(global_msg.getCommand_action().equals(EnumCommand.INVALID))
                vFileReader = new FileReader(MainServices.dataFolder+ "/downloads/"+ global_msg.getMessage_content().trim());
            else
                vFileReader = new FileReader(MainServices.dataFolder+ "/"+ global_msg.getMessage_content().trim());
            BufferedReader vBufferedReader = new BufferedReader(vFileReader);

            while((vValue = vBufferedReader.readLine())!=null)
                vContent = vContent + vValue +"\r\n";
            vBufferedReader.close();
            vFileReader.close();
            System.out.println("File "+ global_msg.getMessage_content().trim()+" has been sent successfully to peer "+ global_msg.getPeer_Id());

            var_oos.writeObject(vContent); //Sending the content to the Peer
            var_oos.flush();

            gObjectInputStream.close();     //Closing the reader
            global_socket.close();          //Closing the connection

        }
        catch(UnknownHostException vUnknownHost){
            System.err.println("Host is not available..!");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    //method to listens to download request and send the file
    private void downloadFileFromPeer() {
        String vPeerIp = null;
        int vPeerPort = 0;
        try {
            for (int i = 0; i < MainServices.global_list_peers.size(); i++) {
                if (MainServices.global_list_peers.get(i).getPeerId_Neighbor().equals(global_msg.getPeer_Id())) {
                    vPeerIp = MainServices.global_list_peers.get(i).getPeerIpAddress_Neighbor();
                    vPeerPort = MainServices.global_list_peers.get(i).getPeerPort_Neighbor();
                }
            }
            System.out.println("File name: " + global_msg.getMessage_content()+" with version number: "+ global_msg.getFile_Version()+".\n");
            if (!(vPeerPort == Integer.parseInt(MainServices.peerPortNumber))) {
                Socket vSocket = new Socket(global_msg.getPeer_IpAddress(), Integer.parseInt(global_msg.getPeer_port()));
                System.out.println("\nConnected to peer : " + vPeerIp + " through port : " + vPeerPort + "\n");

                ObjectOutputStream var_oos = new ObjectOutputStream(vSocket.getOutputStream()); // initializing the Object OUtput Stream
                var_oos.flush();

                HitQueryProp vHitQueryMessage = new HitQueryProp();    //Initializing an object of the HitQueryProp class
                vHitQueryMessage.setPeer_Id(MainServices.peerID);
                vHitQueryMessage.setPeer_IpAddress(MainServices.peerIPAddress);
                vHitQueryMessage.setCommand_action(EnumCommand.DOWNLOAD);
                vHitQueryMessage.setTtl(3);
                vHitQueryMessage.setPeer_port(MainServices.peerPortNumber);
                vHitQueryMessage.setMessage_content(global_msg.message_content.trim());
                var_oos.writeObject(vHitQueryMessage);
                var_oos.flush();
                ObjectInputStream vObjectInputStream = new ObjectInputStream(vSocket.getInputStream());
                String vFileContent = vObjectInputStream.readObject().toString();

                //Checking if file exists on the peer location
                if (vFileContent.trim().equals("File not found".trim()))
                {
                    System.out.println("File not found");
                } else {
                    new MainServices().downloadToLocalPeer(global_msg.getMessage_content().trim(), vFileContent);
                    System.out.println(global_msg.getMessage_content().trim() + " updated file has been downloaded successfully, with version number " + global_msg.getFile_Version() + "\n");
                    MainServices.global_registered_files.add(global_msg.getMessage_content().trim());
                }

                vObjectInputStream.close();    //Closing the reader
                var_oos.close();   //Closing the writer
                vSocket.close();               //Closing the connection
            } else {
                System.out.println("Download is not allowed from the current peer that you are using !\n");
            }
        } catch(UnknownHostException vUnknownHost){
            System.err.println("Host is not available..!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            //Ending the current executing thread.
            Thread.currentThread().stop();
        }
    }
}
