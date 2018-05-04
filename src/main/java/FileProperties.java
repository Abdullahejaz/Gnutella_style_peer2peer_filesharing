import java.io.File;


/**
 * @author Abdullah Ejaz
 *
 */


public class FileProperties {

    File file;
    Long last_update;
    Integer version_number;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Long getLast_update() {
        return last_update;
    }

    public void setLast_update(Long last_update) {
        this.last_update = last_update;
    }

    public Integer getVersion_number() {
        return version_number;
    }

    public void setVersion_number(){
        this.version_number++;
    }

    public void setVersion_number(Integer version_number) {
        this.version_number = version_number;
    }


}
