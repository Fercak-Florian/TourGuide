package tourGuide.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jsoniter.output.JsonStream;
import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tourGuide.utils.ProximityAttraction;
import tourGuide.utils.UserMostRecentLocation;
import tripPricer.Provider;

/**
 * This class contains the endpoints called by the front-end API of TourGuide.
 * It allows to obtain information about the user such as his position, his places visited, his rewards.
 */
@RestController
public class TourGuideController {
    TourGuideService tourGuideService;
    UserService userService;

    public TourGuideController(TourGuideService tourGuideService, UserService userService) {
        this.tourGuideService = tourGuideService;
        this.userService = userService;
    }

    /**
     * This method gets a welcome message
     *
     * @return a String containing the welcome message
     */
    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * This method gets the user GPS position
     *
     * @param userName of type String
     * @return a String with the latitude and longitude
     */
    @GetMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
        if (visitedLocation == null) {
            return "{}";
        } else {
            return JsonStream.serialize(visitedLocation.location);
        }
    }

    /**
     * This method gets the 5 closest attractions from the user GPS position
     *
     * @param userName of type String
     * @return List of ProximityAttraction, each element of the list contains the attraction GPS position,
     * the GPS user position, the distance in miles between the user and the attraction,
     * the user rewards points and the attraction name.
     */
    @GetMapping("/getNearbyAttractions")
    public List<ProximityAttraction> getNearbyAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(userService.getUser(userName));
        return tourGuideService.getNearByAttractions(visitedLocation);
    }

    /**
     * This method gets the user rewards
     *
     * @param userName of type String
     * @return a List of UserRewards, each element of the list contains a visitedLocation,
     * an attraction and rewards points.
     */
    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        User user = userService.getUser(userName);
        return (user.getUserRewards());

    }

    /**
     * This method gets all the most recent user GPS position
     *
     * @return a List of UserMostRecentLocation, each element of the list contains a user id
     * and the last user GPS position
     */
    @GetMapping("/getAllCurrentLocations")
    public List<UserMostRecentLocation> getAllCurrentLocations() {
        return tourGuideService.getAllUsersMostRecentLocation();
    }

    /**
     * This method gets the user trip deals
     *
     * @param userName of type String
     * @return a List of Provider, each element of the list contains a travel provider name,
     * the travel price and a trip id
     */
    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        return tourGuideService.getTripDeals(userService.getUser(userName));
    }
}