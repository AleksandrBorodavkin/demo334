package com.example.demo.repository;

import com.example.demo.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query("SELECT p FROM Person p WHERE " +
            "LOWER(CAST(p.id AS string)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.surname) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(CAST(p.age AS string)) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Person> searchAllFields(@Param("query") String query);

    @Query("SELECT p FROM Person p WHERE LOWER(CAST(p.id AS string)) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Person> searchById(@Param("query") String query);

    @Query("SELECT p FROM Person p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Person> searchByName(@Param("query") String query);

    @Query("SELECT p FROM Person p WHERE LOWER(p.surname) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Person> searchBySurname(@Param("query") String query);

    @Query("SELECT p FROM Person p WHERE LOWER(p.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Person> searchByEmail(@Param("query") String query);

    @Query("SELECT p FROM Person p WHERE LOWER(CAST(p.age AS string)) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Person> searchByAge(@Param("query") String query);
}
