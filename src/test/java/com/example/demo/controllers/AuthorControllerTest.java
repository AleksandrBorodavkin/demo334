package com.example.demo.controllers;

import com.example.demo.entity.Author;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AuthorRepository authorRepository;
    private int size = 100_000;

    //

    @Test
    @Order(1)
    @DisplayName("Создание автора с валидацией")

    public void create_author_valid_data_return_created_author() throws Exception {
        AuthorDTO author = new AuthorDTO("Farid", "fabdullayev902@gmail.com");

        mockMvc.perform(
                        post("/api/author/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(author)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Farid")))
                .andExpect(jsonPath("$.email", is("fabdullayev902@gmail.com")));
    }


    @Test
    @Order(2)
    public void get_author_existing_id_returns_author() throws Exception {
        Author author = new Author("Farid", "fabdullayev902@gmail.com");
        author = authorRepository.save(author);

        mockMvc.perform(
                        get("/api/author/getById/{id}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Farid")))
                .andExpect(jsonPath("$.email", is("fabdullayev902@gmail.com")))
                .andExpect(jsonPath("$.id").value(author.getId()));
    }

    @Test
    @Order(3)
    public void get_author_non_existing_id_returns_not_found() throws Exception {
        mockMvc.perform(
                        get("/api/author/getById/{id}", 9999999))
                .andExpect(status().isNotFound());
    }

    private void add_100_000_authors() {
        if (authorRepository.count() < size) {
            List<Author> authors = new ArrayList<>();

            for (int i = 0; i < 100_000; i++) {
                String name = faker.name().fullName();
                String email = faker.internet().emailAddress();

                Author author = new Author(name, email);
                authors.add(author);
            }

            authorRepository.saveAll(authors);
        }
    }

    @BeforeAll
    public static void test(){
        System.out.println("Hello World");
        System.out.println("Hello World");
        System.out.println("Hello World");
        System.out.println("Hello World");
        System.out.println("Hello World");
        System.out.println("Hello World");
        System.out.println("Hello World");
    }

    @AfterAll
    public static void test2(){
        System.out.println("Bye World");
        System.out.println("Bye World");
        System.out.println("Bye World");
        System.out.println("Bye World");
        System.out.println("Bye World");
        System.out.println("Bye World");
        System.out.println("Bye World");
    }


    @BeforeEach
    public  void test10(){
        System.out.println("11111111111111111");
    }

    @AfterEach
    public  void test11(){
        System.out.println("99999999999999");
    }

    @Test
    @Order(7)
//    @Timeout(2)
    @Disabled
    public void get_all_authors_returns_all_authors() throws Exception {
        add_100_000_authors();

        mockMvc.perform(
                        get("/api/author/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(size))));
    }

    @Test
    @Order(4)
    public void get_empty_list_return_not_content() throws Exception {
        authorRepository.deleteAll();
        mockMvc.perform(get("/api/author/all"))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(5)
    public void delete_author_existing_id_return_ok() throws Exception {
        Author author = new Author("Farid", "fabdullayev902@gmail.com");
        author = authorRepository.save(author);

        mockMvc.perform(
                        delete("/api/author/{id}", author.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    public void delete_author_non_existing_id_return_not_found() throws Exception {

        mockMvc.perform(
                        delete("/api/author/delete/{id}", -1))
                .andExpect(status().isNotFound());
    }
}
