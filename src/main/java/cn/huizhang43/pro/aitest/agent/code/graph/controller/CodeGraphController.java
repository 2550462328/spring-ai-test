package cn.huizhang43.pro.aitest.agent.code.graph.controller;

import cn.huizhang43.pro.aitest.agent.code.graph.service.CodeGraphService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("code/graph")
@AllArgsConstructor
@Slf4j
public class CodeGraphController {
    private final CodeGraphService codeGraphService;

    @PostMapping("build")
    public String buildGraph() {
        codeGraphService.buildGraph();
        return "SUCCESS";
    }
}
