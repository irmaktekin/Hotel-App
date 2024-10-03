package com.mockitotutorial.happyhotel.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class BookingServiceTest {

    private BookingService bookingService;
    private PaymentService paymentServiceMock;
    private RoomService roomServiceMock;
    private BookingDAO bookingDAOMock;
    private MailSender mailSenderMock;
    private ArgumentCaptor<Double> doubleCaptor;

    @BeforeEach
    void setup() {
        this.paymentServiceMock = mock(PaymentService.class);
        this.roomServiceMock = mock(RoomService.class);
        this.bookingDAOMock = mock(BookingDAO.class);
        this.mailSenderMock = mock(MailSender.class);
        this.bookingService = new BookingService(paymentServiceMock, roomServiceMock, bookingDAOMock, mailSenderMock);
        this.doubleCaptor = ArgumentCaptor.forClass(Double.class);
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
    void shouldCountAvailablePlaces_When_MultipleRoomsAvailable() {
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
                .thenReturn(Collections.singletonList(new Room("Room 1", 5)))
                .thenReturn(Collections.emptyList());
        int expectedFirstCall = 5;
        int expectedSecondCall = 0;
        //when
        int actualFirst = bookingService.getAvailablePlaceCount();
        int actualSecond = bookingService.getAvailablePlaceCount();

        //then
        assertAll(
                () -> assertEquals(expectedFirstCall, actualFirst),
                () -> assertEquals(expectedSecondCall, actualSecond)
        );

    }
        @Test
        void should_NotCompleteBooking_When_PriceTooHigh(){
            //given
            BookingRequest bookingRequest = new BookingRequest("2",LocalDate.of(2020,01,01),
                    LocalDate.of(2020,01,05),2,true);

            when(this.paymentServiceMock.pay(any(),eq(4000))).thenThrow(BusinessException.class);
            //when
            Executable executable = ()-> bookingService.makeBooking(bookingRequest);
            //then
            assertThrows(BusinessException.class,executable);
        }

    @Test
        void should_InvokePayment_When_Prepaid() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);

        //when
        bookingService.makeBooking(bookingRequest);
        //then
        verify(paymentServiceMock,times(1)).pay(bookingRequest,400.0);
        verifyNoMoreInteractions(paymentServiceMock);
    }
    @Test
    void should_NotInvokePayment_When_NotPrepaid() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);

        //when
        bookingService.makeBooking(bookingRequest);

        //then
        verify(paymentServiceMock,never()).pay(any(),anyDouble());

    }
    @Test
    void should_MakeBooking_When_InputOK() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);

        //when
        String bookingId = bookingService.makeBooking(bookingRequest);

        //then
        verify(bookingDAOMock).save(bookingRequest);
        System.out.println("bookingID="+ bookingId);

    }
    @Test
    void should_CancelBooking_When_InputOK() {
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);
        bookingRequest.setRoomId("1.3");
        String bookingId = "1";
        doReturn(bookingRequest).when(bookingDAOMock).get(bookingId);

        //when
        bookingService.cancelBooking(bookingId);
        //then
        System.out.println("bookingID="+ bookingId);

    }
    @Test
    void should_ThrowException_When_MailNotReady(){
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);
        doThrow(new BusinessException()).when(mailSenderMock).sendBookingConfirmation(any());
        //when
        Executable executable = () -> bookingService.makeBooking(bookingRequest);
        //then
        assertThrows(BusinessException.class,executable);
    }

    @Test
    void should_NotThrowException_When_MailNotReady(){
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, false);
        //doNothing().when(mailSenderMock).sendBookingConfirmation(any());
        //when
        bookingService.makeBooking(bookingRequest);
        //then
        //no exception thrown
    }
    @Test
    void should_PayCorrectPrice_When_InputOK(){
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);

        //when
        bookingService.makeBooking(bookingRequest);

        //then
        verify(paymentServiceMock,times(1)).pay(eq(bookingRequest),doubleCaptor.capture());
        double capturedArgument = doubleCaptor.getValue();
        System.out.println(capturedArgument);
        assertEquals(400.0,capturedArgument);

    }

    @Test
    void should_PayCorrectPrice_When_MultipleCalls(){
        //given
        BookingRequest bookingRequest = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 05), 2, true);
        BookingRequest bookingRequest2 = new BookingRequest("1", LocalDate.of(2020, 01, 01), LocalDate.of(2020, 01, 02), 2, true);
        List <Double> expectedValues = Arrays.asList(400.0,100.0);
        //when
        bookingService.makeBooking(bookingRequest);
        bookingService.makeBooking(bookingRequest2);

        //then
        verify(paymentServiceMock,times(2)).pay(any(),doubleCaptor.capture());
        List<Double> capturedArguments= doubleCaptor.getAllValues();
        assertEquals(expectedValues,capturedArguments);

    }
}