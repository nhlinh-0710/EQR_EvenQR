package com.eventqr.dto;

import com.eventqr.model.Account;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;

public class CheckinResponse {

    private Event event;
    private EventTicket ticket;
    private Account user;

    // ⭐ Thêm trường số điện thoại (được lấy từ EventTicket)
    private String phone;

    public CheckinResponse(Event event, EventTicket ticket, Account user) {
        this.event = event;
        this.ticket = ticket;
        this.user = user;

        // ⭐ Gắn số điện thoại từ vé (EventTicket)
        this.phone = ticket.getPhone();
    }

    public Event getEvent() {
        return event;
    }

    public EventTicket getTicket() {
        return ticket;
    }

    public Account getUser() {
        return user;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
