package be.kuleuven.distributedsystems.cloud;

import be.kuleuven.distributedsystems.cloud.entities.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.ByteArray;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class Model {
    @Resource(name = "webClientBuilder")
    WebClient.Builder webClientBuilder;

    // This way the bookings will be available only in one session?????
    List<Booking> bookings = new ArrayList<>();
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
        Show show = webClientBuilder.baseUrl("https://"+company).build().get().uri(uriBuilder ->
                uriBuilder.pathSegment("/shows/"+showId)
                        .queryParam("key","wCIoTqec6vGJijW2meeqSokanZuqOL").build()).retrieve()
                .bodyToMono(Show.class).block();
        return show;
    }



    public List<LocalDateTime> getShowTimes(String company, UUID showId) {
        // TODO: return a list with all possible times for the given show
        List<LocalDateTime> l = new ArrayList(webClientBuilder.baseUrl("https://"+company).build().get().uri(uriBuilder ->
                uriBuilder.pathSegment("/shows/"+showId+"/times")
                        .queryParam("key","wCIoTqec6vGJijW2meeqSokanZuqOL").build()).retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<LocalDateTime>>() {
                }).block().getContent());
        return l;
    }

    public List<Seat> getAvailableSeats(String company, UUID showId, LocalDateTime time) {
        // TODO: return all available seats for a given show and time
        List<Seat> l = new ArrayList(webClientBuilder.baseUrl("https://"+company).build().get().uri(uriBuilder ->
                uriBuilder.pathSegment("/shows/"+showId+"/seats")
                        .queryParam("key","wCIoTqec6vGJijW2meeqSokanZuqOL").queryParam("time",time)
                        .queryParam("available",true).build()).retrieve()
                .bodyToMono(new ParameterizedTypeReference<CollectionModel<Seat>>() {
                }).block().getContent());
        return l;
    }
    //https://reliabletheatrecompany.com/shows/d843d1cb-bbee-4423-b444-eed26a7d5d12/seats?time=2021-12-20T15:00:00&available=true&key=wCIoTqec6vGJijW2meeqSokanZuqOL
    //url for getting seat of mamamia at 20 december 15:00
    public Seat getSeat(String company, UUID showId, UUID seatId) {
        try{
            Seat s = webClientBuilder.baseUrl("https://"+company).build().get().uri(uriBuilder ->
                    uriBuilder.pathSegment("shows").pathSegment(showId.toString())
                            .pathSegment("seats").pathSegment(seatId.toString())
                            .queryParam("key","wCIoTqec6vGJijW2meeqSokanZuqOL").build()).retrieve()
                    .bodyToMono(Seat.class).block();
            return s;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Ticket getTicket(String company, UUID showId, UUID seatId) {
        // TODO: return the ticket for the given seat

        try{
            Ticket t = webClientBuilder.baseUrl("https://" + company).build().get().uri(uriBuilder ->
                    uriBuilder.pathSegment("/shows"  ).pathSegment(showId.toString())
                            .pathSegment("seats").pathSegment(seatId.toString())
                            .pathSegment("ticket")
                            .queryParam("key", "wCIoTqec6vGJijW2meeqSokanZuqOL")
                            .build()).retrieve()
                    .bodyToMono(Ticket.class).block();
            return t;
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public List<Booking> getBookings(String customer) {
        // TODO: return all bookings from the customer
        List<Booking> b = new ArrayList<>();
        for(Booking booking:bookings){
            if(booking.getCustomer()==customer){
                b.add(booking);
            }
        }
        return b;
    }

    public List<Booking> getAllBookings() {
        // TODO: return all bookings
        return bookings;
    }

    public Set<String> getBestCustomers() {
        // TODO: return the best customer (highest number of tickets, return all of them if multiple customers have an equal amount)
        return null;
    }

    public void confirmQuotes(List<Quote> quotes, String customer) throws IOException {
        // TODO: reserve all seats for the given quotes
        List<Ticket> tickets = new ArrayList<>();
        List<String> l = new ArrayList<>();
        l.add(customer);
        try {
            for (Quote q : quotes) {
                // have to do the all or none semantic....
                /**
                Ticket t =webClientBuilder.baseUrl("https://" + q.getCompany()).build().put().uri(uriBuilder ->
                        uriBuilder.pathSegment("shows").pathSegment(q.getShowId().toString())
                                .pathSegment("seats").pathSegment(q.getSeatId().toString())
                                .pathSegment("ticket")
                                .queryParam("key", "wCIoTqec6vGJijW2meeqSokanZuqOL")
                                .queryParam("customer", customer).build()).retrieve()
                        .bodyToMono(Ticket.class).block();
                tickets.add(t);
                System.out.println(t.getCompany());**/
                l.add("");
                l.add(q.getCompany());
                l.add(q.getShowId().toString());
                l.add(q.getSeatId().toString());
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        TopicName topicName = TopicName.of("demo-distributed-systems-kul", "Booking");

        Publisher publisher = null;
        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();

            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream =
                    new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(l);
            objectOutputStream.flush();
            objectOutputStream.close();
            final byte[] byteArray = byteArrayOutputStream.toByteArray();

            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(ByteString.copyFrom(byteArray)).build();

            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
        }finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                //publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }



        //Booking b =new Booking(UUID.randomUUID(),LocalDateTime.now(),tickets,customer);
        //bookings.add(b);
    }
}
