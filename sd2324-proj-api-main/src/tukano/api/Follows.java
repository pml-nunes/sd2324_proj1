package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Follows {

    @Id
    String followId;
    String ownerId;
    String userId;

    public Follows(){}

    public Follows(String followId, String ownerId, String userId){

        this.followId = followId;
        this.ownerId = ownerId;
        this.userId = userId;
    }

    public String getFollowId(){
        return followId;
    }

    public void setFollowId(String followId){
        this.followId = followId;
    }

    public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

    @Override
    public String toString() {
		return "Follow [followId="+ followId +", ownerId=" + ownerId + ", userId="+ userId + "]";
	}
}
