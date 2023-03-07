package tourGuide.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TourGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Greetings from TourGuide!")));
    }

    @Test
    public void testGetLocation() throws Exception {
        mockMvc.perform(get("/getLocation")
                        .param("userName", "internalUser1"))
                .andExpect(content().string(containsString("latitude")));
    }
    @Test
    public void testGetNearbyAttraction() throws Exception {
        mockMvc.perform(get("/getNearbyAttractions")
                        .param("userName", "internalUser1"))
                .andExpect(content().string(containsString("proximityAttractionName")));
    }

    @Test
    public void testGetRewards() throws Exception {
        mockMvc.perform(get("/getRewards")
                        .param("userName", "internalUser1"))
                .andExpect(content().string(containsString("[]")));
    }
    @Test
    public void testGetAllCurrentLocations() throws Exception {
        mockMvc.perform(get("/getAllCurrentLocations")
                        .param("userName", "internalUser1"))
                .andExpect(content().string(containsString("latitude")));
    }

    @Test
    public void testGetTripDeals() throws Exception {
        mockMvc.perform(get("/getTripDeals")
                        .param("userName", "internalUser1"))
                .andExpect(content().string(containsString("price")));
    }
}