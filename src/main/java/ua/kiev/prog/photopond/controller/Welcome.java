package ua.kiev.prog.photopond.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Welcome {

    @RequestMapping(value = {"/", "index"})
    public String index() {
        return "index";
    }
}
