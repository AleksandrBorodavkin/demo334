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
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private Faker fakerData;

    @GetMapping("/people")
    public Object getAll(@RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "All") String field,
            @RequestParam(required = false) String format,
            @RequestHeader(value = "Accept", required = false) String accept,
            Model model) {
        List<Person> people;

        if (query != null && !query.trim().isEmpty()) {
            String searchQuery = query.trim();
            switch (field) {
                case "Id":
                    people = personRepository.searchById(searchQuery);
                    break;
                case "Name":
                    people = personRepository.searchByName(searchQuery);
                    break;
                case "Surname":
                    people = personRepository.searchBySurname(searchQuery);
                    break;
                case "Email":
                    people = personRepository.searchByEmail(searchQuery);
                    break;
                case "Age":
                    people = personRepository.searchByAge(searchQuery);
                    break;
                default:
                    people = personRepository.searchAllFields(searchQuery);
            }
        } else {
            people = personRepository.findAll();
        }

        // Если запрос JSON (через параметр format=json или заголовок Accept)
        if ("json".equals(format) || (accept != null && accept.contains("application/json"))) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(people);
        }

        // Иначе возвращаем HTML страницу
        model.addAttribute("people", people);
        model.addAttribute("showFooter", false);
        model.addAttribute("showHeader", true);
        model.addAttribute("pageName", "Home page");
        model.addAttribute("searchQuery", query != null ? query : "");
        model.addAttribute("searchField", field);
        return "/home";
    }

    @GetMapping("/addAll")
    public String addAll(Model model) {
        generationData(50);
        return "redirect:/home/people";
    }

    @DeleteMapping("/delete/{id}")
    public String deleteById(@PathVariable Long id, Model model) {
        personRepository.deleteById(id);
        return "redirect:/home/people";
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public String deleteByIdAjax(@PathVariable Long id) {
        Optional<Person> removedPerson = personRepository.findById(id);
        Person person = removedPerson.get();
        if (person != null) {
            personRepository.delete(person);
            return "User delete => " + person.toString();
        } else {
            return "User not found => " + id;
        }
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("pageName", "Create page");
        model.addAttribute("person", new Person());
        model.addAttribute("showFooter", false);
        model.addAttribute("showHeader", true);
        return "create";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Optional<Person> edit = personRepository.findById(id);
        Person person = edit.get();
        model.addAttribute("pageName", "Update page");
        model.addAttribute("person", person);
        model.addAttribute("showFooter", true);
        model.addAttribute("showHeader", false);
        return "edit";
    }

    @PostMapping("/{id}")
    public String update(@Valid Person person, BindingResult bindir, Model model) {
        personRepository.save(person);
        return "redirect:/home/people";
    }

    @PostMapping("/create")
    public String add(@Valid Person person, BindingResult bindir, Model model) {
        if (bindir.hasErrors()) {
            model.addAttribute("person", person);
            return "create";
        }

        personRepository.save(person);
        return "redirect:/home/people";
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
