package com.service.authentication.LectureRooms;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class LectureRoom {

    @Id
    private String id;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Enter a valid room name")
    private String roomName;

    @NotBlank(message = "This field is required")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Enter a location of the room")
    private String location;

    @NotNull(message = "This field is required")
    private Integer capacity;

    @NotNull(message = "This field is required")
    private Double width;

    @NotNull(message = "This field is required")
    private Double length;
    
    @NotNull(message = "This field is required")
    private Double latitude;    
    
    @NotNull(message = "This field is required")
    private Double longitude;
    

}
