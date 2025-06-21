package com.personal.authservice.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@JsonDeserialize(as  = Role.class)
public class Role extends BaseModel {
    private String role;
}
