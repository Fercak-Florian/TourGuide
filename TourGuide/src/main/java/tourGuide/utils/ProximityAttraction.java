package tourGuide.utils;

import gpsUtil.location.Location;

public class ProximityAttraction {
	
	public ProximityAttraction(String name, double attractionLatitude, double attractionLongitude, Location userLocation, double distance, int rewardPoints) {
		this.name = name;
		this.attractionLocation = new Location(attractionLatitude, attractionLongitude);
		this.userLocation = userLocation;
		this.distance = distance;
		this.rewardPoints = rewardPoints;
	}
	
	private String name;
	private Location attractionLocation;
	private Location userLocation;
	private double distance;
	private int rewardPoints;
	
	public String getProximityAttractionName() {
		return name;
	}
	
	public Location getAttractionLocation() {
		return this.attractionLocation;
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