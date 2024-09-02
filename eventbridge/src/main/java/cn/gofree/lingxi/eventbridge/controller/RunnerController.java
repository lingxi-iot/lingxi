package cn.gofree.lingxi.eventbridge.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/runner")
public class RunnerController {

    @GetMapping("/start")
    private String  start(){
          return "runner start";
    }
}
