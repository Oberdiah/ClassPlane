package main.components;

import javax.swing.*;

public class World extends JLayeredPane {
    public static final int SIZE = 500_000;

    public World() {
        setSize(SIZE, SIZE);
    }
}
