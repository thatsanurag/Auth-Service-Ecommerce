package com.personal.authservice.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Session extends BaseModel{
    private String token;
    private Date expiringAt;

     @ManyToOne
     @JoinColumn(name = "user_id")
     private User user;

     @Enumerated(EnumType.STRING)
     private SessionStatus status;
}
