package com.jyotimoykashyap.bookmyshowdesign.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table (name = "cities")
@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor 
@Builder 
public class City {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private int id;

    @Column (nullable = false)
    private String name;

    @Column (nullable = false)
    private String state;

    @OneToMany (mappedBy = "city", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties ("city")
    @Builder.Default
    private List<Theatre> theatres = new ArrayList<>();

}
