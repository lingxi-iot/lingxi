package cn.gofree.lingxi.eventcenter.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageQueue implements Comparable<MessageQueue>, Serializable {
    private static final long seriaVersionUID= 9023490920234L;
    private String topic;
    private String brokerName;
    private int queueId;
    public MessageQueue(){}
    public MessageQueue(MessageQueue other){
        this.topic=other.topic;
        this.brokerName=other.brokerName;
        this.queueId =other.queueId;
    }
    public MessageQueue(String topic, String brokerName, int queueId) {
        this.topic = topic;
        this.brokerName = brokerName;
        this.queueId = queueId;
    }


    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            MessageQueue other = (MessageQueue)obj;
            if (this.brokerName == null) {
                if (other.brokerName != null) {
                    return false;
                }
            } else if (!this.brokerName.equals(other.brokerName)) {
                return false;
            }

            if (this.queueId != other.queueId) {
                return false;
            } else {
                if (this.topic == null) {
                    if (other.topic != null) {
                        return false;
                    }
                } else if (!this.topic.equals(other.topic)) {
                    return false;
                }

                return true;
            }
        }
    }

    public String toString() {
        return "MessageQueue [topic=" + this.topic + ", brokerName=" + this.brokerName + ", queueId=" + this.queueId + "]";
    }

    @Override
    public int compareTo(MessageQueue o) {
        int result = this.topic.compareTo(o.topic);
        if (result != 0) {
            return result;
        } else {
            result = this.brokerName.compareTo(o.brokerName);
            return result != 0 ? result : this.queueId - o.queueId;
        }
    }
}
