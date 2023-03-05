package tourGuide.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jsoniter.output.JsonStream;
import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.utils.ProximityAttraction;
import tourGuide.utils.UserMostRecentLocation;
import tripPricer.Provider;

@RestController
public class TourGuideController {
	TourGuideService tourGuideService;
	UserService userService;

	public TourGuideController(TourGuideService tourGuideService, UserService userService){
		this.tourGuideService = tourGuideService;
		this.userService = userService;
	}

	@GetMapping("/")
	public String index() {
		return "Greetings from TourGuide!";
	}

	@GetMapping("/getLocation")
	public String getLocation(@RequestParam String userName) {
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
		if(visitedLocation == null) {
			return "{}";
		} else {
			return JsonStream.serialize(visitedLocation.location);
		}
	}

	@GetMapping("/getNearbyAttractions")
	public List<ProximityAttraction> getNearbyAttractions(@RequestParam String userName) {
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
		return tourGuideService.getNearByAttractions(visitedLocation);
	}

	@GetMapping("/getRewards")
	public String getRewards(@RequestParam String userName) {
		User user = userService.getUser(userName);
		return JsonStream.serialize(user.getUserRewards());

	}

	@GetMapping("/getAllCurrentLocations")
	public List<UserMostRecentLocation> getAllCurrentLocations() {
		return tourGuideService.getAllUsersMostRecentLocation();
	}

	@GetMapping("/getTripDeals")
	public String getTripDeals(@RequestParam String userName) {
		List<Provider> providers = tourGuideService.getTripDeals(userService.getUser(userName));
		return JsonStream.serialize(providers);
	}
}