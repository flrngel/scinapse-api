package network.pluto.absolute.controller;

import lombok.Getter;

@Getter
public class Result {

    private final boolean success;

    private Result(boolean success) {
        this.success = success;
    }

    public static Result success() {
        return new Result(true);
    }

}
