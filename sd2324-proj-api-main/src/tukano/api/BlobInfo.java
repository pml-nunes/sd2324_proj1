package tukano.api;


import java.net.URI;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class BlobInfo {
	@Id
	private int blobIndex;
	private String blobURI;
	private int currentLoad;


	public BlobInfo() {}
	
	public BlobInfo(int blobIndex, String blobURI, int currentLoad) {
		this.blobIndex = blobIndex;
		this.blobURI = blobURI;
		this.currentLoad = currentLoad;
	}

	public int getBlobIndex() {
		return this.blobIndex;
	}
	public void setBlobIndex(int blobIndex) {
		this.blobIndex = blobIndex;
	}

	public String getBlobURI() {
		return this.blobURI;
	}
	public void setBlobURI(String blobURI) {
		this.blobURI = blobURI;
	}

	public int getCurrentLoad() {
		return currentLoad;
	}
	public void setCurrentLoad(int currentLoad) {
		this.currentLoad = currentLoad;
	}
	
	public int blobIndex() {
		return blobIndex;
	}

	public String blobURI() {
		return blobURI;
	}
	
	public int currentLoad() {
		return currentLoad;
	}
	
	@Override
	public String toString() {
		return "BlobInfo [blobIndex=" + blobIndex + ", blobURI=" + blobURI + ", currentLoad=" + currentLoad + "]";
	}
}
