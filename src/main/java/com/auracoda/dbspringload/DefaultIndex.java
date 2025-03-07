package com.auracoda.dbspringload;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class DefaultIndex {

    // @GetMapping("*")
    // public String getMethodName(Model model) {
    // return "redirect:/";
    // }

    @GetMapping
    public String defaultPage() {
        return "redirect:/HomePage";
    }

}
