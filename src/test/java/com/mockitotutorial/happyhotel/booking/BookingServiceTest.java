package com.mockitotutorial.happyhotel.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingService bookingService;
    private PaymentService paymentServiceMock;
    private RoomService roomServiceMock;
    private BookingDAO bookingDAOMock;
    private MailSender mailSenderMock;

    @BeforeEach
    void setup() {
        this.paymentServiceMock = mock(PaymentService.class);
        this.roomServiceMock = mock(RoomService.class);
        this.bookingDAOMock = mock(BookingDAO.class);
        this.mailSenderMock = mock(MailSender.class);
        this.bookingService = new BookingService(paymentServiceMock, roomServiceMock, bookingDAOMock, mailSenderMock);

    }

    @Test
    void should_CalculateCorrectPrice_When_CorrectInput() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);
        double expected = 4 * 2 * 50.0;

        //when
        double actual = bookingService.calculatePrice(bookingRequest);

        //then
        assertEquals(expected, actual);
    }

    @Test
    void should_CountAvailablePlaces() {
        //given
        int expected = 0;
        //when
        int actual = bookingService.getAvailablePlaceCount();

        //then
        assertEquals(expected, actual);
    }

    @Test
    void shouldCountAvailablePlaces_When_OneRoomAvailable() {
        //given
        when(this.roomServiceMock.getAvailableRooms())
                .thenReturn(Collections.singletonList(new Room("Room 1", 2)));
        int expected = 2;
        //when
        int actual = bookingService.getAvailablePlaceCount();
        //then
        assertEquals(expected, actual);
    }

    @Test
    void shouldCountAvailavlePlaces_When_MultipleRoomsAvailable() {
        //given
        List<Room> rooms = Arrays.asList(new Room("Room 1", 2), new Room("Room 2", 5));
        when(this.roomServiceMock.getAvailableRooms())
                .thenReturn(rooms);
        int expected = 7;
        //when
        int actual = bookingService.getAvailablePlaceCount();
        //then
        assertEquals(expected, actual);
    }


    @Test
    void shouldCountAvailablePlaces_When_CalledMultipleTimes() {
        //given
        when(this.roomServiceMock.getAvailableRooms())
                .thenReturn(Collections.singletonList(new Room("Room 1",5)))
                .thenReturn(Collections.emptyList());
        int expectedFirstCall= 5;
        int expectedSecondCall = 0;
        //when
        int actualFirst = bookingService.getAvailablePlaceCount();
        int actualSecond = bookingService.getAvailablePlaceCount();

        //then
        assertAll(
                ()->assertEquals(expectedFirstCall,actualFirst),
                ()->assertEquals(expectedSecondCall,actualSecond)
        );


    }
}