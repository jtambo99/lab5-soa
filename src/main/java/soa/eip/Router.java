package soa.eip;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class Router extends RouteBuilder {

    public static final String DIRECT_URI = "direct:twitter";

    @Override
    public void configure() {
        from(DIRECT_URI)
                .log("Body contains \"${body}\"")
                .log("Searching twitter for \"${body}\"!")
                // Source: https://camel.apache.org/components/latest/eips/process-eip.html
                .process(exchange -> {
                    String message = exchange.getIn().getBody(String.class);
                    // keyboard could contain a 'max' command to limit the number of retrieved tweets
                    if (message.matches("^.+ max:[0-9]+$")) {
                        // Split the message by the delimiter ':'
                        String[] splitted = message.split(":");
                        // Obtain the last part:  .... max:<count> -> <count>
                        int count = Integer.parseInt(splitted[splitted.length - 1]);
                        // Set 'count' parameter with count
                        exchange.getIn().setHeader("count", count);
                        // Remove <count> from the message
                        message = message.replace("max:" + count, "");
                        exchange.getIn().setBody(message);
                    } else {
                        // By default, count = 5
                        exchange.getIn().setHeader("count", 5);
                    }
                })
                .log("count: ${header.count}")
                .toD("twitter-search:${body}?count=${header.count}")
                .log("Body now contains the response from twitter:\n${body}");
    }
}
