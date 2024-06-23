package be.kuleuven.distributedsystems.cloud;

import be.kuleuven.distributedsystems.cloud.entities.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class Model {
    @Resource(name = "webClientBuilder")
    WebClient.Builder webClientBuilder;



    public List<Show> getShows() {

        List<Show> shows = new ArrayList(webClientBuilder.baseUrl("https://reliabletheatrecompany.com/" ).build()
                .get().uri(uriBuilder -> uriBuilder.pathSegment("shows")
                        .queryParam("key","wCIoTqec6vGJijW2meeqSokanZuqOL").build())
                .retrieve().bodyToMono(new ParameterizedTypeReference<CollectionModel<Show>>() {
                }).block().getContent());

        // TODO: return all shows
        return shows;
    }

    public Show getShow(String company, UUID showId) {
        // TODO: return the given show
        return null;
    }



    public List<LocalDateTime> getShowTimes(String company, UUID showId) {
        // TODO: return a list with all possible times for the given show
        return new ArrayList<>();
    }

    public List<Seat> getAvailableSeats(String company, UUID showId, LocalDateTime time) {
        // TODO: return all available seats for a given show and time
        return new ArrayList<>();
    }

    public Seat getSeat(String company, UUID showId, UUID seatId) {
        // TODO: return the given seat
        return null;
    }

    public Ticket getTicket(String company, UUID showId, UUID seatId) {
        // TODO: return the ticket for the given seat
        return null;
    }

    public List<Booking> getBookings(String customer) {
        // TODO: return all bookings from the customer
        return new ArrayList<>();
    }

    public List<Booking> getAllBookings() {
        // TODO: return all bookings
        return new ArrayList<>();
    }

    public Set<String> getBestCustomers() {
        // TODO: return the best customer (highest number of tickets, return all of them if multiple customers have an equal amount)
        return null;
    }

    public void confirmQuotes(List<Quote> quotes, String customer) {
        // TODO: reserve all seats for the given quotes
    }
}
