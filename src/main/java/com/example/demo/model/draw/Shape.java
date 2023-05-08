package com.example.demo.model.draw;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Shape {
    private int size;
    private int x;
    private int y;
    private String type;

    public Shape(int size, int x, int y, String type) {
        this.size = size;
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
