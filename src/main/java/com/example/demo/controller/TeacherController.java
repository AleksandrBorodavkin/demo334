package com.example.demo.controller;

import com.example.demo.entity.Person;
import com.example.demo.repository.PersonRepository;
import com.github.javafaker.Faker;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/teacher")
public class TeacherController {


    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private Faker fakerData;


    @GetMapping("/people")
    public String getAll(Model model) {
        List<Person> people = personRepository.findAll();
        model.addAttribute("people", people);
        model.addAttribute("pageName", "Home page");
        return "home";
    }

    @GetMapping("/addAll")
    public String addAll(Model model) {
        generationData(10);
        return "redirect:/home/people";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteById(@PathVariable Long id, Model model) {
        personRepository.deleteById(id);
        return "redirect:/home/people";
    }


    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public String deleteByIdAjax(@PathVariable Long id ) {
        Optional<Person> removedPerson = personRepository.findById(id);
        Person person = removedPerson.get();
        if(person!=null) {
            personRepository.delete(person);
            return "User delete => " + person.toString();
        }else{
            return "User not found => " + id;
        }
    }

//    @GetMapping("/delete/{id}")
//    public String deleteById(@PathVariable Long id, Model model) {
//        personRepository.deleteById(id);
//        List<Person> people = personRepository.findAll();
//        model.addAttribute("people", people);
//        model.addAttribute("pageName", "Home page");
//
//        return "home";
//    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("pageName", "Create page");
        model.addAttribute("person", new Person());

        return "create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id,Model model) {
        Optional<Person> edit = personRepository.findById(id);
        Person person = edit.get();
        model.addAttribute("pageName", "Update page");
        model.addAttribute("person", person);

        return "edit";
    }

    @PostMapping("/{id}")
    public String update(@Valid Person person, BindingResult bindir, Model model) {
        personRepository.save(person);
        return "redirect:/home/people";
    }

    @PostMapping("/create")
    public String add(@Valid Person person, BindingResult bindir, Model model) {


        bindir.hasErrors(); // => est li kakie libo osibki
        bindir.getAllErrors(); // => spisok vsex osibok
        bindir.hasFieldErrors("age"); // => est li v pole age osibka
        bindir.getFieldError("age"); // => 1 soobsenie


        if (bindir.hasErrors()) {
            System.out.println("One or more fields are invalid");

            model.addAttribute("person", person);
//            model.addAttribute("messages", "Error valid");

            return "create";
        }

        personRepository.save(person);
        List<Person> people = personRepository.findAll();
        model.addAttribute("people", people);
        return "home";
    }

    @GetMapping("/clear")
    public String clear(Model model) {
        personRepository.deleteAll();
        return "redirect:/home/people";
    }


    private void generationData(int count) {
        if (personRepository.count() == 0) {
            for (int i = 0; i < count; i++) {
                String name = fakerData.name().name();
                String firstName = fakerData.name().firstName();
                int age = fakerData.number().numberBetween(18, 100);
                String email = fakerData.internet().emailAddress();
                personRepository.save(new Person(name, firstName, email, age));
            }
        }
    }

}
