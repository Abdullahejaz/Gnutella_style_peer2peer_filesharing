
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;


/**
 * @author Abdullah Ejaz
 *
 */


//This class provides all the main functionality like searching registering, identifying
public class MainServices implements Serializable {

    //getting the path of the directory where the class file is.
    static final  File pathFile = new File(MainServices.class.getProtectionDomain().getCodeSource().getLocation().getPath());

    //Getting the parent directory of the above pathFile
    static File parentOfpathFile = new File(pathFile.getParent());

    //Getting parent again of the parentOfpathFile and appending path to reach to the main folder
    static File dataDirectory = new File(parentOfpathFile.getParent() +"/src/main/");

    //getting the path converted to strings
    public static String dataFolder = dataDirectory.toString();

    //Getting workspace path
    static String workspaceDirectory = parentOfpathFile.getParent();

    //Properties of Peer
    public static String peerIPAddress;
    public static String peerPortNumber;
    public static String  peerID;

    //List of neighbors in the config file for all peers
    public static ArrayList<NeighborProperties> list_Neighbors = new ArrayList<NeighborProperties>();
    //list of all the peers mentioned in the config file
    public static ArrayList<NeighborProperties> global_list_peers = new ArrayList<NeighborProperties>();

    public static ArrayList<HitQueryProp> message_received = new ArrayList<HitQueryProp>();

    public static ArrayList<String> global_registered_files = new ArrayList<String>();

    //list to keep track of the changes in the file
    public static ArrayList<FileProperties> list_fileChanges = new ArrayList<FileProperties>();


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    //Method to read the config file. Every peer will read the file.
    public static void configFileRead (String inputPeerId, String peersTopology ){

        File configFile = null;
        //if the topology is mesh
        if(peersTopology.equalsIgnoreCase("mesh")){
            //Read config file for mesh topology
            configFile = new File(workspaceDirectory + "/peer_config_2dmesh.txt");

        }else{
            //otherwise it will automatically choose star topology
            configFile = new File(workspaceDirectory + "/peer_config_star.txt");
        }

        try{
            //Reading the File
            FileReader configFileReader = new FileReader(configFile);

            //Reading text from the FileReader
            BufferedReader bufferedReader = new BufferedReader(configFileReader);

            String lines = new String();

            //Array for storing the information about the peer
            ArrayList<String> peerInfo = new ArrayList<String>();

            //Another list to store information about the neighbors
            ArrayList<String> peerNeighbors = new ArrayList<String>();

            String[] arrayForPeer;

            int counter = 0;

            while ((lines = bufferedReader.readLine())!= null){

                NeighborProperties neighbor_peer = new NeighborProperties();
                peerInfo.add(counter++, lines);
                arrayForPeer = lines.split(" ");

                if (arrayForPeer[0].equals(inputPeerId)){

                    peerID = arrayForPeer[0];
                    peerIPAddress = arrayForPeer[1];
                    peerPortNumber = arrayForPeer[2];

                    Collections.addAll(peerNeighbors, arrayForPeer[3].split("-"));
                    System.out.println("Entered Peer ID is: " +peerID + " " +peerIPAddress+ " " + peerPortNumber + "\n");
                    System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

                }

                neighbor_peer.setPeerId_Neighbor(arrayForPeer[0]);
                neighbor_peer.setPeerIpAddress_Neighbor(arrayForPeer[1]);
                neighbor_peer.setPeerPort_Neighbor((Integer.parseInt(arrayForPeer[2])));

                global_list_peers.add(neighbor_peer);

            }

            System.out.println("The neighbors are:\n");

            for (int i = 0; i< peerNeighbors.size(); i++){
                String[] peer;
                String[] neighbors;
                NeighborProperties neighbor_peer = new NeighborProperties();

                for (int j=0; j< peerInfo.size(); j++){

                    peer = peerInfo.get(j).split(" ");
                    neighbors = peerNeighbors.get(i).split(" ");

                    if (neighbors[0].trim().equals(peer[0].trim())){

                      neighbor_peer.setPeerId_Neighbor(peer[0]);
                      neighbor_peer.setPeerIpAddress_Neighbor(peer[1]);
                      neighbor_peer.setPeerPort_Neighbor(Integer.parseInt(peer[2]));

                      list_Neighbors.add(i, neighbor_peer);
                        System.out.println(neighbor_peer.getPeerId_Neighbor() + " " + neighbor_peer.getPeerIpAddress_Neighbor()+ " " + neighbor_peer.getPeerPort_Neighbor()+ "\n");

                    }
                }
                configFileReader.close();
                bufferedReader.close();
            }

        }
        catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }


    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    //Method to show all the files with a peer
    public void availableFilesAtPeers(){

        //Getting all the files from the folder.
        File filesAtPeers = new File(dataFolder);
        //Storing all files into an array.
        File[] listOfFiles = filesAtPeers.listFiles();

        //Taking files into the download folder also
        File fileAtDownloadFolder = new File(dataFolder + "/downloads");
        File[] listOfFilesInDownloads = fileAtDownloadFolder.listFiles();

        if ((listOfFiles != null && listOfFiles.length!= 0 )||(listOfFilesInDownloads != null && listOfFilesInDownloads.length != 0)){

            if (listOfFiles != null && listOfFiles.length!= 0 ){

                for (int i =0; i< listOfFiles.length; i++){

                    if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("txt")){
                        System.out.println("Files: " + listOfFiles[i].getName());
                    }
                }
            }

            if (listOfFilesInDownloads != null && listOfFilesInDownloads.length !=0){

                //read every file and print the names.
                for (int i =0; i< listOfFilesInDownloads.length; i++){

                    if (listOfFilesInDownloads[i].isFile() && listOfFilesInDownloads[i].getName().contains("txt")){
                        System.out.println("Files: " + listOfFilesInDownloads[i].getName());
                    }
                }
            }
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        }


    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/


