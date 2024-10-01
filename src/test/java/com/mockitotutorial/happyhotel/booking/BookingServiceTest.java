package com.mockitotutorial.happyhotel.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingService bookingService;
    private PaymentService paymentServiceMock;
    private RoomService roomServiceMock;
    private BookingDAO bookingDAOMock;
    private MailSender mailSenderMock;

    @BeforeEach
    void setup(){
        this.paymentServiceMock = mock(PaymentService.class);
        this.roomServiceMock = mock(RoomService.class);
        this.bookingDAOMock = mock(BookingDAO.class);
        this.mailSenderMock = mock(MailSender.class);
        this.bookingService = new BookingService(paymentServiceMock,roomServiceMock,bookingDAOMock,mailSenderMock);

        System.out.println("List returned "+ roomServiceMock.getAvailableRooms());
        System.out.println("Object returned" + roomServiceMock.findAvailableRoomId(null));
        System.out.println("Primitive returned " + roomServiceMock.getRoomCount());
    }

    @Test
    void should_CalculateCorrectPrice_When_CorrectInput(){
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020,01,01),LocalDate.of(2020,01,05),2,false);
        double expected = 4*2*50.0;

        //when
        double actual =  bookingService.calculatePrice(bookingRequest);

        //then
        assertEquals(expected,actual);
    }

    @Test
    void should_CountAvailablePlaces(){
        //given
        int expected =0 ;
        //when
        int actual = bookingService.getAvailablePlaceCount();

        //then
        assertEquals(expected,actual);
    }



}