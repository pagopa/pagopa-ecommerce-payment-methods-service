package it.pagopa.pspmatcher.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.server.EntityResponse;
import org.springframework.web.reactive.function.server.ServerResponse;

@Controller
public class PSPMatcherController {

    @GetMapping(value = "/psps")
    public void filterPSPs(){}
}