    //To check the existence of the file
    public Boolean fileExistence(String par_fileName){
        //getting data folder path
        File var_dataFolder = new File(MainServices.dataFolder);
        //Getting files from the resource folder
        File[] listOfFiles = var_dataFolder.listFiles();

        for (int i=0; i< listOfFiles.length; i++){
            String var_fileName = listOfFiles[i].getName();
            if (var_fileName.startsWith(par_fileName)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public void registration(String par_fileName){

        if (fileExistence(par_fileName)){
            global_registered_files.add(par_fileName);
            File var_registeredFile = new File(dataFolder+"/" + par_fileName);

            //Using FileProperties to keep track of updates
            FileProperties var_fileProperties = new FileProperties();
            var_fileProperties.setFile(var_registeredFile);
            var_fileProperties.setLast_update(var_registeredFile.lastModified());
            var_fileProperties.setVersion_number(0);

            list_fileChanges.add(var_fileProperties);
            System.out.println("File " + var_registeredFile+ " has been registered successfully");
        }
        else{
            System.out.println("File does not exist in the local folder");
        }

    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    public void registration_AllFiles(){
        File var_dataFolder = new File(MainServices.dataFolder);
        File[] listOfFiles = var_dataFolder.listFiles();
        if (listOfFiles != null && listOfFiles.length !=0){

            for(int i=0; i<listOfFiles.length; i++){
                if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("txt")){
                    registration(listOfFiles[i].getName());
                }
            }
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    public void downloadFromPeer(){
        Scanner var_userInput = new Scanner(System.in);
        String var_fileName;
        String var_Ip = null;
        int var_peerPort =0;


        System.out.println("Enter the peer id and the file name using the format (peerID-fileName.txt):");
        String var_UserStr = var_userInput.nextLine();
        String[] var_IpArray = var_UserStr.split(Pattern.quote("-"));

        //if the length is not equal to 2
        if (var_IpArray.length !=2){
            System.out.println("Invalid filename.... Enter the correct name\n");
            System.out.println("The correct format is peerID-filename... For eg. a-a4.txt !\n");
            return;

        }
        var_fileName= var_IpArray[1];
        for (int i =0; i<global_list_peers.size(); i++){
            if (global_list_peers.get(i).getPeerId_Neighbor().equals(var_IpArray[0])){
                var_Ip = global_list_peers.get(i).getPeerIpAddress_Neighbor();
                var_peerPort = global_list_peers.get(i).getPeerPort_Neighbor();

            }
        }
        //Download the file only if it is available at other peer not locally
        if (!(var_peerPort == ClientServer.global_port)){
            try{
                Socket vSocket = new Socket(var_Ip, var_peerPort);
                ObjectOutputStream var_oos;
                System.out.println("\nConnected to Peer : " +var_Ip + "with port : " +var_peerPort + "\n");
                var_oos = new ObjectOutputStream(vSocket.getOutputStream());
                var_oos.flush();
                HitQueryProp var_msg = new HitQueryProp();
                var_msg.setPeer_Id(peerID);
                var_msg.setPeer_IpAddress(peerIPAddress);
                var_msg.setCommand_action(EnumCommand.DOWNLOAD);
                var_msg.setTtl(10);
                var_msg.setPeer_port(peerPortNumber);
                var_msg.setMessage_content(var_fileName.trim());

                var_oos.writeObject(var_msg);
                var_oos.flush();

                ObjectInputStream var_ois = new ObjectInputStream(vSocket.getInputStream());
                String var_fileContnt = var_ois.readObject().toString();
                if (var_fileContnt.trim().equals("File not found".trim())){
                    System.out.println("File not found");
                }
                else{
                    downloadToLocalPeer(var_fileName, var_fileContnt);
                    System.out.println(var_fileName+ " has been downloaded successfully\n");
                    global_registered_files.add(var_fileName);
                }
                var_ois.close();
                var_oos.close();
                vSocket.close();

            }catch(UnknownHostException ex){
                ex.printStackTrace();
            }catch(IOException ex){
                ex.printStackTrace();
            }catch(ClassNotFoundException ex){
                ex.printStackTrace();
            }
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    //method to download file to the local peer
    public void downloadToLocalPeer(String par_fileName, String par_fileContent){
        try{
            File var_File = new File(dataFolder);
            FileWriter var_writer = new FileWriter(var_File+ "/downloads"+"/"+par_fileName.trim(), true);
            var_writer.write(par_fileContent);
            var_writer.close();
        }
        catch (UnknownHostException ex){
            System.out.println("Host is not available..");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

   //Method to search for a file. takes the file name as the parameter
    public void searching(String par_fileName) {


        //Initializing an object of the HitQueryProp class
        HitQueryProp var_msg = new HitQueryProp();

        var_msg.setPeer_Id(peerID);                        //Setting peerId
        var_msg.setPeer_IpAddress(peerIPAddress);          //Setting peer IP Address
        var_msg.setPeer_port(peerPortNumber);              //Setting Peer Port number
        var_msg.setTtl(10);                                //Setting time to live as 10
        var_msg.setCommand_action(EnumCommand.SEARCH);     //Setting the action command which is SEARCH
        var_msg.setMessage_content(par_fileName.trim());   //Setting the message content
        var_msg.setUnique_key();                           //Setting the Unique Key
        System.out.println("Search message initiated for file "+par_fileName);

        broadcast(var_msg);                                //Setting the information to message object and passing it for broadcasting.
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

   //Method to check whether the file name is valid or not
    public Boolean validationFileName(String par_fileName){
        if (par_fileName.contains(".")){
            //Splitting the file name into parts
            String[] var_name = par_fileName.split(Pattern.quote("."));
            if (var_name.length ==2){
                //Checking for both ht parts are valid or not
                if (!(var_name[0].equals(null)) && !(var_name[1].equals(null))){
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    //Method to broadcast the message
    public void broadcast(HitQueryProp par_msg) {
        boolean var_flag = Boolean.TRUE;

        if(var_flag) {
            //Add the message object to received message list to keep a track of messages invoked
            message_received.add(par_msg);
            if(par_msg.getTtl() > 0) {
                par_msg.setTtl(Boolean.FALSE);
                par_msg.addHeader(peerID);
                ObjectOutputStream var_msgToSend = null;
                for (int i = 0; i < list_Neighbors.size(); i++) {

                    try {
                        //Socket connection is created by passing the neighbor peer ip and port.
                     Socket vSocket = new Socket(list_Neighbors.get(i).getPeerIpAddress_Neighbor(), list_Neighbors.get(i).getPeerPort_Neighbor());

                        var_msgToSend = new ObjectOutputStream(vSocket.getOutputStream());

                        var_msgToSend.flush();
                        var_msgToSend.writeObject(par_msg);
                        var_msgToSend.flush();
                        var_msgToSend.close();
                        System.out.println("Broadcast message is sent to " + list_Neighbors.get(i).getPeerIpAddress_Neighbor() + "-" + list_Neighbors.get(i).getPeerPort_Neighbor() + "!\n");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();

                    } catch (IOException e) {
                       e.printStackTrace();
                        System.out.println("Broadcast message is sent to " + list_Neighbors.get(i).getPeerIpAddress_Neighbor() + "-" + list_Neighbors.get(i).getPeerPort_Neighbor() + "!\n");


                    }
                }
            } else {
                System.out.println(par_msg.getPeer_Id()+" message cannot be processed because the Time To Live exceeded the maximum limit!");
            }
        } else {
            System.out.println(par_msg.getPeer_Id()+" message cannot be processed because it is duplicate !");
        }
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/


    //Method to push the query
    public void push(String par_fileName, int par_version) {
        HitQueryProp var_msg = new HitQueryProp();
        var_msg.setPeer_Id(peerID);
        var_msg.setPeer_IpAddress(peerIPAddress);
        var_msg.setCommand_action(EnumCommand.INVALID);
        var_msg.setTtl(10);
        var_msg.setPeer_port(peerPortNumber);
        var_msg.setMessage_data(par_fileName.trim());
        var_msg.setFile_Version(par_version);
        var_msg.setUnique_key();

        System.out.println("Invalid query is created for file "+par_fileName +" with version number "+var_msg.getFile_Version());

        //For broadcasting th message on the console
        broadcast(var_msg);
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/


    //To calculate the performance of the search query
    public Long query_performance(String par_fileName, int par_loop){

        long var_sum=0;
        System.out.println("Start of calculation of time taken by Search Query !!!");
        for(int i = 0 ; i < par_loop ; i++) {
            long startTime = System.currentTimeMillis();

            //Invoke search method of utility class.
            searching(par_fileName);
            var_sum = var_sum + System.currentTimeMillis() - startTime;
            System.out.println("Calculation for the search query time calculation ended !!!");
        }
        var_sum = var_sum/par_loop;
        return var_sum;
    }

}



