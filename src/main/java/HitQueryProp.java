import java.io.Serializable;

/**
 * @author Abdullah Ejaz
 *
 */


public class HitQueryProp implements Serializable {

    private static final long version = 1L;
    private String peer_IpAddress;
    private String peer_Id;
    //ttl here is Time To Live
    private Integer ttl = 0;
    private String peer_port;
    private EnumCommand command_action;
    private String message_data;
    protected String message_content;
    protected String message_header;
    private String unique_key;
    private Integer file_Version = 0;

    public String getPeer_IpAddress() {
        return peer_IpAddress;
    }

    public void setPeer_IpAddress(String peer_IpAddress) {
        this.peer_IpAddress = peer_IpAddress;
    }

    public String getPeer_Id() {
        return peer_Id;
    }

    public void setPeer_Id(String peer_Id) {
        this.peer_Id = peer_Id;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public String getPeer_port() {
        return peer_port;
    }

    public void setPeer_port(String peer_port) {
        this.peer_port = peer_port;
    }

    public String getMessage_data() {
        return message_data;
    }

    public void setMessage_data(String message_data) {
        this.message_data = message_data;
    }

    public String getMessage_content() {
        return message_content;
    }

    public void setMessage_content(String message_content) {
        this.message_content = message_content;
    }

    public String getMessage_header() {
        return message_header;
    }

    public void setMessage_header(String message_header) {
        this.message_header = message_header;
    }

    public String getUnique_key() {
        return unique_key;
    }

    public void setUnique_key() {
        unique_key = MainServices.peerID + System.currentTimeMillis();
    }

    public Integer getFile_Version() {
        return file_Version;
    }

    public void setFile_Version(Integer file_Version) {
        this.file_Version += file_Version;
    }

    public EnumCommand getCommand_action() {
        return command_action;
    }

    public void setCommand_action(EnumCommand command_action) {
        this.command_action = command_action;
    }

    public void setTtl(Boolean val) {
        if (val == Boolean.TRUE)
            ttl++;
        else if (val == Boolean.FALSE)
            ttl--;
    }

    public void addHeader(String id) {
        message_header = message_header+"-"+id;
    }

    public void removeHeader() {
        String [] content= message_header.split("-");
        message_header = null;
        for (int i = 0 ; i < content.length - 1 ; i++) {
            message_header = message_header + "-" + content[i];
        }
    }

    public void add_header(String id) {
        message_header = message_header + "-" + id;
    }

}

