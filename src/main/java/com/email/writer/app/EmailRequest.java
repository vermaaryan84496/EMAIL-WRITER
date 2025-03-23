package com.email.writer.app;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailcontent;
    private String tone;
}
