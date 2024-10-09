package com.heima.freemarker.controller;

import com.heima.freemarker.entity.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.FileWriter;
import java.util.*;

@Controller
public class HalloController {

    @Autowired
    private Configuration configuration;


    @GetMapping("/basic")
    public String hello(Model model) {
        model.addAttribute("name", "freemarker");
        Student student = new Student();
        student.setName("xiaoming");
        student.setAge(18);
        model.addAttribute("stu", student);
        return "01-basic";
    }

    @GetMapping("/list")
    public String list(Model model) {
        Student stu1 = new Student();
        stu1.setName("xiaoqiang");
        stu1.setAge(19);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        Student stu2 = new Student();
        stu2.setName("xiaohong");
        stu2.setAge(19);
        stu2.setMoney(200.86f);

        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        model.addAttribute("stus", stus);

        Map<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1", stu1);
        stuMap.put("stu2", stu2);
        model.addAttribute("stuMap", stuMap);

        return "02-list";
    }
}
