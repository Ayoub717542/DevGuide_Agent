package com.example.DevTutor.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SourceResponse {

    private String filename;
    private Integer startPage;
    private Integer endPage;
}