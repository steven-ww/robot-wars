package com.example.backend.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * A simple REST endpoint that provides greeting messages.
 */
@Path("/api/greeting")
public class GreetingResource {

    /**
     * Returns a simple greeting message.
     *
     * @return A greeting message
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Robot Wars Backend!";
    }

    /**
     * Returns a greeting message in JSON format.
     *
     * @return A JSON object containing a greeting message
     */
    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Greeting helloJson() {
        return new Greeting("Hello from Robot Wars Backend!");
    }

    /**
     * A simple POJO to represent a greeting message.
     */
    public static class Greeting {
        private String message;

        public Greeting() {
        }

        public Greeting(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}