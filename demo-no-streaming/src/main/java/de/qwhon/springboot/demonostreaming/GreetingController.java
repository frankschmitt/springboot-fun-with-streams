package de.qwhon.springboot.demonostreaming;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

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
}