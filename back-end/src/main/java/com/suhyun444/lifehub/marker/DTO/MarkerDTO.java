package com.suhyun444.lifehub.marker.DTO;

import java.util.List;
import java.util.stream.Collectors;

import com.suhyun444.lifehub.card.DTO.TransactionDto;
import com.suhyun444.lifehub.marker.Entity.Marker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarkerDto
{
    Long id;
    String title;
    String color;
    List<LinkDto> links;
    public static MarkerDto from(Marker marker) {
        return new MarkerDto(
            marker.getId(),
            marker.getTitle(),
            marker.getColor(),
            marker.getLinks().stream().map(LinkDto::from).collect(Collectors.toList())
        );
    }
}