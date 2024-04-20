package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Likes {

    @Id
    String likeId;
    String shortId;
    String userId;

    public Likes(){}

    public Likes(String likeId, String shortId, String userId){

        this.likeId = likeId;
        this.shortId = shortId;
        this.userId = userId;
    }

    public String getLikeId(){
        return likeId;
    }

    public void setLikeId(String likeId){
        this.likeId = likeId;
    }

    public String getShortId() {
		return shortId;
	}
	public void setShortId(String shortId) {
		this.shortId = shortId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}

    @Override
    public String toString() {
		return "Likes [likeId="+likeId+", downerId=" + shortId + ", userId="+ userId + "]";
	}
}
