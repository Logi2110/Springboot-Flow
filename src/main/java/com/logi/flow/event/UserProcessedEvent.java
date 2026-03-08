package com.logi.flow.event;

import org.springframework.context.ApplicationEvent;

/**
 * Event / Async Layer — Custom Application Event
 *
 * Fired by UserService after a user is successfully processed (Flow 2).
 *
 * Key concept — decoupled communication:
 *   - UserService publishes the event but knows NOTHING about who listens.
 *   - Listeners are added/removed without touching UserService.
 *   - Spring's ApplicationEventMulticaster routes the event to all matching listeners.
 *
 * Extends ApplicationEvent (Spring's base class for typed events).
 * 'source' is the object that fired the event (UserService instance).
 */
public class UserProcessedEvent extends ApplicationEvent {

    private final String userName;
    private final String email;

    public UserProcessedEvent(Object source, String userName, String email) {
        super(source);
        this.userName = userName;
        this.email = email;
    }

    public String getUserName() { return userName; }
    public String getEmail()    { return email; }

    @Override
    public String toString() {
        return "UserProcessedEvent{userName='" + userName + "', email='" + email + "'}";
    }
}
