package com.suhyun444.lifehub.marker.DTO;

import java.util.stream.Collectors;

import com.suhyun444.lifehub.marker.Entity.Link;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LinkDto
{
    Long id;
    String title;
    String url;
    public static LinkDto from(Link link) {
        return new LinkDto(
            link.getId(),
            link.getTitle(),
            link.getUrl()
        );
    }
}