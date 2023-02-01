package tourGuide.utils;

import java.util.UUID;
import gpsUtil.location.Location;

public class UserMostRecentLocation {

	private UUID userId;
	private Location location;
	
	public UserMostRecentLocation(UUID userId, Location location) {
		this.userId = userId;
		this.location = new Location(location.latitude, location.longitude);
	}
	
	public UUID getUserId() {
		return this.userId;
	}
	
	public Location getUserLocation() {
		return this.location;
	}
}