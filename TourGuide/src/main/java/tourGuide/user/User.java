package tourGuide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

public class User {
	private final UUID USER_ID;
	private final String USER_NAME;
	private String phoneNumber;
	private String emailAddress;
	private Date latestLocationTimestamp;
	private List<VisitedLocation> visitedLocations = new ArrayList<>();
	private List<UserReward> userRewards = new ArrayList<>();
	private UserPreferences userPreferences = new UserPreferences();
	private List<Provider> tripDeals = new ArrayList<>();
	private ReentrantLock lock = new ReentrantLock();

	public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
		this.USER_ID = userId;
		this.USER_NAME = userName;
		this.phoneNumber = phoneNumber;
		this.emailAddress = emailAddress;
	}

	public UUID getUserId() {
		return USER_ID;
	}

	public String getUserName() {
		return USER_NAME;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
		this.latestLocationTimestamp = latestLocationTimestamp;
	}

	public Date getLatestLocationTimestamp() {
		return latestLocationTimestamp;
	}

	public void addToVisitedLocations(VisitedLocation visitedLocation) {
		lock.lock();
		try {
			visitedLocations.add(visitedLocation);
		} finally {
			lock.unlock();
		}
	}

	public List<VisitedLocation> getVisitedLocations() {
		lock.lock();
		try {
			return visitedLocations;
		} finally {
			lock.unlock();
		}
	}

	public void clearVisitedLocations() {
		visitedLocations.clear();
	}

	public void addUserReward(UserReward userReward) {
		lock.lock();
		try {
			if (userRewards.stream().filter(r -> r.ATTRACTION.attractionName.equals(userReward.ATTRACTION.attractionName))
					.count() == 0) {
				userRewards.add(userReward);
			}
		} finally {
			lock.unlock();
		}
	}

	public List<UserReward> getUserRewards() {
		lock.lock();
		try {
			return userRewards;
		} finally {
			lock.unlock();
		}
	}

	public UserPreferences getUserPreferences() {
		return userPreferences;
	}

	public void setUserPreferences(UserPreferences userPreferences) {
		this.userPreferences = userPreferences;
	}

	public VisitedLocation getLastVisitedLocation() {
		return visitedLocations.get(visitedLocations.size() - 1);
	}

	public void setTripDeals(List<Provider> tripDeals) {
		this.tripDeals = tripDeals;
	}

	public List<Provider> getTripDeals() {
		return tripDeals;
	}
}
