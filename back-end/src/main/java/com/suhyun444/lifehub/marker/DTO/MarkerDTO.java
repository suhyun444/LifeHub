package com.suhyun444.lifehub.marker.DTO;

import java.util.List;

// record를 사용하면 생성자, getter, equals, hashCode 등을 자동으로 만들어줍니다.
public record MarkerDTO(int id,String title,String color,List<LinkDTO> links) {
}