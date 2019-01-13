package ua.kiev.prog.photopond.twitter;

import com.fasterxml.jackson.annotation.JsonView;
import ua.kiev.prog.photopond.transfer.Exist;
import ua.kiev.prog.photopond.transfer.New;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class TweetDTO implements Serializable {
    @NotEmpty(groups = {New.class})
    @Size(max = 4, groups = {New.class}, message = "Size.tweet.images")
    @JsonView(value = {New.class})
    private List<String> paths;

    @NotNull(groups = {New.class})
    @Size(min = 1, max = 240, groups = {New.class}, message = "Size.tweet.message")
    @JsonView(value = {New.class})
    private String message;

    @JsonView(value = {Exist.class})
    private String url;

    @JsonView(value = {Exist.class})
    private String responseMessage;

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
}
