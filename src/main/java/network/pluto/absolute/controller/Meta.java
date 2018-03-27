package network.pluto.absolute.controller;

public class Meta {

    public boolean available;

    public static Meta available() {
        Meta meta = new Meta();
        meta.available = true;
        return meta;
    }

    public static Meta unavailable() {
        Meta meta = new Meta();
        meta.available = false;
        return meta;
    }

}
