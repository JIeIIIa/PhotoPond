package ua.kiev.prog.photopond.twitter;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ua.kiev.prog.photopond.core.BindingErrorResolver;
import ua.kiev.prog.photopond.transfer.Exist;
import ua.kiev.prog.photopond.transfer.New;

import java.util.Locale;

@Controller
public class TwitterApiController {

    private static final Logger LOG = LogManager.getLogger(TwitterApiController.class);

    private final TwitterService twitterService;

    private BindingErrorResolver bindingErrorResolver;

    @Autowired
    public TwitterApiController(TwitterService twitterService,
                                BindingErrorResolver bindingErrorResolver) {
        LOG.info("Create instance of {}", TwitterApiController.class);

        this.twitterService = twitterService;
        this.bindingErrorResolver = bindingErrorResolver;
    }

    @RequestMapping(value = "/api/{login}/tweet", method = RequestMethod.POST)
    @JsonView(value = {Exist.class})
    public ResponseEntity<TweetDTO> tweet(@PathVariable("login") String userLogin,
                                          @Validated(value = {New.class}) @RequestBody TweetDTO tweetDTO,
                                          Locale locale) {
        LOG.traceEntry("Try to create tweet by [userLogin = {}]", userLogin);
        
        TweetDTO responseTweetDTO = twitterService.publishTweet(userLogin, tweetDTO);
        responseTweetDTO.setResponseMessage(bindingErrorResolver.resolveMessage("twitter.tweet.publish.success", locale));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseTweetDTO);
    }


}
