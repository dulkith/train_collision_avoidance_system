package lk.edu.nchs.traincollisionavoidancesystem.m_Model;

/**
 * Created by Oclemy on 6/21/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 */
public class DoorStatusHistory {
    String status;
    long timestamp;

    public DoorStatusHistory() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
