import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;


/**
 * @author Abdullah Ejaz
 *
 */

public class ClientServer implements Runnable{

    static int global_port;
    static String topology;
    public static boolean flag = Boolean.FALSE;

    public ClientServer(int newPort) {
        this.global_port = newPort;
    }

/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    private static String dataFromTopologyFile(){


        File var_file = new File(MainServices.workspaceDirectory + "/topology_config.properties");
        FileInputStream fis;
        String var_topology  = null;

        try {
            fis = new FileInputStream(var_file);
            Properties var_prop = new Properties();
            var_prop.load(fis);
            fis.close();

            Enumeration var_enumKeys = var_prop.keys();
            while (var_enumKeys.hasMoreElements()) {
                String var_key = (String) var_enumKeys.nextElement();
                String var_value = var_prop.getProperty(var_key);
                System.out.println(var_key + ": " + var_value);
                var_topology = var_value;
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return var_topology;
    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    //For checking the validity of the files for the peers

    public static void checkFileValidity() {
        while(true) {
            System.out.println("Checking the validity of local files at the peer !\n");

            File vPeerFile = new File(MainServices.dataFolder);
            //Stores file into file array
            File[] listOfFiles = vPeerFile.listFiles();

            if (listOfFiles != null || !listOfFiles.equals(null)) {

                for (int i = 0 ; i < listOfFiles.length ; i++) {

                    if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("txt")) {

                        for (int j = 0; j < MainServices.list_fileChanges.size() ; j++) {

                            if (listOfFiles[i].getName().trim().equals(MainServices.list_fileChanges.get(j).file.getName().trim())) {

                                //IF last modified and last updated files does not match, that means invalid copying
                                if (listOfFiles[i].lastModified() != MainServices.list_fileChanges.get(j).getLast_update()) {

                                    System.out.println("Copying of invalid file has been detected "+listOfFiles[i].getName()+"\n");

                                    MainServices.list_fileChanges.get(j).setLast_update(listOfFiles[i].lastModified());
                                    MainServices.list_fileChanges.get(j).setVersion_number();

                                    //Invoking push to copy the updated files
                                    new MainServices().push(listOfFiles[i].getName().trim(), MainServices.list_fileChanges.get(j).getVersion_number());
                                }
                            }
                        }
                    }
                }
            }
            try {
                Thread.sleep(100000);
            }
            catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }



/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    public void run() {

        if (!flag) {
            flag = Boolean.TRUE;
            checkFileValidity();
        } else {
            try {
                //Initialize a socket to listen to specific port.
                ServerSocket ss = new ServerSocket(global_port);
                while (true) {
                    Socket vSocket = null;

                    //Accept a peer connection.
                    vSocket = ss.accept();
                    //Create a new thread for every new connection.
                    new Thread(new Listener(vSocket, global_port)).start();
                }
            } catch (UnknownHostException pUnknownHost) {
                System.err.println("Host is not available..!");
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


/*******************************************************************************************************************************************************************************/
/*******************************************************************************************************************************************************************************/

    public static void main(String[] args) {
        String var_userInput = null;
        Thread vThread1,vThread2;
        //This method will read topology_config file and set the type of topology (star or 2dmesh).
        topology = dataFromTopologyFile();
        if(topology != null) {
            if(topology.equalsIgnoreCase("mesh"))
                System.out.println("Set up a peer by entering the PEER ID (a, b, c, d, e, f, g, h, i) :");
            else
                System.out.println("Set up a peer by entering the PEER ID (a, b, c, d, e, f, g, h, i, j) :");
            //By default consider star topology
        } else {
            topology = "star";
            System.out.println("Set up a peer by entering the PEER ID  (a, b, c, d, e, f, g, h, i, j) :");
        }

        Scanner vCmdInput1 = new Scanner(System.in);
        String var_input = vCmdInput1.nextLine();
        //Pass the topology type read from config file along with user input
        MainServices.configFileRead(var_input, topology);
        MainServices.dataFolder+="/"+var_input+"/";
        System.out.println("Your files are in:"+ MainServices.dataFolder+ "location");
        System.out.println("*********************************************");

        System.out.println("\nWaiting for peers to download files..");
        vThread1 = new Thread (new ClientServer(Integer.parseInt(MainServices.peerPortNumber)));
        vThread1.start();
        vThread2 = new Thread(new ClientServer(Integer.parseInt(MainServices.peerPortNumber)));
        vThread2.start();

        while (true) {
            System.out.println("*******************************************************");
            System.out.println("Enter 1 : Register a file.");
            System.out.println("Enter 2 : Register all files of the working directory.");
            System.out.println("Enter 3 : Search a file on peers.");
            System.out.println("Enter 4 : Download file from a peer.");
            System.out.println("Enter 5 : List my files of the current directory.");
            System.out.println("Enter 6 : Calculate the performance of search requests.");
            System.out.println("Enter 7 : To exit the program.");
            System.out.println("*******************************************************");

            //Creating an object for the MainServices class.
            MainServices vUtility = new MainServices();

            //Read the service action from the user.
            Scanner var_cmdInput2 = new Scanner(System.in);
            var_userInput = var_cmdInput2.nextLine();


            switch (var_userInput){

                case "1":

                    System.out.println("Enter a valid file name along with the file extension");
                    //Read file name with extension
                    var_userInput = var_cmdInput2.nextLine();
                    //Test if file name is valid
                    if(vUtility.validationFileName(var_userInput))
                        vUtility.registration(var_userInput);
                    else
                        System.out.println("File format is incorrect ! Please enter again");
                    break;

                case "2":

                    vUtility.registration_AllFiles();
                    break;

                case "3":

                    System.out.println("Enter a valid file name along with the file extension i.e (.txt) ");
                    //Reads the file name for search
                    var_userInput = var_cmdInput2.nextLine();
                    //Checks if the file name is valid
                    if(vUtility.validationFileName(var_userInput)) {
                       vUtility.searching(var_userInput);
                    } else
                        System.out.println("File format is incorrect !");
                    break;

                case "4":

                    vUtility.downloadFromPeer();
                    break;

                case "5":

                    System.out.println("List Of Files located in:"+ MainServices.dataFolder);
                    vUtility.availableFilesAtPeers();

                    break;

                case "6":

                    System.out.println("Enter a valid file name along with the file extension (.txt)");
                    var_userInput = var_cmdInput2.nextLine();
                    if(vUtility.validationFileName(var_userInput)) {
                        System.out.println("Enter the number of requests you want to process");
                        int vLoop ;
                        if(var_cmdInput2.hasNextInt()) {
                            vLoop = var_cmdInput2.nextInt();
                            System.out.println("Test time: "+System.currentTimeMillis());
                            System.out.println(vLoop+" search requests average rate is " + vUtility.query_performance(var_userInput.trim(), vLoop) + " ms.");
                        } else {
                            System.out.println("The integer input is in wrong format !");
                        }
                    } else {
                        System.out.println("The entered file name is in wrong format !");
                    }
                    break;

                case "7":

                    System.out.println("Exiting Program !!!");
                    System.out.println("Good Bye.. Have a nice one..!!!");
                    System.exit(0);
                    break;


                default:

                    System.out.println("Wrong input! Valid inputs are 1, 2, 3, 4, 5, 6, 7 \n");
                    System.out.println("Please Try Again !!");
                    break;

            }


        }
    }



}
