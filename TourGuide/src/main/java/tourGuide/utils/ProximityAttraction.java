package tourGuide.utils;

import gpsUtil.location.Location;

public class ProximityAttraction {
	
	public ProximityAttraction(String name, double attractionLatitude, double attractionLongitude, Location userLocation, double distance, int rewardPoints) {
		this.name = name;
		this.attractionLatitude = attractionLatitude;
		this.attractionLongitude = attractionLongitude;
		this.userLocation = userLocation;
		this.distance = distance;
		this.rewardPoints = rewardPoints;
	}
	
	private String name;
	private double attractionLatitude;
	private double attractionLongitude;
	private Location userLocation;
	private double distance;
	private int rewardPoints;
	// Name of Tourist attraction -> OK 
    // Tourist attraction lat/long -> OK 
    // The user's location lat/long -> OK
    // The distance in miles between the user's location and each of the attractions -> OK
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
	
	public String getProximityAttractionName() {
		return name;
	}
	
	public double getProximityAttractionLatitude() {
		return attractionLatitude;
	}
	
	public double getProximityAttractionLongitude() {
		return attractionLongitude;
	}
	
	public Location getUserLocation() {
		return userLocation;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public int getRewardPoints() {
		return rewardPoints;
	}
}
