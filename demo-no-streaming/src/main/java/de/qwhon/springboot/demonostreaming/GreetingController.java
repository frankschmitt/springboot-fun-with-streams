package de.qwhon.springboot.demonostreaming;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController @Service
public class GreetingController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = new Gson();

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @GetMapping("/greetings")
    public List<Greeting> greetings(@RequestParam(value="count", defaultValue = "1")Long count) {
        List<Greeting> result = new ArrayList<Greeting>();
        for(Long i=0l; i<count; ++i) {
            result.add(new Greeting(counter.incrementAndGet(), String.format(template, i.toString())));
        }
        return result;
    }

    // normally, we would perform this inside a Service class, but for our dummy, we'll just stream it here
    private ResponseEntity<StreamingResponseBody> getGreetingsStream(Long count) {
        final Long val = 0l;
        Stream<Greeting> greetings = Stream.iterate(new Greeting(counter.incrementAndGet(), String.format(template, val.toString())),
                (Greeting g) -> new Greeting(counter.incrementAndGet(), String.format(template, val.toString())))
                .limit(count);
        StreamingResponseBody responseBody = httpResponseOutputStream -> {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(httpResponseOutputStream))) {
                greetings.forEach(greeting -> {
                    try {
                        writer.write(gson.toJson(greeting));
                        writer.flush();
                    } catch (IOException exception) {
                        logger.error("exception occurred while writing object to stream", exception);
                    }
                });
            } catch (Exception exception) {
                logger.error("exception occurred while publishing data", exception);
            }
            logger.info("finished streaming records");
        };

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(responseBody);
    }



    // Streaming endpoints
    @GetMapping(value = "/greetingsStreaming")
    public ResponseEntity<StreamingResponseBody> greetingsStream(@RequestParam(value="count", defaultValue = "1")Long count) throws ExecutionException, InterruptedException {
        logger.info("request received to fetch all employee details");
        return getGreetingsStream(count);
    }
}