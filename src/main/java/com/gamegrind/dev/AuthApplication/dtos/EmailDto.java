package com.gamegrind.dev.AuthApplication.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EmailDto {
    private String email;
    private String message;
    private String subject;
}
